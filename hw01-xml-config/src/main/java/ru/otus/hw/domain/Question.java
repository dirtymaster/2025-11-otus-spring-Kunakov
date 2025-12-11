package ru.otus.hw.domain;

import com.opencsv.bean.CsvBindByPosition;

import java.util.List;

public record Question(
        @CsvBindByPosition(position = 0)
        String text,

        @CsvBindByPosition(position = 1)
        List<Answer> answers) {
}
