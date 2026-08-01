package com.talentiq.dto.response;

import com.talentiq.entity.enums.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InterviewQuestionItem {
    private QuestionType type;
    private String questionText;
}
