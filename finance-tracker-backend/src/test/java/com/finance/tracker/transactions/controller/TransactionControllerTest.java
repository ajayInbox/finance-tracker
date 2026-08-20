package com.finance.tracker.transactions.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.tracker.transactions.domain.*;
import com.finance.tracker.transactions.domain.dtos.*;
import com.finance.tracker.transactions.domain.entities.Transaction;
import com.finance.tracker.transactions.service.TransactionBatchService;
import com.finance.tracker.transactions.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.finance.tracker.auth.service.JwtService;
import com.finance.tracker.auth.service.UserDetailsServiceImpl;

@WebMvcTest(TransactionController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransactionService transactionService;

    @MockitoBean
    private TransactionBatchService batchService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private UUID userId;
    private UUID transactionId;
    private UUID categoryId;
    private UUID accountId;
    private Authentication auth;
    private TransactionResponseDto sampleResponseDto;

    @BeforeEach
    void setUp() {
        userId = UUID.fromString("960bbe86-b62c-4171-a8e5-94c4bfd3bdb4");
        transactionId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        accountId = UUID.randomUUID();

        auth = new UsernamePasswordAuthenticationToken(userId.toString(), null, Collections.emptyList());

        sampleResponseDto = TransactionResponseDto.builder()
                .id(transactionId)
                .transactionName("Grocery Shopping")
                .amount(new BigDecimal("150.50"))
                .type("EXPENSE")
                .categoryName("Food & Groceries")
                .accountName("Main Bank Account")
                .occurredAt(LocalDateTime.now())
                .tags(List.of("groceries", "supermarket"))
                .status("CONFIRMED")
                .originalMessage("Debited Rs 150.50 at Supermarket")
                .build();
    }

    // -----------------------------------------------------
    // 1. Create Transaction (POST /api/v1/transactions)
    // -----------------------------------------------------
    @Nested
    @DisplayName("POST /api/v1/transactions")
    class CreateTransactionTests {

        @Test
        @DisplayName("Should create transaction and return 201 Created when request is valid")
        void create_ValidDto_Returns201Created() throws Exception {
            CreateTransactionRequestDto requestDto = new CreateTransactionRequestDto(
                    "Grocery Shopping",
                    new BigDecimal("150.50"),
                    "EXPENSE",
                    categoryId,
                    accountId,
                    LocalDateTime.now(),
                    "Supermarket",
                    "Weekly groceries",
                    List.of("groceries"),
                    "INR"
            );

            when(transactionService.create(any(CreateTransactionRequestDto.class), eq(userId)))
                    .thenReturn(sampleResponseDto);

            mockMvc.perform(post("/api/v1/transactions")
                            .principal(auth)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id", is(transactionId.toString())))
                    .andExpect(jsonPath("$.transactionName", is("Grocery Shopping")))
                    .andExpect(jsonPath("$.amount", is(150.50)))
                    .andExpect(jsonPath("$.type", is("EXPENSE")))
                    .andExpect(jsonPath("$.status", is("CONFIRMED")));

            verify(transactionService, times(1)).create(any(CreateTransactionRequestDto.class), eq(userId));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when request DTO fails validation (blank name & negative amount)")
        void create_InvalidDto_Returns400BadRequest() throws Exception {
            CreateTransactionRequestDto invalidDto = new CreateTransactionRequestDto(
                    "", // blank name
                    new BigDecimal("-50.00"), // negative amount
                    "EXPENSE",
                    categoryId,
                    accountId,
                    LocalDateTime.now(),
                    "Supermarket",
                    "Weekly groceries",
                    List.of("groceries"),
                    "INR"
            );

            mockMvc.perform(post("/api/v1/transactions")
                            .principal(auth)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest());

            verify(transactionService, never()).create(any(), any());
        }
    }

    // -----------------------------------------------------
    // 2. Get Single Transaction (GET /api/v1/transactions/{id})
    // -----------------------------------------------------
    @Nested
    @DisplayName("GET /api/v1/transactions/{id}")
    class GetSingleTransactionTests {

        @Test
        @DisplayName("Should return transaction and 200 OK when transaction exists")
        void getOne_ExistingId_Returns200Ok() throws Exception {
            Transaction mockTransaction = Transaction.builder()
                    .id(transactionId)
                    .transactionName("Grocery Shopping")
                    .amount(new BigDecimal("150.50"))
                    .userId(userId)
                    .build();

            when(transactionService.getTransaction(transactionId)).thenReturn(Optional.of(mockTransaction));
            when(transactionService.mapToResponseDto(mockTransaction)).thenReturn(sampleResponseDto);

            mockMvc.perform(get("/api/v1/transactions/{id}", transactionId)
                            .principal(auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(transactionId.toString())))
                    .andExpect(jsonPath("$.transactionName", is("Grocery Shopping")));

            verify(transactionService, times(1)).getTransaction(transactionId);
            verify(transactionService, times(1)).mapToResponseDto(mockTransaction);
        }

        @Test
        @DisplayName("Should return 404 Not Found when transaction does not exist")
        void getOne_NonExistingId_Returns404NotFound() throws Exception {
            when(transactionService.getTransaction(transactionId)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/v1/transactions/{id}", transactionId)
                            .principal(auth))
                    .andExpect(status().isNotFound());

            verify(transactionService, times(1)).getTransaction(transactionId);
            verify(transactionService, never()).mapToResponseDto(any());
        }
    }

    // -----------------------------------------------------
    // 3. Get All Transactions (GET /api/v1/transactions)
    // -----------------------------------------------------
    @Nested
    @DisplayName("GET /api/v1/transactions")
    class GetAllTransactionsTests {

        @Test
        @DisplayName("Version 1: Should return confirmed transactions list and 200 OK")
        void getAll_Version1_Returns200OkWithConfirmedTransactions() throws Exception {
            PageRequest expectedPageRequest = PageRequest.of(0, 50, Sort.by("occurredAt").descending());

            when(transactionService.getAll(userId, TransactionStatus.CONFIRMED, expectedPageRequest))
                    .thenReturn(List.of(sampleResponseDto));

            mockMvc.perform(get("/api/v1/transactions")
                            .param("page", "1")
                            .param("size", "50")
                            .param("version", "1")
                            .param("status", "CONFIRMED")
                            .principal(auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id", is(transactionId.toString())));

            verify(transactionService, times(1)).getAll(userId, TransactionStatus.CONFIRMED, expectedPageRequest);
        }

        @Test
        @DisplayName("Version 2: Should return transactions list via getTransactions and 200 OK")
        void getAll_Version2_Returns200OkWithTransactions() throws Exception {
            PageRequest expectedPageRequest = PageRequest.of(0, 50, Sort.by("occurredAt").descending());

            when(transactionService.getTransactions(expectedPageRequest))
                    .thenReturn(List.of(sampleResponseDto));

            mockMvc.perform(get("/api/v1/transactions")
                            .param("page", "1")
                            .param("size", "50")
                            .param("version", "2")
                            .principal(auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].transactionName", is("Grocery Shopping")));

            verify(transactionService, times(1)).getTransactions(expectedPageRequest);
        }

        @Test
        @DisplayName("Version 3: Should return draft transactions list and 200 OK")
        void getAll_Version3_Returns200OkWithDraftTransactions() throws Exception {
            PageRequest expectedPageRequest = PageRequest.of(0, 20, Sort.by("occurredAt").descending());

            when(transactionService.getAll(userId, TransactionStatus.DRAFT, expectedPageRequest))
                    .thenReturn(List.of(sampleResponseDto));

            mockMvc.perform(get("/api/v1/transactions")
                            .param("page", "1")
                            .param("size", "20")
                            .param("version", "3")
                            .principal(auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));

            verify(transactionService, times(1)).getAll(userId, TransactionStatus.DRAFT, expectedPageRequest);
        }

        @Test
        @DisplayName("Invalid Version: Should return 400 Bad Request with error message")
        void getAll_InvalidVersion_Returns400BadRequest() throws Exception {
            mockMvc.perform(get("/api/v1/transactions")
                            .param("version", "99")
                            .principal(auth))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string("Invalid API version"));
        }
    }

    // -----------------------------------------------------
    // 4. Update Transaction (PUT /api/v1/transactions/{id})
    // -----------------------------------------------------
    @Nested
    @DisplayName("PUT /api/v1/transactions/{id}")
    class UpdateTransactionTests {

        @Test
        @DisplayName("Should update transaction and return 200 OK when request is valid")
        void update_ValidDto_Returns200Ok() throws Exception {
            UpdateTransactionRequestDto updateDto = new UpdateTransactionRequestDto(
                    "Updated Grocery Shopping",
                    new BigDecimal("200.00"),
                    "EXPENSE",
                    categoryId,
                    accountId,
                    LocalDateTime.now(),
                    "Supermarket",
                    "Updated notes",
                    List.of("groceries"),
                    "INR"
            );

            TransactionResponseDto updatedResponse = TransactionResponseDto.builder()
                    .id(transactionId)
                    .transactionName("Updated Grocery Shopping")
                    .amount(new BigDecimal("200.00"))
                    .type("EXPENSE")
                    .status("CONFIRMED")
                    .build();

            when(transactionService.update(eq(userId), eq(transactionId), any(UpdateTransactionRequestDto.class)))
                    .thenReturn(updatedResponse);

            mockMvc.perform(put("/api/v1/transactions/{id}", transactionId)
                            .principal(auth)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(transactionId.toString())))
                    .andExpect(jsonPath("$.transactionName", is("Updated Grocery Shopping")))
                    .andExpect(jsonPath("$.amount", is(200.00)));

            verify(transactionService, times(1)).update(eq(userId), eq(transactionId), any(UpdateTransactionRequestDto.class));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when update DTO validation fails (null categoryId and accountId)")
        void update_InvalidDto_Returns400BadRequest() throws Exception {
            UpdateTransactionRequestDto invalidUpdateDto = new UpdateTransactionRequestDto(
                    "Updated Grocery",
                    new BigDecimal("200.00"),
                    "EXPENSE",
                    null, // categoryId required
                    null, // accountId required
                    LocalDateTime.now(),
                    "Supermarket",
                    "Notes",
                    List.of(),
                    "INR"
            );

            mockMvc.perform(put("/api/v1/transactions/{id}", transactionId)
                            .principal(auth)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidUpdateDto)))
                    .andExpect(status().isBadRequest());

            verify(transactionService, never()).update(any(), any(), any());
        }
    }

    // -----------------------------------------------------
    // 5. Delete Transaction (DELETE /api/v1/transactions/{id})
    // -----------------------------------------------------
    @Nested
    @DisplayName("DELETE /api/v1/transactions/{id}")
    class DeleteTransactionTests {

        @Test
        @DisplayName("Should delete transaction and return 204 No Content when transaction exists")
        void delete_ExistingId_Returns204NoContent() throws Exception {
            Transaction mockTransaction = Transaction.builder()
                    .id(transactionId)
                    .userId(userId)
                    .build();

            when(transactionService.getTransaction(transactionId)).thenReturn(Optional.of(mockTransaction));

            mockMvc.perform(delete("/api/v1/transactions/{id}", transactionId)
                            .principal(auth))
                    .andExpect(status().isNoContent());

            verify(transactionService, times(1)).getTransaction(transactionId);
            verify(transactionService, times(1)).deleteTransaction(userId, mockTransaction);
        }

        @Test
        @DisplayName("Should return 404 Not Found when transaction to delete does not exist")
        void delete_NonExistingId_Returns404NotFound() throws Exception {
            when(transactionService.getTransaction(transactionId)).thenReturn(Optional.empty());

            mockMvc.perform(delete("/api/v1/transactions/{id}", transactionId)
                            .principal(auth))
                    .andExpect(status().isNotFound());

            verify(transactionService, times(1)).getTransaction(transactionId);
            verify(transactionService, never()).deleteTransaction(any(), any());
        }
    }

    // -----------------------------------------------------
    // 6. Daily Average (POST /api/v1/transactions/avg-daily)
    // -----------------------------------------------------
    @Nested
    @DisplayName("POST /api/v1/transactions/avg-daily")
    class GetDailyAverageTests {

        @Test
        @DisplayName("Should return daily average calculation and 200 OK")
        void getDailyAverage_ValidRequest_Returns200Ok() throws Exception {
            SearchRequest searchRequest = new SearchRequest("2026-01-01", "2026-01-31", null, null, null);

            TransactionsAverage average = TransactionsAverage.builder()
                    .days(31)
                    .averageDailyExpense(45.50)
                    .build();

            when(transactionService.search(any(SearchRequest.class))).thenReturn(average);

            mockMvc.perform(post("/api/v1/transactions/avg-daily")
                            .principal(auth)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(searchRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.days", is(31)))
                    .andExpect(jsonPath("$.averageDailyExpense", is(45.50)));

            verify(transactionService, times(1)).search(any(SearchRequest.class));
        }
    }

    // -----------------------------------------------------
    // 7. Monthly Expense Analysis (POST /api/v1/transactions/analysis)
    // -----------------------------------------------------
    @Nested
    @DisplayName("POST /api/v1/transactions/analysis")
    class GetExpenseAnalysisTests {

        @Test
        @DisplayName("Should return monthly expense report and 200 OK")
        void getExpenseAnalysis_ValidRequest_Returns200Ok() throws Exception {
            ExpenseReportRequest reportRequest = new ExpenseReportRequest(
                    LocalDateTime.now().minusMonths(1),
                    LocalDateTime.now(),
                    "EXPENSE"
            );

            MonthlyExpenseResponse mockResponse = new MonthlyExpenseResponse(
                    reportRequest.start(),
                    reportRequest.end(),
                    "INR",
                    new BigDecimal("1500.00"),
                    List.of()
            );

            when(transactionService.getExpenseReport(eq(userId), any(ExpenseReportRequest.class)))
                    .thenReturn(mockResponse);

            mockMvc.perform(post("/api/v1/transactions/analysis")
                            .principal(auth)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reportRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.currency", is("INR")))
                    .andExpect(jsonPath("$.total", is(1500.00)));

            verify(transactionService, times(1)).getExpenseReport(eq(userId), any(ExpenseReportRequest.class));
        }
    }

    // -----------------------------------------------------
    // 8. Export Messages (POST /api/v1/transactions/export-messages)
    // -----------------------------------------------------
    @Nested
    @DisplayName("POST /api/v1/transactions/export-messages")
    class ExportMessagesTests {

        @Test
        @DisplayName("Should send SMS requests to queue and return 200 OK")
        void exportMessages_ValidList_Returns200Ok() throws Exception {
            List<SmsRequest> messages = List.of(
                    new SmsRequest("SMS-1", "BANK-INF", "Debited Rs 100", System.currentTimeMillis())
            );

            mockMvc.perform(post("/api/v1/transactions/export-messages")
                            .principal(auth)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(messages)))
                    .andExpect(status().isOk());

            verify(transactionService, times(1)).exportMessagesSendToQueue(any());
        }
    }

    // -----------------------------------------------------
    // 9. Parse SMS (POST /api/v1/transactions/parse)
    // -----------------------------------------------------
    @Nested
    @DisplayName("POST /api/v1/transactions/parse")
    class ParseSmsTests {

        @Test
        @DisplayName("Should parse SMS request and return 200 OK")
        void parse_ValidSms_Returns200Ok() throws Exception {
            SmsRequest smsRequest = new SmsRequest("SMS-100", "BANK-ALRT", "Spent 500 INR at Store", System.currentTimeMillis());

            ParsedTxnResponse response = ParsedTxnResponse.builder()
                    .status("SUCCESS")
                    .uniqueIdentifier("SMS-100")
                    .build();

            when(transactionService.parse(eq(userId), any(SmsRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/v1/transactions/parse")
                            .principal(auth)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(smsRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status", is("SUCCESS")))
                    .andExpect(jsonPath("$.uniqueIdentifier", is("SMS-100")));

            verify(transactionService, times(1)).parse(eq(userId), any(SmsRequest.class));
        }
    }

    // -----------------------------------------------------
    // 10. Batch Update (PUT /api/v1/transactions/batch)
    // -----------------------------------------------------
    @Nested
    @DisplayName("PUT /api/v1/transactions/batch")
    class BatchUpdateTests {

        @Test
        @DisplayName("Should process batch update and return 200 OK")
        void batchUpdate_ValidList_Returns200Ok() throws Exception {
            BatchUpdateTransactionRequestDto requestDto = new BatchUpdateTransactionRequestDto(
                    transactionId,
                    "Updated Transaction",
                    new BigDecimal("100.00"),
                    "EXPENSE",
                    categoryId,
                    accountId,
                    LocalDateTime.now(),
                    null,
                    "Merchant",
                    "Notes",
                    List.of("tag1"),
                    "INR"
            );

            mockMvc.perform(put("/api/v1/transactions/batch")
                            .principal(auth)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(List.of(requestDto))))
                    .andExpect(status().isOk());

            verify(batchService, times(1)).batchConfirmAndUpdate(eq(userId), any());
        }
    }

    // -----------------------------------------------------
    // 11. Batch Delete Drafts (POST /api/v1/transactions/drafts/batch-delete)
    // -----------------------------------------------------
    @Nested
    @DisplayName("POST /api/v1/transactions/drafts/batch-delete")
    class BatchDeleteDraftsTests {

        @Test
        @DisplayName("Should batch delete draft transactions and return 204 No Content when list is non-empty")
        void batchDelete_ValidList_Returns204NoContent() throws Exception {
            List<UUID> idsToDelete = List.of(transactionId, UUID.randomUUID());

            mockMvc.perform(post("/api/v1/transactions/drafts/batch-delete")
                            .principal(auth)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(idsToDelete)))
                    .andExpect(status().isNoContent());

            verify(batchService, times(1)).deleteDraftTransactions(idsToDelete);
        }

        @Test
        @DisplayName("Should return 400 Bad Request when transaction IDs list is empty")
        void batchDelete_EmptyList_Returns400BadRequest() throws Exception {
            mockMvc.perform(post("/api/v1/transactions/drafts/batch-delete")
                            .principal(auth)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[]"))
                    .andExpect(status().isBadRequest());

            verify(batchService, never()).deleteDraftTransactions(any());
        }
    }
}
