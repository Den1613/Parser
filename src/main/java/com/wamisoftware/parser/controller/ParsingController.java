package com.wamisoftware.parser.controller;

import com.wamisoftware.parser.service.ParsingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ParsingController {

    private final ParsingService parsingService;

    @GetMapping("/get")
    public Map<String, Long> getResults() {
        return parsingService.getResults();
    }

}
