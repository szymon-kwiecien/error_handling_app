package pl.error_handling_app.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FieldsMatchValidatorTest {

    private FieldsMatchValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new FieldsMatchValidator();
        FieldsMatch annotation = mock(FieldsMatch.class);
        when(annotation.field()).thenReturn("password");
        when(annotation.fieldMatch()).thenReturn("confirmPassword");
        validator.initialize(annotation);
    }

    @Test
    void shouldReturnTrueWhenBothFieldsMatch() {
        //given
        DummyDto dto = new DummyDto("tajneHaslo123", "tajneHaslo123");

        //when
        boolean isValid = validator.isValid(dto, context);

        //then
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldReturnFalseWhenFieldsDoNotMatch() {
        //given
        DummyDto dto = new DummyDto("tajneHaslo123", "innehaslo");

        //when
        boolean isValid = validator.isValid(dto, context);

        //then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldReturnTrueWhenBothFieldsAreNull() {
        //given
        DummyDto dto = new DummyDto(null, null);

        //when
        boolean isValid = validator.isValid(dto, context);

        //then
        assertThat(isValid).isTrue();
    }

    @Test
    void shouldReturnFalseWhenFirstFieldIsNullAndSecondIsNot() {
        //given
        DummyDto dto = new DummyDto(null, "tajneHaslo123");

        //when
        boolean isValid = validator.isValid(dto, context);

        //then
        assertThat(isValid).isFalse();
    }

    @Test
    void shouldReturnFalseWhenFirstFieldIsNotNullAndSecondIsNull() {
        //given
        DummyDto dto = new DummyDto("tajneHaslo123", null);

        //when
        boolean isValid = validator.isValid(dto, context);

        //then
        assertThat(isValid).isFalse();
    }

    //Wewnetzna klasa pomocnicza która symuluje DTO przesłane z formularza
    static class DummyDto {
        private String password;
        private String confirmPassword;

        public DummyDto(String password, String confirmPassword) {
            this.password = password;
            this.confirmPassword = confirmPassword;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }
    }
}