package com.yessinCoding.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MfaSetupRequest {

    @NotBlank(message = "Email is mandatory")
    private String email;
}
