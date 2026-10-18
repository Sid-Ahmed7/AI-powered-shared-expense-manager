package com.pactole.dto.request.expense;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor
public class ItemRequest {

    @NotBlank
    @Size(max = 120)
    private String label;

    @NotNull
    @DecimalMin("0.00")
    @DecimalMax("1000000.00")
    @Digits(integer = 7, fraction = 2)
    private BigDecimal amount;

    @NotNull
    @Size(min = 1, max = 50)
    private List<UUID> memberIds;
}
