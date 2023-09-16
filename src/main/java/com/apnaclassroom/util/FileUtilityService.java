package com.apnaclassroom.util;

import com.apnaclassroom.model.student.Student;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

@Service
public class FileUtilityService {

    private static final Logger LOG = LoggerFactory.getLogger(FileUtilityService.class);

    public byte[] generateStudentDataExcel(List<Student> studentList) {
        LOG.info("Creating excel workwork to write data...");
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        //Create a new Excel workbook
        try (Workbook workbook = new XSSFWorkbook()) {
            //Create a sheet in the workbook
            Sheet sheet = workbook.createSheet("DataSheet");

            //Create a header row
            Row headerRow = sheet.createRow(0);
            String[] headers = getHeaderNames(studentList.get(0));
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            //Add data to the sheet
            int rowNum = 1;
            for (Student student : studentList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(student.getStudentId());
                row.createCell(1).setCellValue(student.getFirstName());
                row.createCell(2).setCellValue(student.getLastName());
                row.createCell(3).setCellValue(student.getEmail());
                row.createCell(4).setCellValue(student.getPhone());
                row.createCell(5).setCellValue(student.getDob());
                row.createCell(6).setCellValue(student.getGender());
                row.createCell(7).setCellValue(student.getFatherName());
                row.createCell(8).setCellValue(student.getMotherName());
                row.createCell(9).setCellValue(student.getAddress().getStreet());
                row.createCell(10).setCellValue(student.getAddress().getCity());
                row.createCell(11).setCellValue(student.getAddress().getState());
                row.createCell(12).setCellValue(student.getAddress().getCountry());
                row.createCell(13).setCellValue(student.getCourseId());
                row.createCell(14).setCellValue(student.getDepartmentId());
                row.createCell(15).setCellValue(student.getCreateTs());
                row.createCell(16).setCellValue(student.getUpdateTs());
                row.createCell(17).setCellValue(student.getCreateBy());
                row.createCell(18).setCellValue(student.getUpdateBy());
                row.createCell(19).setCellValue(String.valueOf(student.getRole()));
                row.createCell(20).setCellValue(student.isEnabled());
                row.createCell(21).setCellValue(student.getDoa());
                row.createCell(22).setCellValue(student.getDop());
            }

            //Save the workbook to a ByteArrayOutputStream
            FileOutputStream outputStream1 = new FileOutputStream("src/main/resources/sample.xlsx");

            workbook.write(outputStream1);
        } catch (Exception ex) {
            LOG.error("Unable to create excel fro student data, Exception: {}", ExceptionUtils.getStackTrace(ex));
        }
        return outputStream.toByteArray();
    }
    private String[] getHeaderNames(Student student){
        // Create a StringBuilder to store field values
        StringBuilder fieldValuesBuilder = new StringBuilder();

        Class<?> clazz = student.getClass();
        while (clazz != null) {
            for (Field field : clazz.getDeclaredFields()) {
                try {
                    field.setAccessible(true);
                    Object fieldValue = field.get(student);
                    if (fieldValue != null) {
                        String fieldValueAsString = fieldValue.toString().toUpperCase();
                        fieldValuesBuilder.append(fieldValueAsString).append(",");
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            clazz = clazz.getSuperclass(); // Move to superclass for recursive processing
        }
        // Convert the StringBuilder to a String[] array
        String fieldValuesString = fieldValuesBuilder.toString();
        if (!fieldValuesString.isEmpty()) {
            return fieldValuesString.split(",");
        } else {
            return new String[0]; // Return an empty array if no fields were found
        }
    }
}
