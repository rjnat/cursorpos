package com.cursorpos.admin.service;

import com.cursorpos.admin.dto.CreateCustomerRequest;
import com.cursorpos.admin.dto.CustomerResponse;
import com.cursorpos.admin.entity.Customer;
import com.cursorpos.admin.entity.LoyaltyTier;
import com.cursorpos.admin.mapper.AdminMapper;
import com.cursorpos.admin.repository.CustomerRepository;
import com.cursorpos.admin.repository.LoyaltyTierRepository;
import com.cursorpos.shared.exception.ResourceNotFoundException;
import com.cursorpos.shared.security.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomerService.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods", "null" })
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private LoyaltyTierRepository loyaltyTierRepository;

    @Mock
    private AdminMapper adminMapper;

    @InjectMocks
    private CustomerService customerService;

    private MockedStatic<TenantContext> tenantContextMock;

    private static final String TENANT_ID = "tenant-test-001";
    private UUID customerId;
    private UUID loyaltyTierId;
    private Customer customer;
    private LoyaltyTier loyaltyTier;
    private CreateCustomerRequest request;
    private CustomerResponse response;

    @BeforeEach
    void setUp() {
        tenantContextMock = mockStatic(TenantContext.class);
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(TENANT_ID);

        customerId = UUID.randomUUID();
        loyaltyTierId = UUID.randomUUID();

        loyaltyTier = new LoyaltyTier();
        loyaltyTier.setId(loyaltyTierId);
        loyaltyTier.setTenantId(TENANT_ID);
        loyaltyTier.setCode("BRONZE");
        loyaltyTier.setName("Bronze");
        loyaltyTier.setMinPoints(0);
        loyaltyTier.setDiscountPercentage(BigDecimal.ZERO);
        loyaltyTier.setPointsMultiplier(BigDecimal.ONE);

        customer = new Customer();
        customer.setId(customerId);
        customer.setTenantId(TENANT_ID);
        customer.setCode("CUST-001");
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setEmail("john.doe@example.com");
        customer.setPhone("555-1234");
        customer.setDateOfBirth(LocalDate.of(1990, 1, 15));
        customer.setLoyaltyTierId(loyaltyTierId);
        customer.setLifetimePoints(100);
        customer.setAvailablePoints(50);
        customer.setIsActive(true);

        request = CreateCustomerRequest.builder()
                .code("CUST-001")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("555-1234")
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .build();

        response = CustomerResponse.builder()
                .id(customerId)
                .code("CUST-001")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("555-1234")
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .loyaltyTierId(loyaltyTierId)
                .lifetimePoints(100)
                .availablePoints(50)
                .isActive(true)
                .build();
    }

    @AfterEach
    void tearDown() {
        tenantContextMock.close();
    }

    @Nested
    @DisplayName("createCustomer tests")
    class CreateCustomerTests {

        @Test
        @DisplayName("Should create customer successfully with default loyalty tier")
        void shouldCreateCustomerSuccessfullyWithDefaultLoyaltyTier() {
            when(customerRepository.existsByTenantIdAndCode(TENANT_ID, "CUST-001")).thenReturn(false);
            when(adminMapper.toCustomer(request)).thenReturn(customer);
            when(loyaltyTierRepository.findTierForPoints(TENANT_ID, 0)).thenReturn(Optional.of(loyaltyTier));
            when(customerRepository.save(customer)).thenReturn(customer);
            when(adminMapper.toCustomerResponse(customer)).thenReturn(response);

            CustomerResponse result = customerService.createCustomer(request);

            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo("CUST-001");
            verify(customerRepository).save(customer);
            verify(loyaltyTierRepository).findTierForPoints(TENANT_ID, 0);
        }

        @Test
        @DisplayName("Should create customer successfully without loyalty tier")
        void shouldCreateCustomerSuccessfullyWithoutLoyaltyTier() {
            when(customerRepository.existsByTenantIdAndCode(TENANT_ID, "CUST-001")).thenReturn(false);
            when(adminMapper.toCustomer(request)).thenReturn(customer);
            when(loyaltyTierRepository.findTierForPoints(TENANT_ID, 0)).thenReturn(Optional.empty());
            when(customerRepository.save(customer)).thenReturn(customer);
            when(adminMapper.toCustomerResponse(customer)).thenReturn(response);

            CustomerResponse result = customerService.createCustomer(request);

            assertThat(result).isNotNull();
            verify(customerRepository).save(customer);
        }

        @Test
        @DisplayName("Should throw exception when customer code already exists")
        void shouldThrowExceptionWhenCustomerCodeExists() {
            when(customerRepository.existsByTenantIdAndCode(TENANT_ID, "CUST-001")).thenReturn(true);

            assertThatThrownBy(() -> customerService.createCustomer(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("Should throw exception when request is null")
        void shouldThrowExceptionWhenRequestIsNull() {
            assertThatThrownBy(() -> customerService.createCustomer(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getCustomerById tests")
    class GetCustomerByIdTests {

        @Test
        @DisplayName("Should return customer when found")
        void shouldReturnCustomerWhenFound() {
            when(customerRepository.findByIdAndTenantIdAndDeletedAtIsNull(customerId, TENANT_ID))
                    .thenReturn(Optional.of(customer));
            when(adminMapper.toCustomerResponse(customer)).thenReturn(response);

            CustomerResponse result = customerService.getCustomerById(customerId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(customerId);
        }

        @Test
        @DisplayName("Should throw exception when customer not found")
        void shouldThrowExceptionWhenNotFound() {
            when(customerRepository.findByIdAndTenantIdAndDeletedAtIsNull(customerId, TENANT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> customerService.getCustomerById(customerId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> customerService.getCustomerById(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getCustomerByCode tests")
    class GetCustomerByCodeTests {

        @Test
        @DisplayName("Should return customer when found by code")
        void shouldReturnCustomerWhenFoundByCode() {
            when(customerRepository.findByTenantIdAndCodeAndDeletedAtIsNull(TENANT_ID, "CUST-001"))
                    .thenReturn(Optional.of(customer));
            when(adminMapper.toCustomerResponse(customer)).thenReturn(response);

            CustomerResponse result = customerService.getCustomerByCode("CUST-001");

            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo("CUST-001");
        }

        @Test
        @DisplayName("Should throw exception when customer not found by code")
        void shouldThrowExceptionWhenNotFoundByCode() {
            when(customerRepository.findByTenantIdAndCodeAndDeletedAtIsNull(TENANT_ID, "INVALID"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> customerService.getCustomerByCode("INVALID"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getCustomerByEmail tests")
    class GetCustomerByEmailTests {

        @Test
        @DisplayName("Should return customer when found by email")
        void shouldReturnCustomerWhenFoundByEmail() {
            when(customerRepository.findByTenantIdAndEmailAndDeletedAtIsNull(TENANT_ID, "john.doe@example.com"))
                    .thenReturn(Optional.of(customer));
            when(adminMapper.toCustomerResponse(customer)).thenReturn(response);

            CustomerResponse result = customerService.getCustomerByEmail("john.doe@example.com");

            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
        }

        @Test
        @DisplayName("Should throw exception when customer not found by email")
        void shouldThrowExceptionWhenNotFoundByEmail() {
            when(customerRepository.findByTenantIdAndEmailAndDeletedAtIsNull(TENANT_ID, "invalid@example.com"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> customerService.getCustomerByEmail("invalid@example.com"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getCustomerByPhone tests")
    class GetCustomerByPhoneTests {

        @Test
        @DisplayName("Should return customer when found by phone")
        void shouldReturnCustomerWhenFoundByPhone() {
            when(customerRepository.findByTenantIdAndPhoneAndDeletedAtIsNull(TENANT_ID, "555-1234"))
                    .thenReturn(Optional.of(customer));
            when(adminMapper.toCustomerResponse(customer)).thenReturn(response);

            CustomerResponse result = customerService.getCustomerByPhone("555-1234");

            assertThat(result).isNotNull();
            assertThat(result.getPhone()).isEqualTo("555-1234");
        }

        @Test
        @DisplayName("Should throw exception when customer not found by phone")
        void shouldThrowExceptionWhenNotFoundByPhone() {
            when(customerRepository.findByTenantIdAndPhoneAndDeletedAtIsNull(TENANT_ID, "999-9999"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> customerService.getCustomerByPhone("999-9999"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getAllCustomers tests")
    class GetAllCustomersTests {

        @Test
        @DisplayName("Should return all customers paginated")
        void shouldReturnAllCustomersPaginated() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Customer> page = new PageImpl<>(List.of(customer));

            when(customerRepository.findByTenantIdAndDeletedAtIsNull(TENANT_ID, pageable)).thenReturn(page);
            when(adminMapper.toCustomerResponse(customer)).thenReturn(response);

            var result = customerService.getAllCustomers(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getCustomersByLoyaltyTier tests")
    class GetCustomersByLoyaltyTierTests {

        @Test
        @DisplayName("Should return customers by loyalty tier")
        void shouldReturnCustomersByLoyaltyTier() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Customer> page = new PageImpl<>(List.of(customer));

            when(customerRepository.findByTenantIdAndLoyaltyTierIdAndDeletedAtIsNull(
                    TENANT_ID, loyaltyTierId, pageable)).thenReturn(page);
            when(adminMapper.toCustomerResponse(customer)).thenReturn(response);

            var result = customerService.getCustomersByLoyaltyTier(loyaltyTierId, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("updateCustomer tests")
    class UpdateCustomerTests {

        @Test
        @DisplayName("Should update customer successfully")
        void shouldUpdateCustomerSuccessfully() {
            when(customerRepository.findByIdAndTenantIdAndDeletedAtIsNull(customerId, TENANT_ID))
                    .thenReturn(Optional.of(customer));
            when(customerRepository.save(customer)).thenReturn(customer);
            when(adminMapper.toCustomerResponse(customer)).thenReturn(response);

            CustomerResponse result = customerService.updateCustomer(customerId, request);

            assertThat(result).isNotNull();
            verify(adminMapper).updateCustomerFromRequest(request, customer);
            verify(customerRepository).save(customer);
        }

        @Test
        @DisplayName("Should throw exception when customer not found")
        void shouldThrowExceptionWhenCustomerNotFound() {
            when(customerRepository.findByIdAndTenantIdAndDeletedAtIsNull(customerId, TENANT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> customerService.updateCustomer(customerId, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> customerService.updateCustomer(null, request))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should throw exception when request is null")
        void shouldThrowExceptionWhenRequestIsNull() {
            assertThatThrownBy(() -> customerService.updateCustomer(customerId, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("deleteCustomer tests")
    class DeleteCustomerTests {

        @Test
        @DisplayName("Should soft delete customer successfully")
        void shouldSoftDeleteCustomerSuccessfully() {
            when(customerRepository.findByIdAndTenantIdAndDeletedAtIsNull(customerId, TENANT_ID))
                    .thenReturn(Optional.of(customer));
            when(customerRepository.save(customer)).thenReturn(customer);

            customerService.deleteCustomer(customerId);

            verify(customerRepository).save(customer);
        }

        @Test
        @DisplayName("Should throw exception when customer not found")
        void shouldThrowExceptionWhenCustomerNotFound() {
            when(customerRepository.findByIdAndTenantIdAndDeletedAtIsNull(customerId, TENANT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> customerService.deleteCustomer(customerId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> customerService.deleteCustomer(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("activateCustomer tests")
    class ActivateCustomerTests {

        @Test
        @DisplayName("Should activate customer successfully")
        void shouldActivateCustomerSuccessfully() {
            customer.setIsActive(false);
            when(customerRepository.findByIdAndTenantIdAndDeletedAtIsNull(customerId, TENANT_ID))
                    .thenReturn(Optional.of(customer));
            when(customerRepository.save(customer)).thenReturn(customer);
            when(adminMapper.toCustomerResponse(customer)).thenReturn(response);

            CustomerResponse result = customerService.activateCustomer(customerId);

            assertThat(result).isNotNull();
            assertThat(customer.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when customer not found")
        void shouldThrowExceptionWhenCustomerNotFound() {
            when(customerRepository.findByIdAndTenantIdAndDeletedAtIsNull(customerId, TENANT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> customerService.activateCustomer(customerId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> customerService.activateCustomer(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("deactivateCustomer tests")
    class DeactivateCustomerTests {

        @Test
        @DisplayName("Should deactivate customer successfully")
        void shouldDeactivateCustomerSuccessfully() {
            when(customerRepository.findByIdAndTenantIdAndDeletedAtIsNull(customerId, TENANT_ID))
                    .thenReturn(Optional.of(customer));
            when(customerRepository.save(customer)).thenReturn(customer);
            when(adminMapper.toCustomerResponse(customer)).thenReturn(response);

            CustomerResponse result = customerService.deactivateCustomer(customerId);

            assertThat(result).isNotNull();
            assertThat(customer.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should throw exception when customer not found")
        void shouldThrowExceptionWhenCustomerNotFound() {
            when(customerRepository.findByIdAndTenantIdAndDeletedAtIsNull(customerId, TENANT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> customerService.deactivateCustomer(customerId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> customerService.deactivateCustomer(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
