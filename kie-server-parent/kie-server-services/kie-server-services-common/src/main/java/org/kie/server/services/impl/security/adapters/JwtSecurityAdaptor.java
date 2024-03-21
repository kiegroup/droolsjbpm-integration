package org.kie.server.services.impl.security.adapters;

import java.util.List;

import org.kie.server.api.security.SecurityAdapter;
import org.kie.server.services.impl.util.JwtUserDetails;

public class JwtSecurityAdaptor implements SecurityAdapter {

    private static ThreadLocal<JwtUserDetails> threadLocal = new ThreadLocal<JwtUserDetails>() {
        @Override
        public JwtUserDetails initialValue() {
            return new JwtUserDetails();
        }
    };

    public static void login(JwtUserDetails userDetails) {
        threadLocal.set(userDetails);
    }

    @Override
    public String getUser(Object... params) {
        JwtUserDetails userDetails = threadLocal.get();
        if (!userDetails.isLogged()) {
            return null;
        }
        return userDetails.getUser();
    }

    @Override
    public List<String> getRoles(Object... params) {
        return threadLocal.get().getRoles();
    }

    public static void logout() {
        threadLocal.set(null);
    }

}
