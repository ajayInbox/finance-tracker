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
        return CACHE.computeIfAbsent(bankName.toLowerCase(), TemplateLoader::loadFromFile);
    }

    private static List<BankTemplate> loadFromFile(String bankName) {
        String file = bankName + "_bank.json";

        try (InputStream is =
                     TemplateLoader.class.getResourceAsStream(TEMPLATE_DIR + file)) {

            if (is == null) {
                throw new RuntimeException("No template for bank: " + bankName);
            }

            ObjectMapper mapper = new ObjectMapper();
            List<BankTemplate> templates =
                    mapper.readValue(is, new TypeReference<>() {});

            templates.forEach(BankTemplate::compile);
            return templates;

        } catch (Exception e) {
            throw new RuntimeException("Failed loading templates for " + bankName, e);
        }
    }
}
