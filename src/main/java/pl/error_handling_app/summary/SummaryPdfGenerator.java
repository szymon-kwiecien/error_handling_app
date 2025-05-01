package pl.error_handling_app.summary;

import com.lowagie.text.pdf.BaseFont;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Service
public class SummaryPdfGenerator {

    private final TemplateEngine templateEngine;

    public SummaryPdfGenerator(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public void generatePdf(String templateName, Context context, HttpServletResponse response) {
        String htmlContent = templateEngine.process(templateName, context);

        response.setContentType("application/pdf");
        DateTimeFormatter fileNameFormatter = DateTimeFormatter.ofPattern("yyyy_MM_dd, HH:mm:ss");
        String formattedFileName = "REPORT_" + LocalDateTime.now().format(fileNameFormatter);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + formattedFileName + ".pdf\"");

        try (OutputStream outputStream = response.getOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlContent);
            ITextFontResolver fontResolver = renderer.getFontResolver();
            fontResolver.addFont(Objects.requireNonNull(getClass().getResource("/fonts/Roboto-Regular.ttf")).toString(),
                    BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            renderer.layout();
            renderer.createPDF(outputStream);
        } catch (IOException e) {
            throw new RuntimeException("Błąd podczas generowania pliku PDF", e);
        }
    }
}
