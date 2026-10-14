package com.pactole.dto.request.expense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.pactole.enums.Category;
import com.pactole.enums.Source;
import com.pactole.enums.SplitMode;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseRequest {

    @NotBlank
    @Size(max = 200)
    private String description;

    @NotNull 
    @DecimalMin("0.01")
    @DecimalMax("1000000.00")
    @Digits(integer = 7, fraction = 2)
    private BigDecimal amount;

    @NotNull
    private UUID payerMemberId;

    private Category category;

    @NotNull
    private SplitMode splitMode;

    private LocalDate date;

    @Valid 
    @Size(max = 50)
    private List<SplitValueRequest> splits;

    @Valid
    @Size(max = 200)
    private List<ItemRequest> items;

    private Source source;

    private Long version;

    private boolean force;
}
