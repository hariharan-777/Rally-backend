package com.userservice.User.service;

import com.userservice.User.dto.MatchResultRequest;
import com.userservice.User.dto.MatchResultResponse;
import com.userservice.User.dto.MatchResultResponse.RatingChange;
import com.userservice.User.dto.PeerRatingRequest;
import com.userservice.User.dto.PeerRatingResponse;
import com.userservice.User.dto.PlayerProfileResponse;
import com.userservice.User.dto.PlayerProfileResponse.SportProfile;
import com.userservice.User.dto.ReliabilityResponse;
import com.userservice.User.exception.DuplicateResourceException;
import com.userservice.User.exception.InvalidRequestException;
import com.userservice.User.exception.UserNotFoundException;
import com.userservice.User.model.MatchResult;
import com.userservice.User.model.PeerRating;
import com.userservice.User.model.SkillRating;
import com.userservice.User.model.Sport;
import com.userservice.User.model.user;
import com.userservice.User.repository.MatchResultRepository;
import com.userservice.User.repository.PeerRatingRepository;
import com.userservice.User.repository.SkillRatingRepository;
import com.userservice.User.repository.UserRepository;
import com.userservice.User.scoring.EloCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** Peer ratings (players rating each other) and Elo skill ratings (from match results). */
@Service
public class RatingService {

    private final UserRepository userRepository;
    private final PeerRatingRepository peerRatingRepository;
    private final SkillRatingRepository skillRatingRepository;
    private final MatchResultRepository matchResultRepository;
    private final ReliabilityService reliabilityService;

    public RatingService(UserRepository userRepository,
                         PeerRatingRepository peerRatingRepository,
                         SkillRatingRepository skillRatingRepository,
                         MatchResultRepository matchResultRepository,
                         ReliabilityService reliabilityService) {
        this.userRepository = userRepository;
        this.peerRatingRepository = peerRatingRepository;
        this.skillRatingRepository = skillRatingRepository;
        this.matchResultRepository = matchResultRepository;
        this.reliabilityService = reliabilityService;
    }

    // ---------------------------------------------------------------- peer ratings

    @Transactional
    public PeerRatingResponse ratePlayer(UUID rateeId, PeerRatingRequest request) {
        if (rateeId.equals(request.raterId())) {
            throw new InvalidRequestException("Players cannot rate themselves");
        }
        requireUser(rateeId);
        requireUser(request.raterId());

        if (peerRatingRepository.existsByRaterIdAndRateeIdAndGameId(request.raterId(), rateeId, request.gameId())) {
            throw new DuplicateResourceException(
                    "User " + request.raterId() + " has already rated this player for game " + request.gameId());
        }

        PeerRating rating = new PeerRating();
        rating.setRaterId(request.raterId());
        rating.setRateeId(rateeId);
        rating.setGameId(request.gameId());
        rating.setSport(request.sport());
        rating.setSkillScore(request.skill());
        rating.setSportsmanshipScore(request.sportsmanship());
        rating.setComment(request.comment());

        return PeerRatingResponse.from(peerRatingRepository.save(rating));
    }

    @Transactional(readOnly = true)
    public List<PeerRatingResponse> getRatingsReceived(UUID userId) {
        requireUser(userId);
        return peerRatingRepository.findByRateeIdOrderByCreatedAtDesc(userId).stream()
                .map(PeerRatingResponse::from)
                .toList();
    }

    // ---------------------------------------------------------------- Elo match results

    /**
     * Applies a finished match to every player's Elo rating for that sport.
     * Each game id can only be applied once.
     */
    @Transactional
    public MatchResultResponse recordMatchResult(MatchResultRequest request) {
        if (matchResultRepository.existsByGameId(request.gameId())) {
            throw new DuplicateResourceException("Result for game " + request.gameId() + " was already recorded");
        }

        Set<UUID> winners = new LinkedHashSet<>(request.winnerIds());
        Set<UUID> losers = new LinkedHashSet<>(request.loserIds());
        if (winners.size() != request.winnerIds().size() || losers.size() != request.loserIds().size()) {
            throw new InvalidRequestException("The same player is listed twice on one side");
        }
        if (winners.stream().anyMatch(losers::contains)) {
            throw new InvalidRequestException("A player cannot be on both the winning and losing side");
        }

        Map<UUID, SkillRating> ratings = new LinkedHashMap<>();
        for (UUID id : winners) {
            ratings.put(id, loadOrCreate(id, request.sport()));
        }
        for (UUID id : losers) {
            ratings.put(id, loadOrCreate(id, request.sport()));
        }

        // Team strength is measured before anyone's rating changes.
        double winnerAverage = averageRating(winners, ratings);
        double loserAverage = averageRating(losers, ratings);

        List<RatingChange> changes = new ArrayList<>();
        for (Map.Entry<UUID, SkillRating> entry : ratings.entrySet()) {
            UUID userId = entry.getKey();
            SkillRating rating = entry.getValue();
            boolean won = winners.contains(userId);

            int delta = won
                    ? EloCalculator.ratingChange(winnerAverage, loserAverage, rating.getGamesPlayed(), true)
                    : EloCalculator.ratingChange(loserAverage, winnerAverage, rating.getGamesPlayed(), false);

            int oldRating = rating.getRating();
            int newRating = EloCalculator.applyChange(oldRating, delta);

            rating.setRating(newRating);
            rating.setGamesPlayed(rating.getGamesPlayed() + 1);
            if (won) {
                rating.setWins(rating.getWins() + 1);
            } else {
                rating.setLosses(rating.getLosses() + 1);
            }
            changes.add(new RatingChange(userId, won, oldRating, newRating, newRating - oldRating));
        }

        skillRatingRepository.saveAll(ratings.values());
        matchResultRepository.save(new MatchResult(request.gameId(), request.sport()));

        return new MatchResultResponse(request.gameId(), request.sport(), changes);
    }

    // ---------------------------------------------------------------- profile

    /** Combines Elo, peer ratings and reliability into one profile, one entry per sport played. */
    @Transactional(readOnly = true)
    public PlayerProfileResponse getPlayerProfile(UUID userId) {
        user player = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " does not exist"));

        Map<Sport, SkillRating> eloBySport = new EnumMap<>(Sport.class);
        for (SkillRating rating : skillRatingRepository.findByUserId(userId)) {
            eloBySport.put(rating.getSport(), rating);
        }

        Map<Sport, List<PeerRating>> peerBySport = peerRatingRepository.findByRateeIdOrderByCreatedAtDesc(userId)
                .stream()
                .collect(Collectors.groupingBy(PeerRating::getSport, () -> new EnumMap<>(Sport.class), Collectors.toList()));

        Set<Sport> sports = EnumSet.noneOf(Sport.class);
        sports.addAll(eloBySport.keySet());
        sports.addAll(peerBySport.keySet());

        List<SportProfile> sportProfiles = new ArrayList<>();
        for (Sport sport : sports) {
            SkillRating elo = eloBySport.get(sport);
            List<PeerRating> peers = peerBySport.getOrDefault(sport, List.of());

            sportProfiles.add(new SportProfile(
                    sport,
                    elo == null ? null : elo.getRating(),
                    elo == null ? 0 : elo.getGamesPlayed(),
                    elo == null ? 0 : elo.getWins(),
                    elo == null ? 0 : elo.getLosses(),
                    peers.isEmpty() ? null : round2(peers.stream().mapToInt(PeerRating::getSkillScore).average().orElse(0)),
                    peers.isEmpty() ? null : round2(peers.stream().mapToInt(PeerRating::getSportsmanshipScore).average().orElse(0)),
                    peers.size()));
        }

        ReliabilityResponse reliability = reliabilityService.getReliability(userId);

        return new PlayerProfileResponse(
                userId,
                player.getUsername(),
                reliability.score(),
                reliability.tier(),
                sportProfiles);
    }

    @Transactional
    public void deleteAllForUser(UUID userId) {
        peerRatingRepository.deleteAllInvolvingUser(userId);
        skillRatingRepository.deleteAllForUser(userId);
    }

    // ---------------------------------------------------------------- helpers

    private SkillRating loadOrCreate(UUID userId, Sport sport) {
        requireUser(userId);
        return skillRatingRepository.findByUserIdAndSport(userId, sport)
                .orElseGet(() -> new SkillRating(userId, sport));
    }

    private static double averageRating(Set<UUID> players, Map<UUID, SkillRating> ratings) {
        return players.stream().mapToInt(id -> ratings.get(id).getRating()).average().orElse(SkillRating.STARTING_RATING);
    }

    private static double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private void requireUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User with id " + userId + " does not exist");
        }
    }
}
