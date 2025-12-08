package com.cursorpos.admin.controller;

import com.cursorpos.admin.dto.CreateStoreRequest;
import com.cursorpos.admin.dto.StoreResponse;
import com.cursorpos.admin.service.StoreService;
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
 * Controller unit tests for StoreController.
 * Uses standalone MockMvc setup for testing controller logic in isolation.
 * Security is tested separately via integration tests.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StoreController Tests")
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods", "null" })
class StoreControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private StoreService storeService;

    private StoreController storeController;

    private UUID storeId;
    private UUID branchId;
    private CreateStoreRequest request;
    private StoreResponse response;

    @BeforeEach
    void setUp() {
        storeController = new StoreController(storeService);
        mockMvc = MockMvcBuilders.standaloneSetup(storeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        storeId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        request = CreateStoreRequest.builder()
                .branchId(branchId)
                .code("STORE-001")
                .name("Main Store")
                .description("Main store location")
                .address("456 Store St")
                .city("New York")
                .state("NY")
                .country("USA")
                .postalCode("10002")
                .phone("555-9999")
                .email("store@example.com")
                .globalDiscountPercentage(BigDecimal.valueOf(5.0))
                .build();

        response = StoreResponse.builder()
                .id(storeId)
                .branchId(branchId)
                .code("STORE-001")
                .name("Main Store")
                .description("Main store location")
                .address("456 Store St")
                .city("New York")
                .state("NY")
                .country("USA")
                .postalCode("10002")
                .phone("555-9999")
                .email("store@example.com")
                .isActive(true)
                .globalDiscountPercentage(BigDecimal.valueOf(5.0))
                .build();
    }

    @Nested
    @DisplayName("POST /stores")
    class CreateStoreTests {

        @Test
        @DisplayName("Should create store with valid request")
        void shouldCreateStoreWithValidRequest() throws Exception {
            when(storeService.createStore(any(CreateStoreRequest.class))).thenReturn(response);

            mockMvc.perform(post("/stores")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.code").value("STORE-001"));
        }
    }

    @Nested
    @DisplayName("GET /stores/{id}")
    class GetStoreByIdTests {

        @Test
        @DisplayName("Should return store when found")
        void shouldReturnStoreWhenFound() throws Exception {
            when(storeService.getStoreById(storeId)).thenReturn(response);

            mockMvc.perform(get("/stores/{id}", storeId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(storeId.toString()));
        }

        @Test
        @DisplayName("Should return 404 when not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(storeService.getStoreById(storeId))
                    .thenThrow(new ResourceNotFoundException("Store not found"));

            mockMvc.perform(get("/stores/{id}", storeId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /stores/code/{code}")
    class GetStoreByCodeTests {

        @Test
        @DisplayName("Should return store when found by code")
        void shouldReturnStoreWhenFoundByCode() throws Exception {
            when(storeService.getStoreByCode("STORE-001")).thenReturn(response);

            mockMvc.perform(get("/stores/code/{code}", "STORE-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.code").value("STORE-001"));
        }
    }

    @Nested
    @DisplayName("GET /stores")
    class GetAllStoresTests {

        @Test
        @DisplayName("Should return paginated stores")
        void shouldReturnPaginatedStores() throws Exception {
            PagedResponse<StoreResponse> pagedResponse = PagedResponse.<StoreResponse>builder()
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
            when(storeService.getAllStores(any())).thenReturn(pagedResponse);

            mockMvc.perform(get("/stores"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].code").value("STORE-001"));
        }
    }

    @Nested
    @DisplayName("GET /stores/branch/{branchId}")
    class GetStoresByBranchTests {

        @Test
        @DisplayName("Should return stores by branch")
        void shouldReturnStoresByBranch() throws Exception {
            PagedResponse<StoreResponse> pagedResponse = PagedResponse.<StoreResponse>builder()
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
            when(storeService.getStoresByBranch(eq(branchId), any())).thenReturn(pagedResponse);

            mockMvc.perform(get("/stores/branch/{branchId}", branchId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("GET /stores/branch/{branchId}/active")
    class GetActiveStoresByBranchTests {

        @Test
        @DisplayName("Should return active stores by branch")
        void shouldReturnActiveStoresByBranch() throws Exception {
            when(storeService.getActiveStoresByBranch(branchId)).thenReturn(List.of(response));

            mockMvc.perform(get("/stores/branch/{branchId}/active", branchId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data[0].code").value("STORE-001"));
        }
    }

    @Nested
    @DisplayName("PUT /stores/{id}")
    class UpdateStoreTests {

        @Test
        @DisplayName("Should update store successfully")
        void shouldUpdateStoreSuccessfully() throws Exception {
            when(storeService.updateStore(eq(storeId), any(CreateStoreRequest.class))).thenReturn(response);

            mockMvc.perform(put("/stores/{id}", storeId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("DELETE /stores/{id}")
    class DeleteStoreTests {

        @Test
        @DisplayName("Should delete store successfully")
        void shouldDeleteStoreSuccessfully() throws Exception {
            doNothing().when(storeService).deleteStore(storeId);

            mockMvc.perform(delete("/stores/{id}", storeId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("POST /stores/{id}/activate")
    class ActivateStoreTests {

        @Test
        @DisplayName("Should activate store successfully")
        void shouldActivateStoreSuccessfully() throws Exception {
            when(storeService.activateStore(storeId)).thenReturn(response);

            mockMvc.perform(post("/stores/{id}/activate", storeId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("POST /stores/{id}/deactivate")
    class DeactivateStoreTests {

        @Test
        @DisplayName("Should deactivate store successfully")
        void shouldDeactivateStoreSuccessfully() throws Exception {
            when(storeService.deactivateStore(storeId)).thenReturn(response);

            mockMvc.perform(post("/stores/{id}/deactivate", storeId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
