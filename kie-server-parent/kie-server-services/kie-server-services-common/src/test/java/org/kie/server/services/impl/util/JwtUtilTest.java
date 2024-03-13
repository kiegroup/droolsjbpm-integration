package org.kie.server.services.impl.util;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

public class JwtUtilTest {

    private static Logger LOGGER = LoggerFactory.getLogger(JwtUtilTest.class);

    @Test
    public void testJwtSigned() throws Exception {

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        KeyPair keyPair = kpg.generateKeyPair();

        JwtService service = JwtService.newJwtServiceBuilder()
                .keys((RSAPublicKey) keyPair.getPublic(), (RSAPrivateKey) keyPair.getPrivate())
                .issuer("jBPM")
                .build();

        String token = service.token("myUser", "role1", "role2");

        JwtUserDetails user = service.decodeUserDetails(token);
        Assertions.assertThat(user.getUser()).isEqualTo("myUser");
        Assertions.assertThat(user.getRoles()).containsExactly("role1", "role2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJwtBadSigned() throws Exception {

        KeyPairGenerator kpgIn = KeyPairGenerator.getInstance("RSA");
        KeyPair keyPairIn = kpgIn.generateKeyPair();

        JwtService serviceInput = JwtService.newJwtServiceBuilder()
                .keys((RSAPublicKey) keyPairIn.getPublic(), (RSAPrivateKey) keyPairIn.getPrivate())
                .issuer("jBPM")
                .build();

        String token = serviceInput.token("myUser", "role1", "role2");

        KeyPairGenerator kpgOut = KeyPairGenerator.getInstance("RSA");
        KeyPair keyPairOut = kpgOut.generateKeyPair();

        JwtService serviceOutput = JwtService.newJwtServiceBuilder()
                .keys((RSAPublicKey) keyPairOut.getPublic(), (RSAPrivateKey) keyPairOut.getPrivate())
                .issuer("jBPM")
                .build();

        serviceOutput.decodeUserDetails(token);
    }

    @Test
    public void testJwtNotSigned() throws Exception {
        JwtService service = JwtService.newJwtServiceBuilder()
                .issuer("jBPM")
                .build();

        String token = service.token("myUser", "role1", "role2");
        LOGGER.info(token);

        JwtUserDetails user = service.decodeUserDetails(token);
        Assertions.assertThat(user.getUser()).isEqualTo("myUser");
        Assertions.assertThat(user.getRoles()).containsExactly("role1", "role2");
    }

    @Test
    public void testJwtMissingSubjectInfo() throws Exception {
        String token =  JWT.create().withIssuer("jBPM").withClaim("roles", Arrays.asList("role1")).sign(Algorithm.none());
        LOGGER.info(token);

        JwtService service = JwtService.newJwtServiceBuilder()
                .issuer("jBPM")
                .build();
        JwtUserDetails user = service.decodeUserDetails(token);
        Assertions.assertThat(user.getUser()).isNull();
        Assertions.assertThat(user.getRoles()).containsExactly("role1");
    }

    @Test
    public void testJwtMissingRolesInfo() throws Exception {
        String token =  JWT.create().withIssuer("jBPM").withSubject("myUser").sign(Algorithm.none());
        LOGGER.info(token);

        JwtService service = JwtService.newJwtServiceBuilder()
                .issuer("jBPM")
                .build();
        JwtUserDetails user = service.decodeUserDetails(token);
        Assertions.assertThat(user.getUser()).isEqualTo("myUser");
        Assertions.assertThat(user.getRoles()).isEmpty();
    }

    @Test
    public void testJwtEmptyToken() throws Exception {
        String token =  JWT.create().withIssuer("jBPM").sign(Algorithm.none());
        LOGGER.info(token);

        JwtService service = JwtService.newJwtServiceBuilder()
                .issuer("jBPM")
                .build();
        JwtUserDetails user = service.decodeUserDetails(token);
        Assertions.assertThat(user.getUser()).isNull();
        Assertions.assertThat(user.getRoles()).isEmpty();
    }
}
