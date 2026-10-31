package com.yessinCoding.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RevokeAllSessionsRequest {
    private String email;
}
