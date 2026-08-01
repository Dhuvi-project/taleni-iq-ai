package com.talentiq.dto.request;

import com.talentiq.dto.EducationEntry;
import com.talentiq.dto.ExperienceEntry;
import jakarta.validation.Valid;
import lombok.Data;

import java.util.List;

@Data
public class ProfileUpdateRequest {
    private String headline;

    @Valid
    private List<ExperienceEntry> experience;

    @Valid
    private List<EducationEntry> education;

    private List<String> skills;
}
