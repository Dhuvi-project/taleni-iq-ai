package com.talentiq.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A single work-experience entry on a user's profile. When {@code current} is {@code true} the
 * candidate is still working this role, so {@code endDate} should be null/blank.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExperienceEntry {

    @NotBlank(message = "Experience title is required")
    private String title;

    @NotBlank(message = "Experience company is required")
    private String company;

    @NotBlank(message = "Experience start date is required")
    private String startDate;

    private String endDate;
    private boolean current;
}
