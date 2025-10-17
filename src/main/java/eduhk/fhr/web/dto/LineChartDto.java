package eduhk.fhr.web.dto;

import java.util.Arrays;
import java.util.List;

public class LineChartDto {
	private String[] labels;
	private List<ChartDatasetDto> data;

	public List<ChartDatasetDto> getData() {
		return data;
	}

	public void setData(List<ChartDatasetDto> data) {
		this.data = data;
	}

	public String[] getLabels() {
		return labels;
	}

	public void setLabels(String[] labels) {
		this.labels = labels;
	}

	@Override
	public String toString() {
		return "LineChartDto [labels=" + Arrays.toString(labels) + "]";
	}

}