package com.pactole.dto.request.group;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor 
public class CreateGroupRequest {
    @NotBlank 
    @Size(max = 80) 
    private String name;
    private String currency;
    @Size(max = 30)
     private List<String> members;
}
