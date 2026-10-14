package com.pactole.dto.response.group;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberBalance {

    private UUID memberId;
    private String name;
    private long paidCents;
    private long consumedCents;
    private long netCents;
}
