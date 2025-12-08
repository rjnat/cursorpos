package com.cursorpos.admin.mapper;

import com.cursorpos.admin.dto.*;
import com.cursorpos.admin.entity.*;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Admin entities and DTOs.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdminMapper {

    // ========== Tenant Mappings ==========
    Tenant toTenant(CreateTenantRequest request);

    @Mapping(target = "hasActiveSubscription", expression = "java(tenant.hasActiveSubscription())")
    TenantResponse toTenantResponse(Tenant tenant);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateTenantFromRequest(CreateTenantRequest request, @MappingTarget Tenant tenant);

    // ========== Customer Mappings ==========
    Customer toCustomer(CreateCustomerRequest request);

    @Mapping(target = "fullName", expression = "java(customer.getFullName())")
    CustomerResponse toCustomerResponse(Customer customer);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCustomerFromRequest(CreateCustomerRequest request, @MappingTarget Customer customer);

    // ========== Store Mappings ==========
    Store toStore(CreateStoreRequest request);

    StoreResponse toStoreResponse(Store store);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateStoreFromRequest(CreateStoreRequest request, @MappingTarget Store store);

    // ========== Branch Mappings ==========
    Branch toBranch(CreateBranchRequest request);

    BranchResponse toBranchResponse(Branch branch);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateBranchFromRequest(CreateBranchRequest request, @MappingTarget Branch branch);

    // ========== Settings Mappings ==========
    Settings toSettings(SettingsRequest request);

    SettingsResponse toSettingsResponse(Settings settings);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSettingsFromRequest(SettingsRequest request, @MappingTarget Settings settings);

    // ========== Subscription Plan Mappings ==========
    @Mapping(target = "features", expression = "java(featuresToJson(request.getFeatures()))")
    SubscriptionPlan toSubscriptionPlan(SubscriptionPlanRequest request);

    SubscriptionPlanResponse toSubscriptionPlanResponse(SubscriptionPlan plan);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "features", expression = "java(featuresToJson(request.getFeatures()))")
    void updateSubscriptionPlanFromRequest(SubscriptionPlanRequest request, @MappingTarget SubscriptionPlan plan);

    // ========== Loyalty Tier Mappings ==========
    LoyaltyTier toLoyaltyTier(LoyaltyTierRequest request);

    LoyaltyTierResponse toLoyaltyTierResponse(LoyaltyTier tier);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateLoyaltyTierFromRequest(LoyaltyTierRequest request, @MappingTarget LoyaltyTier tier);

    // ========== Loyalty Transaction Mappings ==========
    @Mapping(target = "points", source = "pointsChange")
    LoyaltyTransaction toLoyaltyTransaction(LoyaltyTransactionRequest request);

    LoyaltyTransactionResponse toLoyaltyTransactionResponse(LoyaltyTransaction transaction);

    // ========== Store Price Override Mappings ==========
    StorePriceOverride toStorePriceOverride(StorePriceOverrideRequest request);

    StorePriceOverrideResponse toStorePriceOverrideResponse(StorePriceOverride override);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateStorePriceOverrideFromRequest(StorePriceOverrideRequest request,
            @MappingTarget StorePriceOverride override);

    // ========== Helper Methods ==========
    default String featuresToJson(List<String> features) {
        if (features == null || features.isEmpty()) {
            return null;
        }
        return "[\"" + String.join("\",\"", features) + "\"]";
    }
}
