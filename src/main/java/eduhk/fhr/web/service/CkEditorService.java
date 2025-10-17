package eduhk.fhr.web.service;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import eduhk.fhr.web.dto.CkEditorUpload;
import eduhk.fhr.web.dto.CkFile;
import eduhk.fhr.web.exception.DangerException;

@Service
@Transactional(rollbackFor = Exception.class)
public class CkEditorService extends BaseService {

	public static final String LOCAL_UPLOAD_PATH = "D:\\LDCS\\ckeditor\\";
	
	@Autowired
	private ParameterService parameterService;
	
	private String getUploadPath(HttpServletRequest req) throws DangerException {
		if (req.getRemoteAddr().indexOf("127.0.0.1") == 0) {
			return LOCAL_UPLOAD_PATH;
		} else {
			String uploadPath = parameterService.getParameter("SUFS_CKEDITOR_FILE_UPLOAD_PATH");
			
			if (StringUtils.isBlank(uploadPath)) {
				throw new DangerException("The parameter SUFS_CKEDITOR_FILE_UPLOAD_PATH is not yet specified.");
			}
			
			return uploadPath;
		}
	}
	
	private String getUrl(String fileName) {
		ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromCurrentContextPath();
		String returnUrl = builder.build().toUri().toString();
		returnUrl += "/public/file/" + fileName;
		
		return returnUrl;
	}
	
	public List<CkFile> getFiles(HttpServletRequest req) {
		List<CkFile> resultList = new ArrayList<>();
		try {
			Path path = Paths.get(getUploadPath(req));
			if (!path.toFile().exists() || !path.toFile().isDirectory()) {
				// Create the upload folder
				Files.createDirectories(path);
			}
			
			Files.walk(path).filter(Files::isRegularFile).forEach(v -> {
				CkFile ckFile = new CkFile();
				File file = v.toFile();
				ckFile.setFileName(file.getName());
				ckFile.setUploadedDate(new Date(file.lastModified()));
				ckFile.setUrl(getUrl(ckFile.getFileName()));
				
				resultList.add(ckFile);
			});
			
		} catch (Exception e) {
			logger.error("error on CkEditorService.getFiles. " + e.getMessage());
		}
		return resultList;
	}

	public File getFile(String fileName, HttpServletRequest request) {
		try {
			Path path = Paths.get(getUploadPath(request), fileName);
			File file = path.toFile();
		
			if (file != null && file.exists() && file.isFile()) {
				return file;
			}
			
		} catch (Exception e) {
			logger.error("error on CkEditorService.getFile. " + e.getMessage());
		}
		
		return null;
	}

	public CkFile uploadFile(CkEditorUpload ckEditorUpload, HttpServletRequest request) {
		CkFile ckFile = null;
		try {
			String fileName = ckEditorUpload.getUpload().getOriginalFilename();
			fileName = fileName.replaceAll("[^a-zA-Z0-9\\._]+", "_"); // Clean the filename
			
			Path path = Paths.get(getUploadPath(request), fileName);
			File file = path.toFile();
			
			Path parentPath = path.getParent();
			if (!parentPath.toFile().exists() || !parentPath.toFile().isDirectory()) {
				// Create the upload folder
				Files.createDirectories(parentPath);
			}
			
			if (!file.exists() || file.isFile()) {
				OutputStream os = Files.newOutputStream(path);
				os.write(ckEditorUpload.getUpload().getBytes());
				os.close();
								
				File savedFile = Paths.get(getUploadPath(request), ckEditorUpload.getUpload().getOriginalFilename()).toFile();				
				String mimetype = new MimetypesFileTypeMap().getContentType(savedFile);
				String type = mimetype.split("/")[0];			
				
				if (type.equals("image")) {
					ckFile = new CkFile();
					ckFile.setFileName(fileName);
					ckFile.setMessage("The file is uploaded successfully");
					ckFile.setUrl(getUrl(ckFile.getFileName()));					
				} else {
					Files.delete(path);
					throw new DangerException("The file is not image");
				}
			}
			
		} catch (Exception e) {
			logger.error("error on CkEditorService.uploadFile. " + e.getMessage());
		}
		return ckFile;
	}
	
}
