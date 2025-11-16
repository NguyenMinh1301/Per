package com.per.auth.dto.response;

import lombok.Getter;

@Getter
public class IntrospectResponse {
    private final boolean introspect;

    private IntrospectResponse(boolean introspect) {
        this.introspect = introspect;
    }

    public static IntrospectResponse active() {
        return new IntrospectResponse(true);
    }

    public static IntrospectResponse inactive() {
        return new IntrospectResponse(false);
    }
}
