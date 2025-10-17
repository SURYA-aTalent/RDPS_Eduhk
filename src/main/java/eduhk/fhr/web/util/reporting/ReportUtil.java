package eduhk.fhr.web.util.reporting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.multipart.MultipartFile;

import com.ibm.icu.text.SimpleDateFormat;
import com.monitorjbl.xlsx.StreamingReader;

import eduhk.fhr.web.exception.DangerException;
import eduhk.fhr.web.util.Common;

public class ReportUtil {
		
	public static String convertReportFileName(String originalFileName) {
		String[] splitFileName = Common.null2Empty(originalFileName).split("\\.");
		if (splitFileName == null || splitFileName.length < 2) {
			throw new IllegalArgumentException("Invalid File Name. Please specify valid file name. e.g.: example.xlsx");
		}
		String extension = splitFileName[splitFileName.length - 1];
		String convertedFileName = splitFileName[0];

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		Date today = new Date();
		
		if (!convertedFileName.endsWith("_")) {
			convertedFileName += "_";
		}
		
		convertedFileName += sdf.format(today);
		convertedFileName += "." + extension;
		return convertedFileName;
		
	}
	
	public static List<List<String>> extractExcel(MultipartFile multipartFile, List<String> headerList, Map<String, CellType> expectedCellTypeMap) throws IOException, DangerException {
		
//		List<CellType> mergedCellTypeList = new ArrayList<>(); 
//		if (expectedCellTypeMap == null || expectedCellTypeMap.isEmpty()) {
//			for (int i = 0; i < headerList.size(); i++) {
//				mergedCellTypeList.add(CellType.STRING);
//			}
//		}
		
//		if (headerList.size() != expectedCellTypeMap.size()) {
////			throw new IllegalArgumentException("expectedCellTypeList must have same size as headerList");
//			for (int i = 0; i < headerList.size(); i++) {
//				CellType expectedCellType = expectedCellTypeMap.get(0);
//				if (expectedCellType != null) {
//					mergedCellTypeList.add(expectedCellType);
//				} else {
//					mergedCellTypeList.add(CellType.STRING);
//				}
//			}
//		}
		
		if (expectedCellTypeMap == null) {
			expectedCellTypeMap = new HashMap<>();
		}
		
		ArrayList<List<String>> resultList = new ArrayList<>();
		Workbook workbook = null;
		try {					
			workbook = StreamingReader.builder().rowCacheSize(200).bufferSize(1024).open(multipartFile.getInputStream());
			Sheet mainSheet = workbook.getSheetAt(0);
			if (mainSheet == null) {
				throw new DangerException("Sheet not found");
			}	
			
			int headerRowNo = 0;	
			List<Integer> neededHeaderCellIdx = new ArrayList<>();
			
			for (Row contentRow: mainSheet) {
				if (contentRow.getRowNum() == headerRowNo) {										
					for (String header: headerList) {
						int i = 0;
						Integer neededCellId = null;
						for (Cell headerCell: contentRow) {
							if (headerCell.getStringCellValue().equals(header)) {
								neededCellId = i;
								break;
							}
							i++;
						}
						if (neededCellId != null) {
							neededHeaderCellIdx.add(neededCellId);
						} else {
							throw new DangerException("Header Field: " + header + " is missing");
						}
					}				
				} else {
					List<String> resultObjMap = new ArrayList<>();										
					
					int i = 0;
					for (int contentCellIdx: neededHeaderCellIdx) {
						Cell contentCell = contentRow.getCell(contentCellIdx);
						if (contentCell != null) {						
//							CellType cellType = contentCell.getCellTypeEnum();
							CellType cellType = expectedCellTypeMap.get(headerList.get(i));
							
							if (cellType == null) {
								cellType = CellType.STRING;
							}
							
							if (cellType == CellType.NUMERIC) {
								resultObjMap.add(i++, contentCell.getNumericCellValue() + "");
							} else {
								resultObjMap.add(i++, contentCell.getStringCellValue());
							}
							
						} else {
							resultObjMap.add(i++, "");
						}
						
					}
										
					resultList.add(resultObjMap);					
				}

			}
						
			
		} catch (DangerException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		} finally {
			if (workbook != null) {
				workbook.close();
			}
		}
		return resultList;
	}		
	
}
