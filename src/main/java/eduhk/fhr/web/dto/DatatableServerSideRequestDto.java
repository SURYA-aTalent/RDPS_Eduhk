package eduhk.fhr.web.dto;

import java.util.HashMap;
import java.util.List;

public class DatatableServerSideRequestDto {
	private int runNo;
	private int draw;
	private int start;
	private int length;
	private List<HashMap<String, String>> order;

	public List<HashMap<String, String>> getOrder() {
		return order;
	}

	public void setOrder(List<HashMap<String, String>> order) {
		this.order = order;
	}

	public int getRunNo() {
		return runNo;
	}

	public void setRunNo(int runNo) {
		this.runNo = runNo;
	}

	public int getDraw() {
		return draw;
	}

	public void setDraw(int draw) {
		this.draw = draw;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DatatableServerSideRequestDto [runNo=").append(runNo).append(", draw=").append(draw).append(", start=").append(start).append(", length=").append(length).append(", order=").append(order).append("]");
		return builder.toString();
	}

}
