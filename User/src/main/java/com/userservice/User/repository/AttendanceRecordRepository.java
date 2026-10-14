package com.userservice.User.repository;

import com.userservice.User.model.AttendanceRecord;
import com.userservice.User.model.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, UUID> {

    Optional<AttendanceRecord> findByUserIdAndGameId(UUID userId, String gameId);

    /** The most recent 20 games: this is the window the reliability score is calculated over. */
    List<AttendanceRecord> findTop20ByUserIdOrderByRecordedAtDesc(UUID userId);

    long countByUserIdAndStatus(UUID userId, AttendanceStatus status);

    @Modifying
    @Query("delete from AttendanceRecord a where a.userId = :userId")
    void deleteAllForUser(@Param("userId") UUID userId);
}
