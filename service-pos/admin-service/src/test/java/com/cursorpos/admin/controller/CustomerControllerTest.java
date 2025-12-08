package com.cursorpos.admin.controller;

import com.cursorpos.admin.dto.CreateCustomerRequest;
import com.cursorpos.admin.dto.CustomerResponse;
import com.cursorpos.admin.service.CustomerService;
import com.cursorpos.shared.dto.PagedResponse;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller unit tests for CustomerController.
 * Uses standalone MockMvc setup for testing controller logic in isolation.
 * Security is tested separately via integration tests.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerController Tests")
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods", "null" })
class CustomerControllerTest {

    // Test constants
    private static final String CUSTOMER_CODE = "CUST-001";
    private static final String CUSTOMER_EMAIL = "john.doe@example.com";
    private static final String CUSTOMER_PHONE = "555-1234";
    private static final String JSON_SUCCESS = "$.success";
    private static final String CUSTOMERS_ID_PATH = "/customers/{id}";

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private CustomerService customerService;

    private CustomerController customerController;

    private UUID customerId;
    private UUID loyaltyTierId;
    private CreateCustomerRequest request;
    private CustomerResponse response;

    @BeforeEach
    void setUp() {
        // Create controller with mocked service
        customerController = new CustomerController(customerService);

        // Setup standalone MockMvc with the controller, exception handler, and pageable
        // support
        mockMvc = MockMvcBuilders.standaloneSetup(customerController)
                .setControllerAdvice(new com.cursorpos.shared.exception.GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // For Java 8 date/time support

        customerId = UUID.randomUUID();
        loyaltyTierId = UUID.randomUUID();

        request = CreateCustomerRequest.builder()
                .code(CUSTOMER_CODE)
                .firstName("John")
                .lastName("Doe")
                .email(CUSTOMER_EMAIL)
                .phone(CUSTOMER_PHONE)
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .build();

        response = CustomerResponse.builder()
                .id(customerId)
                .code(CUSTOMER_CODE)
                .firstName("John")
                .lastName("Doe")
                .email(CUSTOMER_EMAIL)
                .phone(CUSTOMER_PHONE)
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .loyaltyTierId(loyaltyTierId)
                .lifetimePoints(100)
                .availablePoints(50)
                .isActive(true)
                .build();
    }

    @Nested
    @DisplayName("POST /customers")
    class CreateCustomerTests {

        @Test
        @DisplayName("Should create customer with valid request")
        void shouldCreateCustomerWithValidRequest() throws Exception {
            when(customerService.createCustomer(any(CreateCustomerRequest.class))).thenReturn(response);

            mockMvc.perform(post("/customers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath(JSON_SUCCESS).value(true))
                    .andExpect(jsonPath("$.data.code").value(CUSTOMER_CODE));
        }

        // Security authorization tests are done in integration tests
    }

    @Nested
    @DisplayName("GET /customers/{id}")
    class GetCustomerByIdTests {

        @Test
        @DisplayName("Should return customer when found")
        void shouldReturnCustomerWhenFound() throws Exception {
            when(customerService.getCustomerById(customerId)).thenReturn(response);

            mockMvc.perform(get(CUSTOMERS_ID_PATH, customerId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(JSON_SUCCESS).value(true))
                    .andExpect(jsonPath("$.data.id").value(customerId.toString()));
        }

        @Test
        @DisplayName("Should return 404 when not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(customerService.getCustomerById(customerId))
                    .thenThrow(new ResourceNotFoundException("Customer not found"));

            mockMvc.perform(get(CUSTOMERS_ID_PATH, customerId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /customers/code/{code}")
    class GetCustomerByCodeTests {

        @Test
        @DisplayName("Should return customer when found by code")
        void shouldReturnCustomerWhenFoundByCode() throws Exception {
            when(customerService.getCustomerByCode(CUSTOMER_CODE)).thenReturn(response);

            mockMvc.perform(get("/customers/code/{code}", CUSTOMER_CODE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(JSON_SUCCESS).value(true))
                    .andExpect(jsonPath("$.data.code").value(CUSTOMER_CODE));
        }
    }

    @Nested
    @DisplayName("GET /customers/email/{email}")
    class GetCustomerByEmailTests {

        @Test
        @DisplayName("Should return customer when found by email")
        void shouldReturnCustomerWhenFoundByEmail() throws Exception {
            when(customerService.getCustomerByEmail(CUSTOMER_EMAIL)).thenReturn(response);

            mockMvc.perform(get("/customers/email/{email}", CUSTOMER_EMAIL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(JSON_SUCCESS).value(true))
                    .andExpect(jsonPath("$.data.email").value(CUSTOMER_EMAIL));
        }
    }

    @Nested
    @DisplayName("GET /customers/phone/{phone}")
    class GetCustomerByPhoneTests {

        @Test
        @DisplayName("Should return customer when found by phone")
        void shouldReturnCustomerWhenFoundByPhone() throws Exception {
            when(customerService.getCustomerByPhone(CUSTOMER_PHONE)).thenReturn(response);

            mockMvc.perform(get("/customers/phone/{phone}", CUSTOMER_PHONE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(JSON_SUCCESS).value(true))
                    .andExpect(jsonPath("$.data.phone").value(CUSTOMER_PHONE));
        }
    }

    @Nested
    @DisplayName("GET /customers")
    class GetAllCustomersTests {

        @Test
        @DisplayName("Should return paginated customers")
        void shouldReturnPaginatedCustomers() throws Exception {
            PagedResponse<CustomerResponse> pagedResponse = PagedResponse.<CustomerResponse>builder()
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
            when(customerService.getAllCustomers(any())).thenReturn(pagedResponse);

            mockMvc.perform(get("/customers"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(JSON_SUCCESS).value(true))
                    .andExpect(jsonPath("$.data.content[0].code").value(CUSTOMER_CODE));
        }
    }

    @Nested
    @DisplayName("GET /customers/loyalty-tier/{tierId}")
    class GetCustomersByLoyaltyTierTests {

        @Test
        @DisplayName("Should return customers by loyalty tier")
        void shouldReturnCustomersByLoyaltyTier() throws Exception {
            PagedResponse<CustomerResponse> pagedResponse = PagedResponse.<CustomerResponse>builder()
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
            when(customerService.getCustomersByLoyaltyTier(eq(loyaltyTierId), any())).thenReturn(pagedResponse);

            mockMvc.perform(get("/customers/loyalty-tier/{tierId}", loyaltyTierId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(JSON_SUCCESS).value(true));
        }
    }

    @Nested
    @DisplayName("PUT /customers/{id}")
    class UpdateCustomerTests {

        @Test
        @DisplayName("Should update customer successfully")
        void shouldUpdateCustomerSuccessfully() throws Exception {
            when(customerService.updateCustomer(eq(customerId), any(CreateCustomerRequest.class))).thenReturn(response);

            mockMvc.perform(put(CUSTOMERS_ID_PATH, customerId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(JSON_SUCCESS).value(true));
        }
    }

    @Nested
    @DisplayName("DELETE /customers/{id}")
    class DeleteCustomerTests {

        @Test
        @DisplayName("Should delete customer successfully")
        void shouldDeleteCustomerSuccessfully() throws Exception {
            doNothing().when(customerService).deleteCustomer(customerId);

            mockMvc.perform(delete(CUSTOMERS_ID_PATH, customerId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(JSON_SUCCESS).value(true));
        }
    }

    @Nested
    @DisplayName("POST /customers/{id}/activate")
    class ActivateCustomerTests {

        @Test
        @DisplayName("Should activate customer successfully")
        void shouldActivateCustomerSuccessfully() throws Exception {
            when(customerService.activateCustomer(customerId)).thenReturn(response);

            mockMvc.perform(post(CUSTOMERS_ID_PATH + "/activate", customerId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(JSON_SUCCESS).value(true));
        }
    }

    @Nested
    @DisplayName("POST /customers/{id}/deactivate")
    class DeactivateCustomerTests {

        @Test
        @DisplayName("Should deactivate customer successfully")
        void shouldDeactivateCustomerSuccessfully() throws Exception {
            when(customerService.deactivateCustomer(customerId)).thenReturn(response);

            mockMvc.perform(post(CUSTOMERS_ID_PATH + "/deactivate", customerId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(JSON_SUCCESS).value(true));
        }
    }
}
