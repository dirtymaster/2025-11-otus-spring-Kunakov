package ru.otus.hw.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionValidationException;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith({MockitoExtension.class})
class TestServiceTest {
    @InjectMocks
    private TestServiceImpl testService;

    @Mock
    private IOService ioService;

    @Mock
    private QuestionDao questionDao;

    @Mock
    private CorrectAnswerHolder correctAnswerHolder;

    @Captor
    ArgumentCaptor<String> lineArgumentCaptor;

    @Captor
    ArgumentCaptor<String> patternArgumentCaptor;

    @Captor
    ArgumentCaptor<Object[]> argsArgumentCaptor;

    @DisplayName("Should execute 2 tests")
    @Test
    void shouldExecuteTwoTests() {
        List<Question> questions = createQuestions(2, true, false);
        mockDependencies(questions);
        testService.executeTest();
        verifyIoServicePrintForTwoQuestions();
        verifyDependencies(2, 4, 2);
    }

    @DisplayName("Should execute 1 test with correct answer")
    @Test
    void shouldExecuteOneTestWithCorrectAnswer() {
        List<Question> questions = createQuestions(1, true, false);
        mockDependencies(questions);
        testService.executeTest();
        verifyIoServicePrintForOneQuestionsWithCorrectAnswer();
        verifyDependencies(1, 2, 1);
    }

    @DisplayName("Should execute 1 test with wrong answer")
    @Test
    void shouldExecuteOneTestWithWrongAnswer() {
        List<Question> questions = createQuestions(1, false, true);
        mockDependencies(questions);
        given(correctAnswerHolder.getCorrectAnswer()).willReturn(questions.get(0).answers().get(1));
        testService.executeTest();
        verifyIoServicePrintForOneQuestionsWithWrongAnswer();
        verifyDependencies(1, 2, 1);
    }

    @DisplayName("Should throw QuestionValidationException with no questions found")
    @Test
    void shouldThrowQuestionValidationExceptionWithNoQuestionsFound() {
        List<Question> questions = createQuestions(0, true, false);
        given(questionDao.findAll()).willReturn(questions);
        assertThatThrownBy(() -> testService.executeTest())
                .isInstanceOf(QuestionValidationException.class)
                .hasMessageContaining("No questions found");
    }

    @DisplayName("Should throw QuestionValidationException with no correct answer for question")
    @Test
    void shouldThrowQuestionValidationExceptionWithNoCorrectAnswerForQuestion() {
        List<Question> questions = createQuestions(1, false, false);
        given(questionDao.findAll()).willReturn(questions);
        given(correctAnswerHolder.isEmpty()).willReturn(true);
        assertThatThrownBy(() -> testService.executeTest())
                .isInstanceOf(QuestionValidationException.class)
                .hasMessageContaining("Correct answer not found for question: question1");
    }

    private List<Question> createQuestions(int count, boolean isFirstAnswerIsCorrect, boolean isSecondAnswerIsCorrect) {
        List<Question> questions = new ArrayList<>(count);
        for (int i = 0; i < count; ++i) {
            Answer answer1 = new Answer("option1", isFirstAnswerIsCorrect);
            Answer answer2 = new Answer("option2", isSecondAnswerIsCorrect);
            List<Answer> answers = List.of(answer1, answer2);
            Question question = new Question("question" + (i + 1), answers);
            questions.add(question);
        }
        return questions;
    }

    private void mockDependencies(List<Question> questions) {
        given(questionDao.findAll()).willReturn(questions);
        given(correctAnswerHolder.isEmpty()).willReturn(false);
        given(ioService.getInt()).willReturn(1);
    }

    private void verifyDependencies(int ioServiceGetIntTimes,
                                    int holdAnswerIfCorrectTimes, int correctAnswerHolderIsEmptyTimes) {
        verify(ioService, times(ioServiceGetIntTimes)).getInt();
        verify(questionDao, times(1)).findAll();
        verify(correctAnswerHolder, times(holdAnswerIfCorrectTimes)).holdAnswerIfCorrect(any(), anyInt());
        verify(correctAnswerHolder, times(correctAnswerHolderIsEmptyTimes)).isEmpty();
    }

    private void verifyIoServicePrintForTwoQuestions() {
        verify(ioService, times(5)).printLine(lineArgumentCaptor.capture());
        List<String> printedLines = lineArgumentCaptor.getAllValues();
        assertThat(printedLines.get(0)).isEqualTo("");
        assertThat(printedLines.get(1)).isEqualTo("question1");
        assertThat(printedLines.get(2)).isEqualTo("Correct!\n");
        assertThat(printedLines.get(3)).isEqualTo("question2");
        assertThat(printedLines.get(4)).isEqualTo("Correct!\n");

        verify(ioService, times(8)).printFormattedLine(patternArgumentCaptor.capture(), argsArgumentCaptor.capture());
        List<String> patterns = patternArgumentCaptor.getAllValues();
        assertThat(patterns.get(0)).isEqualTo("Please answer the questions below%n");
        assertThat(List.of(patterns.get(1), patterns.get(2), patterns.get(4), patterns.get(5)))
                .containsOnly("%d. %s");
        assertThat(List.of(patterns.get(3), patterns.get(6)))
                .containsOnly("Choose the correct answer number from 1 to %s: ");
        assertThat(patterns.get(7)).isEqualTo("Test completed. Correct answers: %s/%s");
        List<Object[]> args = argsArgumentCaptor.getAllValues();
        assertThat(args.get(0)).hasSize(0);
        for (int index : List.of(1, 2, 4, 5)) {
            assertThat(args.get(index)[0]).isInstanceOf(Integer.class);
            assertThat((String) args.get(index)[1]).startsWith("option");
        }
        for (int index : List.of(3, 6)) {
            assertThat(args.get(index)[0]).isEqualTo(2);
        }
        assertThat(args.get(7)[0]).isEqualTo(2);
        assertThat(args.get(7)[1]).isEqualTo(2);
    }

    private void verifyIoServicePrintForOneQuestionsWithCorrectAnswer() {
        verify(ioService, times(3)).printLine(lineArgumentCaptor.capture());
        List<String> printedLines = lineArgumentCaptor.getAllValues();
        assertThat(printedLines.get(0)).isEqualTo("");
        assertThat(printedLines.get(1)).isEqualTo("question1");
        assertThat(printedLines.get(2)).isEqualTo("Correct!\n");

        verify(ioService, times(5)).printFormattedLine(patternArgumentCaptor.capture(), argsArgumentCaptor.capture());
        List<String> patterns = patternArgumentCaptor.getAllValues();
        assertThat(patterns.get(0)).isEqualTo("Please answer the questions below%n");
        assertThat(List.of(patterns.get(1), patterns.get(2)))
                .containsOnly("%d. %s");
        assertThat(patterns.get(3)).isEqualTo("Choose the correct answer number from 1 to %s: ");
        assertThat(patterns.get(4)).isEqualTo("Test completed. Correct answers: %s/%s");
        List<Object[]> args = argsArgumentCaptor.getAllValues();
        assertThat(args.get(0)).hasSize(0);
        for (int index : List.of(1, 2)) {
            assertThat(args.get(index)[0]).isInstanceOf(Integer.class);
            assertThat((String) args.get(index)[1]).startsWith("option");
        }
        assertThat(args.get(3)[0]).isEqualTo(2);
        assertThat(args.get(4)[0]).isEqualTo(1);
        assertThat(args.get(4)[1]).isEqualTo(1);
    }

    private void verifyIoServicePrintForOneQuestionsWithWrongAnswer() {
        verify(ioService, times(2)).printLine(lineArgumentCaptor.capture());
        List<String> printedLines = lineArgumentCaptor.getAllValues();
        assertThat(printedLines.get(0)).isEqualTo("");
        assertThat(printedLines.get(1)).isEqualTo("question1");

        verify(ioService, times(6)).printFormattedLine(patternArgumentCaptor.capture(), argsArgumentCaptor.capture());
        List<String> patterns = patternArgumentCaptor.getAllValues();
        assertThat(patterns.get(0)).isEqualTo("Please answer the questions below%n");
        assertThat(List.of(patterns.get(1), patterns.get(2)))
                .containsOnly("%d. %s");
        assertThat(patterns.get(3)).isEqualTo("Choose the correct answer number from 1 to %s: ");
        assertThat(patterns.get(4)).isEqualTo("Wrong! The correct answer is: %d. %s\n");
        assertThat(patterns.get(5)).isEqualTo("Test completed. Correct answers: %s/%s");
        List<Object[]> args = argsArgumentCaptor.getAllValues();
        assertThat(args.get(0)).hasSize(0);
        for (int index : List.of(1, 2)) {
            assertThat(args.get(index)[0]).isInstanceOf(Integer.class);
            assertThat((String) args.get(index)[1]).startsWith("option");
        }
        assertThat(args.get(3)[0]).isEqualTo(2);
        assertThat(args.get(4)[0]).isEqualTo(1);
        assertThat(args.get(4)[1]).isEqualTo("option2");
        assertThat(args.get(5)[0]).isEqualTo(0);
        assertThat(args.get(5)[1]).isEqualTo(1);
    }
}