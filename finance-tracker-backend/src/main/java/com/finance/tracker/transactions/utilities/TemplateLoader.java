package com.finance.tracker.transactions.utilities;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.tracker.transactions.domain.BankTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TemplateLoader {

    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, List<BankTemplate>> cache = new ConcurrentHashMap<>();

    // Configurable directory path
    @Value("${app.templates.directory:/bank_templates/}")
    private String templateDir;

    public List<BankTemplate> load(String bankName) {
        return cache.computeIfAbsent(bankName.toLowerCase(), this::loadFromFile);
    }

    private List<BankTemplate> loadFromFile(String bankName) {
        String fileName = templateDir + bankName + "_bank.json";

        try (InputStream is = getClass().getResourceAsStream(fileName)) {
            if (is == null) {
                // Instead of a RuntimeException, you could return an empty list
                // and log a warning to keep the parser running.
                return List.of();
            }

            List<BankTemplate> templates = mapper.readValue(is, new TypeReference<>() {});
            templates.forEach(BankTemplate::compile);
            return templates;
        } catch (Exception e) {
            throw new RuntimeException("Failed loading templates for " + bankName, e);
        }
    }

    // Optional: Method to clear cache if you update files without restarting
    public void refreshCache() {
        cache.clear();
    }
}