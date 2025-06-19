package com.apso.app.service;

import com.apso.app.model.SorteoGrupal;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PDFService {

    public byte[] generarPDFSorteo(SorteoGrupal sorteo) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            
            document.open();
            
            // Fuentes
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            
            // Título
            Paragraph title = new Paragraph("Resultado del Sorteo de Grupos", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);
            
            // Información del sorteo
            document.add(new Paragraph("Título: " + sorteo.getTitulo(), subtitleFont));
            document.add(new Paragraph("Fecha: " + 
                sorteo.getFechaHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a")), normalFont));
            document.add(new Paragraph("Cantidad de Grupos: " + sorteo.getCantidadGrupos(), normalFont));
            document.add(new Paragraph("", normalFont)); // Espacio
            
            // Resultado del sorteo
            Paragraph resultadoTitle = new Paragraph("Distribución de Grupos:", subtitleFont);
            resultadoTitle.setSpacingBefore(20);
            resultadoTitle.setSpacingAfter(10);
            document.add(resultadoTitle);
            
            document.add(new Paragraph(sorteo.getResultado(), normalFont));
            
            // Pie de página
            Paragraph footer = new Paragraph("Generado por APSO - Aplicación para Sorteo de Grupos", 
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10));
            footer.setSpacingBefore(30);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);
            
            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF: " + e.getMessage());
        }
    }
}
