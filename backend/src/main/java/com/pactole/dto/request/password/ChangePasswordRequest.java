package com.pactole.dto.request.password;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor 
@Getter 
@Setter 
public class ChangePasswordRequest {

    @NotBlank
    String currentPassword;

    @NotBlank
    @Size(min = 12, message = "{validation.password.size}")
    String newPassword;
    
    @Override
    public String toString() {
        return "ChangePasswordRequest[***]";
    }
}
