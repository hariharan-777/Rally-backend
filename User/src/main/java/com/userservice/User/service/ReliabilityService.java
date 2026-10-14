package com.userservice.User.service;

import com.userservice.User.dto.AttendanceRequest;
import com.userservice.User.dto.ReliabilityResponse;
import com.userservice.User.exception.UserNotFoundException;
import com.userservice.User.model.AttendanceRecord;
import com.userservice.User.model.AttendanceStatus;
import com.userservice.User.repository.AttendanceRecordRepository;
import com.userservice.User.repository.UserRepository;
import com.userservice.User.scoring.ReliabilityCalculator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Records whether players show up, and turns that history into a reliability score. */
@Service
public class ReliabilityService {

    private final AttendanceRecordRepository attendanceRepository;
    private final UserRepository userRepository;

    public ReliabilityService(AttendanceRecordRepository attendanceRepository, UserRepository userRepository) {
        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
    }

    /**
     * Records attendance for one game. If this player already has a record for the game,
     * it is corrected rather than duplicated, so the same call can safely be repeated.
     */
    @Transactional
    public ReliabilityResponse recordAttendance(UUID userId, AttendanceRequest request) {
        requireUser(userId);

        AttendanceRecord record = attendanceRepository.findByUserIdAndGameId(userId, request.gameId())
                .orElseGet(() -> {
                    AttendanceRecord fresh = new AttendanceRecord();
                    fresh.setUserId(userId);
                    fresh.setGameId(request.gameId());
                    return fresh;
                });
        record.setStatus(request.status());
        if (request.sport() != null) {
            record.setSport(request.sport());
        }
        if (request.gameTime() != null) {
            record.setGameTime(request.gameTime());
        }
        attendanceRepository.saveAndFlush(record);

        return getReliability(userId);
    }

    @Transactional(readOnly = true)
    public ReliabilityResponse getReliability(UUID userId) {
        requireUser(userId);

        List<AttendanceRecord> recent = attendanceRepository.findTop20ByUserIdOrderByRecordedAtDesc(userId);
        List<AttendanceStatus> statuses = recent.stream().map(AttendanceRecord::getStatus).toList();

        int score = ReliabilityCalculator.score(statuses);
        String tier = ReliabilityCalculator.tier(score, statuses.size());

        return new ReliabilityResponse(
                userId,
                score,
                tier,
                statuses.size(),
                attendanceRepository.countByUserIdAndStatus(userId, AttendanceStatus.ATTENDED),
                attendanceRepository.countByUserIdAndStatus(userId, AttendanceStatus.LATE_CANCELLATION),
                attendanceRepository.countByUserIdAndStatus(userId, AttendanceStatus.NO_SHOW),
                recent.stream().map(ReliabilityResponse.AttendanceEntry::from).toList());
    }

    @Transactional
    public void deleteAllForUser(UUID userId) {
        attendanceRepository.deleteAllForUser(userId);
    }

    private void requireUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User with id " + userId + " does not exist");
        }
    }
}
