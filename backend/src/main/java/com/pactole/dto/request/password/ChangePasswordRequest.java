package com.pactole.dto.request.password;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor 
@AllArgsConstructor 
@Getter 
@Setter 
public class ChangePasswordRequest {

    @NotBlank
    @Size(max = 12)
    String currentPassword;

    @NotBlank 
    @Size(max = 12)
    String newPassword;
    
    @Override
    public String toString() {
        return "ChangePasswordRequest[***]";
    }
}
