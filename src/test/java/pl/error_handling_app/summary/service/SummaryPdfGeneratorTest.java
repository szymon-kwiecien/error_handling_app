package pl.error_handling_app.summary.service;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SummaryPdfGeneratorTest {

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ServletOutputStream outputStream;

    @InjectMocks
    private SummaryPdfGenerator summaryPdfGenerator;

    @Test
    void shouldGeneratePdfSuccessfully() throws Exception {
        //given
        String templateName = "summary/summary-template";
        Context context = new Context();
        String htmlContent = "<html><body>Raport</body></html>";

        when(templateEngine.process(templateName, context)).thenReturn(htmlContent);
        when(response.getOutputStream()).thenReturn(outputStream);

        try (MockedConstruction<ITextRenderer> mockedRenderer = mockConstruction(ITextRenderer.class,
                (mock, mockedContext) -> {
                    ITextFontResolver fontResolverMock = mock(ITextFontResolver.class);
                    when(mock.getFontResolver()).thenReturn(fontResolverMock);
                })) {

            //when
            summaryPdfGenerator.generatePdf(templateName, context, response);

            //then
            verify(templateEngine).process(templateName, context);
            verify(response).setContentType("application/pdf");

            ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
            verify(response).setHeader(eq("Content-Disposition"), headerCaptor.capture());

            String capturedHeader = headerCaptor.getValue();
            assertThat(capturedHeader).startsWith("attachment; filename=\"REPORT_");
            assertThat(capturedHeader).endsWith(".pdf\"");

            assertThat(mockedRenderer.constructed()).hasSize(1);
            ITextRenderer renderer = mockedRenderer.constructed().get(0);

            verify(renderer).setDocumentFromString(htmlContent);
            verify(renderer).getFontResolver();
            verify(renderer).layout();
            verify(renderer).createPDF(outputStream);
        }
    }

    @Test
    void shouldThrowRuntimeExceptionWhenOutputStreamThrowsIOException() throws IOException {
        //given
        String templateName = "template";
        Context context = new Context();
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html></html>");

        when(response.getOutputStream()).thenThrow(new IOException("Broken pipe"));

        //when, then
        assertThatThrownBy(() -> summaryPdfGenerator.generatePdf(templateName, context, response))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Błąd podczas generowania pliku PDF")
                .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void shouldThrowRuntimeExceptionWhenRendererThrowsException() throws IOException {
        //given
        String templateName = "template";
        Context context = new Context();
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html></html>");
        when(response.getOutputStream()).thenReturn(outputStream);

        try (MockedConstruction<ITextRenderer> mockedRenderer = mockConstruction(ITextRenderer.class,
                (mock, mockedContext) -> {
                    ITextFontResolver fontResolverMock = mock(ITextFontResolver.class);
                    when(mock.getFontResolver()).thenReturn(fontResolverMock);

                    doThrow(new RuntimeException("Wewnętrzny błąd biblioteki")).when(mock).createPDF(any());
                })) {

            //when, then
            assertThatThrownBy(() -> summaryPdfGenerator.generatePdf(templateName, context, response))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Błąd podczas generowania pliku PDF")
                    .hasCauseInstanceOf(RuntimeException.class);
        }
    }
}