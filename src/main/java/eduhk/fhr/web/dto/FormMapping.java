package eduhk.fhr.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;

@JsonFormat(shape = Shape.OBJECT)
public enum FormMapping {
	ocool("ocool", "Stock-take on Implementation of One Course One Online Lesson")
//	, moodle("moodle", "Stock-take on Moodle")
	;

	private String code;
	private String name;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private FormMapping(String code, String name) {
		this.code = code;
		this.name = name;
	}

}
