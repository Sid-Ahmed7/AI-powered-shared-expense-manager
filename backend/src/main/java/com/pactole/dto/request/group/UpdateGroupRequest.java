package com.pactole.dto.request.group;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor 
public class UpdateGroupRequest {
    @Size(max = 80) 
    private String name;
}
