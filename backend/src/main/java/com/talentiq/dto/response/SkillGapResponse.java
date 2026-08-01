package com.talentiq.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkillGapResponse {
    private Long resumeId;
    private List<SkillLevel> currentSkills;
    private List<SkillLevel> requiredSkills;
    private Double gapScore;
    private List<RoadmapWeek> roadmap;
    private List<String> certifications;
}
