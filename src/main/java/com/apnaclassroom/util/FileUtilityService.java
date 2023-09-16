package com.apnaclassroom.util;

import com.apnaclassroom.model.Address;
import com.apnaclassroom.model.FeeReceipt;
import com.apnaclassroom.model.student.Student;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FileUtilityService {

    private static final Logger LOG = LoggerFactory.getLogger(FileUtilityService.class);

    public void generateStudentDataExcel(List<Student> studentList) throws IOException {
        LOG.info("Creating excel workwork to write data...");

        try (InputStream templateFile = FileUtilityService.class.getClassLoader().getResourceAsStream("templates/StudentDataTemplate.xlsx");
             Workbook workbook = new XSSFWorkbook(templateFile)) {

            // Get the first sheet (assuming only one sheet)
            Sheet sheet = workbook.getSheetAt(0);

            int rowIndex = 1; // Start writing data from the second row (index 1)

            for (Student student : studentList) {
                Row row = sheet.createRow(rowIndex++);

                // Set values for each cell based on Student and Address objects
                row.createCell(0).setCellValue(student.getStudentId());
                row.createCell(1).setCellValue(student.getFirstName());
                row.createCell(2).setCellValue(student.getLastName());
                row.createCell(3).setCellValue(student.getEmail());
                row.createCell(4).setCellValue(student.getPhone());
                row.createCell(5).setCellValue(student.getDob());
                row.createCell(6).setCellValue(student.getGender());
                row.createCell(7).setCellValue(student.getFatherName());
                row.createCell(8).setCellValue(student.getMotherName());

                Address address = student.getAddress();
                row.createCell(9).setCellValue(address.getStreet());
                row.createCell(10).setCellValue(address.getCity());
                row.createCell(11).setCellValue(address.getState());
                row.createCell(12).setCellValue(address.getCountry());

                row.createCell(13).setCellValue(student.getCourseId());
                row.createCell(14).setCellValue(student.getDepartmentId());
                row.createCell(15).setCellValue(student.getCreateTs());
                row.createCell(16).setCellValue(student.getUpdateTs());
                row.createCell(17).setCellValue(student.getCreateBy());
                row.createCell(18).setCellValue(student.getUpdateBy());
                row.createCell(19).setCellValue("");
                row.createCell(20).setCellValue(student.getDoa());
                row.createCell(21).setCellValue(student.getDop());
            }

            // Save the modified workbook to the specified output file path
            try (FileOutputStream output = new FileOutputStream("StudentData.xlsx")) {
                workbook.write(output);
            } catch (IOException e) {
                LOG.error("Unable to write data in excel file, Exception: {}", ExceptionUtils.getStackTrace(e));
            }
        }
    }

    public void generatePaymentReceipt(FeeReceipt feeReceipt, String generateBy) throws IOException {
        try(XWPFDocument document = new XWPFDocument()) {
            String localDateTime = LocalDateTime.now().toString();
            FileOutputStream out = new FileOutputStream("PaymentReceipt"+localDateTime+".pdf");

            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setBold(true);
            run.setFontSize(18);
            run.setText("Fee Payment Receipt");
            run.addBreak();

            XWPFTable table = document.createTable(2, 2);
            table.setCellMargins(100, 100, 100, 100);

            XWPFTableRow row1 = table.getRow(0);
            row1.getCell(0).setText("Student Name:");
            row1.getCell(1).setText(feeReceipt.getId());

            XWPFTableRow row2 = table.getRow(1);
            row2.getCell(0).setText("Amount Paid:");
            row2.getCell(1).setText("$" + feeReceipt.getAmount());

            XWPFTableRow row3 = table.getRow(1);
            row3.getCell(0).setText("Fee Type:");
            row3.getCell(1).setText("$" + feeReceipt.getExamFee());

            XWPFTableRow row4 = table.getRow(1);
            row4.getCell(0).setText("Amount Paid:");
            row4.getCell(1).setText("$" + generateBy);

            XWPFParagraph dateParagraph = document.createParagraph();
            XWPFRun dateRun = dateParagraph.createRun();
            dateRun.setText("Payment Date: " + feeReceipt.getPaymentDate());

            document.write(out);
            out.close();
        }
    }
}
