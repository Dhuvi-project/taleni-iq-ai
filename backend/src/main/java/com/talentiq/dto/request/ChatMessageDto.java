package com.talentiq.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A single prior turn in an assistant chat conversation. {@code role} is "user" or "assistant". */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDto {

    private String role;

    private String text;
}
