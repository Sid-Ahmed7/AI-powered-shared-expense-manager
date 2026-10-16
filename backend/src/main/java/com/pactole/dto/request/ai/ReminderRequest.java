package com.pactole.dto.request.ai;

import java.util.UUID;

import com.pactole.enums.Tone;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReminderRequest {

    @NotNull
    private UUID fromMemberId;

    @NotNull
    private UUID toMemberId;

    @NotNull
    @Positive
    private long amountCents;

    private Tone tone;
}
