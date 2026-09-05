package com.finance.tracker.accounts.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.finance.tracker.accounts.domain.AccountCategory;
import com.finance.tracker.accounts.domain.AccountCreateUpdateRequest;
import com.finance.tracker.accounts.domain.AccountType;
import com.finance.tracker.accounts.domain.NetworthSummary;
import com.finance.tracker.accounts.domain.dto.AccountResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AccountSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testAccountCreateUpdateRequest_DeserializationWithFrontendAliases() throws Exception {
        String json = """
                {
                    "name": "HDFC Salary Account",
                    "type": "SAVINGS",
                    "balance": 25000.50,
                    "institution": "HDFC Bank",
                    "lastFour": "9876",
                    "currency": "INR",
                    "category": "ASSET"
                }
                """;

        AccountCreateUpdateRequest req = objectMapper.readValue(json, AccountCreateUpdateRequest.class);

        assertEquals("HDFC Salary Account", req.name());
        assertEquals("HDFC Salary Account", req.accountName());
        assertEquals(AccountType.SAVINGS, req.accountType());
        assertEquals("HDFC Bank", req.institution());
        assertEquals("9876", req.lastFour());
        assertEquals(new BigDecimal("25000.50"), req.balance());
        assertEquals(AccountCategory.ASSET, req.category());
    }

    @Test
    void testAccountResponse_SerializationContainsBothStandardAndClientFields() throws Exception {
        AccountResponse response = new AccountResponse(
                "acc-123",
                "ICICI Credit Card",
                AccountType.CREDIT_CARD,
                "4321",
                "ICICI Bank",
                "INR",
                LocalDate.now(),
                null,
                new BigDecimal("5000.00"),
                "15",
                "5",
                new BigDecimal("50000.00"),
                null,
                true,
                false,
                LocalDateTime.now(),
                null,
                "Personal card",
                AccountCategory.LIABILITY,
                false
        );

        String json = objectMapper.writeValueAsString(response);

        // Client fields
        assertTrue(json.contains("\"name\":\"ICICI Credit Card\""));
        assertTrue(json.contains("\"type\":\"CREDIT_CARD\""));
        assertTrue(json.contains("\"accountNumber\":\"4321\""));
        assertTrue(json.contains("\"institution\":\"ICICI Bank\""));
        assertTrue(json.contains("\"isActive\":true"));
        assertTrue(json.contains("\"balance\":5000"));
        assertTrue(json.contains("\"availableCredit\":45000"));

        // Backend legacy fields
        assertTrue(json.contains("\"accountName\":\"ICICI Credit Card\""));
        assertTrue(json.contains("\"accountType\":\"CREDIT_CARD\""));
        assertTrue(json.contains("\"lastFour\":\"4321\""));
        assertTrue(json.contains("\"active\":true"));
    }

    @Test
    void testNetworthSummary_SerializationContainsFrontendFields() throws Exception {
        NetworthSummary summary = NetworthSummary.builder()
                .assets(new NetworthSummary.ValueNumber(new BigDecimal("100000.00"), 3))
                .liabilities(new NetworthSummary.ValueNumber(new BigDecimal("25000.00"), 1))
                .netWorth(new BigDecimal("75000.00"))
                .build();

        String json = objectMapper.writeValueAsString(summary);

        assertTrue(json.contains("\"totalAssets\":100000"));
        assertTrue(json.contains("\"totalLiabilities\":25000"));
        assertTrue(json.contains("\"netWorth\":75000"));
        assertTrue(json.contains("\"currency\":\"INR\""));
    }

    @Test
    void testAccountType_JsonCreatorSupport() throws Exception {
        assertEquals(AccountType.CREDIT_CARD, objectMapper.readValue("\"CREDIT CARD\"", AccountType.class));
        assertEquals(AccountType.CREDIT_CARD, objectMapper.readValue("\"credit_card\"", AccountType.class));
        assertEquals(AccountType.CREDIT_CARD, objectMapper.readValue("\"CREDIT_CARD\"", AccountType.class));
        assertEquals(AccountType.SAVINGS, objectMapper.readValue("\"SAVINGS\"", AccountType.class));
        assertEquals(AccountType.CHECKING, objectMapper.readValue("\"CHECKING\"", AccountType.class));
        assertEquals(AccountType.LOAN, objectMapper.readValue("\"loan\"", AccountType.class));
    }
}
