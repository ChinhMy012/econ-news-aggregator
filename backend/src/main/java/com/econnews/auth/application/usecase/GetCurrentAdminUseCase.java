package com.econnews.auth.application.usecase;

import com.econnews.auth.application.exception.AdminUnavailableException;
import com.econnews.auth.application.model.AuthenticatedAdmin;
import com.econnews.auth.domain.repository.AdminUserRepository;

public final class GetCurrentAdminUseCase {
    private final AdminUserRepository repository;
    public GetCurrentAdminUseCase(AdminUserRepository repository) { this.repository = repository; }
    public AuthenticatedAdmin execute(Long id) {
        var user = repository.findById(id).filter(account -> account.isEnabled())
                .orElseThrow(AdminUnavailableException::new);
        return new AuthenticatedAdmin(user.getId(), user.getUsername());
    }
}
