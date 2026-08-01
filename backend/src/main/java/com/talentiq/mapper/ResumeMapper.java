package com.talentiq.mapper;

import com.talentiq.dto.response.ResumeSummaryResponse;
import com.talentiq.dto.response.ResumeUploadResponse;
import com.talentiq.entity.Resume;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ResumeMapper {
    ResumeSummaryResponse toSummaryResponse(Resume resume);
    ResumeUploadResponse toUploadResponse(Resume resume);
}
