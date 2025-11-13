package com.cursorpos.admin.mapper;

import com.cursorpos.admin.dto.*;
import com.cursorpos.admin.entity.*;
import org.mapstruct.*;

/**
 * MapStruct mapper for Admin entities and DTOs.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-13
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AdminMapper {

    Tenant toTenant(CreateTenantRequest request);

    TenantResponse toTenantResponse(Tenant tenant);

    Customer toCustomer(CreateCustomerRequest request);

    @Mapping(target = "fullName", expression = "java(customer.getFullName())")
    CustomerResponse toCustomerResponse(Customer customer);

    Store toStore(CreateStoreRequest request);

    StoreResponse toStoreResponse(Store store);

    Branch toBranch(CreateBranchRequest request);

    BranchResponse toBranchResponse(Branch branch);

    Settings toSettings(SettingsRequest request);

    SettingsResponse toSettingsResponse(Settings settings);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateTenantFromRequest(CreateTenantRequest request, @MappingTarget Tenant tenant);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCustomerFromRequest(CreateCustomerRequest request, @MappingTarget Customer customer);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateStoreFromRequest(CreateStoreRequest request, @MappingTarget Store store);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateBranchFromRequest(CreateBranchRequest request, @MappingTarget Branch branch);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSettingsFromRequest(SettingsRequest request, @MappingTarget Settings settings);
}
