package pl.error_handling_app.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import pl.error_handling_app.attachment.entity.Attachment;
import pl.error_handling_app.exception.FileStorageException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @InjectMocks
    private FileService fileService;

    @Test
    void shouldReturnTrueWhenFilesAreValid() {
        //given
        MockMultipartFile validFile = new MockMultipartFile("file", "raport.pdf", "application/pdf", "dane".getBytes());

        //when
        boolean isValid = fileService.areFilesValid(List.of(validFile));

        //then
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldReturnFalseWhenFilesListIsNull() {
        assertThat(fileService.areFilesValid(null)).isFalse();
    }

    @Test
    void shouldReturnFalseWhenAllFilesAreEmptyOrInvalid() {
        //given
        MockMultipartFile emptyFile = new MockMultipartFile("file", "pusty.txt", "text/plain", new byte[0]);
        MockMultipartFile noNameFile = new MockMultipartFile("file", "", "text/plain", "dane".getBytes());

        //when, then
        assertThat(fileService.areFilesValid(List.of(emptyFile))).isFalse();
        assertThat(fileService.areFilesValid(List.of(noNameFile))).isFalse();
    }

    @Test
    void shouldCreatePathWithUniqueFilename() {
        //when
        Path uniquePath = fileService.createPathWithUniqueFilename("dokument.docx", 2, "uploads");

        //then
        assertThat(uniquePath.toString()).contains("dokument(2).docx");
    }

    @Test
    void shouldStoreValidFilesSuccessfully() {
        //given
        MockMultipartFile validFile = new MockMultipartFile("file", "zdjecie.png", "image/png", "piksele".getBytes());
        String userEmail = "jan@kowalski.pl";

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {

            mockedFiles.when(() -> Files.exists(any(Path.class))).thenReturn(false);
            mockedFiles.when(() -> Files.copy(any(InputStream.class), any(Path.class),
                            eq(StandardCopyOption.REPLACE_EXISTING)))
                    .thenReturn(100L);

            //when
            List<Attachment> attachments = fileService.storeFiles(List.of(validFile), userEmail);

            //then
            assertThat(attachments).hasSize(1);
            Attachment savedAttachment = attachments.get(0);

            assertThat(savedAttachment.getFileName()).isEqualTo("zdjecie.png");
            assertThat(savedAttachment.getAddingUser()).isEqualTo(userEmail);
            assertThat(savedAttachment.getFilePath()).contains("/uploads/zdjecie.png");

            mockedFiles.verify(() -> Files.copy(any(InputStream.class), any(Path.class),
                    eq(StandardCopyOption.REPLACE_EXISTING)), times(1));
        }
    }

    @Test
    void shouldGenerateUniqueNameWhenFileAlreadyExistsDuringStoring() {
        //given
        MockMultipartFile validFile = new MockMultipartFile("file", "raport.pdf", "application/pdf", "dane".getBytes());

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.exists(any(Path.class))).thenAnswer(invocation -> {
                Path path = invocation.getArgument(0);
                return path.toString().endsWith("raport.pdf");
            });

            mockedFiles.when(() -> Files.copy(any(InputStream.class), any(Path.class), eq(StandardCopyOption.REPLACE_EXISTING)))
                    .thenReturn(100L);

            //when
            List<Attachment> attachments = fileService.storeFiles(List.of(validFile), "admin@firma.pl");

            //then
            assertThat(attachments).hasSize(1);
            Attachment savedAttachment = attachments.get(0);

            assertThat(savedAttachment.getFileName()).isEqualTo("raport.pdf");
            assertThat(savedAttachment.getFilePath()).contains("raport(1).pdf");
        }
    }

    @Test
    void shouldThrowFileStorageExceptionWhenIoErrorOccurs() {
        //given
        MockMultipartFile validFile = new MockMultipartFile("file", "wirus.exe", "application/octet-stream", "dane".getBytes());

        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {

            mockedFiles.when(() -> Files.exists(any(Path.class))).thenReturn(false);
            mockedFiles.when(() -> Files.copy(any(InputStream.class), any(Path.class), eq(StandardCopyOption.REPLACE_EXISTING)))
                    .thenThrow(new IOException("Brak miejsca na dysku"));

            //when, then
            assertThatThrownBy(() -> fileService.storeFiles(List.of(validFile), "test@test.pl"))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessageContaining("Wystąpił błąd podczas zapisywania załączników.");
        }
    }
}