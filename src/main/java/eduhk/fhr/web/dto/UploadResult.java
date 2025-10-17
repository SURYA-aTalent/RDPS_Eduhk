package eduhk.fhr.web.dto;

import java.util.List;

public class UploadResult<T> {
	private List<List<String>> data;
	private List<T> objList;
	private List<String> header;
	private boolean validatedResult;
	private String errors;

	private boolean eraseBeforeChange = false;

//	private String term;

	public UploadResult(List<List<String>> data, List<T> objList, List<String> header, boolean validatedResult) {
		super();
		this.data = data;
		this.header = header;
//		this.roundNo = roundNo;
		this.objList = objList;
		this.validatedResult = validatedResult;
	}

	public boolean isEraseBeforeChange() {
		return eraseBeforeChange;
	}

	public void setEraseBeforeChange(boolean eraseBeforeChange) {
		this.eraseBeforeChange = eraseBeforeChange;
	}

	public List<T> getObjList() {
		return objList;
	}

	public void setObjList(List<T> dataList) {
		this.objList = dataList;
	}

	public UploadResult() {
		super();
	}

	public String getErrors() {
		return errors;
	}

	public void setErrors(String errors) {
		this.errors = errors;
	}

	public List<List<String>> getData() {
		return data;
	}

	public void setData(List<List<String>> data) {
		this.data = data;
	}

	public List<String> getHeader() {
		return header;
	}

	public void setHeader(List<String> header) {
		this.header = header;
	}

	public boolean isValidatedResult() {
		return validatedResult;
	}

	public void setValidatedResult(boolean validatedResult) {
		this.validatedResult = validatedResult;
	}

//	public int getRoundNo() {
//		return roundNo;
//	}
//
//	public void setRoundNo(int roundNo) {
//		this.roundNo = roundNo;
//	}

	@Override
	public String toString() {
		return "UploadResult [data=" + data + ", objList=" + objList + ", header=" + header + ", validatedResult="
				+ validatedResult + ", errors=" + errors + ", eraseBeforeChange=" + eraseBeforeChange + "]";
	}

}
