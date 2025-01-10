package com.mysite.sbb.mark;

import com.mysite.sbb.CommonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MarkService {

    private final CommonUtil commonUtil;

    @Autowired
    public MarkService(CommonUtil commonUtil) {
        this.commonUtil = commonUtil;
    }

    public String convertMarkdownToHtml(String markdown) {
        return commonUtil.markdown(markdown); // CommonUtil을 통해 마크다운을 HTML로 변환
    }
}