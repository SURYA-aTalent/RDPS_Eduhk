package eduhk.fhr.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFRow;

@Service
public class ExportService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Export all tables without date filter
    public ByteArrayInputStream exportAllTablesToExcel() throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();

        List<String> tables = List.of(
                "RDPS_CANDIDATE",
                "RDPS_EDU_PROF_QUAL",
                "RDPS_PERSON_INFO",
                "RDPS_PERSON_CHILD",
                "RDPS_PERSON_SPOUSE",
                "RDPS_PHOTO_UPLOAD",
                "RDPS_REFEREE",
                "RDPS_OFFER",
                "RDPS_BENEFIT_MAIN",
                "RDPS_BENEFIT_SPOUSE",
                "RDPS_BENEFIT_CHILD",
                "RDPS_WORK_EXPERIENCE",
                "RDPS_OTHER_INFO",
                "RDPS_TALENTLINK_USER_STAGING",
                "RDPS_USER_PROFILE",
                "RDPS_USER_ROLE",
                "RDPS_BANK_INFO"
        );

        for (String table : tables) {
            XSSFSheet sheet = workbook.createSheet(table);

            List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM " + table);

            if (rows.isEmpty()) continue;

            // Header row
            XSSFRow header = sheet.createRow(0);
            int colIndex = 0;
            for (String column : rows.get(0).keySet()) {
                header.createCell(colIndex++).setCellValue(column);
            }

            // Data rows
            int rowIndex = 1;
            for (Map<String, Object> row : rows) {
                XSSFRow dataRow = sheet.createRow(rowIndex++);
                int dataCol = 0;
                for (Object value : row.values()) {
                    dataRow.createCell(dataCol++).setCellValue(value == null ? "" : value.toString());
                }
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return new ByteArrayInputStream(out.toByteArray());
    }

    // Export tables filtered by TIMESTAMP column
    public ByteArrayInputStream exportTablesByDate(String fromDate, String toDate) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Tables that have TIMESTAMP column
        List<String> tables = List.of(
                "RDPS_BANK_INFO",
                "RDPS_BENEFIT_CHILD",
                "RDPS_BENEFIT_MAIN",
                "RDPS_BENEFIT_SPOUSE",
                "RDPS_CANDIDATE",
                "RDPS_EDU_PROF_QUAL",
                "RDPS_LOV_COUNTRY",
                "RDPS_LOV_DISTRICT",
                "RDPS_LOV_EDU_LEVEL",
                "RDPS_LOV_NATIONALITY",
                "RDPS_LOV_PLACE_OF_ORIGIN",
                "RDPS_LOV_QUAL_AWARD_CLASS",
                "RDPS_LOV_QUAL_AWARD_DESC",
                "RDPS_LOV_STUDY_MODE",
                "RDPS_OFFER",
                "RDPS_OTHER_INFO",
                "RDPS_PERSON_CHILD",
                "RDPS_PERSON_ECP",
                "RDPS_PERSON_INFO",
                "RDPS_PERSON_SPOUSE",
                "RDPS_PHOTO_UPLOAD",
                "RDPS_REFEREE",
                "RDPS_TALENTLINK_USER_STAGING",
                "RDPS_USER_PROFILE"
        );

        // Convert Strings to Timestamps
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        LocalDateTime from = LocalDateTime.parse(fromDate, formatter);
        LocalDateTime to = LocalDateTime.parse(toDate, formatter);
        Timestamp tsFrom = Timestamp.valueOf(from);
        Timestamp tsTo = Timestamp.valueOf(to);

        for (String table : tables) {
            XSSFSheet sheet = workbook.createSheet(table);

            // Filter by TIMESTAMP column (quoted because TIMESTAMP is reserved)
            String sql = "SELECT * FROM " + table + " WHERE \"TIMESTAMP\" BETWEEN ? AND ?";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, tsFrom, tsTo);

            if (rows.isEmpty()) continue;

            // Header row
            XSSFRow header = sheet.createRow(0);
            int colIndex = 0;
            for (String column : rows.get(0).keySet()) {
                header.createCell(colIndex++).setCellValue(column);
            }

            // Data rows
            int rowIndex = 1;
            for (Map<String, Object> row : rows) {
                XSSFRow dataRow = sheet.createRow(rowIndex++);
                int dataCol = 0;
                for (Object value : row.values()) {
                    dataRow.createCell(dataCol++)
                            .setCellValue(value == null ? "" : value.toString());
                }
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return new ByteArrayInputStream(out.toByteArray());
    }
}
