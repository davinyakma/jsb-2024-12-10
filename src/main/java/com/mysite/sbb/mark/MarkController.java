package com.mysite.sbb.mark;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/markdown")
public class MarkController {

    private final MarkService markService;

    @Autowired
    public MarkController(MarkService markService) {
        this.markService = markService;
    }

    @PostMapping("/convert")
    public String convertMarkdown(@RequestBody String markdown) {
        return markService.convertMarkdownToHtml(markdown); // 마크다운을 HTML로 변환하여 반환
    }
}