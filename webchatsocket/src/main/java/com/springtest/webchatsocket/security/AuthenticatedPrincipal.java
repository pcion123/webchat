package com.springtest.webchatsocket.security;

import java.security.Principal;

public record AuthenticatedPrincipal(String name, String username) implements Principal {

    @Override
    public String getName() {
        return name;
    }

}
