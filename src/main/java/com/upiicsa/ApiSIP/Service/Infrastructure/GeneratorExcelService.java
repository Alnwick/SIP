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

            Sheet sheet = workbook.createSheet("Dashboard de Control");
            sheet.setDisplayGridlines(true); // Asegura que las líneas de división sean visibles

            // --- PALETA DE COLORES Y ESTILOS ---
            // Estilo Título Principal
            CellStyle tituloStyle = crearEstiloCelda(workbook, IndexedColors.DARK_BLUE,
                    IndexedColors.WHITE, true, (short) 16);
            // Estilo Subtítulos e Info General
            CellStyle subtituloStyle = crearEstiloCelda(workbook, IndexedColors.GREY_25_PERCENT,
                    IndexedColors.BLACK, false, (short) 10);
            // Estilo Cabeceras de Tablas
            CellStyle cabeceraStyle = crearEstiloCelda(workbook, IndexedColors.DARK_BLUE,
                    IndexedColors.WHITE, true, (short) 11);

            // Estilo Datos Comunes
            CellStyle datosStyle = workbook.createCellStyle();
            datosStyle.setBorderBottom(BorderStyle.THIN);
            datosStyle.setBorderTop(BorderStyle.THIN);
            datosStyle.setBorderRight(BorderStyle.THIN);
            datosStyle.setBorderLeft(BorderStyle.THIN);
            datosStyle.setAlignment(HorizontalAlignment.LEFT);

            // Estilo Datos Centrados (Números/Estados cortos)
            CellStyle datosCentradosStyle = workbook.createCellStyle();
            datosCentradosStyle.cloneStyleFrom(datosStyle);
            datosCentradosStyle.setAlignment(HorizontalAlignment.CENTER);

            // Estilo Tarjetas de Totales (Negritas y bordes)
            CellStyle totalCardStyle = crearEstiloCelda(workbook, IndexedColors.WHITE,
                    IndexedColors.BLACK, true, (short) 12);
            totalCardStyle.setAlignment(HorizontalAlignment.CENTER);

            // --- FILA 1: TÍTULO PRINCIPAL (Fila indexada 1, Columna B en adelante)
            Row row1 = sheet.createRow(1);
            Cell cellTitulo = row1.createCell(1);
            cellTitulo.setCellValue("SISTEMA SIP - REPORTES");
            cellTitulo.setCellStyle(tituloStyle);
            sheet.addMergedRegion(new CellRangeAddress(1, 1, 1, 15));

            // --- FILA 2: METADATOS Y FECHAS
            Row row2 = sheet.createRow(2);
            Cell cellMeta = row2.createCell(1);
            String fechaHoy = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            cellMeta.setCellValue("Generado el: " + fechaHoy + " | Rango de fechas (" + excelDto.startDate() + " : "
                    + excelDto.endDate() + ")");
            cellMeta.setCellStyle(subtituloStyle);
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 1, 15));

            // --- FILA 4 Y 5: BLOQUE DE TOTALES (KPIs superiores izquierdos)
            Row row4 = sheet.createRow(4);
            Row row5 = sheet.createRow(5);

            // KPI 1: Total Alumnos
            crearCeldaCombinadaKpi(sheet, row4, row5, 1, 2, "Total Alumnos",
                    excelDto.totalStudents(), cabeceraStyle, totalCardStyle);
            // KPI 2: Trámites Activos
            crearCeldaCombinadaKpi(sheet, row4, row5, 3, 4, "Trámites Activos",
                    excelDto.activeProcess(), cabeceraStyle, totalCardStyle);
            // KPI 3: Pasantes / Concluidos
            crearCeldaCombinadaKpi(sheet, row4, row5, 5, 6, "Pasantes / Concluidos",
                    excelDto.finishProcess(), cabeceraStyle, totalCardStyle);

            // --- CABECERA DE LA TABLA PRINCIPAL DE ALUMNOS (Inicia en fila 4, columna H [7])
            String[] headersAlumnos = {"Boleta", "Nombre Completo", "Correo Electrónico", "Teléfono", "Carrera", "Plan de Estudios", "Semestre Actual", "Egresado", "Estado"};
            for (int i = 0; i < headersAlumnos.length; i++) {
                Cell cell = row4.createCell(7 + i);
                cell.setCellValue(headersAlumnos[i]);
                cell.setCellStyle(cabeceraStyle);
                // Combinar de forma vertical con la fila 5 para emparejar la altura con los KPIs
                sheet.addMergedRegion(new CellRangeAddress(4, 5, 7 + i, 7 + i));
            }

            // --- CABECERA DE LA TABLA DE DESGLOSE POR CARRERA (Inicia en fila 7, columna B [1])
            Row row7 = sheet.createRow(7);
            String[] headersCarreras = {"Carrera (Acrónimo)", "Alumnos Registrados", "Alumnos en Proceso", "Alumnos Liberados"};
            for (int i = 0; i < headersCarreras.length; i++) {
                Cell cell = row7.createCell(1 + i);
                cell.setCellValue(headersCarreras[i]);
                cell.setCellStyle(cabeceraStyle);
            }

            int maxFilas = Math.max(tableCareers.size(), tableStudents.size());
            int inicioFilaDatos = 8;

            for (int idx = 0; idx < maxFilas; idx++) {
                int nFilaActual = inicioFilaDatos + idx;
                Row row = sheet.getRow(nFilaActual);
                if (row == null) {
                    row = sheet.createRow(nFilaActual);
                }

                // Escribir datos de Carrera (Izquierda) si aún quedan elementos
                if (idx < tableCareers.size()) {
                    ExcelTableCareerDto careers = tableCareers.get(idx);

                    Cell c1 = row.createCell(1); c1.setCellValue(careers.career()); c1.setCellStyle(datosCentradosStyle);
                    Cell c2 = row.createCell(2); c2.setCellValue(careers.registered()); c2.setCellStyle(datosStyle);
                    Cell c3 = row.createCell(3); c3.setCellValue(careers.inProcess()); c3.setCellStyle(datosStyle);
                    Cell c4 = row.createCell(4); c4.setCellValue(careers.released()); c4.setCellStyle(datosStyle);
                }

                // Escribir datos de Alumnos (Derecha) si aún quedan elementos
                if (idx < tableStudents.size()) {
                    ExcelTableStudentsDto students = tableStudents.get(idx);

                    Cell c7 = row.createCell(7); c7.setCellValue(students.enrollment()); c7.setCellStyle(datosCentradosStyle);
                    Cell c8 = row.createCell(8); c8.setCellValue(students.fullName()); c8.setCellStyle(datosStyle);
                    Cell c9 = row.createCell(9); c9.setCellValue(students.email()); c9.setCellStyle(datosStyle);
                    Cell c10 = row.createCell(10); c10.setCellValue(students.phone()); c10.setCellStyle(datosCentradosStyle);
                    Cell c11 = row.createCell(11); c11.setCellValue(students.career()); c11.setCellStyle(datosCentradosStyle);
                    Cell c12 = row.createCell(12); c12.setCellValue(students.syllabus()); c12.setCellStyle(datosCentradosStyle);
                    Cell c13 = row.createCell(13); c13.setCellValue(students.semester()); c13.setCellStyle(datosStyle);
                    Cell c14 = row.createCell(14); c14.setCellValue(students.isGraduate()); c14.setCellStyle(datosCentradosStyle);
                    Cell c15 = row.createCell(15); c15.setCellValue(students.processStatus()); c15.setCellStyle(datosCentradosStyle);
                }
            }

            // Autoajustar el tamaño de las columnas utilizadas (Desde la B[1] hasta la P[15])
            for (int i = 1; i <= 15; i++) {
                sheet.autoSizeColumn(i);
                // Añadir un margen extra al ancho automático debido a fuentes negritas
                int currentWidth = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, currentWidth + 1000);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    // Método auxiliar para no duplicar código de generación de estilos
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

    // Método auxiliar para estructurar las "Tarjetas" de los KPIs superiores
    private void crearCeldaCombinadaKpi(Sheet sheet, Row r4, Row r5, int colInicio, int colFin, String titulo, long valor, CellStyle styleTitulo, CellStyle styleValor) {
        Cell cellT = r4.createCell(colInicio);
        cellT.setCellValue(titulo);
        cellT.setCellStyle(styleTitulo);
        sheet.addMergedRegion(new CellRangeAddress(4, 4, colInicio, colFin));

        Cell cellV = r5.createCell(colInicio);
        cellV.setCellValue(valor);
        cellV.setCellStyle(styleValor);
        sheet.addMergedRegion(new CellRangeAddress(5, 5, colInicio, colFin));

        // Aplicar bordes exteriores simulados a las celdas vacías combinadas
        for (int i = colInicio; i <= colFin; i++) {
            if (r4.getCell(i) == null) r4.createCell(i).setCellStyle(styleTitulo);
            if (r5.getCell(i) == null) r5.createCell(i).setCellStyle(styleValor);
        }
    }
}
