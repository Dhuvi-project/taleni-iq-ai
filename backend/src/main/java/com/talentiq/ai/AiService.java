package com.talentiq.ai;

import com.talentiq.dto.response.CareerRecommendationResponse;
import com.talentiq.dto.response.InterviewQuestionItem;
import com.talentiq.dto.response.MatchResponse;
import com.talentiq.dto.response.ResumeAnalysisResponse;
import com.talentiq.dto.response.SkillGapResponse;

import java.util.List;

/**
 * Pluggable AI capability contract for TalentIQ.
 *
 * <p>This interface intentionally decouples "what the platform needs from AI" from "how it is
 * computed." {@link com.talentiq.ai.impl.GroqAiServiceImpl} is the sole implementation and is
 * backed by the Groq API (see {@link com.talentiq.ai.GroqClient}). A future
 * implementation backed by a different LLM provider can implement this same interface and be
 * wired in without touching any controller or service code that depends on {@link AiService}.
 */
public interface AiService {

    /**
     * Analyzes raw resume text and produces an ATS-style scoring breakdown.
     * The returned response's {@code resumeId} field is left null; callers set it.
     */
    ResumeAnalysisResponse analyzeResume(String parsedResumeText);

    /** Computes a match score and matched/missing keyword sets between a resume and a job description. */
    MatchResponse matchResumeToJob(String resumeText, String jobDescription);

    /**
     * Generates a realistic set (8-12) of interview questions for the given role.
     *
     * @param role the target role, e.g. "Java Backend Developer"
     * @param type one of TECHNICAL, BEHAVIORAL, CODING, ALL (case-insensitive); null/blank treated as ALL
     */
    List<InterviewQuestionItem> generateInterviewQuestions(String role, String type);

    /**
     * Computes a skill gap analysis for the given resume text against the best-fit curated role.
     * The returned response's {@code resumeId} field is left null; callers set it.
     */
    SkillGapResponse computeSkillGap(String resumeText);

    /**
     * Recommends 4-6 realistic, currently-open-style job openings in the Indian IT market tailored
     * to the candidate, judged against both their resume text and their extracted current skills.
     *
     * @param resumeText the candidate's latest parsed resume text; may be blank if no resume has
     *                    been uploaded yet, in which case recommendations fall back to the skill list alone
     * @param currentSkills the candidate's current skill set (profile + resume-derived)
     */
    List<CareerRecommendationResponse> recommendCareers(String resumeText, List<String> currentSkills);
}
