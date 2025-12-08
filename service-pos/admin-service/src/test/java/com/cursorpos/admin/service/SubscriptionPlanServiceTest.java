package com.cursorpos.admin.service;

import com.cursorpos.admin.dto.SubscriptionPlanRequest;
import com.cursorpos.admin.dto.SubscriptionPlanResponse;
import com.cursorpos.admin.entity.SubscriptionPlan;
import com.cursorpos.admin.mapper.AdminMapper;
import com.cursorpos.admin.repository.SubscriptionPlanRepository;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SubscriptionPlanService.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-12-04
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({ "PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods", "null" })
class SubscriptionPlanServiceTest {

    @Mock
    private SubscriptionPlanRepository subscriptionPlanRepository;

    @Mock
    private AdminMapper adminMapper;

    @InjectMocks
    private SubscriptionPlanService subscriptionPlanService;

    private UUID planId;
    private SubscriptionPlan plan;
    private SubscriptionPlanRequest request;
    private SubscriptionPlanResponse response;

    @BeforeEach
    void setUp() {
        planId = UUID.randomUUID();

        plan = SubscriptionPlan.builder()
                .id(planId)
                .code("FREE")
                .name("Free Plan")
                .description("Basic free plan")
                .maxUsers(1)
                .maxStores(1)
                .maxProducts(50)
                .priceMonthly(BigDecimal.ZERO)
                .priceYearly(BigDecimal.ZERO)
                .isActive(true)
                .displayOrder(1)
                .build();

        request = SubscriptionPlanRequest.builder()
                .code("FREE")
                .name("Free Plan")
                .description("Basic free plan")
                .maxUsers(1)
                .maxStores(1)
                .maxProducts(50)
                .priceMonthly(BigDecimal.ZERO)
                .priceYearly(BigDecimal.ZERO)
                .isActive(true)
                .displayOrder(1)
                .build();

        response = SubscriptionPlanResponse.builder()
                .id(planId)
                .code("FREE")
                .name("Free Plan")
                .description("Basic free plan")
                .maxUsers(1)
                .maxStores(1)
                .maxProducts(50)
                .priceMonthly(BigDecimal.ZERO)
                .priceYearly(BigDecimal.ZERO)
                .isActive(true)
                .displayOrder(1)
                .build();
    }

    @Nested
    @DisplayName("createPlan tests")
    class CreatePlanTests {

        @Test
        @DisplayName("Should create plan successfully")
        void shouldCreatePlanSuccessfully() {
            when(subscriptionPlanRepository.existsByCodeAndDeletedAtIsNull("FREE")).thenReturn(false);
            when(adminMapper.toSubscriptionPlan(request)).thenReturn(plan);
            when(subscriptionPlanRepository.save(plan)).thenReturn(plan);
            when(adminMapper.toSubscriptionPlanResponse(plan)).thenReturn(response);

            SubscriptionPlanResponse result = subscriptionPlanService.createPlan(request);

            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo("FREE");
            verify(subscriptionPlanRepository).save(plan);
        }

        @Test
        @DisplayName("Should throw exception when plan code already exists")
        void shouldThrowExceptionWhencodeExists() {
            when(subscriptionPlanRepository.existsByCodeAndDeletedAtIsNull("FREE")).thenReturn(true);

            assertThatThrownBy(() -> subscriptionPlanService.createPlan(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("Should throw exception when request is null")
        void shouldThrowExceptionWhenRequestIsNull() {
            assertThatThrownBy(() -> subscriptionPlanService.createPlan(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getPlanById tests")
    class GetPlanByIdTests {

        @Test
        @DisplayName("Should return plan when found")
        void shouldReturnPlanWhenFound() {
            when(subscriptionPlanRepository.findByIdAndDeletedAtIsNull(planId)).thenReturn(Optional.of(plan));
            when(adminMapper.toSubscriptionPlanResponse(plan)).thenReturn(response);

            SubscriptionPlanResponse result = subscriptionPlanService.getPlanById(planId);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(planId);
        }

        @Test
        @DisplayName("Should throw exception when plan not found")
        void shouldThrowExceptionWhenNotFound() {
            when(subscriptionPlanRepository.findByIdAndDeletedAtIsNull(planId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> subscriptionPlanService.getPlanById(planId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> subscriptionPlanService.getPlanById(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getPlanByCode tests")
    class GetPlanByCodeTests {

        @Test
        @DisplayName("Should return plan when found by code")
        void shouldReturnPlanWhenFoundByCode() {
            when(subscriptionPlanRepository.findByCodeAndDeletedAtIsNull("FREE")).thenReturn(Optional.of(plan));
            when(adminMapper.toSubscriptionPlanResponse(plan)).thenReturn(response);

            SubscriptionPlanResponse result = subscriptionPlanService.getPlanByCode("FREE");

            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo("FREE");
        }

        @Test
        @DisplayName("Should throw exception when plan not found by code")
        void shouldThrowExceptionWhenNotFoundByCode() {
            when(subscriptionPlanRepository.findByCodeAndDeletedAtIsNull("INVALID")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> subscriptionPlanService.getPlanByCode("INVALID"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when code is null")
        void shouldThrowExceptionWhenCodeIsNull() {
            assertThatThrownBy(() -> subscriptionPlanService.getPlanByCode(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("getAllPlans tests")
    class GetAllPlansTests {

        @Test
        @DisplayName("Should return all plans paginated")
        void shouldReturnAllPlansPaginated() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<SubscriptionPlan> page = new PageImpl<>(List.of(plan));

            when(subscriptionPlanRepository.findByDeletedAtIsNull(pageable)).thenReturn(page);
            when(adminMapper.toSubscriptionPlanResponse(plan)).thenReturn(response);

            var result = subscriptionPlanService.getAllPlans(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getActivePlans tests")
    class GetActivePlansTests {

        @Test
        @DisplayName("Should return active plans ordered")
        void shouldReturnActivePlansOrdered() {
            when(subscriptionPlanRepository.findByDeletedAtIsNullOrderByDisplayOrderAsc())
                    .thenReturn(List.of(plan));
            when(adminMapper.toSubscriptionPlanResponse(plan)).thenReturn(response);

            List<SubscriptionPlanResponse> result = subscriptionPlanService.getActivePlans();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should filter out inactive plans")
        void shouldFilterOutInactivePlans() {
            SubscriptionPlan inactivePlan = SubscriptionPlan.builder()
                    .id(UUID.randomUUID())
                    .code("INACTIVE")
                    .isActive(false)
                    .build();

            when(subscriptionPlanRepository.findByDeletedAtIsNullOrderByDisplayOrderAsc())
                    .thenReturn(Arrays.asList(plan, inactivePlan));
            when(adminMapper.toSubscriptionPlanResponse(plan)).thenReturn(response);

            List<SubscriptionPlanResponse> result = subscriptionPlanService.getActivePlans();

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("updatePlan tests")
    class UpdatePlanTests {

        @Test
        @DisplayName("Should update plan successfully")
        void shouldUpdatePlanSuccessfully() {
            when(subscriptionPlanRepository.findByIdAndDeletedAtIsNull(planId)).thenReturn(Optional.of(plan));
            when(subscriptionPlanRepository.save(plan)).thenReturn(plan);
            when(adminMapper.toSubscriptionPlanResponse(plan)).thenReturn(response);

            SubscriptionPlanResponse result = subscriptionPlanService.updatePlan(planId, request);

            assertThat(result).isNotNull();
            verify(adminMapper).updateSubscriptionPlanFromRequest(request, plan);
            verify(subscriptionPlanRepository).save(plan);
        }

        @Test
        @DisplayName("Should throw exception when plan not found")
        void shouldThrowExceptionWhenPlanNotFound() {
            when(subscriptionPlanRepository.findByIdAndDeletedAtIsNull(planId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> subscriptionPlanService.updatePlan(planId, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when changing to existing code")
        void shouldThrowExceptionWhenChangingToExistingCode() {
            SubscriptionPlanRequest updateRequest = SubscriptionPlanRequest.builder()
                    .code("BASIC")
                    .name("Basic Plan")
                    .build();

            when(subscriptionPlanRepository.findByIdAndDeletedAtIsNull(planId)).thenReturn(Optional.of(plan));
            when(subscriptionPlanRepository.existsByCodeAndDeletedAtIsNull("BASIC")).thenReturn(true);

            assertThatThrownBy(() -> subscriptionPlanService.updatePlan(planId, updateRequest))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should throw exception when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> subscriptionPlanService.updatePlan(null, request))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should throw exception when request is null")
        void shouldThrowExceptionWhenRequestIsNull() {
            assertThatThrownBy(() -> subscriptionPlanService.updatePlan(planId, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("deletePlan tests")
    class DeletePlanTests {

        @Test
        @DisplayName("Should soft delete plan successfully")
        void shouldSoftDeletePlanSuccessfully() {
            when(subscriptionPlanRepository.findByIdAndDeletedAtIsNull(planId)).thenReturn(Optional.of(plan));
            when(subscriptionPlanRepository.save(plan)).thenReturn(plan);

            subscriptionPlanService.deletePlan(planId);

            verify(subscriptionPlanRepository).save(plan);
        }

        @Test
        @DisplayName("Should throw exception when plan not found")
        void shouldThrowExceptionWhenPlanNotFound() {
            when(subscriptionPlanRepository.findByIdAndDeletedAtIsNull(planId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> subscriptionPlanService.deletePlan(planId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> subscriptionPlanService.deletePlan(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("activatePlan tests")
    class ActivatePlanTests {

        @Test
        @DisplayName("Should activate plan successfully")
        void shouldActivatePlanSuccessfully() {
            plan.setIsActive(false);
            when(subscriptionPlanRepository.findByIdAndDeletedAtIsNull(planId)).thenReturn(Optional.of(plan));
            when(subscriptionPlanRepository.save(plan)).thenReturn(plan);
            when(adminMapper.toSubscriptionPlanResponse(plan)).thenReturn(response);

            SubscriptionPlanResponse result = subscriptionPlanService.activatePlan(planId);

            assertThat(result).isNotNull();
            assertThat(plan.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when plan not found")
        void shouldThrowExceptionWhenPlanNotFound() {
            when(subscriptionPlanRepository.findByIdAndDeletedAtIsNull(planId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> subscriptionPlanService.activatePlan(planId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> subscriptionPlanService.activatePlan(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("deactivatePlan tests")
    class DeactivatePlanTests {

        @Test
        @DisplayName("Should deactivate plan successfully")
        void shouldDeactivatePlanSuccessfully() {
            when(subscriptionPlanRepository.findByIdAndDeletedAtIsNull(planId)).thenReturn(Optional.of(plan));
            when(subscriptionPlanRepository.save(plan)).thenReturn(plan);
            when(adminMapper.toSubscriptionPlanResponse(plan)).thenReturn(response);

            SubscriptionPlanResponse result = subscriptionPlanService.deactivatePlan(planId);

            assertThat(result).isNotNull();
            assertThat(plan.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should throw exception when plan not found")
        void shouldThrowExceptionWhenPlanNotFound() {
            when(subscriptionPlanRepository.findByIdAndDeletedAtIsNull(planId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> subscriptionPlanService.deactivatePlan(planId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("canChangePlan tests")
    class CanChangePlanTests {

        @Test
        @DisplayName("Should return true when within all limits")
        void shouldReturnTrueWhenWithinLimits() {
            when(subscriptionPlanRepository.findByIdAndDeletedAtIsNull(planId)).thenReturn(Optional.of(plan));

            boolean result = subscriptionPlanService.canChangePlan(planId, 1, 1, 50);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when exceeding user limit")
        void shouldReturnFalseWhenExceedingUserLimit() {
            when(subscriptionPlanRepository.findByIdAndDeletedAtIsNull(planId)).thenReturn(Optional.of(plan));

            boolean result = subscriptionPlanService.canChangePlan(planId, 10, 1, 50);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when exceeding store limit")
        void shouldReturnFalseWhenExceedingStoreLimit() {
            when(subscriptionPlanRepository.findByIdAndDeletedAtIsNull(planId)).thenReturn(Optional.of(plan));

            boolean result = subscriptionPlanService.canChangePlan(planId, 1, 10, 50);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when exceeding product limit")
        void shouldReturnFalseWhenExceedingProductLimit() {
            when(subscriptionPlanRepository.findByIdAndDeletedAtIsNull(planId)).thenReturn(Optional.of(plan));

            boolean result = subscriptionPlanService.canChangePlan(planId, 1, 1, 100);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return true when plan has unlimited (-1)")
        void shouldReturnTrueWhenUnlimited() {
            plan.setMaxUsers(-1);
            plan.setMaxStores(-1);
            plan.setMaxProducts(-1);
            when(subscriptionPlanRepository.findByIdAndDeletedAtIsNull(planId)).thenReturn(Optional.of(plan));

            boolean result = subscriptionPlanService.canChangePlan(planId, 1000, 1000, 10000);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should throw exception when plan not found")
        void shouldThrowExceptionWhenPlanNotFound() {
            when(subscriptionPlanRepository.findByIdAndDeletedAtIsNull(planId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> subscriptionPlanService.canChangePlan(planId, 1, 1, 50))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
