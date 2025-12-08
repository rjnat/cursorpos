package com.cursorpos.admin.service;

import com.cursorpos.admin.dto.CreateTenantRequest;
import com.cursorpos.admin.dto.TenantResponse;
import com.cursorpos.admin.entity.Tenant;
import com.cursorpos.admin.mapper.AdminMapper;
import com.cursorpos.admin.repository.TenantRepository;
import com.cursorpos.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TenantService.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods", "null" })
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private AdminMapper adminMapper;

    @InjectMocks
    private TenantService tenantService;

    private UUID tenantId;
    private Tenant tenant;
    private CreateTenantRequest request;
    private TenantResponse response;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        UUID subscriptionPlanId = UUID.randomUUID();

        tenant = new Tenant();
        tenant.setId(tenantId);
        tenant.setCode("TENANT-001");
        tenant.setName("Test Tenant");
        tenant.setSubdomain("test-tenant");
        tenant.setEmail("tenant@example.com");
        tenant.setPhone("555-1234");
        tenant.setAddress("123 Tenant St");
        tenant.setCity("New York");
        tenant.setState("NY");
        tenant.setCountry("USA");
        tenant.setPostalCode("10001");
        tenant.setSubscriptionPlanId(subscriptionPlanId);
        tenant.setIsActive(true);

        request = CreateTenantRequest.builder()
                .code("TENANT-001")
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
                .code("TENANT-001")
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
    @DisplayName("createTenant tests")
    class CreateTenantTests {

        @Test
        @DisplayName("Should create tenant successfully")
        void shouldCreateTenantSuccessfully() {
            when(tenantRepository.existsByCode("TENANT-001")).thenReturn(false);
            when(tenantRepository.existsBySubdomain("test-tenant")).thenReturn(false);
            when(adminMapper.toTenant(request)).thenReturn(tenant);
            when(tenantRepository.save(tenant)).thenReturn(tenant);
            when(adminMapper.toTenantResponse(tenant)).thenReturn(response);

            TenantResponse result = tenantService.createTenant(request);

            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo("TENANT-001");
            verify(tenantRepository).save(tenant);
        }

        @Test
        @DisplayName("Should throw exception when tenant code already exists")
        void shouldThrowExceptionWhenTenantCodeExists() {
            when(tenantRepository.existsByCode("TENANT-001")).thenReturn(true);

            assertThatThrownBy(() -> tenantService.createTenant(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("Should throw exception when subdomain already exists")
        void shouldThrowExceptionWhenSubdomainExists() {
            when(tenantRepository.existsByCode("TENANT-001")).thenReturn(false);
            when(tenantRepository.existsBySubdomain("test-tenant")).thenReturn(true);

            assertThatThrownBy(() -> tenantService.createTenant(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("subdomain");
        }

        @Test
        @DisplayName("Should create tenant successfully when subdomain is null")
        void shouldCreateTenantSuccessfullyWhenSubdomainIsNull() {
            CreateTenantRequest requestNoSubdomain = CreateTenantRequest.builder()
                    .code("TENANT-001")
                    .name("Test Tenant")
                    .subdomain(null)
                    .email("tenant@example.com")
                    .build();
            Tenant tenantNoSubdomain = new Tenant();
            tenantNoSubdomain.setId(tenantId);
            tenantNoSubdomain.setCode("TENANT-001");
            tenantNoSubdomain.setName("Test Tenant");

            when(tenantRepository.existsByCode("TENANT-001")).thenReturn(false);
            when(adminMapper.toTenant(requestNoSubdomain)).thenReturn(tenantNoSubdomain);
            when(tenantRepository.save(tenantNoSubdomain)).thenReturn(tenantNoSubdomain);
            when(adminMapper.toTenantResponse(tenantNoSubdomain)).thenReturn(response);

            TenantResponse result = tenantService.createTenant(requestNoSubdomain);

            assertThat(result).isNotNull();
            verify(tenantRepository, never()).existsBySubdomain(any());
        }

        @Test
        @DisplayName("Should throw exception when request is null")
        void shouldThrowExceptionWhenRequestIsNull() {
            assertThatThrownBy(() -> tenantService.createTenant(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getTenantById tests")
    class GetTenantByIdTests {

        @Test
        @DisplayName("Should return tenant when found")
        void shouldReturnTenantWhenFound() {
            when(tenantRepository.findByIdAndDeletedAtIsNull(tenantId))
                    .thenReturn(Optional.of(tenant));
            when(adminMapper.toTenantResponse(tenant)).thenReturn(response);

            TenantResponse result = tenantService.getTenantById(tenantId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(tenantId);
        }

        @Test
        @DisplayName("Should throw exception when tenant not found")
        void shouldThrowExceptionWhenNotFound() {
            when(tenantRepository.findByIdAndDeletedAtIsNull(tenantId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> tenantService.getTenantById(tenantId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> tenantService.getTenantById(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getTenantByCode tests")
    class GetTenantByCodeTests {

        @Test
        @DisplayName("Should return tenant when found by code")
        void shouldReturnTenantWhenFoundByCode() {
            when(tenantRepository.findByCode("TENANT-001"))
                    .thenReturn(Optional.of(tenant));
            when(adminMapper.toTenantResponse(tenant)).thenReturn(response);

            TenantResponse result = tenantService.getTenantByCode("TENANT-001");

            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo("TENANT-001");
        }

        @Test
        @DisplayName("Should throw exception when tenant not found by code")
        void shouldThrowExceptionWhenNotFoundByCode() {
            when(tenantRepository.findByCode("INVALID"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> tenantService.getTenantByCode("INVALID"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getAllTenants tests")
    class GetAllTenantsTests {

        @Test
        @DisplayName("Should return all tenants paginated")
        void shouldReturnAllTenantsPaginated() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Tenant> page = new PageImpl<>(List.of(tenant));

            when(tenantRepository.findByDeletedAtIsNull(pageable)).thenReturn(page);
            when(adminMapper.toTenantResponse(tenant)).thenReturn(response);

            var result = tenantService.getAllTenants(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("updateTenant tests")
    class UpdateTenantTests {

        @Test
        @DisplayName("Should update tenant successfully")
        void shouldUpdateTenantSuccessfully() {
            when(tenantRepository.findByIdAndDeletedAtIsNull(tenantId))
                    .thenReturn(Optional.of(tenant));
            when(tenantRepository.save(tenant)).thenReturn(tenant);
            when(adminMapper.toTenantResponse(tenant)).thenReturn(response);

            TenantResponse result = tenantService.updateTenant(tenantId, request);

            assertThat(result).isNotNull();
            verify(adminMapper).updateTenantFromRequest(request, tenant);
            verify(tenantRepository).save(tenant);
        }

        @Test
        @DisplayName("Should throw exception when tenant not found")
        void shouldThrowExceptionWhenTenantNotFound() {
            when(tenantRepository.findByIdAndDeletedAtIsNull(tenantId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> tenantService.updateTenant(tenantId, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> tenantService.updateTenant(null, request))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should throw exception when request is null")
        void shouldThrowExceptionWhenRequestIsNull() {
            assertThatThrownBy(() -> tenantService.updateTenant(tenantId, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("deleteTenant tests")
    class DeleteTenantTests {

        @Test
        @DisplayName("Should soft delete tenant successfully")
        void shouldSoftDeleteTenantSuccessfully() {
            when(tenantRepository.findByIdAndDeletedAtIsNull(tenantId))
                    .thenReturn(Optional.of(tenant));
            when(tenantRepository.save(tenant)).thenReturn(tenant);

            tenantService.deleteTenant(tenantId);

            verify(tenantRepository).save(tenant);
        }

        @Test
        @DisplayName("Should throw exception when tenant not found")
        void shouldThrowExceptionWhenTenantNotFound() {
            when(tenantRepository.findByIdAndDeletedAtIsNull(tenantId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> tenantService.deleteTenant(tenantId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> tenantService.deleteTenant(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("activateTenant tests")
    class ActivateTenantTests {

        @Test
        @DisplayName("Should activate tenant successfully")
        void shouldActivateTenantSuccessfully() {
            tenant.setIsActive(false);
            when(tenantRepository.findByIdAndDeletedAtIsNull(tenantId))
                    .thenReturn(Optional.of(tenant));
            when(tenantRepository.save(tenant)).thenReturn(tenant);
            when(adminMapper.toTenantResponse(tenant)).thenReturn(response);

            TenantResponse result = tenantService.activateTenant(tenantId);

            assertThat(result).isNotNull();
            assertThat(tenant.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when tenant not found")
        void shouldThrowExceptionWhenTenantNotFound() {
            when(tenantRepository.findByIdAndDeletedAtIsNull(tenantId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> tenantService.activateTenant(tenantId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> tenantService.activateTenant(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("deactivateTenant tests")
    class DeactivateTenantTests {

        @Test
        @DisplayName("Should deactivate tenant successfully")
        void shouldDeactivateTenantSuccessfully() {
            when(tenantRepository.findByIdAndDeletedAtIsNull(tenantId))
                    .thenReturn(Optional.of(tenant));
            when(tenantRepository.save(tenant)).thenReturn(tenant);
            when(adminMapper.toTenantResponse(tenant)).thenReturn(response);

            TenantResponse result = tenantService.deactivateTenant(tenantId);

            assertThat(result).isNotNull();
            assertThat(tenant.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should throw exception when tenant not found")
        void shouldThrowExceptionWhenTenantNotFound() {
            when(tenantRepository.findByIdAndDeletedAtIsNull(tenantId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> tenantService.deactivateTenant(tenantId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> tenantService.deactivateTenant(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
