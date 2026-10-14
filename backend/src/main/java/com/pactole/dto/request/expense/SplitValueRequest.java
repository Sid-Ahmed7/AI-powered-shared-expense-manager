package com.pactole.dto.request.expense;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter 
@NoArgsConstructor
@AllArgsConstructor
public class SplitValueRequest {

    @NotNull
    private UUID memberId;

    @DecimalMin("0")
    @DecimalMax("1000000")
    @Digits(integer = 7, fraction = 4)
    private BigDecimal value;
}
