package com.pactole.dto.request.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatTurn {

    @NotBlank
    @Pattern(regexp = "user|assistant")
    private String role;

    @NotBlank
    @Size(max = 2000)
    private String content;
}
