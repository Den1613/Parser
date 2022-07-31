package com.wamisoftware.parser.service.impl;

import com.wamisoftware.parser.service.ParsingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParsingServiceImpl implements ParsingService {

    private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyMMddHHmmss");
    private final static String TAG_READ_START = "src/main/resources/tag_read_start.log";
    private final static String TAG_READ_FINISH = "src/main/resources/tag_reads_finish.log";

    @Override
    public Map<String, Long> getResults() {
        Map<String, ZonedDateTime> start = parseStart();
        Map<String, ZonedDateTime> finish = parseFinish();

        final var result = start.entrySet().stream()
                .filter(e -> finish.containsKey(e.getKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        v -> ChronoUnit.SECONDS.between(v.getValue(), finish.get(v.getKey()))));

        return result.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .sorted(Map.Entry.comparingByValue())
                .limit(10)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
    }

    private Map<String, ZonedDateTime> parseStart() {
        try (Stream<String> stream = Files.lines(Path.of(TAG_READ_START))) {
            return stream.collect(Collectors.toMap(
                    k -> k.substring(4, 16),
                    v -> convertFromUtc(v.substring(20, 32)),
                    (oldValue, newValue) -> oldValue));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, ZonedDateTime> parseFinish() {
        try (Stream<String> stream = Files.lines(Path.of(TAG_READ_FINISH))) {
            return stream.collect(Collectors.toMap(
                    k -> k.substring(4, 16),
                    v -> convertFromKiev(v.substring(20, 32)),
                    (oldValue, newValue) -> newValue));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ZonedDateTime convertFromUtc(String time) {
        return LocalDateTime
                .parse(time, FORMATTER)
                .atZone(ZoneOffset.UTC);
    }

    private ZonedDateTime convertFromKiev(String time) {
        return LocalDateTime
                .parse(time, FORMATTER)
                .atZone(ZoneId.of("Europe/Kiev"));
    }

}
