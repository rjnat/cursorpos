package com.cursorpos.admin.controller;

import com.cursorpos.admin.dto.CreateTenantRequest;
import com.cursorpos.admin.dto.TenantResponse;
import com.cursorpos.admin.service.TenantService;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller unit tests for TenantController.
 * Uses standalone MockMvc setup for testing controller logic in isolation.
 * Security is tested separately via integration tests.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TenantController Tests")
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods", "null" })
class TenantControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private TenantService tenantService;

    private TenantController tenantController;

    private UUID tenantId;
    private CreateTenantRequest request;
    private TenantResponse response;

    @BeforeEach
    void setUp() {
        tenantController = new TenantController(tenantService);
        mockMvc = MockMvcBuilders.standaloneSetup(tenantController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        tenantId = UUID.randomUUID();
        UUID subscriptionPlanId = UUID.randomUUID();

        request = CreateTenantRequest.builder()
                .code("tenant-001")
                .name("Test Tenant")
                .subdomain("test-tenant")
                .email("tenant@example.com")
                .phone("555-1234")
                .address("123 Tenant St")
                .city("New York")
                .state("NY")
                .country("USA")
                .postalCode("10001")
                .subscriptionPlanId(subscriptionPlanId)
                .build();

        response = TenantResponse.builder()
                .id(tenantId)
                .code("tenant-001")
                .name("Test Tenant")
                .subdomain("test-tenant")
                .email("tenant@example.com")
                .phone("555-1234")
                .address("123 Tenant St")
                .city("New York")
                .state("NY")
                .country("USA")
                .postalCode("10001")
                .subscriptionPlanId(subscriptionPlanId)
                .isActive(true)
                .build();
    }

    @Nested
    @DisplayName("POST /tenants")
    class CreateTenantTests {

        @Test
        @DisplayName("Should create tenant with valid request")
        void shouldCreateTenantWithValidRequest() throws Exception {
            when(tenantService.createTenant(any(CreateTenantRequest.class))).thenReturn(response);

            mockMvc.perform(post("/tenants")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.code").value("tenant-001"));
        }
    }

    @Nested
    @DisplayName("GET /tenants/{id}")
    class GetTenantByIdTests {

        @Test
        @DisplayName("Should return tenant when found")
        void shouldReturnTenantWhenFound() throws Exception {
            when(tenantService.getTenantById(tenantId)).thenReturn(response);

            mockMvc.perform(get("/tenants/{id}", tenantId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(tenantId.toString()));
        }

        @Test
        @DisplayName("Should return 404 when not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(tenantService.getTenantById(tenantId))
                    .thenThrow(new ResourceNotFoundException("Tenant not found"));

            mockMvc.perform(get("/tenants/{id}", tenantId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /tenants/code/{code}")
    class GetTenantByCodeTests {

        @Test
        @DisplayName("Should return tenant when found by code")
        void shouldReturnTenantWhenFoundByCode() throws Exception {
            when(tenantService.getTenantByCode("tenant-001")).thenReturn(response);

            mockMvc.perform(get("/tenants/code/{code}", "tenant-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.code").value("tenant-001"));
        }
    }

    @Nested
    @DisplayName("GET /tenants")
    class GetAllTenantsTests {

        @Test
        @DisplayName("Should return paginated tenants")
        void shouldReturnPaginatedTenants() throws Exception {
            PagedResponse<TenantResponse> pagedResponse = PagedResponse.<TenantResponse>builder()
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
            when(tenantService.getAllTenants(any())).thenReturn(pagedResponse);

            mockMvc.perform(get("/tenants"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].code").value("tenant-001"));
        }
    }

    @Nested
    @DisplayName("PUT /tenants/{id}")
    class UpdateTenantTests {

        @Test
        @DisplayName("Should update tenant successfully")
        void shouldUpdateTenantSuccessfully() throws Exception {
            when(tenantService.updateTenant(eq(tenantId), any(CreateTenantRequest.class))).thenReturn(response);

            mockMvc.perform(put("/tenants/{id}", tenantId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("DELETE /tenants/{id}")
    class DeleteTenantTests {

        @Test
        @DisplayName("Should delete tenant successfully")
        void shouldDeleteTenantSuccessfully() throws Exception {
            doNothing().when(tenantService).deleteTenant(tenantId);

            mockMvc.perform(delete("/tenants/{id}", tenantId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("POST /tenants/{id}/activate")
    class ActivateTenantTests {

        @Test
        @DisplayName("Should activate tenant successfully")
        void shouldActivateTenantSuccessfully() throws Exception {
            when(tenantService.activateTenant(tenantId)).thenReturn(response);

            mockMvc.perform(post("/tenants/{id}/activate", tenantId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("POST /tenants/{id}/deactivate")
    class DeactivateTenantTests {

        @Test
        @DisplayName("Should deactivate tenant successfully")
        void shouldDeactivateTenantSuccessfully() throws Exception {
            when(tenantService.deactivateTenant(tenantId)).thenReturn(response);

            mockMvc.perform(post("/tenants/{id}/deactivate", tenantId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}
