package com.apso.app.service;

import com.apso.app.model.SorteoGrupal;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.regex.*;

@Service
@RequiredArgsConstructor
public class PDFService {

    private static final Color COLOR_HEADER = new Color(220, 53, 69); // Bootstrap danger red
    private static final Color COLOR_SUBHEADER = new Color(248, 215, 218); // Bootstrap danger light
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");

    public byte[] generarPDFSorteo(SorteoGrupal sorteo) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 36, 36, 54, 36);
            PdfWriter.getInstance(document, baos);
            
            document.open();
            
            // Fuentes
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, COLOR_HEADER);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
            
            // Encabezado
            agregarEncabezado(document, sorteo, titleFont);
            
            // Información del sorteo
            agregarInformacionSorteo(document, sorteo, subtitleFont, normalFont);
            
            // Procesar y mostrar los grupos
            String[] grupos = sorteo.getResultado().split("Grupo \\d+:");
            
            for (int i = 1; i < grupos.length; i++) {
                // Título del grupo
                PdfPTable groupHeader = new PdfPTable(1);
                groupHeader.setWidthPercentage(100);
                groupHeader.setSpacingBefore(15);
                groupHeader.setSpacingAfter(5);
                
                PdfPCell groupTitle = new PdfPCell(new Phrase("Grupo " + i, subtitleFont));
                groupTitle.setBackgroundColor(COLOR_SUBHEADER);
                groupTitle.setPadding(5);
                groupHeader.addCell(groupTitle);
                document.add(groupHeader);
                
                // Tabla de estudiantes
                PdfPTable estudiantesTable = new PdfPTable(6);
                estudiantesTable.setWidthPercentage(100);
                estudiantesTable.setWidths(new float[]{1, 3, 3, 2, 2, 2});
                
                // Encabezados de la tabla
                String[] headers = {"ID", "Nombre", "Email", "Grupo Teórico", "Asignatura", "Carga ID"};
                for (String headerText : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(headerText, headerFont));
                    cell.setBackgroundColor(COLOR_HEADER);
                    cell.setPadding(5);
                    estudiantesTable.addCell(cell);
                }
                
                // Procesar estudiantes del grupo usando expresiones regulares
                String estudiantesData = grupos[i];
                Pattern pattern = Pattern.compile("- ID: (\\d+)\\s*\\n\\s*Nombre: ([^\\n]+)\\s*\\n\\s*Email: ([^\\n]+)\\s*\\n\\s*Grupo Teórico: ([^\\n]+)\\s*\\n\\s*Asignatura: ([^\\n]+)\\s*\\n\\s*Carga ID: (\\d+)");
                Matcher matcher = pattern.matcher(estudiantesData);
                
                while (matcher.find()) {
                    addCell(estudiantesTable, matcher.group(1), normalFont); // ID
                    addCell(estudiantesTable, matcher.group(2), normalFont); // Nombre
                    addCell(estudiantesTable, matcher.group(3), normalFont); // Email
                    addCell(estudiantesTable, matcher.group(4), normalFont); // Grupo Teórico
                    addCell(estudiantesTable, matcher.group(5), normalFont); // Asignatura
                    addCell(estudiantesTable, matcher.group(6), normalFont); // Carga ID
                }
                
                document.add(estudiantesTable);
            }
            
            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF: " + e.getMessage(), e);
        }
    }

    private void agregarEncabezado(Document document, SorteoGrupal sorteo, Font titleFont) throws DocumentException {
        PdfPTable header = new PdfPTable(1);
        header.setWidthPercentage(100);
        header.setSpacingAfter(20);
        
        PdfPCell titleCell = new PdfPCell(new Phrase(sorteo.getTitulo(), titleFont));
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setPaddingBottom(10);
        header.addCell(titleCell);
        document.add(header);
    }

    private void agregarInformacionSorteo(Document document, SorteoGrupal sorteo, Font subtitleFont, Font normalFont) throws DocumentException {
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{1, 2});
        infoTable.setSpacingAfter(20);
        
        addInfoRow(infoTable, "Fecha de Sorteo:", sorteo.getFechaHora().format(DATE_FORMATTER), subtitleFont, normalFont);
        addInfoRow(infoTable, "Cantidad de Grupos:", String.valueOf(sorteo.getCantidadGrupos()), subtitleFont, normalFont);
        document.add(infoTable);
    }

    private void addInfoRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(valueCell);
    }

    private void addCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        table.addCell(cell);
    }
}
