package com.FundRaise.F25.repository;

import com.FundRaise.F25.model.K25User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface K25UserRepository extends JpaRepository<K25User, Long> {
    Optional<K25User> findByEmail(String email);
    boolean existsByEmail(String email);
}

