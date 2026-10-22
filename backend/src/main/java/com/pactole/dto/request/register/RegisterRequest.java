package com.pactole.dto.request.register;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor 
@Getter 
@Setter 
public class RegisterRequest {
    
    @NotBlank
    @Email 
    @Size(max = 100)
    private String email;

    @NotBlank
    @Size(min = 12, message = "{validation.password.size}")
    private String password;

    @NotBlank 
    @Size(max = 100)
    private String fullName;

    @Override 
    public String toString() {
        return  "RegisterRequest[email=" + email + "]";
    }
}
