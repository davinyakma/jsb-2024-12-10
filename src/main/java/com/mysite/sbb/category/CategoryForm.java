package com.mysite.sbb.category;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryForm {

    @NotEmpty(message = "이름은 필수항목입니다.")
    private String name;
}
