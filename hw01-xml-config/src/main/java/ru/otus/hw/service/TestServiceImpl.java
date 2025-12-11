package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionValidationException;

import java.util.List;

@RequiredArgsConstructor
public class TestServiceImpl implements TestService {
    private final IOService ioService;

    private final QuestionDao questionDao;

    private final CorrectAnswerHolder correctAnswerHolder;

    @Override
    public void executeTest() {
        printIntroduction();
        List<Question> questions = questionDao.findAll();
        if (CollectionUtils.isEmpty(questions)) {
            throw new QuestionValidationException("No questions found");
        }
        int correctAnswersCount = processQuestions(questions);
        printConclusion(correctAnswersCount, questions.size());
    }

    private void printIntroduction() {
        ioService.printLine("");
        ioService.printFormattedLine("Please answer the questions below%n");
    }

    private int processQuestions(List<Question> questions) {
        int correctAnswersCount = 0;
        for (Question question : questions) {
            askQuestion(question);
            boolean isCorrect = processAnswer(question.answers());
            if (isCorrect) {
                ++correctAnswersCount;
            }
        }
        return  correctAnswersCount;
    }

    private void askQuestion(Question question) {
        ioService.printLine(question.text());
        for (int i = 0; i < question.answers().size(); ++i) {
            Answer answer = question.answers().get(i);
            correctAnswerHolder.holdAnswerIfCorrect(answer, i);
            ioService.printFormattedLine("%d. %s", i + 1, answer.text());
        }
        if (correctAnswerHolder.isEmpty()) {
            throw new QuestionValidationException("Correct answer not found for question: " + question.text());
        }
    }

    private boolean processAnswer(List<Answer> answers) {
        int answerNumber = getAnswerNumber(answers.size());
        if (answers.get(answerNumber - 1).isCorrect()) {
            processCorrectAnswer();
            return true;
        } else {
            processWrongAnswer();
            return false;
        }
    }

    private int getAnswerNumber(int totalAnswersCount) {
        int answerNumber;
        do {
            ioService.printFormattedLine(
                    "Choose the correct answer number from 1 to %s: ", totalAnswersCount);
            answerNumber = ioService.getInt();
        } while (answerNumber < 1 || answerNumber > totalAnswersCount);
        return answerNumber;
    }

    private void processCorrectAnswer() {
        ioService.printLine("Correct!\n");
    }

    private void processWrongAnswer() {
        int correctAnswerIndex = correctAnswerHolder.getCorrectAnswerIndex();
        String correctAnswerText = correctAnswerHolder.getCorrectAnswer().text();
        ioService.printFormattedLine("Wrong! The correct answer is: %d. %s\n",
                correctAnswerIndex + 1, correctAnswerText);
    }

    private void printConclusion(int correctAnswersCount, int totalAnswersCount) {
        ioService.printFormattedLine("Test completed. Correct answers: %s/%s", correctAnswersCount, totalAnswersCount);
    }
}
