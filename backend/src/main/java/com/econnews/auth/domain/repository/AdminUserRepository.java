package com.econnews.auth.domain.repository;

import java.util.Optional;

import com.econnews.auth.domain.model.AdminUser;

public interface AdminUserRepository {

    Optional<AdminUser> findByUsername(String username);

    Optional<AdminUser> findById(Long id);
}