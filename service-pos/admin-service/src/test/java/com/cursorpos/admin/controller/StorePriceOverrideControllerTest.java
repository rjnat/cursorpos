package com.cursorpos.admin.controller;

import com.cursorpos.admin.dto.StorePriceOverrideRequest;
import com.cursorpos.admin.dto.StorePriceOverrideResponse;
import com.cursorpos.admin.service.StorePriceOverrideService;
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
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller unit tests for StorePriceOverrideController.
 * Uses standalone MockMvc setup for testing controller logic in isolation.
 * Security is tested separately via integration tests.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StorePriceOverrideController Tests")
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods", "null" })
class StorePriceOverrideControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private StorePriceOverrideService priceOverrideService;

    private StorePriceOverrideController priceOverrideController;

    private UUID overrideId;
    private UUID storeId;
    private UUID productId;
    private StorePriceOverrideRequest request;
    private StorePriceOverrideResponse response;

    @BeforeEach
    void setUp() {
        priceOverrideController = new StorePriceOverrideController(priceOverrideService);
        mockMvc = MockMvcBuilders.standaloneSetup(priceOverrideController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        overrideId = UUID.randomUUID();
        storeId = UUID.randomUUID();
        productId = UUID.randomUUID();

        request = StorePriceOverrideRequest.builder()
                .storeId(storeId)
                .productId(productId)
                .overridePrice(BigDecimal.valueOf(9.99))
                .discountPercentage(BigDecimal.valueOf(10))
                .effectiveFrom(Instant.now().minusSeconds(86400))
                .effectiveTo(Instant.now().plusSeconds(86400L * 30))
                .build();

        response = StorePriceOverrideResponse.builder()
                .id(overrideId)
                .storeId(storeId)
                .productId(productId)
                .overridePrice(BigDecimal.valueOf(9.99))
                .discountPercentage(BigDecimal.valueOf(10))
                .effectiveFrom(Instant.now().minusSeconds(86400))
                .effectiveTo(Instant.now().plusSeconds(86400L * 30))
                .isActive(true)
                .build();
    }

    @Nested
    @DisplayName("POST /price-overrides")
    class CreateOverrideTests {

        @Test
        @DisplayName("Should create override with valid request")
        void shouldCreateOverrideWithValidRequest() throws Exception {
            when(priceOverrideService.createOverride(any(StorePriceOverrideRequest.class))).thenReturn(response);

            mockMvc.perform(post("/price-overrides")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.overridePrice").value(9.99));
        }
    }

    @Nested
    @DisplayName("GET /price-overrides/{id}")
    class GetOverrideByIdTests {

        @Test
        @DisplayName("Should return override when found")
        void shouldReturnOverrideWhenFound() throws Exception {
            when(priceOverrideService.getOverrideById(overrideId)).thenReturn(response);

            mockMvc.perform(get("/price-overrides/{id}", overrideId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(overrideId.toString()));
        }

        @Test
        @DisplayName("Should return 404 when not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(priceOverrideService.getOverrideById(overrideId))
                    .thenThrow(new ResourceNotFoundException("Override not found"));

            mockMvc.perform(get("/price-overrides/{id}", overrideId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /price-overrides/store/{storeId}")
    class GetOverridesByStoreTests {

        @Test
        @DisplayName("Should return overrides by store")
        void shouldReturnOverridesByStore() throws Exception {
            PagedResponse<StorePriceOverrideResponse> pagedResponse = PagedResponse
                    .<StorePriceOverrideResponse>builder()
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
            when(priceOverrideService.getOverridesByStore(eq(storeId), any())).thenReturn(pagedResponse);

            mockMvc.perform(get("/price-overrides/store/{storeId}", storeId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("GET /price-overrides/product/{productId}")
    class GetOverridesByProductTests {

        @Test
        @DisplayName("Should return overrides by product")
        void shouldReturnOverridesByProduct() throws Exception {
            PagedResponse<StorePriceOverrideResponse> pagedResponse = PagedResponse
                    .<StorePriceOverrideResponse>builder()
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
            when(priceOverrideService.getOverridesByProduct(eq(productId), any())).thenReturn(pagedResponse);

            mockMvc.perform(get("/price-overrides/product/{productId}", productId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("GET /price-overrides/active")
    class GetActiveOverrideTests {

        @Test
        @DisplayName("Should return active override when exists")
        void shouldReturnActiveOverrideWhenExists() throws Exception {
            when(priceOverrideService.getActiveOverride(storeId, productId)).thenReturn(Optional.of(response));

            mockMvc.perform(get("/price-overrides/active")
                    .param("storeId", storeId.toString())
                    .param("productId", productId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.overridePrice").value(9.99));
        }

        @Test
        @DisplayName("Should return success with null data when no active override")
        void shouldReturnSuccessWithNullWhenNoActiveOverride() throws Exception {
            when(priceOverrideService.getActiveOverride(storeId, productId)).thenReturn(Optional.empty());

            mockMvc.perform(get("/price-overrides/active")
                    .param("storeId", storeId.toString())
                    .param("productId", productId.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("GET /price-overrides/store/{storeId}/active")
    class GetAllActiveOverridesForStoreTests {

        @Test
        @DisplayName("Should return all active overrides for store")
        void shouldReturnAllActiveOverridesForStore() throws Exception {
            when(priceOverrideService.getAllActiveOverridesForStore(storeId)).thenReturn(List.of(response));

            mockMvc.perform(get("/price-overrides/store/{storeId}/active", storeId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].overridePrice").value(9.99));
        }
    }

    @Nested
    @DisplayName("PUT /price-overrides/{id}")
    class UpdateOverrideTests {

        @Test
        @DisplayName("Should update override successfully")
        void shouldUpdateOverrideSuccessfully() throws Exception {
            when(priceOverrideService.updateOverride(eq(overrideId), any(StorePriceOverrideRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(put("/price-overrides/{id}", overrideId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("DELETE /price-overrides/{id}")
    class DeleteOverrideTests {

        @Test
        @DisplayName("Should delete override successfully")
        void shouldDeleteOverrideSuccessfully() throws Exception {
            doNothing().when(priceOverrideService).deleteOverride(overrideId);

            mockMvc.perform(delete("/price-overrides/{id}", overrideId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("POST /price-overrides/{id}/activate")
    class ActivateOverrideTests {

        @Test
        @DisplayName("Should activate override successfully")
        void shouldActivateOverrideSuccessfully() throws Exception {
            when(priceOverrideService.activateOverride(overrideId)).thenReturn(response);

            mockMvc.perform(post("/price-overrides/{id}/activate", overrideId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("POST /price-overrides/{id}/deactivate")
    class DeactivateOverrideTests {

        @Test
        @DisplayName("Should deactivate override successfully")
        void shouldDeactivateOverrideSuccessfully() throws Exception {
            when(priceOverrideService.deactivateOverride(overrideId)).thenReturn(response);

            mockMvc.perform(post("/price-overrides/{id}/deactivate", overrideId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
