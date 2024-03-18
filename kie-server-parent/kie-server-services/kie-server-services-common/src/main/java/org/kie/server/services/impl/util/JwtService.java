/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.services.impl.util;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

public class JwtService {

    private JWTVerifier verifier;

    private Algorithm algorithm;
    private String issuer;

    private JwtService() {
        this(Algorithm.none());
    }

    private JwtService(Algorithm algorithm) {
        this(algorithm, "jBPM");
    }

    private JwtService(Algorithm algorithm, String issuer) {
        this.issuer = issuer;
        this.algorithm = algorithm;
        this.verifier = JWT.require(algorithm)
                .withIssuer(issuer)
                .build();
    }

    public String getIssuer() {
        return issuer;
    }

    public String token(String user, String... roles) {
        return JWT.create().withIssuer(this.issuer).withSubject(user).withClaim("roles", Arrays.asList(roles)).sign(algorithm);
    }

    public static JwtServiceBuilder newJwtServiceBuilder() {
        return new JwtServiceBuilder();
    }

    public static class JwtServiceBuilder {
        Algorithm algorithm;
        String issuer;

        public JwtServiceBuilder keys(RSAPublicKey publicKey, RSAPrivateKey privateKey) {
            this.algorithm = Algorithm.RSA256(publicKey, privateKey);
            return this;
        }

        public JwtServiceBuilder issuer(String issuer) {
            this.issuer = issuer;
            return this;
        }

        public JwtService build() {
            return new JwtService(algorithm != null ? algorithm : Algorithm.none(), issuer != null ? issuer : "jBPM");
        }

        public JwtServiceBuilder keyPair(KeyPair keyPair) {
            if (keyPair != null) {
                this.algorithm = Algorithm.RSA256((RSAPublicKey) keyPair.getPublic(), (RSAPrivateKey) keyPair.getPrivate());
            }
            return this;
        }

    }

    public JwtUserDetails decodeUserDetails(String token) {
        try {
            DecodedJWT decodedJWT = verifier.verify(token);
            String user = decodedJWT.getSubject();
            Claim rolesClaim = decodedJWT.getClaim("roles");
            List<String> roles = rolesClaim.asList(String.class);
            return new JwtUserDetails(user, roles != null ? roles : new ArrayList<>());
        } catch (JWTVerificationException exception) {
            throw new IllegalArgumentException(exception);
        }
    }

}
