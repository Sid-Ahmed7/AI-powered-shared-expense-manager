package com.pactole.dto.request.login;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor 
@NoArgsConstructor 
@Getter 
@Setter 
public class LoginRequest {

    @NotBlank
    @Email 
    @Size(max = 100)
    private String email;

    @NotBlank 
    @Size(max = 12)
    private String password;

    @Override 
    public String toString() {
        return  "LoginRequest[email=" + email + "]";
    }
    
}
