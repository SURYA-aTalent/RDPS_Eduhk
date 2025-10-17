package eduhk.fhr.web.service.import_;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import eduhk.fhr.web.api.talentlink.TalentLinkApiClient;
import eduhk.fhr.web.api.talentlink.TalentLinkApiException;

import java.util.ArrayList;
import java.util.List;

/**
 * Service to fetch candidate pools from TalentLink API
 */
@Service
public class TalentLinkPoolService {

    private static final Logger logger = LoggerFactory.getLogger(TalentLinkPoolService.class);

    @Autowired
    private TalentLinkApiClient apiClient;

    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Fetch list of available candidate pools from TalentLink
     *
     * @return List of pool information (id, name, description)
     */
    public List<PoolInfo> fetchAvailablePools() throws TalentLinkApiException {
        logger.info("Fetching available candidate pools from TalentLink");

        // Build GraphQL query to get candidate pools
        String query = "{ candidatePools { id name description active } }";

        try {
            String apiResponse = apiClient.executeGraphQLQuery(query);
            logger.debug("Received pool API response: {}", apiResponse);

            // Parse JSON response
            JsonNode rootNode = objectMapper.readTree(apiResponse);
            JsonNode poolsNode = rootNode.path("data").path("candidatePools");

            List<PoolInfo> pools = new ArrayList<>();

            if (poolsNode.isArray()) {
                for (JsonNode poolNode : poolsNode) {
                    PoolInfo pool = new PoolInfo();
                    pool.setId(poolNode.path("id").asText());
                    pool.setName(poolNode.path("name").asText());
                    pool.setDescription(poolNode.path("description").asText());
                    pool.setActive(poolNode.path("active").asBoolean());
                    pools.add(pool);

                    logger.info("Found pool: {} (ID: {})", pool.getName(), pool.getId());
                }
            }

            logger.info("Total pools found: {}", pools.size());
            return pools;

        } catch (Exception e) {
            logger.error("Error fetching candidate pools: {}", e.getMessage(), e);
            throw new TalentLinkApiException("Failed to fetch candidate pools: " + e.getMessage(), e);
        }
    }

    /**
     * Inner class to hold pool information
     */
    public static class PoolInfo {
        private String id;
        private String name;
        private String description;
        private boolean active;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        @Override
        public String toString() {
            return "PoolInfo{id='" + id + "', name='" + name + "', active=" + active + "}";
        }
    }
}
