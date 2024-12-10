package com.mysite.sbb;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MainController {
    //Get http://localhost:8080/
    @GetMapping("/")
    @ResponseBody
    public int home() {
        System.out.println("home");
        return 22;
    }

    //Get http://localhost:8080/about
    @GetMapping("/about")
    @ResponseBody
    public String about() {
        System.out.println("about");
        return "안녕하세요.";
    }

    public void contact() {
        System.out.println("contact");
    }


}
