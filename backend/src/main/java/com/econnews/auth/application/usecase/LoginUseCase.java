package com.econnews.auth.application.usecase;

import com.econnews.auth.application.model.LoginCommand;
import com.econnews.auth.application.model.LoginResult;
import com.econnews.auth.application.port.out.AccessTokenIssuer;

public final class LoginUseCase {
    private final AuthenticateAdminUseCase authenticateAdmin;
    private final AccessTokenIssuer accessTokenIssuer;

    public LoginUseCase(AuthenticateAdminUseCase authenticateAdmin,
            AccessTokenIssuer accessTokenIssuer) {
        this.authenticateAdmin = authenticateAdmin;
        this.accessTokenIssuer = accessTokenIssuer;
    }

    public LoginResult execute(LoginCommand command) {
        var admin = authenticateAdmin.execute(command);
        var token = accessTokenIssuer.issue(admin);
        return new LoginResult(admin, token);
    }
}
