package eduhk.fhr.web.api.talentlink;

// File Path: src/main/java/eduhk/fhr/web/api/talentlink/GraphQLQueryBuilder.java
// Purpose: Build GraphQL queries dynamically for TalentLink API

import org.springframework.stereotype.Component;

/**
 * GraphQL Query Builder
 *
 * Constructs GraphQL queries for the TalentLink API.
 * Provides methods to build queries for different entity types.
 */
@Component
public class GraphQLQueryBuilder {

    /**
     * Build a candidate query with pagination
     *
     * @param first Number of records to fetch
     * @param after Starting offset
     * @param minCandidateId Minimum candidate ID (for incremental import)
     * @return GraphQL query string
     */
    public String buildCandidateQuery(int first, int after, String minCandidateId) {
        StringBuilder query = new StringBuilder();
        query.append("{ candidates(");
        query.append("first: ").append(first).append(", ");
        query.append("after: ").append(after).append(", ");
        query.append("sortBy: id, ");
        query.append("orderBy: ASC");

        // Add where clause if minCandidateId is provided
        if (minCandidateId != null && !minCandidateId.equals("0")) {
            query.append(", where: {id_gt: ").append(minCandidateId).append("}");
        }

        query.append(") { ");

        // Add field selection
        query.append("id ");
        query.append("firstname ");
        query.append("lastname ");
        query.append("email ");
        query.append("position { position } ");

        query.append("} }");

        return query.toString();
    }

    /**
     * Build a candidate query with date range filter (last N days)
     *
     * @param first Number of records to fetch
     * @param after Starting offset
     * @param daysBack Number of days back to fetch (e.g., 30 for last 30 days)
     * @return GraphQL query string
     */
    public String buildCandidateQueryByDateRange(int first, int after, int daysBack) {
        StringBuilder query = new StringBuilder();
        query.append("{ candidates(");
        query.append("first: ").append(first).append(", ");
        query.append("after: ").append(after).append(", ");
        query.append("sortBy: id, ");
        query.append("orderBy: ASC");

        // Add where clause for date filtering
        // Using createdDate_gte (greater than or equal) with current date - daysBack
        // Format: YYYY-MM-DD
        java.time.LocalDate fromDate = java.time.LocalDate.now().minusDays(daysBack);
        query.append(", where: {createdDate_gte: \"").append(fromDate.toString()).append("\"}");

        query.append(") { ");

        // Add field selection
        query.append("id ");
        query.append("firstname ");
        query.append("lastname ");
        query.append("email ");
        query.append("position { position } ");

        query.append("} }");

        return query.toString();
    }

    /**
     * Build a query to fetch work experience for a candidate
     *
     * @param candidateId The candidate ID
     * @return GraphQL query string
     */
    public String buildWorkExperienceQuery(String candidateId) {
        StringBuilder query = new StringBuilder();
        query.append("{ candidate(id: ").append(candidateId).append(") { ");
        query.append("workExperience { ");
        query.append("employerName ");
        query.append("natureOfBusiness ");
        query.append("positionTitle ");
        query.append("currentJob ");
        query.append("modeOfEmployment ");
        query.append("hoursPerWeek ");
        query.append("startDate ");
        query.append("endDate ");
        query.append("natureOfDuties ");
        query.append("} } }");
        return query.toString();
    }

    /**
     * Build a query to fetch education qualifications for a candidate
     *
     * @param candidateId The candidate ID
     * @return GraphQL query string
     */
    public String buildEducationQuery(String candidateId) {
        StringBuilder query = new StringBuilder();
        query.append("{ candidate(id: ").append(candidateId).append(") { ");
        query.append("education { ");
        query.append("institution ");
        query.append("country ");
        query.append("educationLevel ");
        query.append("studyMode ");
        query.append("qualificationAwardDesc ");
        query.append("qualificationAwardClass ");
        query.append("others ");
        query.append("majorStudyArea ");
        query.append("startDate ");
        query.append("dateOfAward ");
        query.append("} } }");
        return query.toString();
    }

    /**
     * Build a query to fetch referees for a candidate
     *
     * @param candidateId The candidate ID
     * @return GraphQL query string
     */
    public String buildRefereeQuery(String candidateId) {
        StringBuilder query = new StringBuilder();
        query.append("{ candidate(id: ").append(candidateId).append(") { ");
        query.append("referees { ");
        query.append("title ");
        query.append("firstname ");
        query.append("lastname ");
        query.append("positionTitle ");
        query.append("phoneNumber ");
        query.append("email ");
        query.append("relationship ");
        query.append("} } }");
        return query.toString();
    }
}
