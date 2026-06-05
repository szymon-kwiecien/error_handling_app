package pl.error_handling_app.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.assertj.core.api.Assertions.assertThat;

class AttachmentUtilsTest {

    @ParameterizedTest(name = "Dla pliku ''{0}'' oczekiwana ikona to ''{1}''")
    @CsvSource({
            "dokument.doc, fa-file-word",
            "tabelka.xlsx, fa-file-excel",
            "prezentacja.ppt, fa-file-powerpoint",
            "archiwum.zip, fa-file-zipper",
            "obrazek.png, fa-file-image",
            "muzyka.mp3, fa-file-audio",
            "film.mp4, fa-file-video",
            "faktura.pdf, fa-file-pdf",
            "logi.txt, fa-file-lines",
            "skrypt.java, fa-file-code",
            "nieznane_rozszerzenie.zzz, fa-file",
            "plik_bez_rozszerzenia, fa-file"
    })
    void shouldReturnCorrectIconClass(String fileName, String expectedIcon) {
        assertThat(AttachmentUtils.getIconClass(fileName)).isEqualTo(expectedIcon);
    }

    @Test
    void shouldReturnCorrectIconClassForUppercaseExtension() {
        //given
        String fileName = "ZDJECIE.PNG";

        //when
        String iconClass = AttachmentUtils.getIconClass(fileName);

        //then, sprawdzenie czy metoda ignoruje wielkość liter
        assertThat(iconClass).isEqualTo("fa-file-image");
    }

    @Test
    void shouldReturnDefaultIconWhenFileNameIsNull() {
        assertThat(AttachmentUtils.getIconClass(null)).isEqualTo("fa-file");
    }

    @ParameterizedTest(name = "Rozszerzenie z pliku ''{0}'' to ''{1}''")
    @CsvSource({
            "plik.txt, txt",
            "archiwum.tar.gz, gz",  //wszystko po ostatniej kropce powinno zostać uciete
            "DOKUMENT.PDF, pdf"     //powinno byc zamienione na małe litery
    })
    void shouldExtractCorrectFileExtension(String fileName, String expectedExtension) {
        assertThat(AttachmentUtils.getFileExtension(fileName)).isEqualTo(expectedExtension);
    }

    @Test
    void shouldReturnEmptyStringWhenNoExtensionOrInvalidDotPlacement() {
        //brak kropki
        assertThat(AttachmentUtils.getFileExtension("plikbezrozszerzenia")).isEmpty();

        //kropka ale bez rozszrzenia
        assertThat(AttachmentUtils.getFileExtension("plik.")).isEmpty();

        //kropka na samym poczatku, brak nazwy
        assertThat(AttachmentUtils.getFileExtension(".gitignore")).isEmpty();
    }

    @Test
    void shouldFormatFileSizeCorrectly() {
        //zero lub ujemne wartosci
        assertThat(AttachmentUtils.getReadableFileSize(0)).isEqualTo("0 B");
        assertThat(AttachmentUtils.getReadableFileSize(-100)).isEqualTo("0 B");

        //bajty
        assertThat(AttachmentUtils.getReadableFileSize(500))
                .isEqualTo(String.format("%.2f B", 500.0));

        //kilobajty
        assertThat(AttachmentUtils.getReadableFileSize(1024))
                .isEqualTo(String.format("%.2f KB", 1.0));
        assertThat(AttachmentUtils.getReadableFileSize(1536))
                .isEqualTo(String.format("%.2f KB", 1.5));

        //megabajty
        assertThat(AttachmentUtils.getReadableFileSize(1048576))
                .isEqualTo(String.format("%.2f MB", 1.0));

        //gigabajty
        assertThat(AttachmentUtils.getReadableFileSize(1073741824))
                .isEqualTo(String.format("%.2f GB", 1.0));
    }
}