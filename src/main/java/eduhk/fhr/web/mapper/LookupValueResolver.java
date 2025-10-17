package eduhk.fhr.web.mapper;

// File Path: src/main/java/eduhk/fhr/web/mapper/LookupValueResolver.java
// Purpose: Resolve API string values to LOV (List of Values) database keys

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Lookup Value Resolver
 *
 * Resolves string values from TalentLink API to numeric keys in RDPS LOV tables.
 * Implements caching for performance.
 */
@Component
public class LookupValueResolver {

    private static final Logger logger = LoggerFactory.getLogger(LookupValueResolver.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Cache for LOV values: Map<TableName, Map<Description, Key>>
    private Map<String, Map<String, Integer>> lovCache = new HashMap<>();

    /**
     * Initialize LOV caches from database
     * Called on application startup
     */
    @PostConstruct
    public void initializeCaches() {
        logger.info("Initializing LOV caches...");

        try {
            loadDistrictCache();
            loadEducationLevelCache();
            loadStudyModeCache();
            loadQualAwardDescCache();
            loadQualAwardClassCache();

            logger.info("LOV caches initialized successfully");
        } catch (Exception e) {
            logger.error("Error initializing LOV caches: {}", e.getMessage(), e);
        }
    }

    private void loadDistrictCache() {
        String sql = "SELECT DISTRICT_KEY, DISTRICT_DESC FROM RDPS.RDPS_LOV_DISTRICT WHERE ACTIVE = 'Y'";
        Map<String, Integer> districtMap = new HashMap<>();

        jdbcTemplate.query(sql, (rs) -> {
            districtMap.put(rs.getString("DISTRICT_DESC").toLowerCase(), rs.getInt("DISTRICT_KEY"));
        });

        lovCache.put("DISTRICT", districtMap);
        logger.debug("Loaded {} districts", districtMap.size());
    }

    private void loadEducationLevelCache() {
        String sql = "SELECT EDU_LEVEL_KEY, EDU_LEVEL_DESC FROM RDPS.RDPS_LOV_EDU_LEVEL WHERE ACTIVE = 'Y'";
        Map<String, Integer> eduLevelMap = new HashMap<>();

        jdbcTemplate.query(sql, (rs) -> {
            eduLevelMap.put(rs.getString("EDU_LEVEL_DESC").toLowerCase(), rs.getInt("EDU_LEVEL_KEY"));
        });

        lovCache.put("EDU_LEVEL", eduLevelMap);
        logger.debug("Loaded {} education levels", eduLevelMap.size());
    }

    private void loadStudyModeCache() {
        String sql = "SELECT STUDY_MODE_KEY, STUDY_MODE_DESC FROM RDPS.RDPS_LOV_STUDY_MODE WHERE ACTIVE = 'Y'";
        Map<String, Integer> studyModeMap = new HashMap<>();

        jdbcTemplate.query(sql, (rs) -> {
            studyModeMap.put(rs.getString("STUDY_MODE_DESC").toLowerCase(), rs.getInt("STUDY_MODE_KEY"));
        });

        lovCache.put("STUDY_MODE", studyModeMap);
        logger.debug("Loaded {} study modes", studyModeMap.size());
    }

    private void loadQualAwardDescCache() {
        String sql = "SELECT QUAL_AWARD_DESC_KEY, QUAL_AWARD_DESC FROM RDPS.RDPS_LOV_QUAL_AWARD_DESC WHERE ACTIVE = 'Y'";
        Map<String, Integer> qualAwardDescMap = new HashMap<>();

        jdbcTemplate.query(sql, (rs) -> {
            qualAwardDescMap.put(rs.getString("QUAL_AWARD_DESC").toLowerCase(), rs.getInt("QUAL_AWARD_DESC_KEY"));
        });

        lovCache.put("QUAL_AWARD_DESC", qualAwardDescMap);
        logger.debug("Loaded {} qualification award descriptions", qualAwardDescMap.size());
    }

    private void loadQualAwardClassCache() {
        String sql = "SELECT QUAL_AWARD_CLASS_KEY, QUAL_AWARD_CLASS_DESC FROM RDPS.RDPS_LOV_QUAL_AWARD_CLASS WHERE ACTIVE = 'Y'";
        Map<String, Integer> qualAwardClassMap = new HashMap<>();

        jdbcTemplate.query(sql, (rs) -> {
            qualAwardClassMap.put(rs.getString("QUAL_AWARD_CLASS_DESC").toLowerCase(), rs.getInt("QUAL_AWARD_CLASS_KEY"));
        });

        lovCache.put("QUAL_AWARD_CLASS", qualAwardClassMap);
        logger.debug("Loaded {} qualification award classes", qualAwardClassMap.size());
    }

    /**
     * Resolve district name to district key
     *
     * @param districtName District name from API
     * @return District key from RDPS_LOV_DISTRICT
     */
    public Integer resolveDistrict(String districtName) {
        if (districtName == null || districtName.isEmpty()) {
            return null;
        }

        Map<String, Integer> districtMap = lovCache.get("DISTRICT");
        if (districtMap == null) {
            logger.warn("District cache not initialized");
            return null;
        }

        Integer key = districtMap.get(districtName.toLowerCase());
        if (key == null) {
            logger.warn("District not found in cache: {}", districtName);
        }
        return key;
    }

    /**
     * Resolve education level to LOV key
     *
     * @param eduLevel Education level from API
     * @return Education level key from RDPS_LOV_EDU_LEVEL
     */
    public Integer resolveEducationLevel(String eduLevel) {
        if (eduLevel == null || eduLevel.isEmpty()) {
            return null;
        }

        Map<String, Integer> eduLevelMap = lovCache.get("EDU_LEVEL");
        if (eduLevelMap == null) {
            logger.warn("Education level cache not initialized");
            return null;
        }

        Integer key = eduLevelMap.get(eduLevel.toLowerCase());
        if (key == null) {
            logger.warn("Education level not found in cache: {}", eduLevel);
        }
        return key;
    }

    /**
     * Resolve study mode to LOV key
     *
     * @param studyMode Study mode from API
     * @return Study mode key from RDPS_LOV_STUDY_MODE
     */
    public Integer resolveStudyMode(String studyMode) {
        if (studyMode == null || studyMode.isEmpty()) {
            return null;
        }

        Map<String, Integer> studyModeMap = lovCache.get("STUDY_MODE");
        if (studyModeMap == null) {
            logger.warn("Study mode cache not initialized");
            return null;
        }

        Integer key = studyModeMap.get(studyMode.toLowerCase());
        if (key == null) {
            logger.warn("Study mode not found in cache: {}", studyMode);
        }
        return key;
    }

    /**
     * Resolve qualification award description to LOV key
     *
     * @param qualAwardDesc Qualification award description from API
     * @return Qualification award desc key from RDPS_LOV_QUAL_AWARD_DESC
     */
    public Integer resolveQualAwardDesc(String qualAwardDesc) {
        if (qualAwardDesc == null || qualAwardDesc.isEmpty()) {
            return null;
        }

        Map<String, Integer> qualAwardDescMap = lovCache.get("QUAL_AWARD_DESC");
        if (qualAwardDescMap == null) {
            logger.warn("Qual award desc cache not initialized");
            return null;
        }

        Integer key = qualAwardDescMap.get(qualAwardDesc.toLowerCase());
        if (key == null) {
            logger.warn("Qual award desc not found in cache: {}", qualAwardDesc);
        }
        return key;
    }

    /**
     * Resolve qualification award class to LOV key
     *
     * @param qualAwardClass Qualification award class from API
     * @return Qualification award class key from RDPS_LOV_QUAL_AWARD_CLASS
     */
    public Integer resolveQualAwardClass(String qualAwardClass) {
        if (qualAwardClass == null || qualAwardClass.isEmpty()) {
            return null;
        }

        Map<String, Integer> qualAwardClassMap = lovCache.get("QUAL_AWARD_CLASS");
        if (qualAwardClassMap == null) {
            logger.warn("Qual award class cache not initialized");
            return null;
        }

        Integer key = qualAwardClassMap.get(qualAwardClass.toLowerCase());
        if (key == null) {
            logger.warn("Qual award class not found in cache: {}", qualAwardClass);
        }
        return key;
    }
}
