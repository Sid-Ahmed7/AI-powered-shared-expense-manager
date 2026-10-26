package com.pactole.dto.request.password;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ForgotPasswordRequest {

    @NotBlank
    @Email
    String email;

    @Override
    public String toString() {
        return "ForgotPasswordRequest[***]";
    }
}