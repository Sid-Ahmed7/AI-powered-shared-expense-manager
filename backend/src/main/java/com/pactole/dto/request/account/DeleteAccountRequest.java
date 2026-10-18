package com.pactole.dto.request.account;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor  
@Getter 
@Setter 
public class DeleteAccountRequest {
    
    @NotBlank 
    @Size(max = 12)
    private String password;

    @Override
    public String toString() {
        return "DeleteAccountRequest[***]";
    }
}
