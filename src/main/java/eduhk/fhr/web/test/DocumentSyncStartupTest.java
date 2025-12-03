package eduhk.fhr.web.test;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eduhk.fhr.web.service.CandidateDocumentSyncService;

/**
 * Startup test for document sync - runs automatically on application start
 * Comment out @Component annotation to disable
 */
@Component
public class DocumentSyncStartupTest {

    private static final Logger logger = LoggerFactory.getLogger(DocumentSyncStartupTest.class);

    @Autowired
    private CandidateDocumentSyncService syncService;

    @PostConstruct
    public void runTest() {
        // Run in separate thread to not block application startup
        new Thread(() -> {
            try {
                Thread.sleep(2000); // Wait for app to fully initialize

                logger.info("=== AUTO-TESTING DOCUMENT SYNC FOR CANDIDATE 99 ===");
                int syncedCount = syncService.syncCandidateDocuments("99");
                logger.info("=== SYNC TEST COMPLETED. Documents synced: {} ===", syncedCount);

            } catch (Exception e) {
                logger.error("=== SYNC TEST FAILED: {} ===", e.getMessage(), e);
            }
        }).start();
    }
}
