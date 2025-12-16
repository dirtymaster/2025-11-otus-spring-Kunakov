package ru.otus.hw.exceptions;

public class QuestionValidationException extends RuntimeException {
    public QuestionValidationException(String message) {
        super(message);
    }
}
