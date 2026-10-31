package com.yessinCoding.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ForgotPasswordRequest {

    @Email(message = "Email is not well formatted")
    @NotBlank(message = "Email is mandatory")
    private String email;
}
