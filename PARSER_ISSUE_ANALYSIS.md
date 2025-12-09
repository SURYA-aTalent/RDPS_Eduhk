# Education/Work Document Parser Issue Analysis

## Problem Summary
The `EducationWorkDocumentParser` is extracting education and work records with mostly NULL fields because:
1. The actual document structure is more deeply nested than expected
2. Keyword matching is too loose, causing field overwrites
3. The parser doesn't recurse deep enough to find all field values

## Actual Document Structure

### Education History
```
Education History (top-level question)
└── School or Institution (1 child = 1 education record)
    ├── School Name: "DBTR School"
    ├── Sub-School / Department: "india"
    └── Degree (nested container)
        ├── Degree Name: "BE"
        ├── Degree Type: "Advanced or honors diploma" (selected option)
        ├── Degree Date: "04/04/2016" (both free text and date answer)
        └── Currently attending: "Yes" (selected option)
```

### Employment History
```
Employment History (top-level question)
└── Employer Organization (1 child = 1 work record)
    ├── Employer Organization Name: "test company"
    ├── Employer Organization Type: "Sole Employer" (selected option)
    └── Location (nested container)
        ├── City: "mayiladuthurai"
        ├── Region: "south"
        └── Country: "India"
```

## Current Parser Logic Issues

### Issue 1: Loose Keyword Matching
**Code:** `EducationWorkDocumentParser.java` lines 134-135
```java
if (question.contains("school") || question.contains("institution") || question.contains("university")) {
    dto.setInstitution(value);
}
```

**Problem:**
- Processes "School Name" → sets institution="DBTR School" ✓
- Then processes "Sub-School / Department" → OVERWRITES institution="india" ✗

**Why:** Both "School Name" and "Sub-School / Department" contain "school", so the second one overwrites the first.

### Issue 2: Insufficient Recursion Depth
**Code:** `EducationWorkDocumentParser.java` lines 117-158
```java
private TalentLinkEducationDTO extractEducationFromChild(Answer child) {
    TalentLinkEducationDTO dto = new TalentLinkEducationDTO();

    if (child.getChildren() == null) {
        return dto;
    }

    // Map child answers to education fields
    for (Answer subAnswer : child.getChildren()) {
        String question = getQuestionText(subAnswer).toLowerCase();
        String value = getAnswerValue(subAnswer);
        // ... field mapping
    }

    return dto;
}
```

**Problem:**
- Parser looks at immediate children of "School or Institution"
- Finds: "School Name", "Sub-School / Department", "Degree"
- For "Degree", it tries to extract a value, but "Degree" itself has no value
- The actual values are INSIDE "Degree" as its children: "Degree Name", "Degree Type", "Degree Date"
- Parser never looks inside "Degree" to find these nested values

### Issue 3: Missing Education Level Mapping
**Critical Field:** `educationLevel` is required by database but not mapped correctly

**Expected Mapping:**
- "Degree Type": "Advanced or honors diploma" → should map to educationLevel
- But parser looks for questions containing "education level" or "level of education" (line 138)
- The actual question is "Degree Type", which doesn't match these keywords

## Current Parsed Output vs Raw Data

### Education Record 1
| Field | Current Parsed Value | Raw Data Location | Actual Value |
|-------|---------------------|-------------------|--------------|
| institution | "india" | School Name | "DBTR School" |
| educationLevel | NULL | Degree Type (selected option) | "Advanced or honors diploma" |
| qualificationAwardDesc | NULL | Degree Name | "BE" |
| dateOfAward | NULL | Degree Date | "04/04/2016" |
| country | NULL | (not in document) | NULL |
| majorStudyArea | NULL | (not in document) | NULL |

### Education Record 2
Parsed from a different education entry with limited data.

### Work Record 1
| Field | Current Parsed Value | Raw Data Location | Actual Value |
|-------|---------------------|-------------------|--------------|
| employerName | "Sole Employer" | Employer Organization Name | "test company" |
| positionTitle | NULL | (not visible in sample) | NULL |
| startDate | NULL | (not visible in sample) | NULL |
| endDate | NULL | (not visible in sample) | NULL |

**Note:** "Sole Employer" is actually the "Employer Organization Type", not the name!

## Root Cause Analysis

The parser was designed for a simpler flat structure:
```
Education (top-level)
├── Education Record 1 (direct fields)
│   ├── institution: "value"
│   ├── educationLevel: "value"
│   └── ...
└── Education Record 2 (direct fields)
    ├── institution: "value"
    └── ...
```

But the actual structure is hierarchical with nested containers:
```
Education History (top-level)
└── School or Institution (record)
    ├── Field containers
    └── Nested containers (Degree, Location, etc.)
        └── Actual field values
```

## Solution Required

The parser needs to:
1. **Implement recursive field extraction** - traverse all nested levels to find field values
2. **Use exact question name matching** - map specific question names to fields:
   - "School Name" → institution
   - "Degree Type" → educationLevel
   - "Degree Name" → qualificationAwardDesc
   - "Degree Date" → dateOfAward
   - "Employer Organization Name" → employerName
   - "Employer Organization Type" → ??? (not a field, maybe skip)
3. **Handle nested containers** - when a question has children but no value, recurse into children
4. **Prioritize exact matches** - don't let "Sub-School" overwrite "School Name"

## Testing Endpoints

Two debug endpoints have been created for troubleshooting:

1. **Parsed Data Endpoint:**
   ```
   GET /api/test-sync/debug/parsed-data/116
   ```
   Shows what the parser extracted (current buggy output).

2. **Raw Document Endpoint:**
   ```
   GET /api/test-sync/debug/raw-document/445
   ```
   Shows the actual document structure with all questions and answers.

## Next Steps

1. Refactor `EducationWorkDocumentParser.extractEducationFromChild()` to recursively extract fields
2. Add specific question name mappings for this document type
3. Test with candidate 116 to verify all fields are correctly extracted
4. Verify database insert succeeds with non-NULL educationLevel
