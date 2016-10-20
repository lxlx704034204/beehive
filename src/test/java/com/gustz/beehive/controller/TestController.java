package com.gustz.beehive.controller;

import com.gustz.beehive.config.url.GetAccessUrl;
import com.gustz.beehive.model.UserDto;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * Test controller
 *
 * @author zhangzhenfeng
 * @since 2015-12-23
 */
@RestController
@RequestMapping("/test/*")
public class TestController {

    @RequestMapping(value = "demo", method = RequestMethod.GET)
    public String demo() {
        System.out.println("demo...111");
        return "ok";
    }

    @RequestMapping(value = "demo2", method = RequestMethod.GET)
    public String demo2() {
        System.out.println("demo2...222");
        return "ok2";
    }

    @GetAccessUrl
    @RequestMapping(value = "demo3", method = RequestMethod.GET)
    public String demo3(@Valid UserDto dto, BindingResult result) {
        System.out.println("demo3...333");
        if (result.hasErrors()) {
            System.out.println("demo3 is errors");
        }
        List<FieldError> list = result.getFieldErrors();
        for (FieldError err : list) {
            System.out.println("demo3-field=" + err.getField() + ",msg=" + err.getDefaultMessage());
        }
        return "ok3";
    }

    @RequestMapping(value = "demo4", method = RequestMethod.GET)
    public String demo4() {
        System.out.println("demo4...444");
        try {
            UserDto dto = new UserDto();
            dto.setId(1);
            //
        } catch (Exception e) {
            System.err.println("demo4: exception");
        }
        return "ok4";
    }
}
