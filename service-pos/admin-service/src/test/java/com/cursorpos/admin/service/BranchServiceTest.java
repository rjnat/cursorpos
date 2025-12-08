package com.cursorpos.admin.service;

import com.cursorpos.admin.dto.BranchResponse;
import com.cursorpos.admin.dto.CreateBranchRequest;
import com.cursorpos.admin.entity.Branch;
import com.cursorpos.admin.mapper.AdminMapper;
import com.cursorpos.admin.repository.BranchRepository;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BranchService.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods", "null" })
class BranchServiceTest {

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private AdminMapper adminMapper;

    @InjectMocks
    private BranchService branchService;

    private MockedStatic<TenantContext> tenantContextMock;

    private static final String TENANT_ID = "tenant-test-001";
    private static final String BRANCH_CODE = "BRANCH-001";
    private static final String BRANCH_NAME = "Main Branch";
    private static final String BRANCH_DESCRIPTION = "Main regional branch";
    private static final String BRANCH_ADDRESS = "123 Main St";
    private static final String BRANCH_CITY = "New York";
    private static final String BRANCH_POSTAL_CODE = "10001";
    private static final String BRANCH_PHONE = "555-1234";
    private static final String BRANCH_EMAIL = "branch@example.com";
    private static final String MANAGER_NAME = "John Manager";
    private static final String MANAGER_EMAIL = "john@example.com";
    private static final String MANAGER_PHONE = "555-5678";

    private UUID branchId;
    private Branch branch;
    private CreateBranchRequest request;
    private BranchResponse response;

    @BeforeEach
    void setUp() {
        tenantContextMock = mockStatic(TenantContext.class);
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(TENANT_ID);

        branchId = UUID.randomUUID();

        branch = new Branch();
        branch.setId(branchId);
        branch.setTenantId(TENANT_ID);
        branch.setCode(BRANCH_CODE);
        branch.setName(BRANCH_NAME);
        branch.setDescription(BRANCH_DESCRIPTION);
        branch.setAddress(BRANCH_ADDRESS);
        branch.setCity(BRANCH_CITY);
        branch.setState("NY");
        branch.setCountry("USA");
        branch.setPostalCode(BRANCH_POSTAL_CODE);
        branch.setPhone(BRANCH_PHONE);
        branch.setEmail(BRANCH_EMAIL);
        branch.setIsActive(true);
        branch.setManagerName(MANAGER_NAME);
        branch.setManagerEmail(MANAGER_EMAIL);
        branch.setManagerPhone(MANAGER_PHONE);

        request = CreateBranchRequest.builder()
                .code(BRANCH_CODE)
                .name(BRANCH_NAME)
                .description(BRANCH_DESCRIPTION)
                .address(BRANCH_ADDRESS)
                .city(BRANCH_CITY)
                .state("NY")
                .country("USA")
                .postalCode(BRANCH_POSTAL_CODE)
                .phone(BRANCH_PHONE)
                .email(BRANCH_EMAIL)
                .managerName(MANAGER_NAME)
                .managerEmail(MANAGER_EMAIL)
                .managerPhone(MANAGER_PHONE)
                .build();

        response = BranchResponse.builder()
                .id(branchId)
                .code("BRANCH-001")
                .name("Main Branch")
                .description("Main regional branch")
                .address("123 Main St")
                .city("New York")
                .state("NY")
                .country("USA")
                .postalCode("10001")
                .phone("555-1234")
                .email("branch@example.com")
                .isActive(true)
                .managerName("John Manager")
                .managerEmail("john@example.com")
                .managerPhone("555-5678")
                .build();
    }

    @AfterEach
    void tearDown() {
        tenantContextMock.close();
    }

    @Nested
    @DisplayName("createBranch tests")
    class CreateBranchTests {

        @Test
        @DisplayName("Should create branch successfully")
        void shouldCreateBranchSuccessfully() {
            when(branchRepository.existsByTenantIdAndCode(TENANT_ID, "BRANCH-001")).thenReturn(false);
            when(adminMapper.toBranch(request)).thenReturn(branch);
            when(branchRepository.save(branch)).thenReturn(branch);
            when(adminMapper.toBranchResponse(branch)).thenReturn(response);

            BranchResponse result = branchService.createBranch(request);

            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo("BRANCH-001");
            verify(branchRepository).save(branch);
        }

        @Test
        @DisplayName("Should throw exception when branch code already exists")
        void shouldThrowExceptionWhenBranchCodeExists() {
            when(branchRepository.existsByTenantIdAndCode(TENANT_ID, "BRANCH-001")).thenReturn(true);

            assertThatThrownBy(() -> branchService.createBranch(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("Should throw exception when request is null")
        void shouldThrowExceptionWhenRequestIsNull() {
            assertThatThrownBy(() -> branchService.createBranch(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getBranchById tests")
    class GetBranchByIdTests {

        @Test
        @DisplayName("Should return branch when found")
        void shouldReturnBranchWhenFound() {
            when(branchRepository.findByIdAndTenantIdAndDeletedAtIsNull(branchId, TENANT_ID))
                    .thenReturn(Optional.of(branch));
            when(adminMapper.toBranchResponse(branch)).thenReturn(response);

            BranchResponse result = branchService.getBranchById(branchId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(branchId);
        }

        @Test
        @DisplayName("Should throw exception when branch not found")
        void shouldThrowExceptionWhenNotFound() {
            when(branchRepository.findByIdAndTenantIdAndDeletedAtIsNull(branchId, TENANT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> branchService.getBranchById(branchId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> branchService.getBranchById(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getBranchByCode tests")
    class GetBranchByCodeTests {

        @Test
        @DisplayName("Should return branch when found by code")
        void shouldReturnBranchWhenFoundByCode() {
            when(branchRepository.findByTenantIdAndCodeAndDeletedAtIsNull(TENANT_ID, "BRANCH-001"))
                    .thenReturn(Optional.of(branch));
            when(adminMapper.toBranchResponse(branch)).thenReturn(response);

            BranchResponse result = branchService.getBranchByCode("BRANCH-001");

            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo("BRANCH-001");
        }

        @Test
        @DisplayName("Should throw exception when branch not found by code")
        void shouldThrowExceptionWhenNotFoundByCode() {
            when(branchRepository.findByTenantIdAndCodeAndDeletedAtIsNull(TENANT_ID, "INVALID"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> branchService.getBranchByCode("INVALID"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getAllBranches tests")
    class GetAllBranchesTests {

        @Test
        @DisplayName("Should return all branches paginated")
        void shouldReturnAllBranchesPaginated() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Branch> page = new PageImpl<>(List.of(branch));

            when(branchRepository.findByTenantIdAndDeletedAtIsNull(TENANT_ID, pageable)).thenReturn(page);
            when(adminMapper.toBranchResponse(branch)).thenReturn(response);

            var result = branchService.getAllBranches(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getActiveBranches tests")
    class GetActiveBranchesTests {

        @Test
        @DisplayName("Should return active branches")
        void shouldReturnActiveBranches() {
            when(branchRepository.findByTenantIdAndIsActiveAndDeletedAtIsNull(TENANT_ID, true))
                    .thenReturn(List.of(branch));
            when(adminMapper.toBranchResponse(branch)).thenReturn(response);

            List<BranchResponse> result = branchService.getActiveBranches();

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("updateBranch tests")
    class UpdateBranchTests {

        @Test
        @DisplayName("Should update branch successfully")
        void shouldUpdateBranchSuccessfully() {
            when(branchRepository.findByIdAndTenantIdAndDeletedAtIsNull(branchId, TENANT_ID))
                    .thenReturn(Optional.of(branch));
            when(branchRepository.save(branch)).thenReturn(branch);
            when(adminMapper.toBranchResponse(branch)).thenReturn(response);

            BranchResponse result = branchService.updateBranch(branchId, request);

            assertThat(result).isNotNull();
            verify(adminMapper).updateBranchFromRequest(request, branch);
            verify(branchRepository).save(branch);
        }

        @Test
        @DisplayName("Should throw exception when branch not found")
        void shouldThrowExceptionWhenBranchNotFound() {
            when(branchRepository.findByIdAndTenantIdAndDeletedAtIsNull(branchId, TENANT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> branchService.updateBranch(branchId, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> branchService.updateBranch(null, request))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should throw exception when request is null")
        void shouldThrowExceptionWhenRequestIsNull() {
            assertThatThrownBy(() -> branchService.updateBranch(branchId, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("deleteBranch tests")
    class DeleteBranchTests {

        @Test
        @DisplayName("Should soft delete branch successfully")
        void shouldSoftDeleteBranchSuccessfully() {
            when(branchRepository.findByIdAndTenantIdAndDeletedAtIsNull(branchId, TENANT_ID))
                    .thenReturn(Optional.of(branch));
            when(branchRepository.save(branch)).thenReturn(branch);

            branchService.deleteBranch(branchId);

            verify(branchRepository).save(branch);
        }

        @Test
        @DisplayName("Should throw exception when branch not found")
        void shouldThrowExceptionWhenBranchNotFound() {
            when(branchRepository.findByIdAndTenantIdAndDeletedAtIsNull(branchId, TENANT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> branchService.deleteBranch(branchId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> branchService.deleteBranch(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("activateBranch tests")
    class ActivateBranchTests {

        @Test
        @DisplayName("Should activate branch successfully")
        void shouldActivateBranchSuccessfully() {
            branch.setIsActive(false);
            when(branchRepository.findByIdAndTenantIdAndDeletedAtIsNull(branchId, TENANT_ID))
                    .thenReturn(Optional.of(branch));
            when(branchRepository.save(branch)).thenReturn(branch);
            when(adminMapper.toBranchResponse(branch)).thenReturn(response);

            BranchResponse result = branchService.activateBranch(branchId);

            assertThat(result).isNotNull();
            assertThat(branch.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when branch not found")
        void shouldThrowExceptionWhenBranchNotFound() {
            when(branchRepository.findByIdAndTenantIdAndDeletedAtIsNull(branchId, TENANT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> branchService.activateBranch(branchId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> branchService.activateBranch(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("deactivateBranch tests")
    class DeactivateBranchTests {

        @Test
        @DisplayName("Should deactivate branch successfully")
        void shouldDeactivateBranchSuccessfully() {
            when(branchRepository.findByIdAndTenantIdAndDeletedAtIsNull(branchId, TENANT_ID))
                    .thenReturn(Optional.of(branch));
            when(branchRepository.save(branch)).thenReturn(branch);
            when(adminMapper.toBranchResponse(branch)).thenReturn(response);

            BranchResponse result = branchService.deactivateBranch(branchId);

            assertThat(result).isNotNull();
            assertThat(branch.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should throw exception when branch not found")
        void shouldThrowExceptionWhenBranchNotFound() {
            when(branchRepository.findByIdAndTenantIdAndDeletedAtIsNull(branchId, TENANT_ID))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> branchService.deactivateBranch(branchId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> branchService.deactivateBranch(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
