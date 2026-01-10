package com.finance.tracker.transactions.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.regex.Pattern;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BankTemplate {

    private String name;
    private String pattern;
    private List<String> fields;

    @JsonIgnore
    private Pattern compiledPattern;

    @PostConstruct
    public void compile() {
        this.compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
    }

}