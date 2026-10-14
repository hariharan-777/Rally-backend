package com.userservice.User.repository;

import com.userservice.User.model.MatchResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MatchResultRepository extends JpaRepository<MatchResult, UUID> {

    boolean existsByGameId(String gameId);
}
