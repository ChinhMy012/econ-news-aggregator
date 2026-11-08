package com.econnews.auth.application.port.out;

import com.econnews.auth.application.model.AuthenticatedAdmin;
import com.econnews.auth.application.model.IssuedAccessToken;

//Port cấp token
public interface AccessTokenIssuer {

    IssuedAccessToken issue(AuthenticatedAdmin admin);
}