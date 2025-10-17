package eduhk.fhr.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import eduhk.fhr.web.dao.TalentLinkUserStagingDao;
import eduhk.fhr.web.model.TalentLinkUserStaging;
import eduhk.fhr.web.service.TalentLinkUserSyncService;

@Controller
@RequestMapping("/admin/user-sync")
public class AdminUserSyncController extends BaseController {

    @Autowired
    private TalentLinkUserSyncService talentLinkUserSyncService;

    @Autowired
    private TalentLinkUserStagingDao talentLinkUserStagingDao;

    /**
     * Show user sync page
     */
    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView showUserSyncPage() {
        ModelAndView mv = new ModelAndView("admin/userSync");

        // Get all users from staging table
        List<TalentLinkUserStaging> allUsers = talentLinkUserStagingDao.getAllUsers();
        mv.addObject("users", allUsers);

        return mv;
    }

    /**
     * Trigger user sync via AJAX
     */
    @RequestMapping(value = "/trigger", method = RequestMethod.POST)
    @ResponseBody
    public String triggerSync() {
        try {
            logger.info("User sync triggered from admin UI");
            String result = talentLinkUserSyncService.syncUsersToTalentLink();
            logger.info("User sync completed: {}", result);
            return result;
        } catch (Exception e) {
            logger.error("Error during user sync: {}", e.getMessage(), e);
            return "Error: " + e.getMessage();
        }
    }
}
