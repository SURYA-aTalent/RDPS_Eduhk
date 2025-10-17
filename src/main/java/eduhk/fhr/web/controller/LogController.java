package eduhk.fhr.web.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
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

import eduhk.fhr.web.config.Parameters;
import eduhk.fhr.web.service.ReportService;

@Controller
@RequestMapping(value = "/log")
public class LogController extends BaseController {
	
	@Autowired
	private Parameters parameters;
	
	@RequestMapping(value = "/downloadLog", method = RequestMethod.POST)
	public ResponseEntity<Resource> downloadReport(@RequestParam String fileName) throws IOException {
		FileSystemResource file = new FileSystemResource(parameters.getLogDir() + "\\" + fileName);
		  HttpHeaders headers = new HttpHeaders();
		  headers.setContentDisposition(
		    ContentDisposition.attachment().filename(fileName).build()
		  );
		  headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

		  return ResponseEntity.ok()
		      .headers(headers)
		      .contentLength(file.contentLength())
		      .body(file);
	}	
}