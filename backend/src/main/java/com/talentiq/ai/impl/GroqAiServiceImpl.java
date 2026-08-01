package com.talentiq.ai.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentiq.ai.AiService;
import com.talentiq.ai.GroqClient;
import com.talentiq.ai.SkillCatalog;
import com.talentiq.dto.response.CareerRecommendationResponse;
import com.talentiq.dto.response.InterviewQuestionItem;
import com.talentiq.dto.response.MatchResponse;
import com.talentiq.dto.response.ResumeAnalysisResponse;
import com.talentiq.dto.response.SkillGapResponse;
import com.talentiq.exception.AiServiceException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * {@link AiService} implementation backed by the real Groq API. Every method builds a
 * task-specific prompt that asks Groq to return ONLY a JSON object/array matching the exact
 * shape of the corresponding response DTO (field names match 1:1 so parsing is direct), then
 * parses that JSON via Jackson. All HTTP/network concerns are delegated to {@link GroqClient}.
 */
@Service
public class GroqAiServiceImpl implements AiService {

    private static final String KNOWN_SKILLS_HINT = String.join(", ", SkillCatalog.ALL_SKILLS);

    private final GroqClient groqClient;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public GroqAiServiceImpl(GroqClient groqClient) {
        this.groqClient = groqClient;
    }

    @Override
    public ResumeAnalysisResponse analyzeResume(String parsedResumeText) {
        String prompt = """
                You are an ATS (Applicant Tracking System) resume analysis engine used by a recruitment
                platform. Analyze the resume text below and respond with ONLY a single valid JSON object
                (no markdown fences, no commentary, no explanation) matching EXACTLY this shape:

                {
                  "atsScore": <number 0-100, overall ATS compatibility score>,
                  "sectionScores": {"contact": <number 0-100>, "summary": <number 0-100>, "experience": <number 0-100>, "education": <number 0-100>, "skills": <number 0-100>},
                  "keywordDensity": <number, percentage of meaningful keywords/action verbs relative to total words, 0-100>,
                  "actionVerbCount": <integer, count of strong action verbs used, e.g. led, built, optimized>,
                  "strengths": [<3 to 6 short strings describing concrete resume strengths>],
                  "weaknesses": [<3 to 6 short strings describing concrete resume weaknesses or improvement areas>],
                  "extractedSkills": [<lowercase strings, specific technical/professional skills actually found in the resume>]
                }

                Known reference skills you may recognize (not exhaustive, use your own judgement too):
                %s

                Resume text:
                \"\"\"
                %s
                \"\"\"
                """.formatted(KNOWN_SKILLS_HINT, nullToEmpty(parsedResumeText));

        String json = groqClient.generateJson(prompt);
        ResumeAnalysisResponse response = parse(json, ResumeAnalysisResponse.class, "resume analysis");
        response.setResumeId(null);
        return response;
    }

    @Override
    public MatchResponse matchResumeToJob(String resumeText, String jobDescription) {
        String prompt = """
                You are a resume-to-job matching engine used by a recruitment platform. Compare the resume
                text and the job description below and respond with ONLY a single valid JSON object (no
                markdown fences, no commentary) matching EXACTLY this shape:

                {
                  "matchScore": <number 0-100, overall fit score between the resume and the job>,
                  "matchedKeywords": [<lowercase strings, important keywords/skills from the job description that ARE present in the resume>],
                  "missingKeywords": [<lowercase strings, important keywords/skills from the job description that are NOT present in the resume>]
                }

                Resume text:
                \"\"\"
                %s
                \"\"\"

                Job description:
                \"\"\"
                %s
                \"\"\"
                """.formatted(nullToEmpty(resumeText), nullToEmpty(jobDescription));

        String json = groqClient.generateJson(prompt);
        return parse(json, MatchResponse.class, "resume/job match");
    }

    @Override
    public List<InterviewQuestionItem> generateInterviewQuestions(String role, String type) {
        String normalizedType = type == null || type.isBlank() ? "ALL" : type.trim().toUpperCase(Locale.ROOT);
        String typeInstruction = "ALL".equals(normalizedType)
                ? "Include a realistic mix of TECHNICAL, BEHAVIORAL, and CODING questions (roughly evenly split)."
                : "Every question must have \"type\": \"" + normalizedType + "\" — do not include other types.";

        String prompt = """
                You are an interview question generator for a recruitment platform. Generate 8 to 12
                realistic interview questions for the role "%s". %s

                Vary the difficulty, topic angle, and exact phrasing each time you are asked — do not
                default to the most generic, most commonly cited textbook questions for this role.
                Draw from a wide range of relevant sub-topics rather than repeating the same handful
                of "classic" questions. (Session variety token, ignore its value: %s)

                Respond with ONLY a single valid JSON object (no markdown fences, no commentary)
                matching EXACTLY this shape:

                {"questions": [{"type": "TECHNICAL" | "BEHAVIORAL" | "CODING", "questionText": "<the question>"}]}

                The "questions" array must contain between 8 and 12 elements total.
                """.formatted(nullToEmpty(role), typeInstruction, ThreadLocalRandom.current().nextInt(1_000_000));

        String json = groqClient.generateJson(prompt, 0.95);
        return parseWrappedList(json, "questions", InterviewQuestionItem.class, "interview questions");
    }

    @Override
    public SkillGapResponse computeSkillGap(String resumeText) {
        String prompt = """
                You are a career skill-gap analysis engine for a recruitment platform. Based on the resume
                text below, identify the candidate's current skills and the skills required for the
                single best-fit role for this candidate, then produce an 8-week upskilling roadmap.
                Respond with ONLY a single valid JSON object (no markdown fences, no commentary) matching
                EXACTLY this shape:

                {
                  "currentSkills": [{"name": "<skill>", "level": "Beginner" | "Intermediate" | "Advanced"}],
                  "requiredSkills": [{"name": "<skill>", "level": "Required"}],
                  "gapScore": <number 0-100, percentage of required skills the candidate is missing>,
                  "roadmap": [{"week": <integer 1-8>, "focus": "<topic to learn that week>", "resources": [<2-3 short strings naming a resource, course, or activity>]}],
                  "certifications": [<3-5 strings naming real, relevant certifications to pursue>]
                }

                The "roadmap" array must contain exactly 8 entries, one per week, weeks 1 through 8.

                Known reference skills you may recognize (not exhaustive, use your own judgement too):
                %s

                Resume text:
                \"\"\"
                %s
                \"\"\"
                """.formatted(KNOWN_SKILLS_HINT, nullToEmpty(resumeText));

        String json = groqClient.generateJson(prompt);
        SkillGapResponse response = parse(json, SkillGapResponse.class, "skill gap analysis");
        response.setResumeId(null);
        return response;
    }

    @Override
    public List<CareerRecommendationResponse> recommendCareers(String resumeText, List<String> currentSkills) {
        String skillsCsv = currentSkills == null || currentSkills.isEmpty()
                ? "(no skills provided)"
                : String.join(", ", currentSkills);
        String resumeSection = resumeText == null || resumeText.isBlank()
                ? "(no resume uploaded yet — base recommendations on the skill list alone, and lean general/entry-to-mid level)"
                : resumeText;

        String prompt = """
                You are a job recommendation engine for a recruitment platform serving candidates in the
                Indian IT job market. Read the candidate's resume text and current skills below, then
                recommend 4 to 6 realistic, currently-plausible job openings that genuinely suit this
                candidate's experience level, background, and skill set.

                Rules:
                - Judge fit against the actual resume content below (seniority, domain, tech stack, years
                  of experience implied), not just the flat skill list.
                - "company" must be a realistic, illustrative example of the KIND of company that hires
                  for this role (e.g. "TCS", "Infosys", "a mid-size Bengaluru fintech startup", "a global
                  GCC/captive center like a Walmart Global Tech", "an early-stage SaaS startup") — framed
                  as representative, not a literal scraped live job posting. Vary company examples across
                  the list; do not repeat the same company or company type for every entry.
                - "location" should be realistic Indian IT hub locations or explicit remote-India
                  arrangements, e.g. "Bengaluru, India", "Hyderabad, India", "Pune, India", "Remote (India)",
                  "Gurugram, India".
                - Salary must reflect REALISTIC current Indian IT industry compensation bands for the
                  seniority level implied by the resume, expressed as LPA (Lakhs Per Annum, annual CTC in
                  INR). Junior/fresher roles are typically single-digit to low-teens LPA, mid-level roles
                  roughly mid-teens to 30s LPA, and senior/lead/architect roles can go well beyond that —
                  use your judgement based on the actual resume seniority signals rather than a fixed table.
                - "matchScore" (0-100) must genuinely reflect how well the role fits THIS candidate's resume
                  and skills, not a generic filler number — vary it meaningfully across the recommendations.
                - "marketDemand" must be 1-2 sentences reflecting genuine, current Indian tech job market
                  conditions and outlook for that specific role (hiring trends, in-demand adjacent skills,
                  competitiveness), not generic filler text.
                - "requiredUpskilling" must list 2-4 short, specific, currently-relevant skills/technologies
                  the candidate should learn to be a stronger fit for that role.
                - Vary which roles and companies you suggest across calls rather than always defaulting to
                  the same obvious title.

                Respond with ONLY a single valid JSON object (no markdown fences, no commentary) matching
                EXACTLY this shape:

                {
                  "recommendations": [
                    {
                      "jobTitle": "<job title>",
                      "company": "<realistic, illustrative employer example>",
                      "location": "<Indian city, India or Remote (India)>",
                      "matchScore": <number 0-100>,
                      "salaryMinLPA": <number, minimum annual CTC in INR Lakhs Per Annum>,
                      "salaryMaxLPA": <number, maximum annual CTC in INR Lakhs Per Annum, greater than salaryMinLPA>,
                      "marketDemand": "<1-2 sentences on current Indian IT market demand/outlook for this role>",
                      "growthPercent": <number, projected year-over-year demand growth percentage for this role>,
                      "requiredUpskilling": [<2-4 short strings naming skills to learn for this role>]
                    }
                  ]
                }

                The "recommendations" array must contain 4 to 6 elements.

                Candidate's current skills: %s

                Candidate's resume text:
                \"\"\"
                %s
                \"\"\"
                """.formatted(skillsCsv, resumeSection);

        String json = groqClient.generateJson(prompt, 0.8);
        return parseWrappedList(json, "recommendations", CareerRecommendationResponse.class, "career recommendations");
    }

    private <T> T parse(String json, Class<T> type, String context) {
        try {
            return objectMapper.readValue(stripCodeFences(json), type);
        } catch (Exception ex) {
            throw new AiServiceException(
                    "Groq returned a response for " + context + " that could not be parsed as the expected JSON shape: "
                            + ex.getMessage(), ex);
        }
    }

    /**
     * Groq's JSON mode ({@code response_format: json_object}) requires the top-level response to
     * be a JSON object, not a bare array — so list-returning prompts ask for the array nested
     * under {@code wrapperField} and this unwraps it before deserializing the element list.
     */
    private <T> List<T> parseWrappedList(String json, String wrapperField, Class<T> elementType, String context) {
        try {
            com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(stripCodeFences(json));
            com.fasterxml.jackson.databind.JsonNode arrayNode = root.path(wrapperField);
            if (!arrayNode.isArray()) {
                throw new IllegalStateException("expected a \"" + wrapperField + "\" array field in the response");
            }
            return objectMapper.readerForListOf(elementType).readValue(arrayNode);
        } catch (Exception ex) {
            throw new AiServiceException(
                    "Groq returned a response for " + context + " that could not be parsed as the expected JSON shape: "
                            + ex.getMessage(), ex);
        }
    }

    /** Defensive: strips ```json / ``` fences in case the model ignores the "no markdown" instruction. */
    private String stripCodeFences(String text) {
        if (text == null) {
            return "";
        }
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline != -1) {
                trimmed = trimmed.substring(firstNewline + 1);
            }
            int lastFence = trimmed.lastIndexOf("```");
            if (lastFence != -1) {
                trimmed = trimmed.substring(0, lastFence);
            }
        }
        return trimmed.trim();
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
