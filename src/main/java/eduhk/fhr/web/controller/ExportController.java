package eduhk.fhr.web.controller;

import eduhk.fhr.web.service.ExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;


import java.io.ByteArrayInputStream;
import java.io.IOException;

@Controller
@RequestMapping("/export")
public class ExportController {

    @Autowired
    private ExportService exportService;

    @GetMapping("/excel")
    public ResponseEntity<InputStreamResource> export() throws IOException {

        // Export Excel file from service
        ByteArrayInputStream excel = exportService.exportAllTablesToExcel();

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=RDPS_All_Tables.xlsx");

        // Return response entity
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(excel));
    }


    @GetMapping("/excel-by-date")
    public ResponseEntity<InputStreamResource> exportByDate(
            @RequestParam String fromDate,
            @RequestParam String toDate) throws IOException {

        ByteArrayInputStream excel = exportService.exportTablesByDate(fromDate, toDate);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=RDPS_Filtered.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(excel));
    }

}
