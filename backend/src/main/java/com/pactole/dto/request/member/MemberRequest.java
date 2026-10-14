package com.pactole.dto.request.member;

import java.util.UUID;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor 
@AllArgsConstructor 
@Data 
public class MemberRequest {
    private UUID groupId;
    private String name;
    private UUID accountId;
}
