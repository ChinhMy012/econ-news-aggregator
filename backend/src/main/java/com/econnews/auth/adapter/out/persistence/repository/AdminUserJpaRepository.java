package com.econnews.auth.adapter.out.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.econnews.auth.adapter.out.persistence.entity.AdminUserJpaEntity;

public interface AdminUserJpaRepository
        extends JpaRepository<AdminUserJpaEntity, Long> {

    Optional<AdminUserJpaEntity> findByUsername(String username);
}
