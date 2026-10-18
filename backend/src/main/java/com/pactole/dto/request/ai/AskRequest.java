package com.pactole.dto.request.ai;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AskRequest {

    @NotBlank
    @Size(max = 500)
    private String question;

    @Size(max = 10)
    private List<ChatTurn> history;
}
