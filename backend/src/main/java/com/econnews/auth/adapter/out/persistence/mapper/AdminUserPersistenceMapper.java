package com.econnews.auth.adapter.out.persistence.mapper;

import com.econnews.auth.adapter.out.persistence.entity.AdminUserJpaEntity;
import com.econnews.auth.domain.model.AdminUser;

public final class AdminUserPersistenceMapper {

    private AdminUserPersistenceMapper() {
    }

    public static AdminUser toDomain(AdminUserJpaEntity entity) {
        return new AdminUser(
                entity.getId(),
                entity.getUsername(),
                entity.getPasswordHash(),
                entity.isEnabled());
    }
}
