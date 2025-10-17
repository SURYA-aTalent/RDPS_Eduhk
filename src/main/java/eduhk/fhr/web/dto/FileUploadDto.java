package eduhk.fhr.web.dto;

import org.springframework.web.multipart.MultipartFile;

public class FileUploadDto {
	private MultipartFile file;


	public MultipartFile getFile() {
		return file;
	}

	public void setFile(MultipartFile file) {
		this.file = file;
	}

	@Override
	public String toString() {
		return "FileUploadDto [file=" + file + "]";
	}

}
