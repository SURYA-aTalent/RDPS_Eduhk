package eduhk.fhr.web.dto;

import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

public class GenTransactionReportDto {

	@DateTimeFormat(pattern = "dd/MM/yyyy")
	private Date dateFrom;
	@DateTimeFormat(pattern = "dd/MM/yyyy")
	private Date dateTo;
	private List<String> payChannels;
	private List<String> paymentStatus;

	public List<String> getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(List<String> paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public Date getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(Date dateFrom) {
		this.dateFrom = dateFrom;
	}

	public Date getDateTo() {
		return dateTo;
	}

	public void setDateTo(Date dateTo) {
		this.dateTo = dateTo;
	}

	public List<String> getPayChannels() {
		return payChannels;
	}

	public void setPayChannels(List<String> payChannels) {
		this.payChannels = payChannels;
	}

	@Override
	public String toString() {
		return "GenTransactionReportDto [dateFrom=" + dateFrom + ", dateTo=" + dateTo + ", payChannels=" + payChannels + ", paymentStatus=" + paymentStatus + "]";
	}

}
