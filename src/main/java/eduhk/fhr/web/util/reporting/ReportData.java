package eduhk.fhr.web.util.reporting;

public class ReportData {
	private String data;
	private boolean moneyColumn;
	private boolean numberColumn;
	private boolean percentageColumn;
	private boolean formulaColumn;
	private ReportCellReference[] cellReferences;
	private int offsetRowNum = 0;
	private String numberFormat = "0.000";

	private ReportCellStyle reportCellStyle = ReportCellStyle.DEFAULT;

	public void addCellReference(ReportCellReference... cellReferences) {
		this.cellReferences = cellReferences;
	}

	public String[] getCellReferencesAsString() {
		if (cellReferences == null)
			return null;
		String[] str = new String[this.cellReferences.length];
		for (int i = 0; i < cellReferences.length; i++) {
			str[i] = cellReferences[i].getCellReferenceString(offsetRowNum);
		}
		return str;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public boolean isMoneyColumn() {
		return moneyColumn;
	}

	public void setMoneyColumn(boolean moneyColumn) {
		this.moneyColumn = moneyColumn;
	}

	public boolean isNumberColumn() {
		return numberColumn;
	}

	public void setNumberColumn(boolean numberColumn) {
		this.numberColumn = numberColumn;
	}

	public boolean isFormulaColumn() {
		return formulaColumn;
	}

	public void setFormulaColumn(boolean formulaColumn) {
		this.formulaColumn = formulaColumn;
	}

	public ReportCellReference[] getCellReferences() {
		return cellReferences;
	}

	public void setCellReferences(ReportCellReference[] cellReferences) {
		this.cellReferences = cellReferences;
	}

	public int getOffsetRowNum() {
		return offsetRowNum;
	}

	public void setOffsetRowNum(int offsetRowNum) {
		this.offsetRowNum = offsetRowNum;
	}

	public boolean isPercentageColumn() {
		return percentageColumn;
	}

	public void setPercentageColumn(boolean percentageColumn) {
		this.percentageColumn = percentageColumn;
	}

	public ReportData() {
		super();
	}

	public ReportCellStyle getReportCellStyle() {
		return reportCellStyle;
	}

	public void setReportCellStyle(ReportCellStyle reportCellStyle) {
		this.reportCellStyle = reportCellStyle;
	}

	public String getNumberFormat() {
		return numberFormat;
	}

	public void setNumberFormat(String numberFormat) {
		this.numberFormat = numberFormat;
	}

	public ReportData(String data, boolean moneyColumn, boolean numberColumn, boolean percentageColumn) {
		super();
		this.data = data;
		this.moneyColumn = moneyColumn;
		this.numberColumn = numberColumn;
		this.percentageColumn = percentageColumn;
	}

	public ReportData(String data, String numberFormat) {
		super();
		this.data = data;
		this.numberFormat = numberFormat;
		this.numberColumn = true;
		this.moneyColumn = false;
		this.percentageColumn = false;
	}
	
	
}
