package ru.otus.hw.dao;

import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.dao.dto.QuestionDto;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RequiredArgsConstructor
public class CsvQuestionDao implements QuestionDao {
    private final TestFileNameProvider fileNameProvider;

    @Override
    public List<Question> findAll() {
        String testFileName = fileNameProvider.getTestFileName();
        try (InputStream resource = getClass().getClassLoader().getResourceAsStream(testFileName)) {
            if (resource == null) {
                throw new QuestionReadException("File not found: " + testFileName);
            }
            List<QuestionDto> questionDtoList = readFromCsv(resource);
            return convertToDomainObjects(questionDtoList);

        } catch (IOException ex) {
            throw new QuestionReadException("Can't read test file", ex);
        }
    }

    private List<QuestionDto> readFromCsv(InputStream resource) {
        Reader reader = new InputStreamReader(resource, StandardCharsets.UTF_8);
        return new CsvToBeanBuilder<QuestionDto>(reader)
                .withType(QuestionDto.class)
                .withSkipLines(1)
                .withSeparator(';')
                .build()
                .parse();
    }

    private List<Question> convertToDomainObjects(List<QuestionDto> questionDtoList) {
        return questionDtoList.stream()
                .map(QuestionDto::toDomainObject)
                .toList();
    }
}
