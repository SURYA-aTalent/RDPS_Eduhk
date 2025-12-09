package eduhk.fhr.web.service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import eduhk.fhr.web.dto.talentlink.TalentLinkEducationDTO;
import eduhk.fhr.web.dto.talentlink.TalentLinkWorkExperienceDTO;
import eduhk.fhr.web.dto.talentlink.TalentLinkRefereeDTO;
import eduhk.fhr.web.dto.talentlink.TalentLinkOtherInfoDTO;
import eduhk.fhr.web.soap.document.Answer;
import eduhk.fhr.web.soap.document.GfDocument;
import eduhk.fhr.web.soap.document.SelectedOption;
import eduhk.fhr.web.soap.document.StructuredDocument;

/**
 * Service to parse education, work experience, referee, and other info data
 * from TalentLink structured documents.
 *
 * Extracts data from application-specific forms like "EdUHK Education/Work Experience"
 */
@Service
public class EducationWorkDocumentParser {

    private static final Logger logger = LoggerFactory.getLogger(EducationWorkDocumentParser.class);

    // Document name to identify education/work forms
    private static final String EDU_WORK_DOCUMENT_NAME = "EdUHK Education/Work Experience";

    // Date formats to try when parsing date strings
    private static final String[] DATE_FORMATS = {
        "yyyy-MM-dd",
        "dd/MM/yyyy",
        "MM/dd/yyyy",
        "yyyy/MM/dd",
        "dd-MM-yyyy"
    };

    /**
     * Parse a structured document to extract education, work, referee, and other info
     *
     * @param document The structured document from TalentLink
     * @return ParsedCandidateData containing extracted information
     */
    public ParsedCandidateData parseEducationWorkDocument(StructuredDocument document) {
        ParsedCandidateData data = new ParsedCandidateData();

        if (document == null) {
            logger.warn("Received null document to parse");
            return data;
        }

        logger.info("Parsing document: {} (ID: {})", document.getName(), document.getId());

        GfDocument gfDoc = document.getAnswers();
        if (gfDoc == null || gfDoc.getAnswer() == null) {
            logger.warn("Document {} has no answers", document.getId());
            return data;
        }

        List<Answer> answers = gfDoc.getAnswer();
        logger.debug("Processing {} top-level answers", answers.size());

        for (Answer answer : answers) {
            String questionText = getQuestionText(answer);

            // Route answers to appropriate parser based on question text
            if (isEducationQuestion(questionText)) {
                parseEducationAnswers(answer, data);
            } else if (isWorkExperienceQuestion(questionText)) {
                parseWorkExperienceAnswers(answer, data);
            } else if (isOtherInfoQuestion(questionText)) {
                parseOtherInfoAnswers(answer, data);
            }
            // Skip isRefereeQuestion check here - referees are handled differently
        }

        // Parse referees separately - they are flat top-level fields like "Referee 1: Name"
        parseRefereesFromTopLevel(answers, data);

        logger.info("Parsed document: {}", data);
        return data;
    }

    /**
     * Parse education section (can be a repeating section with multiple entries)
     */
    private void parseEducationAnswers(Answer answer, ParsedCandidateData data) {
        logger.debug("Parsing education section: {}", getQuestionText(answer));

        // Check if this is a repeating section (education history)
        if (answer.getChildren() != null && !answer.getChildren().isEmpty()) {
            // Each child is one education record
            for (Answer child : answer.getChildren()) {
                TalentLinkEducationDTO education = extractEducationFromChild(child);
                if (education != null && hasEducationData(education)) {
                    data.addEducation(education);
                    logger.debug("Added education: {}", education.getInstitution());
                }
            }
        } else {
            // Single education entry
            TalentLinkEducationDTO education = extractEducationFromChild(answer);
            if (education != null && hasEducationData(education)) {
                data.addEducation(education);
            }
        }
    }

    /**
     * Extract education data from a child answer
     * Now handles deeply nested structures by recursively collecting all question-value pairs
     */
    private TalentLinkEducationDTO extractEducationFromChild(Answer child) {
        TalentLinkEducationDTO dto = new TalentLinkEducationDTO();

        if (child.getChildren() == null) {
            return dto;
        }

        // Recursively collect all question-value pairs from nested structure
        java.util.Map<String, String> questionValueMap = new java.util.HashMap<>();
        collectQuestionValues(child, questionValueMap);

        logger.debug("Collected {} question-value pairs for education", questionValueMap.size());

        // Map to DTO using exact question names first, then fall back to keyword matching
        for (java.util.Map.Entry<String, String> entry : questionValueMap.entrySet()) {
            String question = entry.getKey();
            String value = entry.getValue();
            String questionLower = question.toLowerCase();

            if (value == null || value.trim().isEmpty()) {
                continue;
            }

            // Exact question name matching (priority)
            if ("School Name".equalsIgnoreCase(question)) {
                dto.setInstitution(value);
                logger.debug("Mapped 'School Name' -> institution: {}", value);
            } else if ("Degree Type".equalsIgnoreCase(question)) {
                dto.setEducationLevel(value);
                logger.debug("Mapped 'Degree Type' -> educationLevel: {}", value);
            } else if ("Degree Name".equalsIgnoreCase(question)) {
                dto.setQualificationAwardDesc(value);
                logger.debug("Mapped 'Degree Name' -> qualificationAwardDesc: {}", value);
            } else if ("Degree Date".equalsIgnoreCase(question)) {
                dto.setDateOfAward(parseDate(value));
                logger.debug("Mapped 'Degree Date' -> dateOfAward: {}", value);
            } else if ("Country".equalsIgnoreCase(question)) {
                dto.setCountry(value);
                logger.debug("Mapped 'Country' -> country: {}", value);
            } else if ("Sub-School / Department".equalsIgnoreCase(question)) {
                dto.setMajorStudyArea(value);
                logger.debug("Mapped 'Sub-School / Department' -> majorStudyArea: {}", value);
            } else if ("Study Mode".equalsIgnoreCase(question) || "Is Full Time?".equalsIgnoreCase(question)) {
                dto.setStudyMode(value);
                logger.debug("Mapped '{}' -> studyMode: {}", question, value);
            }
            // Keyword-based matching (fallback) - only if fields not yet set
            else if (dto.getInstitution() == null && (questionLower.contains("institution") || questionLower.contains("university"))) {
                dto.setInstitution(value);
            } else if (dto.getCountry() == null && questionLower.contains("country")) {
                dto.setCountry(value);
            } else if (dto.getEducationLevel() == null && (questionLower.contains("education level") || questionLower.contains("level of education"))) {
                dto.setEducationLevel(value);
            } else if (dto.getStudyMode() == null && (questionLower.contains("study mode") || questionLower.contains("mode of study"))) {
                dto.setStudyMode(value);
            } else if (dto.getQualificationAwardDesc() == null && (questionLower.contains("qualification") || questionLower.contains("award desc"))) {
                dto.setQualificationAwardDesc(value);
            } else if (dto.getQualificationAwardClass() == null && (questionLower.contains("award class") || questionLower.contains("class of award"))) {
                dto.setQualificationAwardClass(value);
            } else if (dto.getMajorStudyArea() == null && (questionLower.contains("major") || questionLower.contains("study area") || questionLower.contains("subject"))) {
                dto.setMajorStudyArea(value);
            } else if (dto.getStartDate() == null && (questionLower.contains("start date") || questionLower.contains("date of commencement"))) {
                dto.setStartDate(parseDate(value));
            } else if (dto.getDateOfAward() == null && (questionLower.contains("graduated") || questionLower.contains("completion date"))) {
                dto.setDateOfAward(parseDate(value));
            } else if (dto.getOthers() == null && questionLower.contains("other")) {
                dto.setOthers(value);
            }
        }

        return dto;
    }

    /**
     * Recursively collect all question-value pairs from nested Answer structure
     * This flattens the hierarchy so we can access deeply nested fields
     */
    private void collectQuestionValues(Answer answer, java.util.Map<String, String> questionValueMap) {
        if (answer == null) {
            return;
        }

        String question = getQuestionText(answer);
        String value = getAnswerValue(answer);

        // Store this question-value pair if it has a non-empty value
        if (question != null && !question.isEmpty() && value != null && !value.isEmpty()) {
            questionValueMap.put(question, value);
        }

        // Recurse into children
        if (answer.getChildren() != null) {
            for (Answer child : answer.getChildren()) {
                collectQuestionValues(child, questionValueMap);
            }
        }
    }

    /**
     * Check if education DTO has meaningful data
     */
    private boolean hasEducationData(TalentLinkEducationDTO education) {
        return education.getInstitution() != null
            || education.getQualificationAwardDesc() != null
            || education.getMajorStudyArea() != null;
    }

    /**
     * Parse work experience section (can be a repeating section with multiple entries)
     */
    private void parseWorkExperienceAnswers(Answer answer, ParsedCandidateData data) {
        logger.debug("Parsing work experience section: {}", getQuestionText(answer));

        // Check if this is a repeating section
        if (answer.getChildren() != null && !answer.getChildren().isEmpty()) {
            // Each child is one work experience record
            for (Answer child : answer.getChildren()) {
                TalentLinkWorkExperienceDTO workExp = extractWorkExperienceFromChild(child);
                if (workExp != null && hasWorkExperienceData(workExp)) {
                    data.addWorkExperience(workExp);
                    logger.debug("Added work experience: {}", workExp.getEmployerName());
                }
            }
        } else {
            // Single work experience entry
            TalentLinkWorkExperienceDTO workExp = extractWorkExperienceFromChild(answer);
            if (workExp != null && hasWorkExperienceData(workExp)) {
                data.addWorkExperience(workExp);
            }
        }
    }

    /**
     * Extract work experience data from a child answer
     * Now handles deeply nested structures by recursively collecting all question-value pairs
     */
    private TalentLinkWorkExperienceDTO extractWorkExperienceFromChild(Answer child) {
        TalentLinkWorkExperienceDTO dto = new TalentLinkWorkExperienceDTO();

        if (child.getChildren() == null) {
            return dto;
        }

        // Recursively collect all question-value pairs from nested structure
        java.util.Map<String, String> questionValueMap = new java.util.HashMap<>();
        collectQuestionValues(child, questionValueMap);

        logger.debug("Collected {} question-value pairs for work experience", questionValueMap.size());

        // Map to DTO using exact question names first, then fall back to keyword matching
        for (java.util.Map.Entry<String, String> entry : questionValueMap.entrySet()) {
            String question = entry.getKey();
            String value = entry.getValue();
            String questionLower = question.toLowerCase();

            if (value == null || value.trim().isEmpty()) {
                continue;
            }

            // Exact question name matching (priority)
            if ("Employer Organization Name".equalsIgnoreCase(question)) {
                dto.setEmployerName(value);
                logger.debug("Mapped 'Employer Organization Name' -> employerName: {}", value);
            } else if ("Employer Organization Type".equalsIgnoreCase(question)) {
                // Skip - this is a type classification, not the employer name
                logger.debug("Skipping 'Employer Organization Type': {}", value);
            } else if ("Position".equalsIgnoreCase(question) || "Job Title".equalsIgnoreCase(question)) {
                dto.setPositionTitle(value);
                logger.debug("Mapped '{}' -> positionTitle: {}", question, value);
            } else if ("City".equalsIgnoreCase(question)) {
                // Could store in a separate field if needed
                logger.debug("Found city: {}", value);
            } else if ("Region".equalsIgnoreCase(question)) {
                logger.debug("Found region: {}", value);
            } else if ("Country".equalsIgnoreCase(question)) {
                // Could map to a location field if needed
                logger.debug("Found country: {}", value);
            } else if ("From".equalsIgnoreCase(question) || "Start Date".equalsIgnoreCase(question)) {
                dto.setStartDate(parseDate(value));
                logger.debug("Mapped '{}' -> startDate: {}", question, value);
            } else if ("To".equalsIgnoreCase(question) || "End Date".equalsIgnoreCase(question) || "Until".equalsIgnoreCase(question)) {
                dto.setEndDate(parseDate(value));
                logger.debug("Mapped '{}' -> endDate: {}", question, value);
            }
            // Keyword-based matching (fallback) - only if fields not yet set
            else if (dto.getEmployerName() == null && (questionLower.contains("employer") || questionLower.contains("company"))) {
                // Don't match "organization type" - check it doesn't contain "type"
                if (!questionLower.contains("type")) {
                    dto.setEmployerName(value);
                }
            } else if (dto.getNatureOfBusiness() == null && (questionLower.contains("nature of business") || questionLower.contains("industry"))) {
                dto.setNatureOfBusiness(value);
            } else if (dto.getPositionTitle() == null && (questionLower.contains("position") || questionLower.contains("title") || questionLower.contains("job title"))) {
                dto.setPositionTitle(value);
            } else if (dto.getCurrentJob() == null && (questionLower.contains("current job") || questionLower.contains("present"))) {
                dto.setCurrentJob(value);
            } else if (dto.getModeOfEmployment() == null && (questionLower.contains("mode of employment") || questionLower.contains("employment type"))) {
                dto.setModeOfEmployment(value);
            } else if (dto.getHoursPerWeek() == null && (questionLower.contains("hours per week") || questionLower.contains("working hours"))) {
                dto.setHoursPerWeek(value);
            } else if (dto.getStartDate() == null && (questionLower.contains("from") || questionLower.contains("start date") || questionLower.contains("commenced"))) {
                dto.setStartDate(parseDate(value));
            } else if (dto.getEndDate() == null && (questionLower.contains("until") || questionLower.contains("end date") || questionLower.contains("to date"))) {
                dto.setEndDate(parseDate(value));
            } else if (dto.getNatureOfDuties() == null && (questionLower.contains("nature of duties") || questionLower.contains("responsibilities") || questionLower.contains("duties"))) {
                dto.setNatureOfDuties(value);
            }
        }

        return dto;
    }

    /**
     * Check if work experience DTO has meaningful data
     */
    private boolean hasWorkExperienceData(TalentLinkWorkExperienceDTO workExp) {
        return workExp.getEmployerName() != null
            || workExp.getPositionTitle() != null;
    }

    /**
     * Parse referees from flat top-level answer fields
     * Referees appear as individual fields like "Referee 1: Name", "Referee 1: Position", etc.
     * This method groups them by referee number and creates separate DTO objects
     */
    private void parseRefereesFromTopLevel(List<Answer> answers, ParsedCandidateData data) {
        logger.debug("Parsing referees from flat top-level fields");

        // Maps to collect referee data by number (1 or 2)
        java.util.Map<Integer, java.util.Map<String, String>> refereeDataMap = new java.util.HashMap<>();
        refereeDataMap.put(1, new java.util.HashMap<>());
        refereeDataMap.put(2, new java.util.HashMap<>());

        // Collect all referee fields
        for (Answer answer : answers) {
            String questionText = getQuestionText(answer);
            String value = getAnswerValue(answer);

            if (questionText == null || questionText.trim().isEmpty()) {
                continue;
            }

            // Check if this is a referee field (starts with "Referee 1:" or "Referee 2:")
            if (questionText.startsWith("Referee 1:") || questionText.startsWith("Referee 1 :")) {
                String fieldName = questionText.substring(questionText.indexOf(':') + 1).trim();
                refereeDataMap.get(1).put(fieldName, value);
                logger.debug("Collected Referee 1 field: {} = {}", fieldName, value);
            } else if (questionText.startsWith("Referee 2:") || questionText.startsWith("Referee 2 :")) {
                String fieldName = questionText.substring(questionText.indexOf(':') + 1).trim();
                refereeDataMap.get(2).put(fieldName, value);
                logger.debug("Collected Referee 2 field: {} = {}", fieldName, value);
            }
        }

        // Create DTOs from collected data
        for (int refereeNum = 1; refereeNum <= 2; refereeNum++) {
            java.util.Map<String, String> fields = refereeDataMap.get(refereeNum);

            if (fields.isEmpty()) {
                logger.debug("No data found for Referee {}", refereeNum);
                continue;
            }

            TalentLinkRefereeDTO referee = createRefereeFromFields(fields);

            if (hasRefereeData(referee)) {
                data.addReferee(referee);
                logger.debug("Added Referee {}: {} {}", refereeNum, referee.getFirstname(), referee.getLastname());
            }
        }

        logger.debug("Total referees parsed: {}", data.getReferees().size());
    }

    /**
     * Create a referee DTO from a map of field names to values
     */
    private TalentLinkRefereeDTO createRefereeFromFields(java.util.Map<String, String> fields) {
        TalentLinkRefereeDTO dto = new TalentLinkRefereeDTO();

        for (java.util.Map.Entry<String, String> entry : fields.entrySet()) {
            String field = entry.getKey();
            String value = entry.getValue();

            if (value == null || value.trim().isEmpty()) {
                continue;
            }

            String fieldLower = field.toLowerCase();

            // Map fields to DTO properties
            if ("name".equalsIgnoreCase(field)) {
                // Parse full name into first and last name
                String[] nameParts = value.trim().split("\\s+", 2);
                if (nameParts.length == 2) {
                    dto.setFirstname(nameParts[0]);
                    dto.setLastname(nameParts[1]);
                } else {
                    // If only one name, put it in lastname
                    dto.setLastname(value.trim());
                }
                logger.debug("Parsed name '{}' -> first: '{}', last: '{}'", value, dto.getFirstname(), dto.getLastname());
            } else if (fieldLower.contains("organization") || fieldLower.contains("company")) {
                // Store organization/company info (could be used for position context)
                logger.debug("Found organization: {}", value);
            } else if (fieldLower.contains("position") || fieldLower.contains("job title")) {
                dto.setPositionTitle(value);
            } else if (fieldLower.contains("tel") || fieldLower.contains("phone") || fieldLower.contains("contact")) {
                dto.setPhoneNumber(value);
            } else if (fieldLower.contains("email") || fieldLower.contains("e-mail")) {
                dto.setEmail(value);
            } else if (fieldLower.contains("relationship")) {
                dto.setRelationship(value);
            }
        }

        return dto;
    }

    /**
     * OLD METHOD: Parse referee section (can be a repeating section with multiple entries)
     * NOTE: This is now deprecated as referees appear as flat fields, not nested sections
     */
    private void parseRefereeAnswers(Answer answer, ParsedCandidateData data) {
        logger.debug("Parsing referee section: {}", getQuestionText(answer));

        // Check if this is a repeating section
        if (answer.getChildren() != null && !answer.getChildren().isEmpty()) {
            // Each child is one referee record
            for (Answer child : answer.getChildren()) {
                TalentLinkRefereeDTO referee = extractRefereeFromChild(child);
                if (referee != null && hasRefereeData(referee)) {
                    data.addReferee(referee);
                    logger.debug("Added referee: {} {}", referee.getFirstname(), referee.getLastname());
                }
            }
        } else {
            // Single referee entry
            TalentLinkRefereeDTO referee = extractRefereeFromChild(answer);
            if (referee != null && hasRefereeData(referee)) {
                data.addReferee(referee);
            }
        }
    }

    /**
     * Extract referee data from a child answer
     */
    private TalentLinkRefereeDTO extractRefereeFromChild(Answer child) {
        TalentLinkRefereeDTO dto = new TalentLinkRefereeDTO();

        if (child.getChildren() == null) {
            return dto;
        }

        for (Answer subAnswer : child.getChildren()) {
            String question = getQuestionText(subAnswer).toLowerCase();
            String value = getAnswerValue(subAnswer);

            if (value == null || value.trim().isEmpty()) {
                continue;
            }

            if (question.contains("title") && !question.contains("position")) {
                dto.setTitle(value);
            } else if (question.contains("first name") || question.contains("firstname") || question.contains("given name")) {
                dto.setFirstname(value);
            } else if (question.contains("last name") || question.contains("lastname") || question.contains("surname") || question.contains("family name")) {
                dto.setLastname(value);
            } else if (question.contains("position") || question.contains("job title")) {
                dto.setPositionTitle(value);
            } else if (question.contains("phone") || question.contains("telephone") || question.contains("contact number")) {
                dto.setPhoneNumber(value);
            } else if (question.contains("email") || question.contains("e-mail")) {
                dto.setEmail(value);
            } else if (question.contains("relationship")) {
                dto.setRelationship(value);
            }
        }

        return dto;
    }

    /**
     * Check if referee DTO has meaningful data
     */
    private boolean hasRefereeData(TalentLinkRefereeDTO referee) {
        return (referee.getFirstname() != null && referee.getLastname() != null)
            || referee.getEmail() != null;
    }

    /**
     * Parse other info section (single entry with various fields)
     */
    private void parseOtherInfoAnswers(Answer answer, ParsedCandidateData data) {
        logger.debug("Parsing other info section: {}", getQuestionText(answer));

        TalentLinkOtherInfoDTO dto = new TalentLinkOtherInfoDTO();
        boolean hasData = false;

        List<Answer> children = answer.getChildren();
        if (children == null || children.isEmpty()) {
            // Try direct value
            String question = getQuestionText(answer).toLowerCase();
            String value = getAnswerValue(answer);

            if (question.contains("expected salary") || question.contains("salary expectation")) {
                BigDecimal salary = parseBigDecimal(value);
                if (salary != null) {
                    dto.setExpectedSalary(salary);
                    hasData = true;
                }
            }
        } else {
            // Parse child answers
            for (Answer subAnswer : children) {
                String question = getQuestionText(subAnswer).toLowerCase();
                String value = getAnswerValue(subAnswer);

                if (value == null || value.trim().isEmpty()) {
                    continue;
                }

                if (question.contains("expected salary") || question.contains("salary expectation")) {
                    BigDecimal salary = parseBigDecimal(value);
                    if (salary != null) {
                        dto.setExpectedSalary(salary);
                        hasData = true;
                    }
                } else if (question.contains("current salary") && !question.contains("expected")) {
                    BigDecimal salary = parseBigDecimal(value);
                    if (salary != null) {
                        dto.setSalary(salary);
                        hasData = true;
                    }
                } else if (question.contains("skills") || question.contains("competencies")) {
                    dto.setSkills(value);
                    hasData = true;
                } else if (question.contains("notice period")) {
                    dto.setNoticePeriod(value);
                    hasData = true;
                } else if (question.contains("date of availability") || question.contains("available from")) {
                    dto.setDateOfAvailability(parseDate(value));
                    hasData = true;
                } else if (question.contains("eduhk employee") || question.contains("current employee")) {
                    dto.setEduhkEmployee(value);
                    hasData = true;
                } else if (question.contains("staff number") || question.contains("employee number")) {
                    dto.setStaffNumber(value);
                    hasData = true;
                }
            }
        }

        if (hasData) {
            data.setOtherInfo(dto);
        }
    }

    /**
     * Extract question text from an answer
     */
    private String getQuestionText(Answer answer) {
        if (answer == null) {
            return "";
        }

        if (answer.getAssignedQuestion() != null) {
            String unlocalValue = answer.getAssignedQuestion().getUnlocalValue();
            if (unlocalValue != null && !unlocalValue.trim().isEmpty()) {
                return unlocalValue;
            }
        }

        return "";
    }

    /**
     * Extract answer value from an answer (handles free text, selected options, dates)
     */
    private String getAnswerValue(Answer answer) {
        if (answer == null) {
            return "";
        }

        // Try free text first
        String freeText = answer.getFreeText();
        if (freeText != null && !freeText.trim().isEmpty()) {
            return freeText.trim();
        }

        // Try selected options
        if (answer.getSelectedOptions() != null && !answer.getSelectedOptions().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (SelectedOption option : answer.getSelectedOptions()) {
                if (option.getAssignedOption() != null) {
                    String optionValue = option.getAssignedOption().getUnlocalValue();
                    if (optionValue != null && !optionValue.trim().isEmpty()) {
                        if (sb.length() > 0) {
                            sb.append(", ");
                        }
                        sb.append(optionValue.trim());
                    }
                }
            }
            if (sb.length() > 0) {
                return sb.toString();
            }
        }

        // Try date answer
        if (answer.getDateAnswer() != null) {
            return answer.getDateAnswer().toString();
        }

        return "";
    }

    /**
     * Determine if a question is related to education
     * Only matches the top-level "Education History" section, not individual education fields
     */
    private boolean isEducationQuestion(String question) {
        if (question == null) {
            return false;
        }
        // Match only the specific top-level section name
        return "Education History".equalsIgnoreCase(question);
    }

    /**
     * Determine if a question is related to work experience
     * Only matches the top-level "Employment History" section, not individual work fields
     */
    private boolean isWorkExperienceQuestion(String question) {
        if (question == null) {
            return false;
        }
        // Match only the specific top-level section name
        return "Employment History".equalsIgnoreCase(question);
    }

    /**
     * Determine if a question is related to referees
     */
    private boolean isRefereeQuestion(String question) {
        if (question == null) {
            return false;
        }
        String lower = question.toLowerCase();
        return lower.contains("referee") || lower.contains("reference");
    }

    /**
     * Determine if a question is related to other info
     */
    private boolean isOtherInfoQuestion(String question) {
        if (question == null) {
            return false;
        }
        String lower = question.toLowerCase();
        return lower.contains("salary") || lower.contains("expected")
            || lower.contains("skill") || lower.contains("notice")
            || lower.contains("availability");
    }

    /**
     * Parse date string to Date object
     */
    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        // Try multiple date formats
        for (String format : DATE_FORMATS) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                sdf.setLenient(false);
                return sdf.parse(dateStr.trim());
            } catch (ParseException e) {
                // Try next format
            }
        }

        logger.warn("Failed to parse date: {}", dateStr);
        return null;
    }

    /**
     * Parse string to BigDecimal
     */
    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            // Remove currency symbols and commas
            String cleaned = value.trim()
                .replaceAll("[^0-9.]", "") // Remove non-numeric except decimal point
                .trim();

            if (cleaned.isEmpty()) {
                return null;
            }

            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse number: {}", value);
            return null;
        }
    }
}
