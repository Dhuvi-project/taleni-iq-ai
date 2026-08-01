package com.talentiq.service;

import com.talentiq.dto.response.SkillGapResponse;

public interface SkillGapService {
    SkillGapResponse computeGap(Long resumeId);
}
