package eduhk.fhr.web.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import eduhk.fhr.web.service.ReportService;

@Controller
@RequestMapping(value = "/report")
public class ReportController extends BaseController {
	
	@Autowired
	private ReportService reportService;
	
	@RequestMapping(value = "/downloadReport", method = RequestMethod.POST)
	public ResponseEntity<Resource> downloadReport(@RequestParam String reportParam) {
		TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>(){};
		ObjectMapper mapper = new ObjectMapper();
		HashMap<String, String> param = new HashMap<>();
		String reportName = "";
		String filename = "";		
		String sheetname = "";
		
		try {
			param = mapper.readValue(reportParam, typeRef);
			reportName = param.get("reportName");
		}  catch (IOException e) {
			logger.error("Error on ReportController: downloadReport." + e.getMessage());
        }

		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
	    Date date = new Date();
	    String timestamp = formatter.format(date);
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    
	    //Report Logic
	    if (reportName.equalsIgnoreCase("sampleReservationReport")) {	    	
	    	ArrayList<HashMap<String, String>> rptData = reportService.getSampleReservationData(param);
	    	SimpleDateFormat nameFormatter = new SimpleDateFormat("ddMMyyyyHHmmss"); 
	    	String nameTimestamp = nameFormatter.format(date);
			filename = "Sample Reservation Report ("+nameTimestamp+").xlsx";			
			sheetname = "All Bookings";
			String[] header = {"Staff Number", "Staff Name", "Email Address", "Booking Email Sent Date"};

			Workbook workbook = new XSSFWorkbook();			
			try {
				Sheet sheet = workbook.createSheet(sheetname);
				int rowIdx = 0;
				//Parameters
				Row row = sheet.createRow(rowIdx++);	
				row.createCell(0).setCellValue("Report Time = "+timestamp);
				rowIdx++; //Skip 1 row
				//Header
				row = sheet.createRow(rowIdx++);
				for (int col = 0; col < header.length; col++) {
					Cell cell = row.createCell(col);
					cell.setCellValue(header[col]);
				}				
				//Content				
				for (HashMap<String, String> rptDataRow : rptData) {
					int col = 0;
					row = sheet.createRow(rowIdx++);
					row.createCell(col++).setCellValue(rptDataRow.get("staff_id"));
					row.createCell(col++).setCellValue(rptDataRow.get("staff_name"));
					row.createCell(col++).setCellValue(rptDataRow.get("email_address"));
					row.createCell(col++).setCellValue(rptDataRow.get("ack_email_sent_date"));
				}				
				//Auto Sizing
				for (int col = 0; col < header.length; col++) {
					sheet.autoSizeColumn(col);
				}
				workbook.write(out);
				workbook.close();
			} 
			catch (IOException e) {
				e.printStackTrace();
			}		
	    }
	    
		InputStreamResource file = new InputStreamResource(new ByteArrayInputStream(out.toByteArray()));
		return ResponseEntity.ok()
		        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
		        .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
		        .body(file);
	}	
}