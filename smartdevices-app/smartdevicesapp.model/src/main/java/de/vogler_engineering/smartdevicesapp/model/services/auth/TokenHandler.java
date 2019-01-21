/**
 * Copyright (c) Vogler Engineering GmbH. All rights reserved.
 * Licensed under the MIT License. See LICENSE.md in the project root for license information.
 */
package de.vogler_engineering.smartdevicesapp.model.services.auth;

import de.vogler_engineering.smartdevicesapp.service.entities.auth.JWTResponse;
import de.vogler_engineering.smartdevicesapp.service.entities.auth.OIDCAuthorityInformation;
import de.vogler_engineering.smartdevicesapp.service.entities.auth.OIDCCerts;
import io.reactivex.Maybe;
import io.reactivex.Single;

/**
 * Created by vh on 20.02.2018.
 */

public class TokenHandler {

    private final String authorityUrl;
    private OIDCAuthorityInformation wellKnown;
    private JWTResponse token;
    private OIDCCerts authorityCert;

    public TokenHandler(String authorityUrl) {
        this.authorityUrl = authorityUrl;
    }

    public Maybe init(){
        return null;//TODO
    }

    private static boolean validateToken(String token, String certString){
        return true;
    }

    public boolean IsTokenValid(){
        return false;
    }

    public boolean IsTokenExpired(){
        return false;
    }

    public Single<JWTResponse> requestAccessToken(String username, String password) {
        return Single.just(null);
    }


}
