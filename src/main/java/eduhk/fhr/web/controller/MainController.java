package eduhk.fhr.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.security.Principal;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import eduhk.fhr.web.service.CkEditorService;
import eduhk.fhr.web.service.TalentLinkUserSyncService;
import eduhk.fhr.web.service.UserProfileService;

@Controller
public class MainController extends BaseController {

	@Autowired
	private CkEditorService ckEditorService;

	@Autowired
	private UserProfileService userProfileService;

	@Autowired
	private TalentLinkUserSyncService talentLinkUserSyncService;
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ModelAndView main(HttpServletRequest request, RedirectAttributes ra) {
		ModelAndView mv = new ModelAndView("main");
		return mv;
	}
	
	@RequestMapping(value = "/makeReservation", method = RequestMethod.GET)
	public ModelAndView makeReservation(HttpServletRequest request, RedirectAttributes ra) {
		ModelAndView mv = new ModelAndView("makeReservation");
		return mv;
	}
	
	@RequestMapping(value = "/about", method = RequestMethod.GET)
	public ModelAndView about(Principal principal) {
		ModelAndView mv = new ModelAndView("about");
		return mv;
	}	
	
	@RequestMapping(value = "/contact", method = RequestMethod.GET)
	public ModelAndView contact(Principal principal) {
		ModelAndView mv = new ModelAndView("contact");
		return mv;
	}		
	
	@RequestMapping(value = "/public/file/{fileName:.+}", method = RequestMethod.GET)
	public ResponseEntity<ByteArrayResource> loadFile(@PathVariable String fileName, HttpServletRequest request) {
		try {
			File file = ckEditorService.getFile(fileName, request);
			
			HttpHeaders header = new HttpHeaders();
			header.set("Cache-Control", "cache, must-revalidate");
			header.set("Pragma", "public");
			header.set("Content-Disposition", "inline;filename=\"" + fileName + "\"");
			header.set("X-Frame-Options", "SAMEORIGIN");	
			
			if (file != null) {
				FileInputStream fis = new FileInputStream(file);
				byte[] byteArr = new byte[(int) file.length()];
				fis.read(byteArr);
				fis.close();
				ByteArrayResource bas = new ByteArrayResource(byteArr);
				return ResponseEntity.status(HttpStatus.OK).headers(header).body(bas);
			}
			
		} catch (Exception e) {
			logger.error("error on EmailTemplateController.loadFile" + e.getMessage());
		}
		
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ByteArrayResource("fail".getBytes()));
	}	

	@RequestMapping(value = {"/clear"}, method = RequestMethod.GET)
	@CacheEvict(allEntries = true, cacheNames = {"systemEnv"})
	public String clearCache() {
		return "redirect:/";
	}
	
	@RequestMapping(value = "/viewReservation", method = RequestMethod.GET)
	public ModelAndView viewReservation(Principal principal) {
		ModelAndView mv = new ModelAndView("viewReservation");
		return mv;
	}
	
	@RequestMapping(value = "/setUserProfile", method = RequestMethod.GET)
	public ModelAndView setUserProfile(Principal principal) {
		ModelAndView mv = new ModelAndView("setUserProfile");
		mv.addObject("userProfileList", userProfileService.getAllUserProfile());
		mv.addObject("roleList", userProfileService.getRoleList());	
		return mv;
	}
	
	@RequestMapping(value = "/downloadLogs", method = RequestMethod.GET)
	public ModelAndView downloadLogs(Principal principal) {
		ModelAndView mv = new ModelAndView("downloadLogs");
		return mv;
	}

	/**
	 * Test endpoint to manually trigger user sync via SOAP
	 */
	@RequestMapping(value = "/admin/test-user-sync", method = RequestMethod.GET)
	public ResponseEntity<String> testUserSync() {
		try {
			logger.info("Manual user sync triggered via /admin/test-user-sync");
			String result = talentLinkUserSyncService.syncUsersToTalentLink();
			logger.info("User sync result: {}", result);
			return ResponseEntity.ok("User sync completed via SOAP API.\n\nResult: " + result + "\n\nCheck logs for details.");
		} catch (Exception e) {
			logger.error("Error during manual user sync: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body("Error during user sync: " + e.getMessage());
		}
	}
}