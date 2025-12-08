package com.cursorpos.admin.controller;

import com.cursorpos.admin.dto.*;
import com.cursorpos.admin.entity.LoyaltyTransaction.LoyaltyTransactionType;
import com.cursorpos.admin.service.LoyaltyService;
import com.cursorpos.shared.dto.PagedResponse;
import com.cursorpos.shared.exception.GlobalExceptionHandler;
import com.cursorpos.shared.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller unit tests for LoyaltyController.
 * Uses standalone MockMvc setup for testing controller logic in isolation.
 * Security is tested separately via integration tests.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoyaltyController Tests")
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods", "null" })
class LoyaltyControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private LoyaltyService loyaltyService;

    private LoyaltyController loyaltyController;

    private UUID tierId;
    private UUID transactionId;
    private UUID customerId;
    private LoyaltyTierRequest tierRequest;
    private LoyaltyTierResponse tierResponse;
    private LoyaltyTransactionRequest transactionRequest;
    private LoyaltyTransactionResponse transactionResponse;

    @BeforeEach
    void setUp() {
        // Create controller with mocked service
        loyaltyController = new LoyaltyController(loyaltyService);

        // Setup standalone MockMvc with the controller, exception handler, and pageable
        // support
        mockMvc = MockMvcBuilders.standaloneSetup(loyaltyController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        tierId = UUID.randomUUID();
        transactionId = UUID.randomUUID();
        customerId = UUID.randomUUID();

        tierRequest = LoyaltyTierRequest.builder()
                .code("GOLD")
                .name("Gold Tier")
                .minPoints(2000)
                .discountPercentage(BigDecimal.TEN)
                .pointsMultiplier(BigDecimal.valueOf(2.0))
                .color("#FFD700")
                .build();

        tierResponse = LoyaltyTierResponse.builder()
                .id(tierId)
                .code("GOLD")
                .name("Gold Tier")
                .minPoints(2000)
                .discountPercentage(BigDecimal.TEN)
                .pointsMultiplier(BigDecimal.valueOf(2.0))
                .color("#FFD700")
                .build();

        transactionRequest = LoyaltyTransactionRequest.builder()
                .customerId(customerId)
                .transactionType(LoyaltyTransactionType.EARN)
                .pointsChange(100)
                .description("Points earned from purchase")
                .build();

        transactionResponse = LoyaltyTransactionResponse.builder()
                .id(transactionId)
                .customerId(customerId)
                .transactionType(LoyaltyTransactionType.EARN)
                .points(100)
                .balanceAfter(1100)
                .referenceType("ORDER")
                .description("Points earned from purchase")
                .build();
    }

    // ============ Tier Endpoints ============

    @Nested
    @DisplayName("POST /loyalty/tiers")
    class CreateTierTests {

        @Test
        @DisplayName("Should create tier with valid request")
        void shouldCreateTierWithValidRequest() throws Exception {
            when(loyaltyService.createTier(any(LoyaltyTierRequest.class))).thenReturn(tierResponse);

            mockMvc.perform(post("/loyalty/tiers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(tierRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.code").value("GOLD"));
        }
    }

    @Nested
    @DisplayName("GET /loyalty/tiers/{id}")
    class GetTierByIdTests {

        @Test
        @DisplayName("Should return tier when found")
        void shouldReturnTierWhenFound() throws Exception {
            when(loyaltyService.getTierById(tierId)).thenReturn(tierResponse);

            mockMvc.perform(get("/loyalty/tiers/{id}", tierId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(tierId.toString()));
        }

        @Test
        @DisplayName("Should return 404 when not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(loyaltyService.getTierById(tierId))
                    .thenThrow(new ResourceNotFoundException("Tier not found"));

            mockMvc.perform(get("/loyalty/tiers/{id}", tierId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /loyalty/tiers/code/{code}")
    class GetTierByCodeTests {

        @Test
        @DisplayName("Should return tier when found by code")
        void shouldReturnTierWhenFoundByCode() throws Exception {
            when(loyaltyService.getTierByCode("GOLD")).thenReturn(tierResponse);

            mockMvc.perform(get("/loyalty/tiers/code/{code}", "GOLD"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.code").value("GOLD"));
        }
    }

    @Nested
    @DisplayName("GET /loyalty/tiers")
    class GetAllTiersTests {

        @Test
        @DisplayName("Should return paginated tiers")
        void shouldReturnPaginatedTiers() throws Exception {
            PagedResponse<LoyaltyTierResponse> pagedResponse = PagedResponse.<LoyaltyTierResponse>builder()
                    .content(List.of(tierResponse))
                    .pageNumber(0)
                    .pageSize(10)
                    .totalElements(1)
                    .totalPages(1)
                    .first(true)
                    .last(true)
                    .hasNext(false)
                    .hasPrevious(false)
                    .numberOfElements(1)
                    .build();
            when(loyaltyService.getAllTiers(any())).thenReturn(pagedResponse);

            mockMvc.perform(get("/loyalty/tiers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].code").value("GOLD"));
        }
    }

    @Nested
    @DisplayName("GET /loyalty/tiers/ordered")
    class GetAllTiersOrderedTests {

        @Test
        @DisplayName("Should return tiers ordered by min points")
        void shouldReturnTiersOrderedByMinPoints() throws Exception {
            when(loyaltyService.getAllTiersOrdered()).thenReturn(List.of(tierResponse));

            mockMvc.perform(get("/loyalty/tiers/ordered"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].code").value("GOLD"));
        }
    }

    @Nested
    @DisplayName("PUT /loyalty/tiers/{id}")
    class UpdateTierTests {

        @Test
        @DisplayName("Should update tier successfully")
        void shouldUpdateTierSuccessfully() throws Exception {
            when(loyaltyService.updateTier(eq(tierId), any(LoyaltyTierRequest.class))).thenReturn(tierResponse);

            mockMvc.perform(put("/loyalty/tiers/{id}", tierId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(tierRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("DELETE /loyalty/tiers/{id}")
    class DeleteTierTests {

        @Test
        @DisplayName("Should delete tier successfully")
        void shouldDeleteTierSuccessfully() throws Exception {
            doNothing().when(loyaltyService).deleteTier(tierId);

            mockMvc.perform(delete("/loyalty/tiers/{id}", tierId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    // ============ Transaction Endpoints ============

    @Nested
    @DisplayName("POST /loyalty/transactions")
    class CreateTransactionTests {

        @Test
        @DisplayName("Should create transaction with valid request")
        void shouldCreateTransactionWithValidRequest() throws Exception {
            when(loyaltyService.createTransaction(any(LoyaltyTransactionRequest.class)))
                    .thenReturn(transactionResponse);

            mockMvc.perform(post("/loyalty/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(transactionRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.points").value(100));
        }
    }

    @Nested
    @DisplayName("GET /loyalty/transactions/{id}")
    class GetTransactionByIdTests {

        @Test
        @DisplayName("Should return transaction when found")
        void shouldReturnTransactionWhenFound() throws Exception {
            when(loyaltyService.getTransactionById(transactionId)).thenReturn(transactionResponse);

            mockMvc.perform(get("/loyalty/transactions/{id}", transactionId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(transactionId.toString()));
        }

        @Test
        @DisplayName("Should return 404 when not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(loyaltyService.getTransactionById(transactionId))
                    .thenThrow(new ResourceNotFoundException("Transaction not found"));

            mockMvc.perform(get("/loyalty/transactions/{id}", transactionId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /loyalty/transactions/customer/{customerId}")
    class GetTransactionsByCustomerTests {

        @Test
        @DisplayName("Should return transactions by customer")
        void shouldReturnTransactionsByCustomer() throws Exception {
            PagedResponse<LoyaltyTransactionResponse> pagedResponse = PagedResponse
                    .<LoyaltyTransactionResponse>builder()
                    .content(List.of(transactionResponse))
                    .pageNumber(0)
                    .pageSize(10)
                    .totalElements(1)
                    .totalPages(1)
                    .first(true)
                    .last(true)
                    .hasNext(false)
                    .hasPrevious(false)
                    .numberOfElements(1)
                    .build();
            when(loyaltyService.getTransactionsByCustomer(eq(customerId), any())).thenReturn(pagedResponse);

            mockMvc.perform(get("/loyalty/transactions/customer/{customerId}", customerId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("GET /loyalty/transactions")
    class GetAllTransactionsTests {

        @Test
        @DisplayName("Should return all transactions paginated")
        void shouldReturnAllTransactionsPaginated() throws Exception {
            PagedResponse<LoyaltyTransactionResponse> pagedResponse = PagedResponse
                    .<LoyaltyTransactionResponse>builder()
                    .content(List.of(transactionResponse))
                    .pageNumber(0)
                    .pageSize(10)
                    .totalElements(1)
                    .totalPages(1)
                    .first(true)
                    .last(true)
                    .hasNext(false)
                    .hasPrevious(false)
                    .numberOfElements(1)
                    .build();
            when(loyaltyService.getAllTransactions(any())).thenReturn(pagedResponse);

            mockMvc.perform(get("/loyalty/transactions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
