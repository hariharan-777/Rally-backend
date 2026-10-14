package com.userservice.User.repository;

import com.userservice.User.model.SkillRating;
import com.userservice.User.model.Sport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SkillRatingRepository extends JpaRepository<SkillRating, UUID> {

    Optional<SkillRating> findByUserIdAndSport(UUID userId, Sport sport);

    List<SkillRating> findByUserId(UUID userId);

    @Modifying
    @Query("delete from SkillRating s where s.userId = :userId")
    void deleteAllForUser(@Param("userId") UUID userId);
}
