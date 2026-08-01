package com.talentiq.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EducationEntry {

    @NotBlank(message = "Degree is required")
    private String degree;

    @NotBlank(message = "Institution is required")
    private String institution;

    @NotBlank(message = "Year is required")
    private String year;
}
