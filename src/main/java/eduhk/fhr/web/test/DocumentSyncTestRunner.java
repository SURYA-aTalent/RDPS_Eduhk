package eduhk.fhr.web.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import eduhk.fhr.web.service.CandidateDocumentSyncService;

/**
 * Test runner for document sync
 * Enable with: -Dtest.sync.candidateId=99
 */
@Component
@ConditionalOnProperty(name = "test.sync.candidateId")
public class DocumentSyncTestRunner implements CommandLineRunner {

    @Autowired
    private CandidateDocumentSyncService syncService;

    @Override
    public void run(String... args) throws Exception {
        String candidateId = System.getProperty("test.sync.candidateId");

        if (candidateId != null && !candidateId.isEmpty()) {
            System.out.println("=== Testing Document Sync for Candidate " + candidateId + " ===");

            int syncedCount = syncService.syncCandidateDocuments(candidateId);

            System.out.println("=== Sync completed. Documents synced: " + syncedCount + " ===");
            System.exit(0);
        }
    }
}
