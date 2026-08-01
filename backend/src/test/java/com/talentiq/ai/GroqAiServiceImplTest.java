package com.talentiq.ai;

import com.talentiq.ai.impl.GroqAiServiceImpl;
import com.talentiq.dto.response.CareerRecommendationResponse;
import com.talentiq.dto.response.InterviewQuestionItem;
import com.talentiq.dto.response.MatchResponse;
import com.talentiq.dto.response.ResumeAnalysisResponse;
import com.talentiq.dto.response.SkillGapResponse;
import com.talentiq.entity.enums.QuestionType;
import com.talentiq.exception.AiServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Verifies {@link GroqAiServiceImpl}'s prompt-to-DTO JSON parsing in isolation. The Groq HTTP
 * call itself ({@link GroqClient}) is mocked so these tests never touch the network or require
 * a real API key — canned, Groq-shaped JSON strings are returned directly.
 */
@ExtendWith(MockitoExtension.class)
class GroqAiServiceImplTest {

    @Mock
    private GroqClient groqClient;

    private GroqAiServiceImpl aiService;

    @BeforeEach
    void setUp() {
        aiService = new GroqAiServiceImpl(groqClient);
    }

    @Test
    void analyzeResume_parsesCannedJsonIntoDto() {
        String json = """
                {
                  "atsScore": 82.5,
                  "sectionScores": {"contact": 90, "summary": 80, "experience": 85, "education": 70, "skills": 88},
                  "keywordDensity": 12.3,
                  "actionVerbCount": 9,
                  "strengths": ["Strong action verbs", "Clear skills section"],
                  "weaknesses": ["Summary could be more concise"],
                  "extractedSkills": ["java", "spring boot", "postgresql"]
                }
                """;
        when(groqClient.generateJson(anyString())).thenReturn(json);

        ResumeAnalysisResponse response = aiService.analyzeResume("some resume text");

        assertThat(response.getResumeId()).isNull();
        assertThat(response.getAtsScore()).isEqualTo(82.5);
        assertThat(response.getSectionScores()).containsEntry("contact", 90.0);
        assertThat(response.getKeywordDensity()).isEqualTo(12.3);
        assertThat(response.getActionVerbCount()).isEqualTo(9);
        assertThat(response.getStrengths()).contains("Strong action verbs");
        assertThat(response.getWeaknesses()).contains("Summary could be more concise");
        assertThat(response.getExtractedSkills()).containsExactly("java", "spring boot", "postgresql");
    }

    @Test
    void analyzeResume_malformedJson_throwsAiServiceException() {
        when(groqClient.generateJson(anyString())).thenReturn("not valid json at all {");

        assertThatThrownBy(() -> aiService.analyzeResume("resume text"))
                .isInstanceOf(AiServiceException.class)
                .hasMessageContaining("resume analysis");
    }

    @Test
    void analyzeResume_stripsMarkdownCodeFences() {
        String json = """
                ```json
                {
                  "atsScore": 60,
                  "sectionScores": {"contact": 50, "summary": 50, "experience": 50, "education": 50, "skills": 50},
                  "keywordDensity": 5,
                  "actionVerbCount": 2,
                  "strengths": [],
                  "weaknesses": [],
                  "extractedSkills": []
                }
                ```
                """;
        when(groqClient.generateJson(anyString())).thenReturn(json);

        ResumeAnalysisResponse response = aiService.analyzeResume("resume text");

        assertThat(response.getAtsScore()).isEqualTo(60.0);
    }

    @Test
    void matchResumeToJob_parsesCannedJsonIntoDto() {
        String json = """
                {
                  "matchScore": 74.0,
                  "matchedKeywords": ["java", "spring boot"],
                  "missingKeywords": ["kubernetes"]
                }
                """;
        when(groqClient.generateJson(anyString())).thenReturn(json);

        MatchResponse response = aiService.matchResumeToJob("resume", "job description");

        assertThat(response.getMatchScore()).isEqualTo(74.0);
        assertThat(response.getMatchedKeywords()).containsExactly("java", "spring boot");
        assertThat(response.getMissingKeywords()).containsExactly("kubernetes");
    }

    @Test
    void generateInterviewQuestions_parsesJsonArrayIntoList() {
        String json = """
                {
                  "questions": [
                    {"type": "TECHNICAL", "questionText": "Explain the JVM memory model."},
                    {"type": "BEHAVIORAL", "questionText": "Tell me about a time you led a project."},
                    {"type": "CODING", "questionText": "Implement a binary search."}
                  ]
                }
                """;
        when(groqClient.generateJson(anyString(), anyDouble())).thenReturn(json);

        List<InterviewQuestionItem> questions = aiService.generateInterviewQuestions("Java Backend Developer", "ALL");

        assertThat(questions).hasSize(3);
        assertThat(questions.get(0).getType()).isEqualTo(QuestionType.TECHNICAL);
        assertThat(questions.get(0).getQuestionText()).isEqualTo("Explain the JVM memory model.");
    }

    @Test
    void computeSkillGap_parsesCannedJsonIntoDto() {
        String json = """
                {
                  "currentSkills": [{"name": "java", "level": "Advanced"}],
                  "requiredSkills": [{"name": "kubernetes", "level": "Required"}],
                  "gapScore": 45.0,
                  "roadmap": [
                    {"week": 1, "focus": "Kubernetes basics", "resources": ["Official docs", "Online course"]},
                    {"week": 2, "focus": "Kubernetes basics", "resources": ["Official docs", "Online course"]},
                    {"week": 3, "focus": "Kubernetes basics", "resources": ["Official docs", "Online course"]},
                    {"week": 4, "focus": "Kubernetes basics", "resources": ["Official docs", "Online course"]},
                    {"week": 5, "focus": "Kubernetes basics", "resources": ["Official docs", "Online course"]},
                    {"week": 6, "focus": "Kubernetes basics", "resources": ["Official docs", "Online course"]},
                    {"week": 7, "focus": "Kubernetes basics", "resources": ["Official docs", "Online course"]},
                    {"week": 8, "focus": "Kubernetes basics", "resources": ["Official docs", "Online course"]}
                  ],
                  "certifications": ["Certified Kubernetes Administrator (CKA)"]
                }
                """;
        when(groqClient.generateJson(anyString())).thenReturn(json);

        SkillGapResponse response = aiService.computeSkillGap("resume text");

        assertThat(response.getResumeId()).isNull();
        assertThat(response.getCurrentSkills()).hasSize(1);
        assertThat(response.getRequiredSkills()).hasSize(1);
        assertThat(response.getGapScore()).isEqualTo(45.0);
        assertThat(response.getRoadmap()).hasSize(8);
        assertThat(response.getCertifications()).contains("Certified Kubernetes Administrator (CKA)");
    }

    @Test
    void recommendCareers_parsesJsonArrayIntoList() {
        String json = """
                {
                  "recommendations": [
                    {"jobTitle": "Senior Backend Engineer", "company": "a mid-size Bengaluru fintech startup", "location": "Bengaluru, India", "matchScore": 88.0, "salaryMinLPA": 18.0, "salaryMaxLPA": 28.0, "marketDemand": "Strong demand for backend engineers with Spring Boot and cloud experience across Indian fintech.", "growthPercent": 12.0, "requiredUpskilling": ["kubernetes", "kafka"]},
                    {"jobTitle": "Solutions Architect", "company": "TCS", "location": "Hyderabad, India", "matchScore": 74.0, "salaryMinLPA": 30.0, "salaryMaxLPA": 45.0, "marketDemand": "Solutions architects remain in high demand as enterprises modernize legacy systems.", "growthPercent": 10.0, "requiredUpskilling": ["aws", "terraform"]},
                    {"jobTitle": "Engineering Manager", "company": "a global GCC like Walmart Global Tech", "location": "Pune, India", "matchScore": 65.0, "salaryMinLPA": 35.0, "salaryMaxLPA": 55.0, "marketDemand": "Engineering leadership roles are growing steadily in India's GCC hubs.", "growthPercent": 9.0, "requiredUpskilling": ["leadership", "stakeholder management"]}
                  ]
                }
                """;
        when(groqClient.generateJson(anyString(), anyDouble())).thenReturn(json);

        List<CareerRecommendationResponse> recommendations = aiService.recommendCareers("some resume text", List.of("java", "spring boot"));

        assertThat(recommendations).hasSize(3);
        assertThat(recommendations.get(0).getJobTitle()).isEqualTo("Senior Backend Engineer");
        assertThat(recommendations.get(0).getCompany()).isEqualTo("a mid-size Bengaluru fintech startup");
        assertThat(recommendations.get(0).getSalaryMaxLPA()).isGreaterThan(recommendations.get(0).getSalaryMinLPA());
    }
}
