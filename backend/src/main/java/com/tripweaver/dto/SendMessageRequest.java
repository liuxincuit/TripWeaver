package com.tripweaver.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendMessageRequest {

    @NotNull(message = "planId 不能为空")
    private Long planId;

    @NotBlank(message = "message 不能为空")
    private String message;
}