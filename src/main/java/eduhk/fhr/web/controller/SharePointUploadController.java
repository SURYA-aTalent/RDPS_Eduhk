package eduhk.fhr.web.controller;

import com.microsoft.graph.models.DriveItem;
import eduhk.fhr.web.model.AjaxResponse;
import eduhk.fhr.web.service.SharePointUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for SharePoint file upload operations
 * Handles manual file upload via web form to SharePoint
 */
@Controller
@RequestMapping("/sharepoint")
@PreAuthorize("hasRole('USE_RDPS')")
public class SharePointUploadController extends BaseController {

    @Autowired
    private SharePointUploadService uploadService;

    /**
     * Show SharePoint upload page
     *
     * @param model Model
     * @return Template name
     */
    @GetMapping("/upload")
    public String uploadPage(Model model) {
        return "sharepoint/upload";
    }

    /**
     * Upload file to SharePoint (AJAX endpoint)
     *
     * @param file File to upload
     * @return AjaxResponse with upload result
     */
    @PostMapping("/ajax/upload")
    @ResponseBody
    public AjaxResponse uploadFile(@RequestParam("file") MultipartFile file) {
        AjaxResponse response = new AjaxResponse();

        try {
            logger.info("Uploading file: {}", file.getOriginalFilename());

            if (file.isEmpty()) {
                response.fail("Please select a file to upload");
                return response;
            }

            // Upload to SharePoint
            DriveItem driveItem = uploadService.uploadFile(file);

            // Build response with SharePoint details
            Map<String, String> result = new HashMap<>();
            result.put("fileId", driveItem.id);
            result.put("fileName", driveItem.name);
            result.put("webUrl", driveItem.webUrl);
            result.put("size", String.valueOf(driveItem.size));

            response.success("File uploaded successfully to SharePoint");
            response.setResult(result);

            logger.info("File uploaded successfully. SharePoint URL: {}", driveItem.webUrl);

        } catch (Exception e) {
            logger.error("Error uploading file: {}", e.getMessage(), e);
            response.fail("Upload failed: " + e.getMessage());
        }

        return response;
    }
}
