package com.compliancemind.soc.service.rcm;

import com.compliancemind.soc.common.constants.SocConstants;
import com.compliancemind.soc.common.exception.BizErrorCode;
import com.compliancemind.soc.common.exception.BizException;
import com.compliancemind.soc.entity.rcm.RcmRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * RCM Excel 导入解析（Apache POI）。
 */
@Service
public class RcmExcelService {

    public List<RcmRecord> parse(InputStream inputStream, Long projectId, Integer operatorId) {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            List<RcmRecord> records = new ArrayList<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row)) {
                    continue;
                }
                RcmRecord record = toRecord(row, projectId, operatorId);
                if (record != null) {
                    records.add(record);
                }
            }
            return records;
        } catch (IOException exception) {
            throw new BizException(BizErrorCode.RCM_EXCEL_PARSE_FAILED);
        }
    }

    public void export(List<RcmRecord> records, OutputStream outputStream) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("RCM数据");
            writeHeader(sheet);
            int rowIndex = 1;
            for (RcmRecord record : records) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(defaultText(record.getControlCode()));
                row.createCell(1).setCellValue(defaultText(record.getControlName()));
                row.createCell(2).setCellValue(defaultText(record.getDescription()));
                row.createCell(3).setCellValue(defaultText(record.getCategory()));
                row.createCell(4).setCellValue(defaultText(record.getStatus()));
                row.createCell(5).setCellValue(defaultText(record.getCurrentVersion()));
                row.createCell(6).setCellValue(Boolean.TRUE.equals(record.getAiGenerated()) ? "是" : "否");
                row.createCell(7).setCellValue(defaultText(record.getControlObjective()));
                row.createCell(8).setCellValue(defaultText(record.getImplementationMethod()));
                row.createCell(9).setCellValue(defaultText(record.getEvidenceRequirement()));
                row.createCell(10).setCellValue(defaultText(record.getModuleName()));
                row.createCell(11).setCellValue(defaultText(record.getRiskDescription()));
                row.createCell(12).setCellValue(defaultText(record.getControlPerformer()));
                row.createCell(13).setCellValue(defaultText(record.getControlReviewer()));
                row.createCell(14).setCellValue(defaultText(record.getAdditionalOwner()));
                row.createCell(15).setCellValue(defaultText(record.getControlRiskRating()));
            }
            for (int i = 0; i < 16; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(outputStream);
        } catch (IOException exception) {
            throw new BizException(BizErrorCode.RCM_EXCEL_EXPORT_FAILED);
        }
    }

    private void writeHeader(Sheet sheet) {
        Row header = sheet.createRow(0);
        String[] titles = {
            "控制代码", "控制名称", "描述", "分类", "状态", "版本", "是否AI生成",
            "控制目标", "实施方法", "证据要求", "模块", "风险描述", "控制执行人",
            "控制复核人", "额外责任人", "风险等级"
        };
        for (int i = 0; i < titles.length; i++) {
            header.createCell(i).setCellValue(titles[i]);
        }
    }

    private RcmRecord toRecord(Row row, Long projectId, Integer operatorId) {
        String controlCode = readCell(row.getCell(0));
        String controlName = readCell(row.getCell(1));
        if (controlCode.isBlank() || controlName.isBlank()) {
            return null;
        }
        RcmRecord record = new RcmRecord();
        record.setProjectId(projectId);
        record.setControlCode(controlCode);
        record.setControlName(controlName);
        record.setDescription(readCell(row.getCell(2)));
        record.setCategory(readCell(row.getCell(3)));
        record.setStatus(readCell(row.getCell(4)).isBlank() ? SocConstants.Rcm.STATUS_IMPORTED : readCell(row.getCell(4)));
        record.setCurrentVersion(readCell(row.getCell(5)).isBlank() ? SocConstants.Project.INITIAL_VERSION : readCell(row.getCell(5)));
        record.setAiGenerated("是".equals(readCell(row.getCell(6))));
        record.setControlObjective(readCell(row.getCell(7)));
        record.setImplementationMethod(readCell(row.getCell(8)));
        record.setEvidenceRequirement(readCell(row.getCell(9)));
        record.setModuleName(readCell(row.getCell(10)));
        record.setRiskDescription(readCell(row.getCell(11)));
        record.setControlPerformer(readCell(row.getCell(12)));
        record.setControlReviewer(readCell(row.getCell(13)));
        record.setAdditionalOwner(readCell(row.getCell(14)));
        record.setControlRiskRating(readCell(row.getCell(15)));
        record.setDeleted(0);
        record.setCreatedBy(operatorId);
        record.setUpdatedBy(operatorId);
        return record;
    }

    private String readCell(Cell cell) {
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue().trim();
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            double value = cell.getNumericCellValue();
            long longValue = (long) value;
            if (longValue == value) {
                return String.valueOf(longValue);
            }
            return String.valueOf(value);
        }
        if (cell.getCellType() == CellType.BOOLEAN) {
            return cell.getBooleanCellValue() ? "是" : "否";
        }
        return "";
    }

    private boolean isEmptyRow(Row row) {
        for (int i = 0; i < 16; i++) {
            if (!readCell(row.getCell(i)).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String defaultText(String value) {
        return value == null ? "" : value;
    }
}

