package com.talentiq.dto.response;

import com.talentiq.dto.EducationEntry;
import com.talentiq.dto.ExperienceEntry;
import com.talentiq.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private String headline;
    private List<ExperienceEntry> experience;
    private List<EducationEntry> education;
    private List<String> skills;
}
