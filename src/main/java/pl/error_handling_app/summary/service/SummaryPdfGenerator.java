package pl.error_handling_app.summary.service;

import com.lowagie.text.pdf.BaseFont;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class SummaryPdfGenerator {

    private static final Logger log = LoggerFactory.getLogger(SummaryPdfGenerator.class);
    private final TemplateEngine templateEngine;

    public SummaryPdfGenerator(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public void generatePdf(String templateName, Context context, HttpServletResponse response) {
        log.info("Rozpoczęto generowanie pliku PDF z szablonu: {}", templateName);
        String htmlContent = templateEngine.process(templateName, context);

        response.setContentType("application/pdf");
        DateTimeFormatter fileNameFormatter = DateTimeFormatter.ofPattern("yyyy_MM_dd, HH:mm:ss");
        String formattedFileName = "REPORT_" + LocalDateTime.now().format(fileNameFormatter);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + formattedFileName + ".pdf\"");

        try (OutputStream outputStream = response.getOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlContent);

            ITextFontResolver fontResolver = renderer.getFontResolver();

            URL fontResource = getClass().getResource("/fonts/Roboto-Regular.ttf");
            if (fontResource == null) {
                log.error("Nie znaleziono czcionki w lokalizacji /fonts/Roboto-Regular.ttf");
                throw new RuntimeException("Wystapił błąd: Nie można załadować czcionki Roboto");
            }

            fontResolver.addFont(fontResource.toString(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            renderer.layout();
            renderer.createPDF(outputStream);

            log.info("Plik PDF '{}' został pomyślnie wygenerowany.", formattedFileName);

        } catch (IOException e) {
            log.error("Błąd I/O podczas generowania PDF (problem ze strumieniem wyjściowym): {}", e.getMessage(), e);
            throw new RuntimeException("Błąd podczas generowania pliku PDF", e);
        } catch (Exception e) {
            log.error("Nieoczekiwany błąd podczas procesowania dokumentu PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Błąd podczas generowania pliku PDF", e);
        }
    }
}