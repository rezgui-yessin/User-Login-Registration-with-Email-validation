package com.yessinCoding.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MfaSetupResponse {
    private String secret;
    private String otpauthUrl;
    private boolean enabled;
}
