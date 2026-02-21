package fdu.kaoyanrank.service.validator;

public interface BusinessValidator<T> {
    void validate(T target);
}
