package eduhk.fhr.web.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import eduhk.fhr.web.dto.talentlink.TalentLinkChildDTO;
import eduhk.fhr.web.dto.talentlink.TalentLinkEmergencyContactDTO;
import eduhk.fhr.web.dto.talentlink.TalentLinkPersonInfoDTO;
import eduhk.fhr.web.dto.talentlink.TalentLinkSpouseDTO;
import eduhk.fhr.web.soap.document.Answer;
import eduhk.fhr.web.soap.document.SelectedOption;
import eduhk.fhr.web.soap.document.StructuredDocument;

/**
 * Onboarding Questionnaire Parser
 *
 * Parses "EdUHK Onboarding Data Submission Questionnaire (Candidate)" structured documents
 * to extract personal information, spouse details, children, and emergency contact data.
 *
 * Document Sections:
 * 1. Personal Info → RDPS_PERSON_INFO
 * 2. Spouse Info → RDPS_PERSON_SPOUSE
 * 3. Child Info (repeating) → RDPS_PERSON_CHILD
 * 4. Emergency Contact → RDPS_PERSON_ECP
 * 5. Photo → RDPS_PHOTO_UPLOAD (future)
 */
@Service
public class OnboardingQuestionnaireParser {

    private static final Logger logger = LoggerFactory.getLogger(OnboardingQuestionnaireParser.class);

    private static final SimpleDateFormat[] DATE_FORMATS = {
        new SimpleDateFormat("yyyy-MM-dd"),
        new SimpleDateFormat("dd/MM/yyyy"),
        new SimpleDateFormat("MM/dd/yyyy"),
        new SimpleDateFormat("dd-MM-yyyy")
    };

    /**
     * Parse onboarding questionnaire structured document
     *
     * @param document Structured document from TalentLink
     * @return ParsedOnboardingData containing all sections
     */
    public ParsedOnboardingData parseOnboardingDocument(StructuredDocument document) {
        ParsedOnboardingData data = new ParsedOnboardingData();
        data.setDocumentFound(true);

        if (document == null || document.getAnswers() == null) {
            logger.warn("Document or answers is null");
            data.setDocumentFound(false);
            return data;
        }

        logger.info("Parsing onboarding document: {} answers",
            document.getAnswers() != null ? document.getAnswers().getAnswer().size() : 0);

        // Collect all question-value pairs from the document
        Map<String, String> questionValueMap = new HashMap<>();
        for (Answer answer : document.getAnswers().getAnswer()) {
            collectQuestionValues(answer, questionValueMap);
        }

        logger.info("Collected {} question-value pairs from onboarding document", questionValueMap.size());

        // Parse each section from the collected data
        data.setPersonInfo(parsePersonInfo(questionValueMap));
        data.setSpouse(parseSpouse(questionValueMap));
        data.setChildren(parseChildren(questionValueMap));
        data.setEmergencyContact(parseEmergencyContact(questionValueMap));

        logger.info("Parsed onboarding data: {}", data);

        return data;
    }

    /**
     * Recursively collect all question-value pairs from nested answer structure
     *
     * @param answer Answer object to process
     * @param questionValueMap Map to store question-value pairs
     */
    private void collectQuestionValues(Answer answer, Map<String, String> questionValueMap) {
        if (answer == null) {
            return;
        }

        // Get question text
        String questionText = getQuestionText(answer);
        String value = getAnswerValue(answer);

        if (questionText != null && !questionText.isEmpty() && value != null && !value.isEmpty()) {
            questionValueMap.put(questionText, value);
            logger.debug("Collected: {} = {}", questionText, value);
        }

        // Recursively process child answers
        if (answer.getChildren() != null && !answer.getChildren().isEmpty()) {
            for (Answer childAnswer : answer.getChildren()) {
                collectQuestionValues(childAnswer, questionValueMap);
            }
        }
    }

    /**
     * Get question text from Answer object
     *
     * @param answer Answer object
     * @return Question text or null
     */
    private String getQuestionText(Answer answer) {
        if (answer.getAssignedQuestion() != null) {
            String unlocalValue = answer.getAssignedQuestion().getUnlocalValue();
            if (unlocalValue != null && !unlocalValue.trim().isEmpty()) {
                return unlocalValue.trim();
            }
        }
        return null;
    }

    /**
     * Get answer value from Answer object
     *
     * @param answer Answer object
     * @return Answer value or null
     */
    private String getAnswerValue(Answer answer) {
        // Priority 1: Free text
        if (answer.getFreeText() != null && !answer.getFreeText().trim().isEmpty()) {
            return answer.getFreeText().trim();
        }

        // Priority 2: Selected option
        if (answer.getSelectedOptions() != null &&
            !answer.getSelectedOptions().isEmpty()) {

            SelectedOption option = answer.getSelectedOptions().get(0);
            if (option.getAssignedOption() != null) {
                String unlocalValue = option.getAssignedOption().getUnlocalValue();
                if (unlocalValue != null && !unlocalValue.trim().isEmpty()) {
                    return unlocalValue.trim();
                }
            }
        }

        return null;
    }

    /**
     * Parse Personal Info section
     *
     * @param questionValueMap Map of question-value pairs
     * @return TalentLinkPersonInfoDTO or null if no data
     */
    private TalentLinkPersonInfoDTO parsePersonInfo(Map<String, String> questionValueMap) {
        TalentLinkPersonInfoDTO dto = new TalentLinkPersonInfoDTO();
        boolean hasData = false;

        for (Map.Entry<String, String> entry : questionValueMap.entrySet()) {
            String question = entry.getKey().toLowerCase();
            String value = entry.getValue();

            // Person Number / Employee Number / Staff ID
            if (question.contains("person number") || question.contains("employee number") ||
                question.contains("staff") && question.contains("id")) {
                dto.setPersonNumber(value);
                hasData = true;
            }
            // Nationality
            else if (question.equals("nationality") || question.contains("nationality")) {
                dto.setNationality(value);
                hasData = true;
            }
            // Place of Origin
            else if (question.contains("place of origin") || question.contains("place") && question.contains("origin")) {
                dto.setPlaceOfOrigin(value);
                hasData = true;
            }
            // Highest Education
            else if (question.contains("highest education") || question.contains("highest") && question.contains("qualification")) {
                dto.setHighestEducation(value);
                hasData = true;
            }
            // Marital Status
            else if (question.contains("marital status") || question.contains("marital")) {
                dto.setMaritalStatus(value);
                hasData = true;
            }
            // Status Date
            else if (question.contains("status date") || question.contains("marital") && question.contains("date")) {
                Date date = parseDate(value);
                if (date != null) {
                    dto.setStatusDate(date);
                    hasData = true;
                }
            }
            // Visa Issue Date
            else if (question.contains("visa issue") || question.contains("visa") && question.contains("issue")) {
                Date date = parseDate(value);
                if (date != null) {
                    dto.setVisaIssueDate(date);
                    hasData = true;
                }
            }
            // Visa Expiry Date
            else if (question.contains("visa expiry") || question.contains("visa") && question.contains("expiry")) {
                Date date = parseDate(value);
                if (date != null) {
                    dto.setVisaExpiryDate(date);
                    hasData = true;
                }
            }
            // Immigration Status
            else if (question.contains("immigration status") || question.contains("immigration")) {
                dto.setImmigrationStatus(value);
                hasData = true;
            }
            // HK Entry Date
            else if (question.contains("hong kong entry") || question.contains("hk entry") ||
                     question.contains("entry") && question.contains("date")) {
                Date date = parseDate(value);
                if (date != null) {
                    dto.setHkEntryDate(date);
                    hasData = true;
                }
            }
        }

        logger.info("Parsed PersonInfo: {}", dto);
        return hasData ? dto : null;
    }

    /**
     * Parse Spouse Info section
     *
     * @param questionValueMap Map of question-value pairs
     * @return TalentLinkSpouseDTO or null if no data
     */
    private TalentLinkSpouseDTO parseSpouse(Map<String, String> questionValueMap) {
        TalentLinkSpouseDTO dto = new TalentLinkSpouseDTO();
        boolean hasData = false;

        for (Map.Entry<String, String> entry : questionValueMap.entrySet()) {
            String question = entry.getKey().toLowerCase();
            String value = entry.getValue();

            // Only process fields that specifically mention "spouse"
            if (!question.contains("spouse")) {
                continue;
            }

            // Spouse Full Name
            if (question.contains("full name") || question.contains("name") && !question.contains("first") && !question.contains("last")) {
                dto.setFullName(value);
                hasData = true;
            }
            // Spouse First Name
            else if (question.contains("first name") || question.contains("given name")) {
                dto.setFirstName(value);
                hasData = true;
            }
            // Spouse Last Name
            else if (question.contains("last name") || question.contains("surname") || question.contains("family name")) {
                dto.setLastName(value);
                hasData = true;
            }
            // Spouse Chinese Name
            else if (question.contains("chinese name") || question.contains("name in chinese")) {
                dto.setChineseName(value);
                hasData = true;
            }
            // Spouse Date of Birth
            else if (question.contains("date of birth") || question.contains("birth date") || question.contains("dob")) {
                Date date = parseDate(value);
                if (date != null) {
                    dto.setDateOfBirth(date);
                    hasData = true;
                }
            }
            // Spouse Gender
            else if (question.contains("gender") || question.contains("sex")) {
                String gender = value.equalsIgnoreCase("Male") ? "M" :
                                value.equalsIgnoreCase("Female") ? "F" : value;
                dto.setGender(gender);
                hasData = true;
            }
            // Spouse HKID
            else if (question.contains("hkid") || question.contains("hong kong id")) {
                dto.setHkid(value);
                hasData = true;
            }
            // Spouse Passport
            else if (question.contains("passport")) {
                dto.setPassport(value);
                hasData = true;
            }
            // Spouse Nationality
            else if (question.contains("nationality")) {
                dto.setNationality(value);
                hasData = true;
            }
            // Spouse Email
            else if (question.contains("email") || question.contains("e-mail")) {
                dto.setEmail(value);
                hasData = true;
            }
            // Spouse Phone
            else if (question.contains("phone") || question.contains("mobile") || question.contains("contact")) {
                dto.setPhoneNo(value);
                hasData = true;
            }
        }

        logger.info("Parsed Spouse: {}", dto);
        return hasData ? dto : null;
    }

    /**
     * Parse Child Info section (repeating section)
     *
     * @param questionValueMap Map of question-value pairs
     * @return List of TalentLinkChildDTO or empty list
     */
    private List<TalentLinkChildDTO> parseChildren(Map<String, String> questionValueMap) {
        List<TalentLinkChildDTO> children = new ArrayList<>();

        // Group child fields by number (Child 1, Child 2, etc.)
        Map<Integer, Map<String, String>> childDataMap = new HashMap<>();

        for (Map.Entry<String, String> entry : questionValueMap.entrySet()) {
            String question = entry.getKey().toLowerCase();
            String value = entry.getValue();

            // Only process fields that mention "child"
            if (!question.contains("child")) {
                continue;
            }

            // Extract child number (e.g., "Child 1: Name" → 1)
            Integer childNum = extractChildNumber(entry.getKey());
            if (childNum == null) {
                continue;
            }

            // Initialize map for this child if needed
            if (!childDataMap.containsKey(childNum)) {
                childDataMap.put(childNum, new HashMap<>());
            }

            // Extract field name after "Child N:"
            String fieldName = extractFieldName(entry.getKey(), "Child " + childNum);
            if (fieldName != null) {
                childDataMap.get(childNum).put(fieldName.toLowerCase(), value);
            }
        }

        // Create DTO for each child
        for (Map.Entry<Integer, Map<String, String>> childEntry : childDataMap.entrySet()) {
            TalentLinkChildDTO child = createChildFromFields(childEntry.getValue(), childEntry.getKey());
            if (child != null) {
                children.add(child);
            }
        }

        logger.info("Parsed {} children", children.size());
        return children;
    }

    /**
     * Extract child number from question text
     *
     * @param questionText Question text (e.g., "Child 1: Name")
     * @return Child number or null
     */
    private Integer extractChildNumber(String questionText) {
        try {
            if (questionText.toLowerCase().contains("child 1")) return 1;
            if (questionText.toLowerCase().contains("child 2")) return 2;
            if (questionText.toLowerCase().contains("child 3")) return 3;
            if (questionText.toLowerCase().contains("child 4")) return 4;
            if (questionText.toLowerCase().contains("child 5")) return 5;
        } catch (Exception e) {
            // Ignore parse errors
        }
        return null;
    }

    /**
     * Extract field name after prefix
     *
     * @param questionText Full question text
     * @param prefix Prefix to remove (e.g., "Child 1")
     * @return Field name or null
     */
    private String extractFieldName(String questionText, String prefix) {
        int colonIndex = questionText.toLowerCase().indexOf(prefix.toLowerCase());
        if (colonIndex >= 0) {
            String afterPrefix = questionText.substring(colonIndex + prefix.length()).trim();
            if (afterPrefix.startsWith(":")) {
                return afterPrefix.substring(1).trim();
            }
        }
        return null;
    }

    /**
     * Create Child DTO from field map
     *
     * @param fields Map of field name → value
     * @param childSeq Child sequence number
     * @return TalentLinkChildDTO or null if no meaningful data
     */
    private TalentLinkChildDTO createChildFromFields(Map<String, String> fields, int childSeq) {
        TalentLinkChildDTO dto = new TalentLinkChildDTO();
        dto.setChildSeq(childSeq);
        boolean hasData = false;

        for (Map.Entry<String, String> entry : fields.entrySet()) {
            String field = entry.getKey().toLowerCase();
            String value = entry.getValue();

            if (field.contains("full name") || field.equals("name")) {
                dto.setFullName(value);
                hasData = true;
            } else if (field.contains("first name") || field.contains("given name")) {
                dto.setFirstName(value);
                hasData = true;
            } else if (field.contains("last name") || field.contains("surname")) {
                dto.setLastName(value);
                hasData = true;
            } else if (field.contains("chinese name") || field.contains("name in chinese")) {
                dto.setChineseName(value);
                hasData = true;
            } else if (field.contains("date of birth") || field.contains("birth date") || field.contains("dob")) {
                Date date = parseDate(value);
                if (date != null) {
                    dto.setDateOfBirth(date);
                    hasData = true;
                }
            } else if (field.contains("gender") || field.equals("sex")) {
                String gender = value.equalsIgnoreCase("Male") ? "M" :
                                value.equalsIgnoreCase("Female") ? "F" : value;
                dto.setGender(gender);
                hasData = true;
            } else if (field.contains("hkid") || field.contains("hong kong id")) {
                dto.setHkid(value);
                hasData = true;
            } else if (field.contains("passport")) {
                dto.setPassport(value);
                hasData = true;
            } else if (field.contains("nationality")) {
                dto.setNationality(value);
                hasData = true;
            }
        }

        logger.debug("Created child {}: {}", childSeq, dto);
        return hasData ? dto : null;
    }

    /**
     * Parse Emergency Contact section
     *
     * @param questionValueMap Map of question-value pairs
     * @return TalentLinkEmergencyContactDTO or null if no data
     */
    private TalentLinkEmergencyContactDTO parseEmergencyContact(Map<String, String> questionValueMap) {
        TalentLinkEmergencyContactDTO dto = new TalentLinkEmergencyContactDTO();
        boolean hasData = false;

        for (Map.Entry<String, String> entry : questionValueMap.entrySet()) {
            String question = entry.getKey().toLowerCase();
            String value = entry.getValue();

            // Only process fields that mention "emergency"
            if (!question.contains("emergency")) {
                continue;
            }

            // Emergency Contact Name
            if (question.contains("name") || question.contains("contact person")) {
                dto.setFullName(value);
                hasData = true;
            }
            // Emergency Contact Phone
            else if (question.contains("phone") || question.contains("mobile") || question.contains("contact number")) {
                dto.setPhoneNo(value);
                hasData = true;
            }
            // Emergency Contact Relationship
            else if (question.contains("relationship") || question.contains("relation")) {
                dto.setRelationship(value);
                hasData = true;
            }
        }

        logger.info("Parsed Emergency Contact: {}", dto);
        return hasData ? dto : null;
    }

    /**
     * Parse date string using multiple formats
     *
     * @param dateStr Date string to parse
     * @return Date object or null if parse fails
     */
    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        for (SimpleDateFormat format : DATE_FORMATS) {
            try {
                return format.parse(dateStr.trim());
            } catch (ParseException e) {
                // Try next format
            }
        }

        logger.warn("Failed to parse date: {}", dateStr);
        return null;
    }
}
