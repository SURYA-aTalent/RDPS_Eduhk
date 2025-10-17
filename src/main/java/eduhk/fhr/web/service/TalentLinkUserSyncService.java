package eduhk.fhr.web.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eduhk.fhr.web.dao.TalentLinkUserStagingDao;
import eduhk.fhr.web.model.TalentLinkUserStaging;
import eduhk.fhr.web.soap.user.UserDto;

/**
 * Service to sync users from staging table to TalentLink using SOAP API
 */
@Service
public class TalentLinkUserSyncService {

    private static final Logger logger = LoggerFactory.getLogger(TalentLinkUserSyncService.class);

    @Autowired
    private TalentLinkUserStagingDao userStagingDao;

    @Autowired
    private TalentLinkSOAPUserService soapUserService;

    /**
     * Sync all unsynced users from staging table to TalentLink
     *
     * @return Summary of sync operation
     */
    public String syncUsersToTalentLink() {
        logger.info("Starting user sync to TalentLink via SOAP");

        List<TalentLinkUserStaging> unsyncedUsers = userStagingDao.getUnsyncedUsers();
        logger.info("Found {} unsynced users", unsyncedUsers.size());

        if (unsyncedUsers.isEmpty()) {
            logger.info("No users to sync");
            return "No users to sync";
        }

        int successCount = 0;
        int failureCount = 0;
        int updatedCount = 0;

        for (TalentLinkUserStaging user : unsyncedUsers) {
            try {
                logger.info("Syncing user: {} {} ({})", user.getFirstName(), user.getLastName(), user.getEmail());

                // Check if user already exists in TalentLink
                UserDto existingUser = soapUserService.getUserByEmailOrLogin(user.getEmail());

                if (existingUser != null) {
                    // User exists, check if we should update or handle status change
                    logger.info("User already exists in TalentLink with ID: {}", existingUser.getId());

                    if ("Inactive".equalsIgnoreCase(user.getStatus())) {
                        // Deactivate user
                        soapUserService.deactivateUser(existingUser.getId());
                        userStagingDao.markAsSynced(user.getUserId(),
                            "User deactivated in TalentLink (ID: " + existingUser.getId() + ")");
                        updatedCount++;
                        logger.info("User {} deactivated successfully", user.getEmail());
                    } else {
                        // Update user
                        soapUserService.updateUser(existingUser.getId(), user);
                        userStagingDao.markAsSynced(user.getUserId(),
                            "User updated in TalentLink (ID: " + existingUser.getId() + ")");
                        updatedCount++;
                        logger.info("User {} updated successfully", user.getEmail());
                    }
                } else {
                    // User doesn't exist, create new user
                    Long userId = soapUserService.createUser(user);
                    userStagingDao.markAsSynced(user.getUserId(),
                        "Successfully created in TalentLink with ID: " + userId);
                    successCount++;
                    logger.info("User {} created successfully with ID: {}", user.getEmail(), userId);
                }

            } catch (Exception e) {
                logger.error("Failed to sync user {}: {}", user.getEmail(), e.getMessage(), e);
                String errorLog = "Failed to sync via SOAP: " + e.getMessage();
                if (errorLog.length() > 4000) {
                    errorLog = errorLog.substring(0, 3900) + "... (truncated)";
                }
                userStagingDao.markAsFailed(user.getUserId(), errorLog);
                failureCount++;
            }
        }

        String summary = String.format(
            "User sync completed via SOAP: %d created, %d updated, %d failed",
            successCount, updatedCount, failureCount
        );
        logger.info(summary);
        return summary;
    }
}
