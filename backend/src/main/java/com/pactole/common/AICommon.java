package com.pactole.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pactole.exceptions.Errors;

import lombok.NoArgsConstructor;

@NoArgsConstructor 
public class AICommon {
   
    public static String extractObject(String content) {
        if (content == null) {
            throw new Errors.AiUnavailable("ai.parse.empty.response");
        }
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new Errors.AiUnavailable("ai.parse.no.json");
        }
        return content.substring(start, end + 1);
    }

    public static JsonNode parse(ObjectMapper mapper, String content) {
        try {
            return mapper.readTree(extractObject(content));
        } catch (Errors.AiUnavailable e) {
            throw e;
        } catch (Exception e) {
            throw new Errors.AiUnavailable("ai.parse.invalid.json");
        }
    }

    public static BigDecimal money(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) return null;
        if (node.isNumber()) return node.decimalValue().setScale(2, RoundingMode.HALF_UP);
        String value = node.asText().replace("€", "").replace(" ", "").replace(" ", "").replace(",", ".").strip();
        try {
            return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static LocalDate date(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) return null;
        try {
            return LocalDate.parse(node.asText().strip());
        } catch (Exception e) {
            return null;
        }
    }

    public static String text(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) return null;
        String value = node.asText().strip();
        return value.isEmpty() ? null : value;
    }

    public static List<String> strings(JsonNode node) {
        List<String> list = new ArrayList<>();
        if (node != null && node.isArray()) {
            node.forEach(n -> {
                String value = text(n);
                if (value != null) list.add(value);
            });
        }
        return list;
    }
}