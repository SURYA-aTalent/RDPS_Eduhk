package eduhk.fhr.web.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class ChartDatasetDto {
	private String label;
	private List<Object> data = new ArrayList<>();
	@JsonInclude(Include.NON_NULL)
	private String borderColor;
	private boolean fill = false;

	public List<Object> getData() {
		return data;
	}

	public void setData(List<Object> data) {
		this.data = data;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(String borderColor) {
		this.borderColor = borderColor;
	}

	public boolean isFill() {
		return fill;
	}

	public void setFill(boolean fill) {
		this.fill = fill;
	}

	@Override
	public String toString() {
		return "ChartDataDto [label=" + label + ", data=" + data + ", borderColor=" + borderColor + ", fill=" + fill + "]";
	}

	public ChartDatasetDto(String label, String borderColor) {
		super();
		this.label = label;
		this.borderColor = borderColor;
	}

	public ChartDatasetDto() {
		super();
	}

}