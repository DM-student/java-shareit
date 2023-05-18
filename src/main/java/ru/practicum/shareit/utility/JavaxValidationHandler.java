package ru.practicum.shareit.utility;

import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

// В этом классе я малость "побаловался" с JavaDoc'ами.

@Component
public class JavaxValidationHandler {
    private ValidatorFactory factory;
    private Validator validator;

    public JavaxValidationHandler() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    /**
     * Данный метод прогоняет объект через аннотационную валидацию, возвращая полный список нарушений валидации.
     * @param target объект, который подвергается валидации.
     * @return сет, состоящий из нарушений валидации.
     */
    public Set<ConstraintViolation<Object>> validateFull(Object target) {
        return validator.validate(target);
    }

    /**
     * Данный метод прогоняет объект через аннотационную валидацию, возвращая краткий,
     * булев ответ об успешности валидации.
     * @param target объект, который подвергается валидации.
     * @return Возвращает простое булево значение. True - объект прошёл проверку, false - нет.
     */
    public boolean validate(Object target) {
        return validateFull(target).isEmpty();
    }
}
