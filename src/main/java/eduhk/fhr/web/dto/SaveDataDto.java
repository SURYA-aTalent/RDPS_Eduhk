package eduhk.fhr.web.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;

public class SaveDataDto<T> {
	@Valid
	private List<T> update = new ArrayList<>();
	@Valid
	private List<T> remove = new ArrayList<>();
	@Valid
	private List<T> insert = new ArrayList<>();

	public List<T> getUpdate() {
		return update;
	}

	public void setUpdate(List<T> update) {
		this.update = update;
	}

	public List<T> getRemove() {
		return remove;
	}

	public void setRemove(List<T> remove) {
		this.remove = remove;
	}

	public List<T> getInsert() {
		return insert;
	}

	public void setInsert(List<T> insert) {
		this.insert = insert;
	}

	@Override
	public String toString() {
		return "SaveDataDto [update=" + update + ", remove=" + remove + ", insert=" + insert + "]";
	}

}
