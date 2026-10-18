package com.pactole.dto.request.member;

import java.util.UUID;


import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor 
@Getter 
@Setter 
@Data 
public class MemberRequest {
    private UUID groupId;
    private String name;
    private UUID accountId;
}
