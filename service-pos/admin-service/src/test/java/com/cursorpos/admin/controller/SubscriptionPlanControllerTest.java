package com.cursorpos.admin.controller;

import com.cursorpos.admin.dto.SubscriptionPlanRequest;
import com.cursorpos.admin.dto.SubscriptionPlanResponse;
import com.cursorpos.admin.service.SubscriptionPlanService;
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
 * Controller unit tests for SubscriptionPlanController.
 * Uses standalone MockMvc setup for testing controller logic in isolation.
 * Security is tested separately via integration tests.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SubscriptionPlanController Tests")
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods", "null" })
class SubscriptionPlanControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private SubscriptionPlanService subscriptionPlanService;

    private SubscriptionPlanController subscriptionPlanController;

    private UUID planId;
    private SubscriptionPlanRequest request;
    private SubscriptionPlanResponse response;

    @BeforeEach
    void setUp() {
        subscriptionPlanController = new SubscriptionPlanController(subscriptionPlanService);
        mockMvc = MockMvcBuilders.standaloneSetup(subscriptionPlanController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        planId = UUID.randomUUID();

        request = SubscriptionPlanRequest.builder()
                .code("PREMIUM")
                .name("Premium Plan")
                .description("Premium subscription with advanced features")
                .maxUsers(100)
                .maxStores(50)
                .maxProducts(500)
                .priceMonthly(BigDecimal.valueOf(99.99))
                .priceYearly(BigDecimal.valueOf(999.99))
                .features(List.of("Advanced Analytics", "Priority Support"))
                .displayOrder(2)
                .build();

        response = SubscriptionPlanResponse.builder()
                .id(planId)
                .code("PREMIUM")
                .name("Premium Plan")
                .description("Premium subscription with advanced features")
                .maxUsers(100)
                .maxStores(50)
                .maxProducts(500)
                .priceMonthly(BigDecimal.valueOf(99.99))
                .priceYearly(BigDecimal.valueOf(999.99))
                .features("[\"Advanced Analytics\", \"Priority Support\"]")
                .isActive(true)
                .displayOrder(2)
                .build();
    }

    @Nested
    @DisplayName("POST /subscription-plans")
    class CreatePlanTests {

        @Test
        @DisplayName("Should create plan with valid request")
        void shouldCreatePlanWithValidRequest() throws Exception {
            when(subscriptionPlanService.createPlan(any(SubscriptionPlanRequest.class))).thenReturn(response);

            mockMvc.perform(post("/subscription-plans")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.code").value("PREMIUM"));
        }
    }

    @Nested
    @DisplayName("GET /subscription-plans/{id}")
    class GetPlanByIdTests {

        @Test
        @DisplayName("Should return plan when found")
        void shouldReturnPlanWhenFound() throws Exception {
            when(subscriptionPlanService.getPlanById(planId)).thenReturn(response);

            mockMvc.perform(get("/subscription-plans/{id}", planId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(planId.toString()));
        }

        @Test
        @DisplayName("Should return 404 when not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(subscriptionPlanService.getPlanById(planId))
                    .thenThrow(new ResourceNotFoundException("Plan not found"));

            mockMvc.perform(get("/subscription-plans/{id}", planId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /subscription-plans/code/{code}")
    class GetPlanByCodeTests {

        @Test
        @DisplayName("Should return plan when found by code")
        void shouldReturnPlanWhenFoundByCode() throws Exception {
            when(subscriptionPlanService.getPlanByCode("PREMIUM")).thenReturn(response);

            mockMvc.perform(get("/subscription-plans/code/{code}", "PREMIUM"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.code").value("PREMIUM"));
        }
    }

    @Nested
    @DisplayName("GET /subscription-plans")
    class GetAllPlansTests {

        @Test
        @DisplayName("Should return paginated plans")
        void shouldReturnPaginatedPlans() throws Exception {
            PagedResponse<SubscriptionPlanResponse> pagedResponse = PagedResponse.<SubscriptionPlanResponse>builder()
                    .content(List.of(response))
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
            when(subscriptionPlanService.getAllPlans(any())).thenReturn(pagedResponse);

            mockMvc.perform(get("/subscription-plans"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].code").value("PREMIUM"));
        }
    }

    @Nested
    @DisplayName("GET /subscription-plans/active")
    class GetActivePlansTests {

        @Test
        @DisplayName("Should return active plans")
        void shouldReturnActivePlans() throws Exception {
            when(subscriptionPlanService.getActivePlans()).thenReturn(List.of(response));

            mockMvc.perform(get("/subscription-plans/active"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].code").value("PREMIUM"));
        }
    }

    @Nested
    @DisplayName("PUT /subscription-plans/{id}")
    class UpdatePlanTests {

        @Test
        @DisplayName("Should update plan successfully")
        void shouldUpdatePlanSuccessfully() throws Exception {
            when(subscriptionPlanService.updatePlan(eq(planId), any(SubscriptionPlanRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(put("/subscription-plans/{id}", planId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("DELETE /subscription-plans/{id}")
    class DeletePlanTests {

        @Test
        @DisplayName("Should delete plan successfully")
        void shouldDeletePlanSuccessfully() throws Exception {
            doNothing().when(subscriptionPlanService).deletePlan(planId);

            mockMvc.perform(delete("/subscription-plans/{id}", planId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("POST /subscription-plans/{id}/activate")
    class ActivatePlanTests {

        @Test
        @DisplayName("Should activate plan successfully")
        void shouldActivatePlanSuccessfully() throws Exception {
            when(subscriptionPlanService.activatePlan(planId)).thenReturn(response);

            mockMvc.perform(post("/subscription-plans/{id}/activate", planId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("POST /subscription-plans/{id}/deactivate")
    class DeactivatePlanTests {

        @Test
        @DisplayName("Should deactivate plan successfully")
        void shouldDeactivatePlanSuccessfully() throws Exception {
            when(subscriptionPlanService.deactivatePlan(planId)).thenReturn(response);

            mockMvc.perform(post("/subscription-plans/{id}/deactivate", planId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
