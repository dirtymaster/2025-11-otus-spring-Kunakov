package ru.otus.hw.service;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.InputMismatchException;
import java.util.Scanner;

public class StreamsIOService implements IOService {
    private final PrintStream printStream;

    private final Scanner scanner;

    public StreamsIOService(PrintStream printStream, InputStream inputStream) {
        this.printStream = printStream;
        scanner = new Scanner(inputStream);
    }

    @Override
    public void printLine(String s) {
        printStream.println(s);
    }

    @Override
    public void printFormattedLine(String s, Object... args) {
        printStream.printf(s + "%n", args);
    }

    @Override
    public int getInt() throws InputMismatchException {
        while (!scanner.hasNextInt()) {
            scanner.next();
        }
        return scanner.nextInt();
    }

    public void destroy() {
        scanner.close();
    }
}
