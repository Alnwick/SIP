package com.upiicsa.ApiSIP.Service.Infrastructure;

import com.upiicsa.ApiSIP.Dto.Data.ExcelDto;
import com.upiicsa.ApiSIP.Dto.Data.ExcelTableCareerDto;
import com.upiicsa.ApiSIP.Dto.Data.ExcelTableStudentsDto;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class GeneratorExcelService {

    public byte[] generateReport(
            ExcelDto excelDto,
            List<ExcelTableCareerDto> tableCareers,
            List<ExcelTableStudentsDto> tableStudents) throws IOException {

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // --- PALETA DE COLORES Y ESTILOS ---
            CellStyle tituloStyle = crearEstiloCelda(workbook, IndexedColors.DARK_BLUE, IndexedColors.WHITE, true, (short) 16);
            CellStyle subtituloStyle = crearEstiloCelda(workbook, IndexedColors.GREY_25_PERCENT, IndexedColors.BLACK, false, (short) 10);
            CellStyle cabeceraStyle = crearEstiloCelda(workbook, IndexedColors.DARK_BLUE, IndexedColors.WHITE, true, (short) 11);

            CellStyle datosStyle = workbook.createCellStyle();
            datosStyle.setBorderBottom(BorderStyle.THIN);
            datosStyle.setBorderTop(BorderStyle.THIN);
            datosStyle.setBorderRight(BorderStyle.THIN);
            datosStyle.setBorderLeft(BorderStyle.THIN);
            datosStyle.setAlignment(HorizontalAlignment.LEFT);

            CellStyle datosCentradosStyle = workbook.createCellStyle();
            datosCentradosStyle.cloneStyleFrom(datosStyle);
            datosCentradosStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle totalCardStyle = crearEstiloCelda(workbook, IndexedColors.WHITE, IndexedColors.BLACK, true, (short) 12);
            totalCardStyle.setAlignment(HorizontalAlignment.CENTER);

            String fechaHoy = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));


            Sheet sheetEstadisticas = workbook.createSheet("Estadísticas");
            sheetEstadisticas.setDisplayGridlines(true);

            Row row1Est = sheetEstadisticas.createRow(1);
            Cell cellTituloEst = row1Est.createCell(1);
            cellTituloEst.setCellValue("SISTEMA SIP - ESTADÍSTICAS GENERALES");
            cellTituloEst.setCellStyle(tituloStyle);
            sheetEstadisticas.addMergedRegion(new CellRangeAddress(1, 1, 1, 6));

            Row row2Est = sheetEstadisticas.createRow(2);
            Cell cellMetaEst = row2Est.createCell(1);
            cellMetaEst.setCellValue("Generado el: " + fechaHoy + " | Rango de fechas (" + excelDto.startDate() + " : " + excelDto.endDate() + ")");
            cellMetaEst.setCellStyle(subtituloStyle);
            sheetEstadisticas.addMergedRegion(new CellRangeAddress(2, 2, 1, 6));

            Row row4Est = sheetEstadisticas.createRow(4);
            Row row5Est = sheetEstadisticas.createRow(5);
            crearCeldaCombinadaKpi(sheetEstadisticas, row4Est, row5Est, 1, 2, "Total Alumnos", excelDto.totalStudents(), cabeceraStyle, totalCardStyle);
            crearCeldaCombinadaKpi(sheetEstadisticas, row4Est, row5Est, 3, 4, "Trámites Activos", excelDto.activeProcess(), cabeceraStyle, totalCardStyle);
            crearCeldaCombinadaKpi(sheetEstadisticas, row4Est, row5Est, 5, 6, "Pasantes / Concluidos", excelDto.finishProcess(), cabeceraStyle, totalCardStyle);

            Row row7Est = sheetEstadisticas.createRow(7);
            String[] headersCarreras = {"Carrera (Acrónimo)", "Alumnos Registrados", "Alumnos en Proceso", "Alumnos Liberados"};
            for (int i = 0; i < headersCarreras.length; i++) {
                Cell cell = row7Est.createCell(1 + i);
                cell.setCellValue(headersCarreras[i]);
                cell.setCellStyle(cabeceraStyle);
            }

            int filaCarrerasInicio = 8;
            for (int i = 0; i < tableCareers.size(); i++) {
                Row row = sheetEstadisticas.createRow(filaCarrerasInicio + i);
                ExcelTableCareerDto careers = tableCareers.get(i);

                Cell c1 = row.createCell(1); c1.setCellValue(careers.career()); c1.setCellStyle(datosCentradosStyle);
                Cell c2 = row.createCell(2); c2.setCellValue(careers.registered()); c2.setCellStyle(datosStyle);
                Cell c3 = row.createCell(3); c3.setCellValue(careers.inProcess()); c3.setCellStyle(datosStyle);
                Cell c4 = row.createCell(4); c4.setCellValue(careers.released()); c4.setCellStyle(datosStyle);
            }

            for (int i = 1; i <= 6; i++) {
                sheetEstadisticas.autoSizeColumn(i);
                sheetEstadisticas.setColumnWidth(i, sheetEstadisticas.getColumnWidth(i) + 1000);
            }


            Sheet sheetAlumnos = workbook.createSheet("Alumnos");
            sheetAlumnos.setDisplayGridlines(true);

            // Título Principal (Pestaña 2)
            Row row1Alu = sheetAlumnos.createRow(1);
            Cell cellTituloAlu = row1Alu.createCell(1);
            cellTituloAlu.setCellValue("SISTEMA SIP - DESGLOSE DE ALUMNOS");
            cellTituloAlu.setCellStyle(tituloStyle);
            sheetAlumnos.addMergedRegion(new CellRangeAddress(1, 1, 1, 9));

            // Metadatos (Pestaña 2)
            Row row2Alu = sheetAlumnos.createRow(2);
            Cell cellMetaAlu = row2Alu.createCell(1);
            cellMetaAlu.setCellValue("Generado el: " + fechaHoy + " | Rango de fechas (" + excelDto.startDate() + " : " + excelDto.endDate() + ")");
            cellMetaAlu.setCellStyle(subtituloStyle);
            sheetAlumnos.addMergedRegion(new CellRangeAddress(2, 2, 1, 9));

            // Cabecera Tabla de Alumnos (Fila 4)
            Row row4Alu = sheetAlumnos.createRow(4);
            String[] headersAlumnos = {"Boleta", "Nombre Completo", "Correo Electrónico", "Teléfono", "Carrera", "Plan de Estudios", "Semestre Actual", "Egresado", "Estado"};
            for (int i = 0; i < headersAlumnos.length; i++) {
                Cell cell = row4Alu.createCell(1 + i); // Inicia en columna B[1] para mantener margen izquierdo estético
                cell.setCellValue(headersAlumnos[i]);
                cell.setCellStyle(cabeceraStyle);
            }

            // Datos Tabla de Alumnos (Fila 5 en adelante)
            int filaAlumnosInicio = 5;
            for (int i = 0; i < tableStudents.size(); i++) {
                Row row = sheetAlumnos.createRow(filaAlumnosInicio + i);
                ExcelTableStudentsDto students = tableStudents.get(i);

                Cell c1 = row.createCell(1); c1.setCellValue(students.enrollment()); c1.setCellStyle(datosCentradosStyle);
                Cell c2 = row.createCell(2); c2.setCellValue(students.fullName()); c2.setCellStyle(datosStyle);
                Cell c3 = row.createCell(3); c3.setCellValue(students.email()); c3.setCellStyle(datosStyle);
                Cell c4 = row.createCell(4); c4.setCellValue(students.phone()); c4.setCellStyle(datosCentradosStyle);
                Cell c5 = row.createCell(5); c5.setCellValue(students.career()); c5.setCellStyle(datosCentradosStyle);
                Cell c6 = row.createCell(6); c6.setCellValue(students.syllabus()); c6.setCellStyle(datosCentradosStyle);
                Cell c7 = row.createCell(7); c7.setCellValue(students.semester()); c7.setCellStyle(datosStyle);
                Cell c8 = row.createCell(8); c8.setCellValue(students.isGraduate()); c8.setCellStyle(datosCentradosStyle);
                Cell c9 = row.createCell(9); c9.setCellValue(students.processStatus()); c9.setCellStyle(datosCentradosStyle);
            }

            // Autoajustar Columnas de Pestaña 2 (De columna B[1] a J[9])
            for (int i = 1; i <= 9; i++) {
                sheetAlumnos.autoSizeColumn(i);
                sheetAlumnos.setColumnWidth(i, sheetAlumnos.getColumnWidth(i) + 1000);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private CellStyle crearEstiloCelda(Workbook workbook, IndexedColors backColor, IndexedColors textColor, boolean bold, short fontSize) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(backColor.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Font font = workbook.createFont();
        font.setColor(textColor.getIndex());
        font.setBold(bold);
        font.setFontHeightInPoints(fontSize);
        style.setFont(font);

        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private void crearCeldaCombinadaKpi(Sheet sheet, Row r4, Row r5, int colInicio, int colFin, String titulo, long valor, CellStyle styleTitulo, CellStyle styleValor) {
        Cell cellT = r4.createCell(colInicio);
        cellT.setCellValue(titulo);
        cellT.setCellStyle(styleTitulo);
        sheet.addMergedRegion(new CellRangeAddress(4, 4, colInicio, colFin));

        Cell cellV = r5.createCell(colInicio);
        cellV.setCellValue(valor);
        cellV.setCellStyle(styleValor);
        sheet.addMergedRegion(new CellRangeAddress(5, 5, colInicio, colFin));

        for (int i = colInicio; i <= colFin; i++) {
            if (r4.getCell(i) == null) r4.createCell(i).setCellStyle(styleTitulo);
            if (r5.getCell(i) == null) r5.createCell(i).setCellStyle(styleValor);
        }
    }
}
