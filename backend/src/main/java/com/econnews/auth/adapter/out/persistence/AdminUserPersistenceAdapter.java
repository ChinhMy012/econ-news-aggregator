package com.econnews.auth.adapter.out.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.econnews.auth.adapter.out.persistence.mapper.AdminUserPersistenceMapper;
import com.econnews.auth.adapter.out.persistence.repository.AdminUserJpaRepository;
import com.econnews.auth.domain.model.AdminUser;
import com.econnews.auth.domain.repository.AdminUserRepository;

@Repository
@Transactional(readOnly = true)
public class AdminUserPersistenceAdapter implements AdminUserRepository {

    private final AdminUserJpaRepository jpaRepository;

    public AdminUserPersistenceAdapter(
            AdminUserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<AdminUser> findByUsername(String username) {
        return jpaRepository.findByUsername(username)
                .map(AdminUserPersistenceMapper::toDomain);
    }

    @Override
    public Optional<AdminUser> findById(Long id) {
        return jpaRepository.findById(id)
                .map(AdminUserPersistenceMapper::toDomain);
    }
}