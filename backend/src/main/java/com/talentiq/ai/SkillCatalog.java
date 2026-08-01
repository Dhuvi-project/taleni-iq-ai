package com.talentiq.ai;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Curated skill dictionary and static role -> required-skill mappings used for skill
 * extraction, skill-gap analysis, and career path recommendations. This is intentionally
 * static/deterministic; a future LLM-backed {@link com.talentiq.ai.AiService} implementation
 * could replace it with a dynamically generated taxonomy.
 */
public final class SkillCatalog {

    private SkillCatalog() {
    }

    public static final List<String> ALL_SKILLS = List.of(
            "java", "python", "javascript", "typescript", "c++", "c#", "go", "rust", "ruby", "php",
            "spring", "spring boot", "hibernate", "django", "flask", "fastapi", "node.js", "express",
            "react", "angular", "vue", "next.js", "redux", "html", "css", "tailwind", "bootstrap",
            "sql", "postgresql", "mysql", "mongodb", "redis", "elasticsearch", "cassandra", "dynamodb",
            "aws", "azure", "gcp", "docker", "kubernetes", "terraform", "jenkins", "ci/cd", "git",
            "microservices", "rest api", "graphql", "kafka", "rabbitmq", "grpc",
            "machine learning", "deep learning", "tensorflow", "pytorch", "scikit-learn", "pandas",
            "numpy", "data analysis", "data visualization", "tableau", "power bi", "sql server",
            "nlp", "computer vision", "statistics", "r", "spark", "hadoop", "airflow",
            "android", "ios", "swift", "kotlin", "flutter", "react native",
            "agile", "scrum", "jira", "figma", "ui/ux", "product management", "communication",
            "leadership", "problem solving", "testing", "selenium", "junit", "cypress", "cucumber",
            "linux", "bash", "networking", "security", "oauth", "jwt"
    );

    /** Role name -> required skills, used for skill-gap analysis and interview generation matching. */
    public static final Map<String, List<String>> ROLE_SKILLS = new LinkedHashMap<>();

    /** Role name -> plausible next-step career roles with metadata, used for career recommendations. */
    public static final Map<String, List<CareerPath>> CAREER_PATHS = new LinkedHashMap<>();

    static {
        ROLE_SKILLS.put("Java Backend Developer", List.of(
                "java", "spring", "spring boot", "hibernate", "sql", "postgresql", "microservices",
                "rest api", "docker", "kubernetes", "git", "junit", "kafka"));

        ROLE_SKILLS.put("Frontend Engineer", List.of(
                "javascript", "typescript", "react", "redux", "html", "css", "tailwind", "next.js",
                "git", "testing", "cypress", "ui/ux"));

        ROLE_SKILLS.put("Full Stack Developer", List.of(
                "javascript", "typescript", "react", "node.js", "express", "sql", "mongodb",
                "rest api", "docker", "git", "html", "css"));

        ROLE_SKILLS.put("Data Scientist", List.of(
                "python", "machine learning", "deep learning", "pandas", "numpy", "scikit-learn",
                "statistics", "sql", "data visualization", "tensorflow", "pytorch", "nlp"));

        ROLE_SKILLS.put("Data Analyst", List.of(
                "sql", "python", "excel", "data analysis", "data visualization", "tableau",
                "power bi", "statistics", "communication"));

        ROLE_SKILLS.put("DevOps Engineer", List.of(
                "docker", "kubernetes", "terraform", "aws", "azure", "gcp", "jenkins", "ci/cd",
                "linux", "bash", "networking", "security"));

        ROLE_SKILLS.put("Mobile Developer", List.of(
                "android", "ios", "swift", "kotlin", "flutter", "react native", "git", "rest api"));

        ROLE_SKILLS.put("Machine Learning Engineer", List.of(
                "python", "machine learning", "deep learning", "tensorflow", "pytorch",
                "spark", "airflow", "sql", "docker", "kubernetes"));

        ROLE_SKILLS.put("QA Engineer", List.of(
                "testing", "selenium", "junit", "cypress", "cucumber", "sql", "agile", "jira"));

        ROLE_SKILLS.put("Product Manager", List.of(
                "product management", "agile", "scrum", "jira", "figma", "communication",
                "leadership", "data analysis"));

        ROLE_SKILLS.put("Software Engineer", List.of(
                "java", "python", "javascript", "sql", "git", "rest api", "problem solving",
                "testing", "agile"));

        // Career recommendation graph: current-role-ish cluster -> next roles.
        CAREER_PATHS.put("Java Backend Developer", List.of(
                new CareerPath("Senior Backend Engineer", 95000, 140000, 12.0,
                        List.of("kubernetes", "kafka", "microservices", "system design")),
                new CareerPath("Solutions Architect", 120000, 165000, 10.0,
                        List.of("aws", "terraform", "microservices", "leadership")),
                new CareerPath("Engineering Manager", 130000, 175000, 9.0,
                        List.of("leadership", "agile", "communication", "budgeting")),
                new CareerPath("DevOps Engineer", 100000, 145000, 15.0,
                        List.of("docker", "kubernetes", "terraform", "ci/cd"))
        ));
        CAREER_PATHS.put("Frontend Engineer", List.of(
                new CareerPath("Senior Frontend Engineer", 90000, 135000, 11.0,
                        List.of("next.js", "typescript", "performance optimization")),
                new CareerPath("Full Stack Developer", 95000, 140000, 13.0,
                        List.of("node.js", "sql", "rest api")),
                new CareerPath("UI/UX Engineer", 85000, 125000, 9.0,
                        List.of("figma", "ui/ux", "design systems")),
                new CareerPath("Frontend Architect", 115000, 160000, 8.0,
                        List.of("system design", "typescript", "leadership"))
        ));
        CAREER_PATHS.put("Full Stack Developer", List.of(
                new CareerPath("Senior Full Stack Engineer", 100000, 145000, 12.0,
                        List.of("system design", "aws", "microservices")),
                new CareerPath("Technical Lead", 120000, 165000, 10.0,
                        List.of("leadership", "system design", "mentoring")),
                new CareerPath("Solutions Architect", 125000, 170000, 9.0,
                        List.of("aws", "azure", "terraform")),
                new CareerPath("Engineering Manager", 130000, 175000, 9.0,
                        List.of("leadership", "agile", "budgeting"))
        ));
        CAREER_PATHS.put("Data Scientist", List.of(
                new CareerPath("Senior Data Scientist", 110000, 155000, 14.0,
                        List.of("deep learning", "nlp", "mlops")),
                new CareerPath("Machine Learning Engineer", 115000, 160000, 18.0,
                        List.of("tensorflow", "pytorch", "docker", "kubernetes")),
                new CareerPath("Data Science Manager", 130000, 175000, 10.0,
                        List.of("leadership", "communication", "product management")),
                new CareerPath("AI Research Scientist", 140000, 190000, 16.0,
                        List.of("deep learning", "research", "statistics"))
        ));
        CAREER_PATHS.put("Data Analyst", List.of(
                new CareerPath("Senior Data Analyst", 80000, 115000, 10.0,
                        List.of("python", "advanced sql", "data visualization")),
                new CareerPath("Data Scientist", 100000, 150000, 16.0,
                        List.of("machine learning", "python", "statistics")),
                new CareerPath("Analytics Manager", 105000, 145000, 9.0,
                        List.of("leadership", "communication", "product management")),
                new CareerPath("Business Intelligence Engineer", 95000, 135000, 11.0,
                        List.of("tableau", "power bi", "sql"))
        ));
        CAREER_PATHS.put("DevOps Engineer", List.of(
                new CareerPath("Senior DevOps Engineer", 105000, 150000, 13.0,
                        List.of("kubernetes", "terraform", "observability")),
                new CareerPath("Site Reliability Engineer", 115000, 160000, 15.0,
                        List.of("kubernetes", "monitoring", "incident response")),
                new CareerPath("Cloud Architect", 125000, 170000, 12.0,
                        List.of("aws", "azure", "gcp", "system design")),
                new CareerPath("Platform Engineering Lead", 130000, 175000, 10.0,
                        List.of("leadership", "kubernetes", "terraform"))
        ));
        CAREER_PATHS.put("Mobile Developer", List.of(
                new CareerPath("Senior Mobile Engineer", 95000, 140000, 11.0,
                        List.of("swift", "kotlin", "performance optimization")),
                new CareerPath("Cross-Platform Lead", 105000, 150000, 10.0,
                        List.of("flutter", "react native", "leadership")),
                new CareerPath("Mobile Architect", 115000, 160000, 9.0,
                        List.of("system design", "ci/cd", "security")),
                new CareerPath("Full Stack Developer", 95000, 140000, 13.0,
                        List.of("node.js", "react", "sql"))
        ));
        CAREER_PATHS.put("Machine Learning Engineer", List.of(
                new CareerPath("Senior ML Engineer", 125000, 170000, 16.0,
                        List.of("mlops", "distributed systems", "kubernetes")),
                new CareerPath("AI Research Scientist", 140000, 195000, 17.0,
                        List.of("research", "deep learning", "statistics")),
                new CareerPath("ML Platform Lead", 135000, 180000, 12.0,
                        List.of("leadership", "kubernetes", "mlops")),
                new CareerPath("Data Science Manager", 130000, 175000, 10.0,
                        List.of("leadership", "communication", "product management"))
        ));
        CAREER_PATHS.put("QA Engineer", List.of(
                new CareerPath("Senior QA Engineer", 80000, 115000, 9.0,
                        List.of("test automation", "selenium", "ci/cd")),
                new CareerPath("SDET", 90000, 130000, 11.0,
                        List.of("java", "python", "test frameworks")),
                new CareerPath("QA Lead", 100000, 140000, 8.0,
                        List.of("leadership", "test strategy", "agile")),
                new CareerPath("DevOps Engineer", 100000, 145000, 15.0,
                        List.of("docker", "kubernetes", "ci/cd"))
        ));
        CAREER_PATHS.put("Product Manager", List.of(
                new CareerPath("Senior Product Manager", 110000, 155000, 11.0,
                        List.of("product strategy", "data analysis", "stakeholder management")),
                new CareerPath("Group Product Manager", 130000, 175000, 9.0,
                        List.of("leadership", "roadmapping", "budgeting")),
                new CareerPath("Director of Product", 150000, 200000, 8.0,
                        List.of("leadership", "vision setting", "communication")),
                new CareerPath("Product Owner", 95000, 135000, 10.0,
                        List.of("agile", "scrum", "backlog management"))
        ));
        CAREER_PATHS.put("Software Engineer", List.of(
                new CareerPath("Senior Software Engineer", 100000, 145000, 12.0,
                        List.of("system design", "leadership", "testing")),
                new CareerPath("Backend Developer", 95000, 140000, 12.0,
                        List.of("spring boot", "sql", "microservices")),
                new CareerPath("Full Stack Developer", 95000, 140000, 13.0,
                        List.of("react", "node.js", "sql")),
                new CareerPath("Technical Lead", 120000, 165000, 10.0,
                        List.of("leadership", "system design", "mentoring"))
        ));
    }

    /** Extracts skills from free text by scanning against {@link #ALL_SKILLS}. */
    public static List<String> extractSkills(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String lower = text.toLowerCase(Locale.ROOT);
        return ALL_SKILLS.stream()
                .filter(skill -> lower.contains(skill.toLowerCase(Locale.ROOT)))
                .toList();
    }

    /** Finds the best-matching curated role for a given set of extracted skills. */
    public static String bestMatchingRole(List<String> skills) {
        String bestRole = "Software Engineer";
        long bestScore = -1;
        for (Map.Entry<String, List<String>> entry : ROLE_SKILLS.entrySet()) {
            long overlap = entry.getValue().stream().filter(skills::contains).count();
            if (overlap > bestScore) {
                bestScore = overlap;
                bestRole = entry.getKey();
            }
        }
        return bestRole;
    }

    public record CareerPath(String role, int salaryBandMin, int salaryBandMax, double growthPercent,
                              List<String> requiredUpskilling) {
    }
}
