package eduhk.fhr.web.util.reporting;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFCreationHelper;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

public class ReportBuilder {

	private SXSSFWorkbook workbook;

	/** Basic Parameter **/
	private String sheetName = "Sheet 1"; //Default sheet name = Sheet 1
	private String fontName = "Calibri"; //Default font = Calibri
	private short fontSize = 11; // Default font size = 11

	private String currencyFormat = "$#,##0.00_);-$#,##0.00";
	private boolean autoFreeze = true;
	private int customFreezeRowNum;

	private boolean subtotalRow = false;
	private String subtotalColumnName = "TOTAL";
	private String subtotalTotalMode = "";

	/** Report Content **/
	private List<List<ReportData>> reportData;
	private List<String> reportHeader;

	private List<String> sheetHeader;

	private boolean autoSize = true;

	private boolean reportBuilt;
	private Font defaultFont;
	private CellStyle headerRowStyle;

	public ReportBuilder buildReport() {
		int rowNum = 0;
		workbook = new SXSSFWorkbook(100);
		SXSSFSheet mainSheet = workbook.createSheet(this.sheetName);
		mainSheet.setAutobreaks(false);

		if (sheetHeader != null) {
			for (String headerStr : sheetHeader) {
				if (StringUtils.isNotEmpty(headerStr)) {
					SXSSFRow dataRow = mainSheet.createRow(rowNum++);
					SXSSFCell cell = dataRow.createCell(0);
					cell.setCellValue(headerStr);
				}
			}
		}

		if (reportHeader != null && reportHeader.size() > 0) {
			SXSSFRow dataRow = mainSheet.createRow(rowNum++);
			int col = 0;
			for (String headerStr : reportHeader) {
				if (StringUtils.isNotEmpty(headerStr)) {
					SXSSFCell cell = dataRow.createCell(col++);
					cell.setCellStyle(this.getHeaderRowStyle());
					cell.setCellValue(headerStr);
				}
			}
		}

		int dataStartRow = 0;
		int dataEndRow = 0;
		int subtotalSumCol = 0;

		if (reportData != null) {
			dataStartRow = rowNum;
			for (List<ReportData> data : reportData) {
				if (data == null) {
					throw new IllegalArgumentException("Null on toReportDataList in Reportable interface object");
				}
				subtotalSumCol = data.size();
				SXSSFRow dataRow = mainSheet.createRow(rowNum++);
				for (int i = 0; i < data.size(); i++) {
					SXSSFCell cell = dataRow.createCell(i);
					ReportData colData = data.get(i);
					if (StringUtils.isNotBlank(colData.getData())) {
						cell.setCellValue(colData.getData());
						if (colData.isMoneyColumn()) {
							cell.setCellStyle(getMoneyCellStyle());
							try {
								Double cellValue = Double.parseDouble(colData.getData());
								cell.setCellValue(cellValue);
							} catch (Exception e) {
								cell.setCellValue("");
							}
						}
						if (colData.isNumberColumn()) {
							cell.setCellType(CellType.NUMERIC);
							try {
								Double cellValue = Double.parseDouble(colData.getData());
								cell.setCellValue(cellValue);
							} catch (Exception e) {
								cell.setCellValue("");
							}
						}
						if (colData.isPercentageColumn()) {
							cell.setCellStyle(getPercentageStyle());
						}
						if (colData.isFormulaColumn()) {
							try {
								if (colData.getCellReferences() != null) {
									String formula = colData.getData();
									colData.setOffsetRowNum(rowNum - 1);
									formula = String.format(formula, (Object[]) colData.getCellReferencesAsString());
									cell.setCellFormula(formula);
								}
							} catch (Exception e) {
								cell.setCellValue("");
							}
						}

						if (colData.getReportCellStyle() != ReportCellStyle.DEFAULT) {
							switch (colData.getReportCellStyle()) {
							case SUBTOTAL_PERCENTAGE: {
								cell.setCellStyle(defaultSubtotalRowStyle(true, true));
								break;
							}
							case SUBTOTAL: {
								cell.setCellStyle(defaultSubtotalRowStyle(true, false));
								break;
							}
							case SUBTOTAL_NO_BORDER: {
								cell.setCellStyle(defaultSubtotalRowStyle(false, false));
								break;
							}
							default:
								break;
							}
						}

					} else {
						cell.setCellValue("");
					}

				}
			}

			dataEndRow = rowNum - 1;

			if (subtotalRow) {
				/** Build the subtotal row **/
				SXSSFRow dataRow = mainSheet.createRow(rowNum++);
				SXSSFCell totalHeadingCell = dataRow.createCell(0);
				totalHeadingCell.setCellStyle(defaultSubtotalRowStyle(false, false));
				totalHeadingCell.setCellValue(subtotalColumnName);
				//				for (int i: subtotalRowSumColumn) {
				int subtotalColStart = 1;
				int subtotalColEnd = subtotalSumCol - 1;
				for (int i = subtotalColStart; i < subtotalSumCol - 1; i++) {
					CellRangeAddress cellRangeAddress = new CellRangeAddress(dataStartRow, dataEndRow, i, i);
					SXSSFCell cell = dataRow.createCell(i);
					cell.setCellStyle(defaultSubtotalRowStyle(true, false));
					cell.setCellFormula("SUM(" + cellRangeAddress.formatAsString() + ")");
				}
				switch (subtotalTotalMode) {
				case "SUM": {
					CellRangeAddress cellRangeAddress = new CellRangeAddress(rowNum - 1, rowNum - 1, subtotalColStart, subtotalColEnd - 1);
					SXSSFCell totalCell = dataRow.createCell(subtotalColEnd);
					totalCell.setCellFormula("SUM(" + cellRangeAddress.formatAsString() + ")");
					totalCell.setCellStyle(defaultSubtotalRowStyle(true, false));
					break;
				}
				case "PERCENTAGE": {
					CellReference c1 = new CellReference(rowNum - 1, 1);
					CellReference c2 = new CellReference(rowNum - 1, 2);
					SXSSFCell totalCell = dataRow.createCell(subtotalColEnd);
					totalCell.setCellStyle(defaultSubtotalRowStyle(true, true));
					totalCell.setCellFormula(c1.formatAsString() + "/" + c2.formatAsString());
					break;
				}
				}

			}
		}

		if (autoSize) {
			int trackColumnFrom = 0;
			int trackColumnTo = 0;
			if (reportHeader != null) {
				trackColumnTo = reportHeader.size();
			}
			// Set auto size column
			for (int i = trackColumnFrom; i < trackColumnTo; i++) {
				mainSheet.trackColumnForAutoSizing(i);
				mainSheet.autoSizeColumn(i);	
			}
		}


		reportBuilt = true;
		return this;
	}

	public byte[] exportToByteArray() {
		if (!reportBuilt) {
			throw new IllegalArgumentException("The report is not yet built");
		}

		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			workbook.write(bos);
			return bos.toByteArray();
		} catch (Exception e) {
			return null;
		}

	}

	public ReportBuilder() {
		super();
	}

	private Font getDefaultFont() {
		if (this.defaultFont == null) {
			this.defaultFont = this.workbook.createFont();
			this.defaultFont.setFontHeightInPoints(this.fontSize);
			this.defaultFont.setFontName(this.fontName);
			this.defaultFont.setItalic(false);
			this.defaultFont.setStrikeout(false);
		}
		return this.defaultFont;
	}

	private CellStyle getMoneyCellStyle() {
		CellStyle moneyStyle;
		SXSSFCreationHelper creationHelper = new SXSSFCreationHelper(this.workbook);
		moneyStyle = this.workbook.createCellStyle();
		moneyStyle.setDataFormat(creationHelper.createDataFormat().getFormat(this.currencyFormat));
		moneyStyle.setFont(this.getDefaultFont());
		return moneyStyle;
	}

	private CellStyle defaultHeaderRowStyle() {
		Font subtotalCellFont = this.workbook.createFont();
		subtotalCellFont.setFontHeightInPoints(this.fontSize);
		subtotalCellFont.setFontName(this.fontName);
		subtotalCellFont.setBold(true);
		CellStyle subtotalCellStyle = this.workbook.createCellStyle();
		subtotalCellStyle.setFont(subtotalCellFont);
		subtotalCellStyle.setBorderTop(BorderStyle.THICK);
		subtotalCellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
		subtotalCellStyle.setDataFormat(this.workbook.createDataFormat().getFormat("0.0"));
		subtotalCellStyle.setWrapText(true);
		return subtotalCellStyle;
	}

	private CellStyle defaultSubtotalRowStyle(boolean needBorder, boolean isPercentage) {
		Font subtotalCellFont = this.workbook.createFont();
		subtotalCellFont.setFontHeightInPoints(this.fontSize);
		subtotalCellFont.setFontName(this.fontName);
		subtotalCellFont.setBold(true);
		CellStyle subtotalCellStyle = this.workbook.createCellStyle();
		subtotalCellStyle.setFont(subtotalCellFont);
		if (needBorder) {
			subtotalCellStyle.setBorderTop(BorderStyle.THICK);
			subtotalCellStyle.setBorderBottom(BorderStyle.DOUBLE);
			subtotalCellStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
		}
		if (isPercentage) {
			subtotalCellStyle.setDataFormat(this.workbook.createDataFormat().getFormat("0.0%"));
		} else {
			subtotalCellStyle.setDataFormat(this.workbook.createDataFormat().getFormat("0.0"));
		}
		subtotalCellStyle.setWrapText(true);
		return subtotalCellStyle;
	}

	private CellStyle getPercentageStyle() {
		Font subtotalCellFont = this.workbook.createFont();
		subtotalCellFont.setFontName(this.fontName);
		subtotalCellFont.setFontHeightInPoints(this.fontSize);
		CellStyle subtotalCellStyle = this.workbook.createCellStyle();
		subtotalCellStyle.setDataFormat(this.workbook.createDataFormat().getFormat("0.0%"));
		subtotalCellStyle.setFont(subtotalCellFont);
		return subtotalCellStyle;
	}

	public ReportBuilder buildReportData(List<? extends Reportable> objectList) {
		if (objectList != null && objectList.size() > 0) {
			this.setReportData(objectList.stream().map(v -> v.toReportDataList()).collect(Collectors.toList()));
		}
		return this;
	}

	public ReportBuilder buildReportHeader(List<String> headerList) {
		if (headerList != null && headerList.size() > 0) {
			this.setReportHeader(headerList);
		}

		return this;
	}

	/** Getter & Setter **/

	public String getSheetName() {
		return sheetName;
	}

	public String getSubtotalColumnName() {
		return subtotalColumnName;
	}

	public void setSubtotalColumnName(String subtotalColumnName) {
		this.subtotalColumnName = subtotalColumnName;
	}

	public String getSubtotalTotalMode() {
		return subtotalTotalMode;
	}

	public void setSubtotalTotalMode(String subtotalTotalMode) {
		this.subtotalTotalMode = subtotalTotalMode;
	}

	public boolean isSubtotalRow() {
		return subtotalRow;
	}

	public void setSubtotalRow(boolean subtotalRow) {
		this.subtotalRow = subtotalRow;
	}

	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}

	public String getFontName() {
		return fontName;
	}

	public void setFontName(String fontName) {
		this.fontName = fontName;
	}

	public short getFontSize() {
		return fontSize;
	}

	public void setFontSize(short fontSize) {
		this.fontSize = fontSize;
	}

	public boolean isAutoFreeze() {
		return autoFreeze;
	}

	public void setAutoFreeze(boolean autoFreeze) {
		this.autoFreeze = autoFreeze;
	}

	public int getCustomFreezeRowNum() {
		return customFreezeRowNum;
	}

	public void setCustomFreezeRowNum(int customFreezeRowNum) {
		this.customFreezeRowNum = customFreezeRowNum;
	}

	public List<List<ReportData>> getReportData() {
		return reportData;
	}

	public void setReportData(List<List<ReportData>> reportData) {
		this.reportData = reportData;
	}

	public List<String> getReportHeader() {
		return reportHeader;
	}

	public void setReportHeader(List<String> reportHeader) {
		this.reportHeader = reportHeader;
	}

	public List<String> getSheetHeader() {
		return sheetHeader;
	}

	public void setSheetHeader(List<String> sheetHeader) {
		this.sheetHeader = sheetHeader;
	}

	public SXSSFWorkbook getWorkbook() {
		return workbook;
	}

	public void setWorkbook(SXSSFWorkbook workbook) {
		this.workbook = workbook;
	}

	public String getCurrencyFormat() {
		return currencyFormat;
	}

	public void setCurrencyFormat(String currencyFormat) {
		this.currencyFormat = currencyFormat;
	}

	public boolean isReportBuilt() {
		return reportBuilt;
	}

	public void setReportBuilt(boolean reportBuilt) {
		this.reportBuilt = reportBuilt;
	}

	public CellStyle getHeaderRowStyle() {
		if (this.headerRowStyle == null) {
			this.headerRowStyle = defaultHeaderRowStyle();
		}
		return headerRowStyle;
	}

	public void setHeaderRowStyle(CellStyle headerRowStyle) {
		this.headerRowStyle = headerRowStyle;
	}

	public void setDefaultFont(Font defaultFont) {
		this.defaultFont = defaultFont;
	}

	public boolean isAutoSize() {
		return autoSize;
	}

	public void setAutoSize(boolean autoSize) {
		this.autoSize = autoSize;
	}

}
