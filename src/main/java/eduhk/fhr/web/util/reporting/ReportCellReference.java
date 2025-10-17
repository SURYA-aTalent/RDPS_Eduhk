package eduhk.fhr.web.util.reporting;

import org.apache.poi.hssf.util.CellReference;

public class ReportCellReference {
//	private CellRangeAddress cellRangeAddress;
	private CellReference cellReference;
	private Integer relativeRolNum;
	private Integer colNum;

	public ReportCellReference(int rowNum, int colNum) {
		this.relativeRolNum = rowNum;
		this.colNum = colNum;
	}

//	public ReportCellReference(int firstRow, int lastRow, int firstCol, int lastCol) {
//		this.cellRangeAddress = new CellRangeAddress(firstRow, lastRow, firstCol, lastCol);
//	}

	public String getCellReferenceString(int offsetRowNum) {
//		if (cellRangeAddress != null) {
//			return cellRangeAddress.formatAsString();
//		}
		
		if (colNum != null && relativeRolNum != null) {
			cellReference = new CellReference(offsetRowNum + relativeRolNum, colNum);
			return cellReference.formatAsString();
		}
		
		return null;
	}

}
