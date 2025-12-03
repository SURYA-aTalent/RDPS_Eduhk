package eduhk.fhr.web.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import eduhk.fhr.web.model.SchedulerModel;
import eduhk.fhr.web.service.SchedulerService;
import eduhk.fhr.web.service.TalentLinkUserSyncService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@Component
public class ScheduleTask{

	private static final Logger logger = LoggerFactory.getLogger(ScheduleTask.class);
	@Autowired
	private SchedulerService schedulerService;

	@Autowired
	private TalentLinkUserSyncService talentLinkUserSyncService;

	@Autowired
	private eduhk.fhr.web.service.CandidateDocumentSyncService candidateDocumentSyncService;

	// Schedule task using cron expression
	@SchedulerLock(name = "RDPSTaskLock", lockAtMostFor = "5m", lockAtLeastFor = "2m")
	@Scheduled(cron = "0 30 14,22 * * ?")
	//@Scheduled(cron = "0/5 * * * * ?")
	//@Scheduled(cron = "0 */1 * * * ?")
    public void performTask() {
        writeLog("[performTask()] Start");
        // Insert your business logic here
        String result = schedulerService.insertIntoTable(new SchedulerModel());
        writeLog("[performTask()] - " + result);
        writeLog("[performTask()] End");
    }
	
    public void writeLog(String message) {
        logger.info(message);
    }
	
    public void writeErrorLog(String message) {
        logger.error(message);
    }

	// Schedule task for TalentLink user sync
	// Runs every hour at minute 0
	@SchedulerLock(name = "TalentLinkUserSyncLock", lockAtMostFor = "10m", lockAtLeastFor = "1m")
	@Scheduled(cron = "0 0 * * * ?")
	//@Scheduled(cron = "0 */5 * * * ?") // Run every 5 minutes for testing
	public void syncUsersToTalentLink() {
		writeLog("[syncUsersToTalentLink()] Start");
		try {
			String result = talentLinkUserSyncService.syncUsersToTalentLink();
			writeLog("[syncUsersToTalentLink()] - " + result);
		} catch (Exception e) {
			writeErrorLog("[syncUsersToTalentLink()] Error: " + e.getMessage());
		}
		writeLog("[syncUsersToTalentLink()] End");
	}

	/**
	 * Scheduled job to sync candidate documents from TalentLink to SharePoint
	 * Runs daily at 3:00 AM
	 * NOTE: Automatic scheduling is currently DISABLED
	 */
	@SchedulerLock(name = "CandidateDocumentSyncLock", lockAtMostFor = "2h", lockAtLeastFor = "5m")
	//@Scheduled(cron = "0 0 3 * * ?")  // Daily at 3:00 AM - DISABLED
	//@Scheduled(cron = "0 */10 * * * ?") // Run every 10 minutes for testing
	public void syncCandidateDocuments() {
		writeLog("=== Starting Candidate Document Sync Job ===");

		try {
			java.util.Map<String, Integer> stats = candidateDocumentSyncService.syncAllCandidateDocuments();

			writeLog("[syncCandidateDocuments()] Job completed successfully");
			writeLog("[syncCandidateDocuments()] Total candidates: " + stats.get("totalCandidates"));
			writeLog("[syncCandidateDocuments()] Candidates processed: " + stats.get("candidatesProcessed"));
			writeLog("[syncCandidateDocuments()] Documents synced: " + stats.get("totalSynced"));
			writeLog("[syncCandidateDocuments()] Failed candidates: " + stats.get("totalFailed"));

		} catch (Exception e) {
			writeErrorLog("[syncCandidateDocuments()] Job failed: " + e.getMessage());
			logger.error("Document sync job error", e);
		}

		writeLog("=== Candidate Document Sync Job Finished ===");
	}
}
