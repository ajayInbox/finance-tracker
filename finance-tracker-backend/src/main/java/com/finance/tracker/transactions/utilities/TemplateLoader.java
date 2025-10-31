package com.finance.tracker.transactions.utilities;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.tracker.transactions.domain.BankTemplate;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TemplateLoader {
    private static final String TEMPLATE_DIR = "/bank_templates/";
    private static final Map<String, List<BankTemplate>> CACHE = new ConcurrentHashMap<>();

    public static List<BankTemplate> load(String bankName) {
        return CACHE.computeIfAbsent(bankName, TemplateLoader::loadFromFile);
    }

    private static List<BankTemplate> loadFromFile(String bankName) {
        String fileName = bankName.toLowerCase()+"_bank" + ".json";
        try (InputStream is = TemplateLoader.class.getResourceAsStream(TEMPLATE_DIR + fileName)) {
            if (is == null) throw new RuntimeException("Template not found for bank: " + bankName);
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(is, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Error loading template for bank: " + bankName, e);
        }
    }
}
