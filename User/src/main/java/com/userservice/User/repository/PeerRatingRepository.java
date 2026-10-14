package com.userservice.User.repository;

import com.userservice.User.model.PeerRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PeerRatingRepository extends JpaRepository<PeerRating, UUID> {

    boolean existsByRaterIdAndRateeIdAndGameId(UUID raterId, UUID rateeId, String gameId);

    List<PeerRating> findByRateeIdOrderByCreatedAtDesc(UUID rateeId);

    @Modifying
    @Query("delete from PeerRating r where r.raterId = :userId or r.rateeId = :userId")
    void deleteAllInvolvingUser(@Param("userId") UUID userId);
}
