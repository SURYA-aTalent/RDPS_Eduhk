package eduhk.fhr.web.dto;

import jakarta.validation.Valid;

public class DataTableDto<T> {
//	private int roundNo;
	@Valid
	private SaveDataDto<T> saveData;

	private boolean eraseAllBeforeSave = false;

	public DataTableDto() {
		super();
		this.saveData = new SaveDataDto<>();
	}

	public SaveDataDto<T> getSaveData() {
		return saveData;
	}

	public void setSaveData(SaveDataDto<T> saveData) {
		this.saveData = saveData;
	}

//	public int getRoundNo() {
//		return roundNo;
//	}
//
//	public void setRoundNo(int roundNo) {
//		this.roundNo = roundNo;
//	}

	public boolean isEraseAllBeforeSave() {
		return eraseAllBeforeSave;
	}

	public void setEraseAllBeforeSave(boolean earseAllBeforeSave) {
		this.eraseAllBeforeSave = earseAllBeforeSave;
	}

	@Override
	public String toString() {
		return "DataTableDto [saveData=" + saveData + ", eraseAllBeforeSave=" + eraseAllBeforeSave + "]";
	}
	
}
