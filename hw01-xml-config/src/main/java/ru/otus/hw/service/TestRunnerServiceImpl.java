package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import ru.otus.hw.exceptions.QuestionReadException;
import ru.otus.hw.exceptions.QuestionValidationException;

@RequiredArgsConstructor
public class TestRunnerServiceImpl implements TestRunnerService {

    private final TestService testService;

    private final IOService ioService;

    @Override
    public void run() {
        try {
            testService.executeTest();
        } catch (QuestionReadException ex) {
            ioService.printLine("Unable to load questions.");
        } catch (QuestionValidationException ex) {
            ioService.printLine("Incorrect questions format.");
        }
    }
}
