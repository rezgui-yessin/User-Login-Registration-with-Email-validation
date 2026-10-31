package com.yessinCoding.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MfaVerificationRequest {

    @NotBlank(message = "Email is mandatory")
    private String email;

    @NotBlank(message = "MFA code is mandatory")
    @Size(min = 6, max = 6, message = "MFA code must be 6 digits")
    private String code;
}
