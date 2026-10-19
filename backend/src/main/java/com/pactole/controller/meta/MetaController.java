package com.pactole.controller.meta;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pactole.enums.Category;

@RestController
@RequestMapping("/api/meta")
public class MetaController {

    @GetMapping("/categories")
    public List<CategoryDto> categories() {
        return Arrays.stream(Category.values()).map(c -> new CategoryDto(c.name(), c.label())).toList();
    }

    record CategoryDto(String value, String label) {}
}
