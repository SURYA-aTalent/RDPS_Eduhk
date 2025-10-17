package eduhk.fhr.web.dto;

import org.springframework.web.multipart.MultipartFile;

public class CkEditorUpload {
	private MultipartFile upload;
	private String CKEditor;
	private String CKEditorFuncNum;
	private String langCode;

	public MultipartFile getUpload() {
		return upload;
	}

	public void setUpload(MultipartFile upload) {
		this.upload = upload;
	}

	public String getCKEditor() {
		return CKEditor;
	}

	public void setCKEditor(String cKEditor) {
		CKEditor = cKEditor;
	}

	public String getCKEditorFuncNum() {
		return CKEditorFuncNum;
	}

	public void setCKEditorFuncNum(String cKEditorFuncNum) {
		CKEditorFuncNum = cKEditorFuncNum;
	}

	public String getLangCode() {
		return langCode;
	}

	public void setLangCode(String langCode) {
		this.langCode = langCode;
	}

	@Override
	public String toString() {
		return "CkEditorUpload [upload=" + upload + ", CKEditor=" + CKEditor + ", CKEditorFuncNum=" + CKEditorFuncNum + ", langCode=" + langCode + "]";
	}

}
