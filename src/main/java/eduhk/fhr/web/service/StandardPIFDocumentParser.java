package eduhk.fhr.web.service;

// File Path: src/main/java/eduhk/fhr/web/service/StandardPIFDocumentParser.java
// Purpose: Service to parse Personal Information Form (PIF) data from TalentLink structured documents

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import eduhk.fhr.web.dto.talentlink.TalentLinkPIFDTO;
import eduhk.fhr.web.soap.document.Answer;
import eduhk.fhr.web.soap.document.GfDocument;
import eduhk.fhr.web.soap.document.SelectedOption;
import eduhk.fhr.web.soap.document.StructuredDocument;

/**
 * Standard PIF Document Parser
 *
 * Parses candidate Personal Information Form (EdUHK Standard PIF) structured documents.
 * Extracts personal details, contact information, and identity documents.
 *
 * Document name: "EdUHK Standard PIF"
 */
@Service
public class StandardPIFDocumentParser {

    private static final Logger logger = LoggerFactory.getLogger(StandardPIFDocumentParser.class);

    // Document name to identify Standard PIF forms
    private static final String PIF_DOCUMENT_NAME = "EdUHK Standard PIF";

    // Date formats to try when parsing date strings
    private static final String[] DATE_FORMATS = {
        "yyyy-MM-dd",
        "dd/MM/yyyy",
        "MM/dd/yyyy",
        "yyyy/MM/dd",
        "dd-MM-yyyy"
    };

    /**
     * Parse a structured document to extract PIF data
     *
     * @param document The structured document from TalentLink
     * @return ParsedPIFData containing extracted PIF information
     */
    public ParsedPIFData parseStandardPIFDocument(StructuredDocument document) {
        ParsedPIFData data = new ParsedPIFData();

        if (document == null) {
            logger.warn("Received null document to parse");
            return data;
        }

        logger.info("Parsing PIF document: {} (ID: {})", document.getName(), document.getId());
        data.setDocumentFound(true);

        GfDocument gfDoc = document.getAnswers();
        if (gfDoc == null || gfDoc.getAnswer() == null) {
            logger.warn("Document {} has no answers", document.getId());
            return data;
        }

        List<Answer> answers = gfDoc.getAnswer();
        logger.debug("Processing {} top-level answers for PIF", answers.size());

        // Collect all question-value pairs (handles nested structure)
        Map<String, String> questionValueMap = new HashMap<>();
        for (Answer answer : answers) {
            collectQuestionValues(answer, questionValueMap);
        }

        logger.debug("Collected {} question-value pairs from PIF document", questionValueMap.size());

        // Map to DTO
        TalentLinkPIFDTO pifDTO = mapToPIFDTO(questionValueMap);
        data.setPifData(pifDTO);

        logger.info("Parsed PIF document successfully: {}", pifDTO);
        return data;
    }

    /**
     * Recursively collect all question-value pairs from nested Answer structure
     */
    private void collectQuestionValues(Answer answer, Map<String, String> questionValueMap) {
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
     * Map question-value pairs to PIF DTO
     */
    private TalentLinkPIFDTO mapToPIFDTO(Map<String, String> questionValueMap) {
        TalentLinkPIFDTO dto = new TalentLinkPIFDTO();

        for (Map.Entry<String, String> entry : questionValueMap.entrySet()) {
            String question = entry.getKey();
            String value = entry.getValue();

            if (value == null || value.trim().isEmpty()) {
                continue;
            }

            String questionLower = question.toLowerCase();

            // Exact question name matching (priority)
            if ("Title".equalsIgnoreCase(question)) {
                dto.setTitle(value);
                logger.debug("Mapped 'Title' -> title: {}", value);
            } else if ("Last Name".equalsIgnoreCase(question)) {
                dto.setLastName(value);
                logger.debug("Mapped 'Last Name' -> lastName: {}", value);
            } else if ("First Name".equalsIgnoreCase(question)) {
                dto.setFirstName(value);
                logger.debug("Mapped 'First Name' -> firstName: {}", value);
            } else if ("Chinese Name".equalsIgnoreCase(question) ||
                       "Name in Chinese (If any)".equalsIgnoreCase(question)) {
                dto.setChineseName(value);
                logger.debug("Mapped 'Chinese Name' -> chineseName: {}", value);
            } else if ("Date of Birth".equalsIgnoreCase(question)) {
                dto.setDateOfBirth(parseDate(value));
                logger.debug("Mapped 'Date of Birth' -> dateOfBirth: {}", value);
            } else if ("Permanent Resident".equalsIgnoreCase(question) ||
                       "Permanent Resident in HK".equalsIgnoreCase(question)) {
                dto.setPermanentResident(value);
                logger.debug("Mapped 'Permanent Resident' -> permanentResident: {}", value);
            } else if ("Gender".equalsIgnoreCase(question)) {
                dto.setGender(value);
                logger.debug("Mapped 'Gender' -> gender: {}", value);
            } else if ("HKID Number".equalsIgnoreCase(question) ||
                       "HKID No.".equalsIgnoreCase(question)) {
                dto.setHkidNumber(value);
                logger.debug("Mapped 'HKID Number' -> hkidNumber: {}", value);
            } else if ("Passport Number".equalsIgnoreCase(question) ||
                       "Passport No.".equalsIgnoreCase(question)) {
                dto.setPassportNumber(value);
                logger.debug("Mapped 'Passport Number' -> passportNumber: {}", value);
            } else if ("Nationality".equalsIgnoreCase(question)) {
                dto.setNationality(value);
                logger.debug("Mapped 'Nationality' -> nationality: {}", value);
            } else if ("Marital Status".equalsIgnoreCase(question)) {
                dto.setMaritalStatus(value);
                logger.debug("Mapped 'Marital Status' -> maritalStatus: {}", value);
            } else if ("Residential Address".equalsIgnoreCase(question)) {
                dto.setResidentialAddress(value);
                logger.debug("Mapped 'Residential Address' -> residentialAddress: {}", value);
            } else if ("Correspondence Address".equalsIgnoreCase(question)) {
                dto.setCorrespondenceAddress(value);
                logger.debug("Mapped 'Correspondence Address' -> correspondenceAddress: {}", value);
            } else if ("Home Telephone".equalsIgnoreCase(question) ||
                       "Home Tel. No.".equalsIgnoreCase(question)) {
                dto.setHomeTelephone(value);
                logger.debug("Mapped 'Home Telephone' -> homeTelephone: {}", value);
            } else if ("Mobile Phone".equalsIgnoreCase(question) ||
                       "Mobile Phone No.".equalsIgnoreCase(question)) {
                dto.setMobilePhone(value);
                logger.debug("Mapped 'Mobile Phone' -> mobilePhone: {}", value);
            } else if ("Email Address".equalsIgnoreCase(question) ||
                       "Email".equalsIgnoreCase(question)) {
                dto.setEmail(value);
                logger.debug("Mapped 'Email' -> email: {}", value);
            }
            // Keyword-based matching (fallback) - only if fields not yet set
            else if (dto.getFirstName() == null && questionLower.contains("first name")) {
                dto.setFirstName(value);
            } else if (dto.getLastName() == null && (questionLower.contains("last name") || questionLower.contains("surname"))) {
                dto.setLastName(value);
            } else if (dto.getEmail() == null && questionLower.contains("email")) {
                dto.setEmail(value);
            } else if (dto.getMobilePhone() == null && (questionLower.contains("mobile") || questionLower.contains("cell phone"))) {
                dto.setMobilePhone(value);
            }
        }

        return dto;
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
}
