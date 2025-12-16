package ru.otus.hw.service;

import lombok.Getter;
import ru.otus.hw.domain.Answer;

@Getter
public class CorrectAnswerHolder {
    private Answer correctAnswer;

    private int correctAnswerIndex;

    public void holdAnswerIfCorrect(Answer correctAnswer, int correctAnswerIndex) {
        if (correctAnswer.isCorrect()) {
            this.correctAnswer = correctAnswer;
            this.correctAnswerIndex = correctAnswerIndex;
        }
    }

    public boolean isEmpty() {
        return correctAnswer == null;
    }
}
