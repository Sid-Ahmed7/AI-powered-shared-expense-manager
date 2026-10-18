package com.pactole.dto.request.settlement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SettlementRequest {

    @NotNull
    private UUID fromMemberId;

    @NotNull
    private UUID toMemberId;

    @NotNull
    @DecimalMin("0.01")
    @DecimalMax("1000000.00")
    @Digits(integer = 7, fraction = 2)
    private BigDecimal amount;

    private LocalDate date;

    @Size(max = 200)
    private String note;
}
