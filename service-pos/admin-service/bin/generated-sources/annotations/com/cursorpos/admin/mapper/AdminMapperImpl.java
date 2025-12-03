package com.cursorpos.admin.mapper;

import com.cursorpos.admin.dto.BranchResponse;
import com.cursorpos.admin.dto.CreateBranchRequest;
import com.cursorpos.admin.dto.CreateCustomerRequest;
import com.cursorpos.admin.dto.CreateStoreRequest;
import com.cursorpos.admin.dto.CreateTenantRequest;
import com.cursorpos.admin.dto.CustomerResponse;
import com.cursorpos.admin.dto.SettingsRequest;
import com.cursorpos.admin.dto.SettingsResponse;
import com.cursorpos.admin.dto.StoreResponse;
import com.cursorpos.admin.dto.TenantResponse;
import com.cursorpos.admin.entity.Branch;
import com.cursorpos.admin.entity.Customer;
import com.cursorpos.admin.entity.Settings;
import com.cursorpos.admin.entity.Store;
import com.cursorpos.admin.entity.Tenant;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-03T23:17:16+0700",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.8 (Oracle Corporation)"
)
@Component
public class AdminMapperImpl implements AdminMapper {

    @Override
    public Tenant toTenant(CreateTenantRequest request) {
        if ( request == null ) {
            return null;
        }

        Tenant.TenantBuilder tenant = Tenant.builder();

        tenant.address( request.getAddress() );
        tenant.businessType( request.getBusinessType() );
        tenant.city( request.getCity() );
        tenant.code( request.getCode() );
        tenant.country( request.getCountry() );
        tenant.currency( request.getCurrency() );
        tenant.email( request.getEmail() );
        tenant.locale( request.getLocale() );
        tenant.logoUrl( request.getLogoUrl() );
        tenant.maxBranches( request.getMaxBranches() );
        tenant.maxStores( request.getMaxStores() );
        tenant.maxUsers( request.getMaxUsers() );
        tenant.name( request.getName() );
        tenant.phone( request.getPhone() );
        tenant.postalCode( request.getPostalCode() );
        tenant.state( request.getState() );
        tenant.subdomain( request.getSubdomain() );
        tenant.subscriptionEndDate( request.getSubscriptionEndDate() );
        tenant.subscriptionPlan( request.getSubscriptionPlan() );
        tenant.subscriptionStartDate( request.getSubscriptionStartDate() );
        tenant.taxId( request.getTaxId() );
        tenant.timezone( request.getTimezone() );

        return tenant.build();
    }

    @Override
    public TenantResponse toTenantResponse(Tenant tenant) {
        if ( tenant == null ) {
            return null;
        }

        TenantResponse.TenantResponseBuilder tenantResponse = TenantResponse.builder();

        tenantResponse.address( tenant.getAddress() );
        tenantResponse.businessType( tenant.getBusinessType() );
        tenantResponse.city( tenant.getCity() );
        tenantResponse.code( tenant.getCode() );
        tenantResponse.country( tenant.getCountry() );
        tenantResponse.createdAt( tenant.getCreatedAt() );
        tenantResponse.currency( tenant.getCurrency() );
        tenantResponse.email( tenant.getEmail() );
        tenantResponse.id( tenant.getId() );
        tenantResponse.isActive( tenant.getIsActive() );
        tenantResponse.locale( tenant.getLocale() );
        tenantResponse.logoUrl( tenant.getLogoUrl() );
        tenantResponse.maxBranches( tenant.getMaxBranches() );
        tenantResponse.maxStores( tenant.getMaxStores() );
        tenantResponse.maxUsers( tenant.getMaxUsers() );
        tenantResponse.name( tenant.getName() );
        tenantResponse.phone( tenant.getPhone() );
        tenantResponse.postalCode( tenant.getPostalCode() );
        tenantResponse.state( tenant.getState() );
        tenantResponse.subdomain( tenant.getSubdomain() );
        tenantResponse.subscriptionEndDate( tenant.getSubscriptionEndDate() );
        tenantResponse.subscriptionPlan( tenant.getSubscriptionPlan() );
        tenantResponse.subscriptionStartDate( tenant.getSubscriptionStartDate() );
        tenantResponse.taxId( tenant.getTaxId() );
        tenantResponse.timezone( tenant.getTimezone() );
        tenantResponse.updatedAt( tenant.getUpdatedAt() );

        return tenantResponse.build();
    }

    @Override
    public Customer toCustomer(CreateCustomerRequest request) {
        if ( request == null ) {
            return null;
        }

        Customer.CustomerBuilder customer = Customer.builder();

        customer.address( request.getAddress() );
        customer.city( request.getCity() );
        customer.code( request.getCode() );
        customer.companyName( request.getCompanyName() );
        customer.country( request.getCountry() );
        customer.customerType( request.getCustomerType() );
        customer.email( request.getEmail() );
        customer.firstName( request.getFirstName() );
        customer.lastName( request.getLastName() );
        customer.notes( request.getNotes() );
        customer.phone( request.getPhone() );
        customer.postalCode( request.getPostalCode() );
        customer.state( request.getState() );
        customer.taxId( request.getTaxId() );

        return customer.build();
    }

    @Override
    public CustomerResponse toCustomerResponse(Customer customer) {
        if ( customer == null ) {
            return null;
        }

        CustomerResponse.CustomerResponseBuilder customerResponse = CustomerResponse.builder();

        customerResponse.address( customer.getAddress() );
        customerResponse.city( customer.getCity() );
        customerResponse.code( customer.getCode() );
        customerResponse.companyName( customer.getCompanyName() );
        customerResponse.country( customer.getCountry() );
        customerResponse.createdAt( customer.getCreatedAt() );
        customerResponse.customerType( customer.getCustomerType() );
        customerResponse.email( customer.getEmail() );
        customerResponse.firstName( customer.getFirstName() );
        customerResponse.id( customer.getId() );
        customerResponse.isActive( customer.getIsActive() );
        customerResponse.lastName( customer.getLastName() );
        customerResponse.loyaltyPoints( customer.getLoyaltyPoints() );
        customerResponse.notes( customer.getNotes() );
        customerResponse.phone( customer.getPhone() );
        customerResponse.postalCode( customer.getPostalCode() );
        customerResponse.state( customer.getState() );
        customerResponse.taxId( customer.getTaxId() );
        customerResponse.updatedAt( customer.getUpdatedAt() );

        customerResponse.fullName( customer.getFullName() );

        return customerResponse.build();
    }

    @Override
    public Store toStore(CreateStoreRequest request) {
        if ( request == null ) {
            return null;
        }

        Store.StoreBuilder store = Store.builder();

        store.address( request.getAddress() );
        store.city( request.getCity() );
        store.code( request.getCode() );
        store.country( request.getCountry() );
        store.description( request.getDescription() );
        store.email( request.getEmail() );
        store.latitude( request.getLatitude() );
        store.longitude( request.getLongitude() );
        store.managerEmail( request.getManagerEmail() );
        store.managerName( request.getManagerName() );
        store.managerPhone( request.getManagerPhone() );
        store.name( request.getName() );
        store.operatingHours( request.getOperatingHours() );
        store.phone( request.getPhone() );
        store.postalCode( request.getPostalCode() );
        store.state( request.getState() );
        store.storeType( request.getStoreType() );
        store.timezone( request.getTimezone() );

        return store.build();
    }

    @Override
    public StoreResponse toStoreResponse(Store store) {
        if ( store == null ) {
            return null;
        }

        StoreResponse.StoreResponseBuilder storeResponse = StoreResponse.builder();

        storeResponse.address( store.getAddress() );
        storeResponse.city( store.getCity() );
        storeResponse.code( store.getCode() );
        storeResponse.country( store.getCountry() );
        storeResponse.createdAt( store.getCreatedAt() );
        storeResponse.description( store.getDescription() );
        storeResponse.email( store.getEmail() );
        storeResponse.id( store.getId() );
        storeResponse.isActive( store.getIsActive() );
        storeResponse.latitude( store.getLatitude() );
        storeResponse.longitude( store.getLongitude() );
        storeResponse.managerEmail( store.getManagerEmail() );
        storeResponse.managerName( store.getManagerName() );
        storeResponse.managerPhone( store.getManagerPhone() );
        storeResponse.name( store.getName() );
        storeResponse.operatingHours( store.getOperatingHours() );
        storeResponse.phone( store.getPhone() );
        storeResponse.postalCode( store.getPostalCode() );
        storeResponse.state( store.getState() );
        storeResponse.storeType( store.getStoreType() );
        storeResponse.timezone( store.getTimezone() );
        storeResponse.updatedAt( store.getUpdatedAt() );

        return storeResponse.build();
    }

    @Override
    public Branch toBranch(CreateBranchRequest request) {
        if ( request == null ) {
            return null;
        }

        Branch.BranchBuilder branch = Branch.builder();

        branch.branchType( request.getBranchType() );
        branch.code( request.getCode() );
        branch.description( request.getDescription() );
        branch.managerEmail( request.getManagerEmail() );
        branch.managerName( request.getManagerName() );
        branch.managerPhone( request.getManagerPhone() );
        branch.name( request.getName() );
        branch.storeId( request.getStoreId() );

        return branch.build();
    }

    @Override
    public BranchResponse toBranchResponse(Branch branch) {
        if ( branch == null ) {
            return null;
        }

        BranchResponse.BranchResponseBuilder branchResponse = BranchResponse.builder();

        branchResponse.branchType( branch.getBranchType() );
        branchResponse.code( branch.getCode() );
        branchResponse.createdAt( branch.getCreatedAt() );
        branchResponse.description( branch.getDescription() );
        branchResponse.id( branch.getId() );
        branchResponse.isActive( branch.getIsActive() );
        branchResponse.managerEmail( branch.getManagerEmail() );
        branchResponse.managerName( branch.getManagerName() );
        branchResponse.managerPhone( branch.getManagerPhone() );
        branchResponse.name( branch.getName() );
        branchResponse.storeId( branch.getStoreId() );
        branchResponse.updatedAt( branch.getUpdatedAt() );

        return branchResponse.build();
    }

    @Override
    public Settings toSettings(SettingsRequest request) {
        if ( request == null ) {
            return null;
        }

        Settings.SettingsBuilder settings = Settings.builder();

        settings.category( request.getCategory() );
        settings.description( request.getDescription() );
        settings.isEncrypted( request.getIsEncrypted() );
        settings.settingKey( request.getSettingKey() );
        settings.settingValue( request.getSettingValue() );
        settings.valueType( request.getValueType() );

        return settings.build();
    }

    @Override
    public SettingsResponse toSettingsResponse(Settings settings) {
        if ( settings == null ) {
            return null;
        }

        SettingsResponse.SettingsResponseBuilder settingsResponse = SettingsResponse.builder();

        settingsResponse.category( settings.getCategory() );
        settingsResponse.createdAt( settings.getCreatedAt() );
        settingsResponse.description( settings.getDescription() );
        settingsResponse.id( settings.getId() );
        settingsResponse.isEncrypted( settings.getIsEncrypted() );
        settingsResponse.isSystem( settings.getIsSystem() );
        settingsResponse.settingKey( settings.getSettingKey() );
        settingsResponse.settingValue( settings.getSettingValue() );
        settingsResponse.updatedAt( settings.getUpdatedAt() );
        settingsResponse.valueType( settings.getValueType() );

        return settingsResponse.build();
    }

    @Override
    public void updateTenantFromRequest(CreateTenantRequest request, Tenant tenant) {
        if ( request == null ) {
            return;
        }

        if ( request.getAddress() != null ) {
            tenant.setAddress( request.getAddress() );
        }
        if ( request.getBusinessType() != null ) {
            tenant.setBusinessType( request.getBusinessType() );
        }
        if ( request.getCity() != null ) {
            tenant.setCity( request.getCity() );
        }
        if ( request.getCode() != null ) {
            tenant.setCode( request.getCode() );
        }
        if ( request.getCountry() != null ) {
            tenant.setCountry( request.getCountry() );
        }
        if ( request.getCurrency() != null ) {
            tenant.setCurrency( request.getCurrency() );
        }
        if ( request.getEmail() != null ) {
            tenant.setEmail( request.getEmail() );
        }
        if ( request.getLocale() != null ) {
            tenant.setLocale( request.getLocale() );
        }
        if ( request.getLogoUrl() != null ) {
            tenant.setLogoUrl( request.getLogoUrl() );
        }
        if ( request.getMaxBranches() != null ) {
            tenant.setMaxBranches( request.getMaxBranches() );
        }
        if ( request.getMaxStores() != null ) {
            tenant.setMaxStores( request.getMaxStores() );
        }
        if ( request.getMaxUsers() != null ) {
            tenant.setMaxUsers( request.getMaxUsers() );
        }
        if ( request.getName() != null ) {
            tenant.setName( request.getName() );
        }
        if ( request.getPhone() != null ) {
            tenant.setPhone( request.getPhone() );
        }
        if ( request.getPostalCode() != null ) {
            tenant.setPostalCode( request.getPostalCode() );
        }
        if ( request.getState() != null ) {
            tenant.setState( request.getState() );
        }
        if ( request.getSubdomain() != null ) {
            tenant.setSubdomain( request.getSubdomain() );
        }
        if ( request.getSubscriptionEndDate() != null ) {
            tenant.setSubscriptionEndDate( request.getSubscriptionEndDate() );
        }
        if ( request.getSubscriptionPlan() != null ) {
            tenant.setSubscriptionPlan( request.getSubscriptionPlan() );
        }
        if ( request.getSubscriptionStartDate() != null ) {
            tenant.setSubscriptionStartDate( request.getSubscriptionStartDate() );
        }
        if ( request.getTaxId() != null ) {
            tenant.setTaxId( request.getTaxId() );
        }
        if ( request.getTimezone() != null ) {
            tenant.setTimezone( request.getTimezone() );
        }
    }

    @Override
    public void updateCustomerFromRequest(CreateCustomerRequest request, Customer customer) {
        if ( request == null ) {
            return;
        }

        if ( request.getAddress() != null ) {
            customer.setAddress( request.getAddress() );
        }
        if ( request.getCity() != null ) {
            customer.setCity( request.getCity() );
        }
        if ( request.getCode() != null ) {
            customer.setCode( request.getCode() );
        }
        if ( request.getCompanyName() != null ) {
            customer.setCompanyName( request.getCompanyName() );
        }
        if ( request.getCountry() != null ) {
            customer.setCountry( request.getCountry() );
        }
        if ( request.getCustomerType() != null ) {
            customer.setCustomerType( request.getCustomerType() );
        }
        if ( request.getEmail() != null ) {
            customer.setEmail( request.getEmail() );
        }
        if ( request.getFirstName() != null ) {
            customer.setFirstName( request.getFirstName() );
        }
        if ( request.getLastName() != null ) {
            customer.setLastName( request.getLastName() );
        }
        if ( request.getNotes() != null ) {
            customer.setNotes( request.getNotes() );
        }
        if ( request.getPhone() != null ) {
            customer.setPhone( request.getPhone() );
        }
        if ( request.getPostalCode() != null ) {
            customer.setPostalCode( request.getPostalCode() );
        }
        if ( request.getState() != null ) {
            customer.setState( request.getState() );
        }
        if ( request.getTaxId() != null ) {
            customer.setTaxId( request.getTaxId() );
        }
    }

    @Override
    public void updateStoreFromRequest(CreateStoreRequest request, Store store) {
        if ( request == null ) {
            return;
        }

        if ( request.getAddress() != null ) {
            store.setAddress( request.getAddress() );
        }
        if ( request.getCity() != null ) {
            store.setCity( request.getCity() );
        }
        if ( request.getCode() != null ) {
            store.setCode( request.getCode() );
        }
        if ( request.getCountry() != null ) {
            store.setCountry( request.getCountry() );
        }
        if ( request.getDescription() != null ) {
            store.setDescription( request.getDescription() );
        }
        if ( request.getEmail() != null ) {
            store.setEmail( request.getEmail() );
        }
        if ( request.getLatitude() != null ) {
            store.setLatitude( request.getLatitude() );
        }
        if ( request.getLongitude() != null ) {
            store.setLongitude( request.getLongitude() );
        }
        if ( request.getManagerEmail() != null ) {
            store.setManagerEmail( request.getManagerEmail() );
        }
        if ( request.getManagerName() != null ) {
            store.setManagerName( request.getManagerName() );
        }
        if ( request.getManagerPhone() != null ) {
            store.setManagerPhone( request.getManagerPhone() );
        }
        if ( request.getName() != null ) {
            store.setName( request.getName() );
        }
        if ( request.getOperatingHours() != null ) {
            store.setOperatingHours( request.getOperatingHours() );
        }
        if ( request.getPhone() != null ) {
            store.setPhone( request.getPhone() );
        }
        if ( request.getPostalCode() != null ) {
            store.setPostalCode( request.getPostalCode() );
        }
        if ( request.getState() != null ) {
            store.setState( request.getState() );
        }
        if ( request.getStoreType() != null ) {
            store.setStoreType( request.getStoreType() );
        }
        if ( request.getTimezone() != null ) {
            store.setTimezone( request.getTimezone() );
        }
    }

    @Override
    public void updateBranchFromRequest(CreateBranchRequest request, Branch branch) {
        if ( request == null ) {
            return;
        }

        if ( request.getBranchType() != null ) {
            branch.setBranchType( request.getBranchType() );
        }
        if ( request.getCode() != null ) {
            branch.setCode( request.getCode() );
        }
        if ( request.getDescription() != null ) {
            branch.setDescription( request.getDescription() );
        }
        if ( request.getManagerEmail() != null ) {
            branch.setManagerEmail( request.getManagerEmail() );
        }
        if ( request.getManagerName() != null ) {
            branch.setManagerName( request.getManagerName() );
        }
        if ( request.getManagerPhone() != null ) {
            branch.setManagerPhone( request.getManagerPhone() );
        }
        if ( request.getName() != null ) {
            branch.setName( request.getName() );
        }
        if ( request.getStoreId() != null ) {
            branch.setStoreId( request.getStoreId() );
        }
    }

    @Override
    public void updateSettingsFromRequest(SettingsRequest request, Settings settings) {
        if ( request == null ) {
            return;
        }

        if ( request.getCategory() != null ) {
            settings.setCategory( request.getCategory() );
        }
        if ( request.getDescription() != null ) {
            settings.setDescription( request.getDescription() );
        }
        if ( request.getIsEncrypted() != null ) {
            settings.setIsEncrypted( request.getIsEncrypted() );
        }
        if ( request.getSettingKey() != null ) {
            settings.setSettingKey( request.getSettingKey() );
        }
        if ( request.getSettingValue() != null ) {
            settings.setSettingValue( request.getSettingValue() );
        }
        if ( request.getValueType() != null ) {
            settings.setValueType( request.getValueType() );
        }
    }
}
