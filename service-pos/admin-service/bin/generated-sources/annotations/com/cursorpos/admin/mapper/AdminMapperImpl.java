package com.cursorpos.admin.mapper;

import com.cursorpos.admin.dto.BranchResponse;
import com.cursorpos.admin.dto.CreateBranchRequest;
import com.cursorpos.admin.dto.CreateCustomerRequest;
import com.cursorpos.admin.dto.CreateStoreRequest;
import com.cursorpos.admin.dto.CreateTenantRequest;
import com.cursorpos.admin.dto.CustomerResponse;
import com.cursorpos.admin.dto.LoyaltyTierRequest;
import com.cursorpos.admin.dto.LoyaltyTierResponse;
import com.cursorpos.admin.dto.LoyaltyTransactionRequest;
import com.cursorpos.admin.dto.LoyaltyTransactionResponse;
import com.cursorpos.admin.dto.SettingsRequest;
import com.cursorpos.admin.dto.SettingsResponse;
import com.cursorpos.admin.dto.StorePriceOverrideRequest;
import com.cursorpos.admin.dto.StorePriceOverrideResponse;
import com.cursorpos.admin.dto.StoreResponse;
import com.cursorpos.admin.dto.SubscriptionPlanRequest;
import com.cursorpos.admin.dto.SubscriptionPlanResponse;
import com.cursorpos.admin.dto.TenantResponse;
import com.cursorpos.admin.entity.Branch;
import com.cursorpos.admin.entity.Customer;
import com.cursorpos.admin.entity.LoyaltyTier;
import com.cursorpos.admin.entity.LoyaltyTransaction;
import com.cursorpos.admin.entity.Settings;
import com.cursorpos.admin.entity.Store;
import com.cursorpos.admin.entity.StorePriceOverride;
import com.cursorpos.admin.entity.SubscriptionPlan;
import com.cursorpos.admin.entity.Tenant;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-05T23:55:27+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.8 (Oracle Corporation)"
)
@Component
public class AdminMapperImpl implements AdminMapper {

    @Override
    public Tenant toTenant(CreateTenantRequest request) {
        if ( request == null ) {
            return null;
        }

        Tenant tenant = new Tenant();

        tenant.setCode( request.getCode() );
        tenant.setName( request.getName() );
        tenant.setSubdomain( request.getSubdomain() );
        tenant.setBusinessType( request.getBusinessType() );
        tenant.setEmail( request.getEmail() );
        tenant.setPhone( request.getPhone() );
        tenant.setAddress( request.getAddress() );
        tenant.setCity( request.getCity() );
        tenant.setState( request.getState() );
        tenant.setCountry( request.getCountry() );
        tenant.setPostalCode( request.getPostalCode() );
        tenant.setTaxId( request.getTaxId() );
        tenant.setSubscriptionPlanId( request.getSubscriptionPlanId() );
        tenant.setSubscriptionStartDate( request.getSubscriptionStartDate() );
        tenant.setSubscriptionEndDate( request.getSubscriptionEndDate() );
        tenant.setLogoUrl( request.getLogoUrl() );
        tenant.setTimezone( request.getTimezone() );
        tenant.setCurrency( request.getCurrency() );
        tenant.setLocale( request.getLocale() );
        tenant.setLoyaltyPointsPerCurrency( request.getLoyaltyPointsPerCurrency() );
        tenant.setLoyaltyEnabled( request.getLoyaltyEnabled() );

        return tenant;
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
        tenantResponse.loyaltyEnabled( tenant.getLoyaltyEnabled() );
        tenantResponse.loyaltyPointsPerCurrency( tenant.getLoyaltyPointsPerCurrency() );
        tenantResponse.name( tenant.getName() );
        tenantResponse.phone( tenant.getPhone() );
        tenantResponse.postalCode( tenant.getPostalCode() );
        tenantResponse.state( tenant.getState() );
        tenantResponse.subdomain( tenant.getSubdomain() );
        tenantResponse.subscriptionEndDate( tenant.getSubscriptionEndDate() );
        tenantResponse.subscriptionPlan( toSubscriptionPlanResponse( tenant.getSubscriptionPlan() ) );
        tenantResponse.subscriptionPlanId( tenant.getSubscriptionPlanId() );
        tenantResponse.subscriptionStartDate( tenant.getSubscriptionStartDate() );
        tenantResponse.subscriptionStatus( tenant.getSubscriptionStatus() );
        tenantResponse.taxId( tenant.getTaxId() );
        tenantResponse.timezone( tenant.getTimezone() );
        tenantResponse.updatedAt( tenant.getUpdatedAt() );

        tenantResponse.hasActiveSubscription( tenant.hasActiveSubscription() );

        return tenantResponse.build();
    }

    @Override
    public void updateTenantFromRequest(CreateTenantRequest request, Tenant tenant) {
        if ( request == null ) {
            return;
        }

        if ( request.getCode() != null ) {
            tenant.setCode( request.getCode() );
        }
        if ( request.getName() != null ) {
            tenant.setName( request.getName() );
        }
        if ( request.getSubdomain() != null ) {
            tenant.setSubdomain( request.getSubdomain() );
        }
        if ( request.getBusinessType() != null ) {
            tenant.setBusinessType( request.getBusinessType() );
        }
        if ( request.getEmail() != null ) {
            tenant.setEmail( request.getEmail() );
        }
        if ( request.getPhone() != null ) {
            tenant.setPhone( request.getPhone() );
        }
        if ( request.getAddress() != null ) {
            tenant.setAddress( request.getAddress() );
        }
        if ( request.getCity() != null ) {
            tenant.setCity( request.getCity() );
        }
        if ( request.getState() != null ) {
            tenant.setState( request.getState() );
        }
        if ( request.getCountry() != null ) {
            tenant.setCountry( request.getCountry() );
        }
        if ( request.getPostalCode() != null ) {
            tenant.setPostalCode( request.getPostalCode() );
        }
        if ( request.getTaxId() != null ) {
            tenant.setTaxId( request.getTaxId() );
        }
        if ( request.getSubscriptionPlanId() != null ) {
            tenant.setSubscriptionPlanId( request.getSubscriptionPlanId() );
        }
        if ( request.getSubscriptionStartDate() != null ) {
            tenant.setSubscriptionStartDate( request.getSubscriptionStartDate() );
        }
        if ( request.getSubscriptionEndDate() != null ) {
            tenant.setSubscriptionEndDate( request.getSubscriptionEndDate() );
        }
        if ( request.getLogoUrl() != null ) {
            tenant.setLogoUrl( request.getLogoUrl() );
        }
        if ( request.getTimezone() != null ) {
            tenant.setTimezone( request.getTimezone() );
        }
        if ( request.getCurrency() != null ) {
            tenant.setCurrency( request.getCurrency() );
        }
        if ( request.getLocale() != null ) {
            tenant.setLocale( request.getLocale() );
        }
        if ( request.getLoyaltyPointsPerCurrency() != null ) {
            tenant.setLoyaltyPointsPerCurrency( request.getLoyaltyPointsPerCurrency() );
        }
        if ( request.getLoyaltyEnabled() != null ) {
            tenant.setLoyaltyEnabled( request.getLoyaltyEnabled() );
        }
    }

    @Override
    public Customer toCustomer(CreateCustomerRequest request) {
        if ( request == null ) {
            return null;
        }

        Customer customer = new Customer();

        customer.setCode( request.getCode() );
        customer.setFirstName( request.getFirstName() );
        customer.setLastName( request.getLastName() );
        customer.setEmail( request.getEmail() );
        customer.setPhone( request.getPhone() );
        customer.setAddress( request.getAddress() );
        customer.setCity( request.getCity() );
        customer.setState( request.getState() );
        customer.setCountry( request.getCountry() );
        customer.setPostalCode( request.getPostalCode() );
        customer.setDateOfBirth( request.getDateOfBirth() );
        customer.setNotes( request.getNotes() );

        return customer;
    }

    @Override
    public CustomerResponse toCustomerResponse(Customer customer) {
        if ( customer == null ) {
            return null;
        }

        CustomerResponse customerResponse = new CustomerResponse();

        customerResponse.setId( customer.getId() );
        customerResponse.setCode( customer.getCode() );
        customerResponse.setFirstName( customer.getFirstName() );
        customerResponse.setLastName( customer.getLastName() );
        customerResponse.setEmail( customer.getEmail() );
        customerResponse.setPhone( customer.getPhone() );
        customerResponse.setAddress( customer.getAddress() );
        customerResponse.setCity( customer.getCity() );
        customerResponse.setState( customer.getState() );
        customerResponse.setCountry( customer.getCountry() );
        customerResponse.setPostalCode( customer.getPostalCode() );
        customerResponse.setDateOfBirth( customer.getDateOfBirth() );
        customerResponse.setIsActive( customer.getIsActive() );
        customerResponse.setNotes( customer.getNotes() );
        customerResponse.setLoyaltyTierId( customer.getLoyaltyTierId() );
        customerResponse.setLoyaltyTier( toLoyaltyTierResponse( customer.getLoyaltyTier() ) );
        customerResponse.setTotalPoints( customer.getTotalPoints() );
        customerResponse.setAvailablePoints( customer.getAvailablePoints() );
        customerResponse.setLifetimePoints( customer.getLifetimePoints() );
        customerResponse.setCreatedAt( customer.getCreatedAt() );
        customerResponse.setUpdatedAt( customer.getUpdatedAt() );

        customerResponse.setFullName( customer.getFullName() );

        return customerResponse;
    }

    @Override
    public void updateCustomerFromRequest(CreateCustomerRequest request, Customer customer) {
        if ( request == null ) {
            return;
        }

        if ( request.getCode() != null ) {
            customer.setCode( request.getCode() );
        }
        if ( request.getFirstName() != null ) {
            customer.setFirstName( request.getFirstName() );
        }
        if ( request.getLastName() != null ) {
            customer.setLastName( request.getLastName() );
        }
        if ( request.getEmail() != null ) {
            customer.setEmail( request.getEmail() );
        }
        if ( request.getPhone() != null ) {
            customer.setPhone( request.getPhone() );
        }
        if ( request.getAddress() != null ) {
            customer.setAddress( request.getAddress() );
        }
        if ( request.getCity() != null ) {
            customer.setCity( request.getCity() );
        }
        if ( request.getState() != null ) {
            customer.setState( request.getState() );
        }
        if ( request.getCountry() != null ) {
            customer.setCountry( request.getCountry() );
        }
        if ( request.getPostalCode() != null ) {
            customer.setPostalCode( request.getPostalCode() );
        }
        if ( request.getDateOfBirth() != null ) {
            customer.setDateOfBirth( request.getDateOfBirth() );
        }
        if ( request.getNotes() != null ) {
            customer.setNotes( request.getNotes() );
        }
    }

    @Override
    public Store toStore(CreateStoreRequest request) {
        if ( request == null ) {
            return null;
        }

        Store store = new Store();

        store.setBranchId( request.getBranchId() );
        store.setCode( request.getCode() );
        store.setName( request.getName() );
        store.setDescription( request.getDescription() );
        store.setStoreType( request.getStoreType() );
        store.setEmail( request.getEmail() );
        store.setPhone( request.getPhone() );
        store.setAddress( request.getAddress() );
        store.setCity( request.getCity() );
        store.setState( request.getState() );
        store.setCountry( request.getCountry() );
        store.setPostalCode( request.getPostalCode() );
        store.setLatitude( request.getLatitude() );
        store.setLongitude( request.getLongitude() );
        store.setManagerName( request.getManagerName() );
        store.setManagerEmail( request.getManagerEmail() );
        store.setManagerPhone( request.getManagerPhone() );
        store.setOperatingHours( request.getOperatingHours() );
        store.setTimezone( request.getTimezone() );
        store.setCurrency( request.getCurrency() );
        store.setTaxRate( request.getTaxRate() );
        store.setGlobalDiscountPercentage( request.getGlobalDiscountPercentage() );

        return store;
    }

    @Override
    public StoreResponse toStoreResponse(Store store) {
        if ( store == null ) {
            return null;
        }

        StoreResponse storeResponse = new StoreResponse();

        storeResponse.setId( store.getId() );
        storeResponse.setBranchId( store.getBranchId() );
        storeResponse.setCode( store.getCode() );
        storeResponse.setName( store.getName() );
        storeResponse.setDescription( store.getDescription() );
        storeResponse.setStoreType( store.getStoreType() );
        storeResponse.setEmail( store.getEmail() );
        storeResponse.setPhone( store.getPhone() );
        storeResponse.setAddress( store.getAddress() );
        storeResponse.setCity( store.getCity() );
        storeResponse.setState( store.getState() );
        storeResponse.setCountry( store.getCountry() );
        storeResponse.setPostalCode( store.getPostalCode() );
        storeResponse.setLatitude( store.getLatitude() );
        storeResponse.setLongitude( store.getLongitude() );
        storeResponse.setIsActive( store.getIsActive() );
        storeResponse.setManagerName( store.getManagerName() );
        storeResponse.setManagerEmail( store.getManagerEmail() );
        storeResponse.setManagerPhone( store.getManagerPhone() );
        storeResponse.setOperatingHours( store.getOperatingHours() );
        storeResponse.setTimezone( store.getTimezone() );
        storeResponse.setCurrency( store.getCurrency() );
        storeResponse.setTaxRate( store.getTaxRate() );
        storeResponse.setGlobalDiscountPercentage( store.getGlobalDiscountPercentage() );
        storeResponse.setCreatedAt( store.getCreatedAt() );
        storeResponse.setUpdatedAt( store.getUpdatedAt() );

        return storeResponse;
    }

    @Override
    public void updateStoreFromRequest(CreateStoreRequest request, Store store) {
        if ( request == null ) {
            return;
        }

        if ( request.getBranchId() != null ) {
            store.setBranchId( request.getBranchId() );
        }
        if ( request.getCode() != null ) {
            store.setCode( request.getCode() );
        }
        if ( request.getName() != null ) {
            store.setName( request.getName() );
        }
        if ( request.getDescription() != null ) {
            store.setDescription( request.getDescription() );
        }
        if ( request.getStoreType() != null ) {
            store.setStoreType( request.getStoreType() );
        }
        if ( request.getEmail() != null ) {
            store.setEmail( request.getEmail() );
        }
        if ( request.getPhone() != null ) {
            store.setPhone( request.getPhone() );
        }
        if ( request.getAddress() != null ) {
            store.setAddress( request.getAddress() );
        }
        if ( request.getCity() != null ) {
            store.setCity( request.getCity() );
        }
        if ( request.getState() != null ) {
            store.setState( request.getState() );
        }
        if ( request.getCountry() != null ) {
            store.setCountry( request.getCountry() );
        }
        if ( request.getPostalCode() != null ) {
            store.setPostalCode( request.getPostalCode() );
        }
        if ( request.getLatitude() != null ) {
            store.setLatitude( request.getLatitude() );
        }
        if ( request.getLongitude() != null ) {
            store.setLongitude( request.getLongitude() );
        }
        if ( request.getManagerName() != null ) {
            store.setManagerName( request.getManagerName() );
        }
        if ( request.getManagerEmail() != null ) {
            store.setManagerEmail( request.getManagerEmail() );
        }
        if ( request.getManagerPhone() != null ) {
            store.setManagerPhone( request.getManagerPhone() );
        }
        if ( request.getOperatingHours() != null ) {
            store.setOperatingHours( request.getOperatingHours() );
        }
        if ( request.getTimezone() != null ) {
            store.setTimezone( request.getTimezone() );
        }
        if ( request.getCurrency() != null ) {
            store.setCurrency( request.getCurrency() );
        }
        if ( request.getTaxRate() != null ) {
            store.setTaxRate( request.getTaxRate() );
        }
        if ( request.getGlobalDiscountPercentage() != null ) {
            store.setGlobalDiscountPercentage( request.getGlobalDiscountPercentage() );
        }
    }

    @Override
    public Branch toBranch(CreateBranchRequest request) {
        if ( request == null ) {
            return null;
        }

        Branch branch = new Branch();

        branch.setCode( request.getCode() );
        branch.setName( request.getName() );
        branch.setDescription( request.getDescription() );
        branch.setAddress( request.getAddress() );
        branch.setCity( request.getCity() );
        branch.setState( request.getState() );
        branch.setCountry( request.getCountry() );
        branch.setPostalCode( request.getPostalCode() );
        branch.setPhone( request.getPhone() );
        branch.setEmail( request.getEmail() );
        branch.setManagerName( request.getManagerName() );
        branch.setManagerEmail( request.getManagerEmail() );
        branch.setManagerPhone( request.getManagerPhone() );

        return branch;
    }

    @Override
    public BranchResponse toBranchResponse(Branch branch) {
        if ( branch == null ) {
            return null;
        }

        BranchResponse branchResponse = new BranchResponse();

        branchResponse.setId( branch.getId() );
        branchResponse.setCode( branch.getCode() );
        branchResponse.setName( branch.getName() );
        branchResponse.setDescription( branch.getDescription() );
        branchResponse.setAddress( branch.getAddress() );
        branchResponse.setCity( branch.getCity() );
        branchResponse.setState( branch.getState() );
        branchResponse.setCountry( branch.getCountry() );
        branchResponse.setPostalCode( branch.getPostalCode() );
        branchResponse.setPhone( branch.getPhone() );
        branchResponse.setEmail( branch.getEmail() );
        branchResponse.setIsActive( branch.getIsActive() );
        branchResponse.setManagerName( branch.getManagerName() );
        branchResponse.setManagerEmail( branch.getManagerEmail() );
        branchResponse.setManagerPhone( branch.getManagerPhone() );
        branchResponse.setCreatedAt( branch.getCreatedAt() );
        branchResponse.setUpdatedAt( branch.getUpdatedAt() );

        return branchResponse;
    }

    @Override
    public void updateBranchFromRequest(CreateBranchRequest request, Branch branch) {
        if ( request == null ) {
            return;
        }

        if ( request.getCode() != null ) {
            branch.setCode( request.getCode() );
        }
        if ( request.getName() != null ) {
            branch.setName( request.getName() );
        }
        if ( request.getDescription() != null ) {
            branch.setDescription( request.getDescription() );
        }
        if ( request.getAddress() != null ) {
            branch.setAddress( request.getAddress() );
        }
        if ( request.getCity() != null ) {
            branch.setCity( request.getCity() );
        }
        if ( request.getState() != null ) {
            branch.setState( request.getState() );
        }
        if ( request.getCountry() != null ) {
            branch.setCountry( request.getCountry() );
        }
        if ( request.getPostalCode() != null ) {
            branch.setPostalCode( request.getPostalCode() );
        }
        if ( request.getPhone() != null ) {
            branch.setPhone( request.getPhone() );
        }
        if ( request.getEmail() != null ) {
            branch.setEmail( request.getEmail() );
        }
        if ( request.getManagerName() != null ) {
            branch.setManagerName( request.getManagerName() );
        }
        if ( request.getManagerEmail() != null ) {
            branch.setManagerEmail( request.getManagerEmail() );
        }
        if ( request.getManagerPhone() != null ) {
            branch.setManagerPhone( request.getManagerPhone() );
        }
    }

    @Override
    public Settings toSettings(SettingsRequest request) {
        if ( request == null ) {
            return null;
        }

        Settings settings = new Settings();

        settings.setCategory( request.getCategory() );
        settings.setSettingKey( request.getSettingKey() );
        settings.setSettingValue( request.getSettingValue() );
        settings.setValueType( request.getValueType() );
        settings.setDescription( request.getDescription() );
        settings.setIsEncrypted( request.getIsEncrypted() );

        return settings;
    }

    @Override
    public SettingsResponse toSettingsResponse(Settings settings) {
        if ( settings == null ) {
            return null;
        }

        SettingsResponse settingsResponse = new SettingsResponse();

        settingsResponse.setId( settings.getId() );
        settingsResponse.setCategory( settings.getCategory() );
        settingsResponse.setSettingKey( settings.getSettingKey() );
        settingsResponse.setSettingValue( settings.getSettingValue() );
        settingsResponse.setValueType( settings.getValueType() );
        settingsResponse.setDescription( settings.getDescription() );
        settingsResponse.setIsSystem( settings.getIsSystem() );
        settingsResponse.setIsEncrypted( settings.getIsEncrypted() );
        settingsResponse.setCreatedAt( settings.getCreatedAt() );
        settingsResponse.setUpdatedAt( settings.getUpdatedAt() );

        return settingsResponse;
    }

    @Override
    public void updateSettingsFromRequest(SettingsRequest request, Settings settings) {
        if ( request == null ) {
            return;
        }

        if ( request.getCategory() != null ) {
            settings.setCategory( request.getCategory() );
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
        if ( request.getDescription() != null ) {
            settings.setDescription( request.getDescription() );
        }
        if ( request.getIsEncrypted() != null ) {
            settings.setIsEncrypted( request.getIsEncrypted() );
        }
    }

    @Override
    public SubscriptionPlan toSubscriptionPlan(SubscriptionPlanRequest request) {
        if ( request == null ) {
            return null;
        }

        SubscriptionPlan subscriptionPlan = new SubscriptionPlan();

        subscriptionPlan.setCode( request.getCode() );
        subscriptionPlan.setName( request.getName() );
        subscriptionPlan.setDescription( request.getDescription() );
        subscriptionPlan.setMaxUsers( request.getMaxUsers() );
        subscriptionPlan.setMaxStores( request.getMaxStores() );
        subscriptionPlan.setMaxProducts( request.getMaxProducts() );
        subscriptionPlan.setPriceMonthly( request.getPriceMonthly() );
        subscriptionPlan.setPriceYearly( request.getPriceYearly() );
        subscriptionPlan.setIsActive( request.getIsActive() );
        subscriptionPlan.setDisplayOrder( request.getDisplayOrder() );

        subscriptionPlan.setFeatures( featuresToJson(request.getFeatures()) );

        return subscriptionPlan;
    }

    @Override
    public SubscriptionPlanResponse toSubscriptionPlanResponse(SubscriptionPlan plan) {
        if ( plan == null ) {
            return null;
        }

        SubscriptionPlanResponse subscriptionPlanResponse = new SubscriptionPlanResponse();

        subscriptionPlanResponse.setId( plan.getId() );
        subscriptionPlanResponse.setCode( plan.getCode() );
        subscriptionPlanResponse.setName( plan.getName() );
        subscriptionPlanResponse.setDescription( plan.getDescription() );
        subscriptionPlanResponse.setMaxUsers( plan.getMaxUsers() );
        subscriptionPlanResponse.setMaxStores( plan.getMaxStores() );
        subscriptionPlanResponse.setMaxProducts( plan.getMaxProducts() );
        subscriptionPlanResponse.setPriceMonthly( plan.getPriceMonthly() );
        subscriptionPlanResponse.setPriceYearly( plan.getPriceYearly() );
        subscriptionPlanResponse.setIsActive( plan.getIsActive() );
        subscriptionPlanResponse.setDisplayOrder( plan.getDisplayOrder() );
        subscriptionPlanResponse.setFeatures( plan.getFeatures() );
        subscriptionPlanResponse.setCreatedAt( plan.getCreatedAt() );
        subscriptionPlanResponse.setUpdatedAt( plan.getUpdatedAt() );

        return subscriptionPlanResponse;
    }

    @Override
    public void updateSubscriptionPlanFromRequest(SubscriptionPlanRequest request, SubscriptionPlan plan) {
        if ( request == null ) {
            return;
        }

        if ( request.getCode() != null ) {
            plan.setCode( request.getCode() );
        }
        if ( request.getName() != null ) {
            plan.setName( request.getName() );
        }
        if ( request.getDescription() != null ) {
            plan.setDescription( request.getDescription() );
        }
        if ( request.getMaxUsers() != null ) {
            plan.setMaxUsers( request.getMaxUsers() );
        }
        if ( request.getMaxStores() != null ) {
            plan.setMaxStores( request.getMaxStores() );
        }
        if ( request.getMaxProducts() != null ) {
            plan.setMaxProducts( request.getMaxProducts() );
        }
        if ( request.getPriceMonthly() != null ) {
            plan.setPriceMonthly( request.getPriceMonthly() );
        }
        if ( request.getPriceYearly() != null ) {
            plan.setPriceYearly( request.getPriceYearly() );
        }
        if ( request.getIsActive() != null ) {
            plan.setIsActive( request.getIsActive() );
        }
        if ( request.getDisplayOrder() != null ) {
            plan.setDisplayOrder( request.getDisplayOrder() );
        }

        plan.setFeatures( featuresToJson(request.getFeatures()) );
    }

    @Override
    public LoyaltyTier toLoyaltyTier(LoyaltyTierRequest request) {
        if ( request == null ) {
            return null;
        }

        LoyaltyTier loyaltyTier = new LoyaltyTier();

        loyaltyTier.setCode( request.getCode() );
        loyaltyTier.setName( request.getName() );
        loyaltyTier.setMinPoints( request.getMinPoints() );
        loyaltyTier.setDiscountPercentage( request.getDiscountPercentage() );
        loyaltyTier.setPointsMultiplier( request.getPointsMultiplier() );
        loyaltyTier.setColor( request.getColor() );
        loyaltyTier.setIcon( request.getIcon() );
        loyaltyTier.setBenefits( request.getBenefits() );
        loyaltyTier.setDisplayOrder( request.getDisplayOrder() );

        return loyaltyTier;
    }

    @Override
    public LoyaltyTierResponse toLoyaltyTierResponse(LoyaltyTier tier) {
        if ( tier == null ) {
            return null;
        }

        LoyaltyTierResponse loyaltyTierResponse = new LoyaltyTierResponse();

        loyaltyTierResponse.setId( tier.getId() );
        loyaltyTierResponse.setCode( tier.getCode() );
        loyaltyTierResponse.setName( tier.getName() );
        loyaltyTierResponse.setMinPoints( tier.getMinPoints() );
        loyaltyTierResponse.setDiscountPercentage( tier.getDiscountPercentage() );
        loyaltyTierResponse.setPointsMultiplier( tier.getPointsMultiplier() );
        loyaltyTierResponse.setColor( tier.getColor() );
        loyaltyTierResponse.setIcon( tier.getIcon() );
        loyaltyTierResponse.setBenefits( tier.getBenefits() );
        loyaltyTierResponse.setDisplayOrder( tier.getDisplayOrder() );
        loyaltyTierResponse.setIsActive( tier.getIsActive() );

        return loyaltyTierResponse;
    }

    @Override
    public void updateLoyaltyTierFromRequest(LoyaltyTierRequest request, LoyaltyTier tier) {
        if ( request == null ) {
            return;
        }

        if ( request.getCode() != null ) {
            tier.setCode( request.getCode() );
        }
        if ( request.getName() != null ) {
            tier.setName( request.getName() );
        }
        if ( request.getMinPoints() != null ) {
            tier.setMinPoints( request.getMinPoints() );
        }
        if ( request.getDiscountPercentage() != null ) {
            tier.setDiscountPercentage( request.getDiscountPercentage() );
        }
        if ( request.getPointsMultiplier() != null ) {
            tier.setPointsMultiplier( request.getPointsMultiplier() );
        }
        if ( request.getColor() != null ) {
            tier.setColor( request.getColor() );
        }
        if ( request.getIcon() != null ) {
            tier.setIcon( request.getIcon() );
        }
        if ( request.getBenefits() != null ) {
            tier.setBenefits( request.getBenefits() );
        }
        if ( request.getDisplayOrder() != null ) {
            tier.setDisplayOrder( request.getDisplayOrder() );
        }
    }

    @Override
    public LoyaltyTransaction toLoyaltyTransaction(LoyaltyTransactionRequest request) {
        if ( request == null ) {
            return null;
        }

        LoyaltyTransaction loyaltyTransaction = new LoyaltyTransaction();

        loyaltyTransaction.setPoints( request.getPointsChange() );
        loyaltyTransaction.setCustomerId( request.getCustomerId() );
        loyaltyTransaction.setTransactionType( request.getTransactionType() );
        loyaltyTransaction.setDescription( request.getDescription() );

        return loyaltyTransaction;
    }

    @Override
    public LoyaltyTransactionResponse toLoyaltyTransactionResponse(LoyaltyTransaction transaction) {
        if ( transaction == null ) {
            return null;
        }

        LoyaltyTransactionResponse.LoyaltyTransactionResponseBuilder loyaltyTransactionResponse = LoyaltyTransactionResponse.builder();

        loyaltyTransactionResponse.balanceAfter( transaction.getBalanceAfter() );
        loyaltyTransactionResponse.createdAt( transaction.getCreatedAt() );
        loyaltyTransactionResponse.customerId( transaction.getCustomerId() );
        loyaltyTransactionResponse.description( transaction.getDescription() );
        loyaltyTransactionResponse.id( transaction.getId() );
        loyaltyTransactionResponse.points( transaction.getPoints() );
        loyaltyTransactionResponse.referenceId( transaction.getReferenceId() );
        loyaltyTransactionResponse.referenceType( transaction.getReferenceType() );
        loyaltyTransactionResponse.transactionType( transaction.getTransactionType() );

        return loyaltyTransactionResponse.build();
    }

    @Override
    public StorePriceOverride toStorePriceOverride(StorePriceOverrideRequest request) {
        if ( request == null ) {
            return null;
        }

        StorePriceOverride storePriceOverride = new StorePriceOverride();

        storePriceOverride.setStoreId( request.getStoreId() );
        storePriceOverride.setProductId( request.getProductId() );
        storePriceOverride.setOverridePrice( request.getOverridePrice() );
        storePriceOverride.setDiscountPercentage( request.getDiscountPercentage() );
        storePriceOverride.setEffectiveFrom( request.getEffectiveFrom() );
        storePriceOverride.setEffectiveTo( request.getEffectiveTo() );
        storePriceOverride.setIsActive( request.getIsActive() );

        return storePriceOverride;
    }

    @Override
    public StorePriceOverrideResponse toStorePriceOverrideResponse(StorePriceOverride override) {
        if ( override == null ) {
            return null;
        }

        StorePriceOverrideResponse storePriceOverrideResponse = new StorePriceOverrideResponse();

        storePriceOverrideResponse.setId( override.getId() );
        storePriceOverrideResponse.setStoreId( override.getStoreId() );
        storePriceOverrideResponse.setProductId( override.getProductId() );
        storePriceOverrideResponse.setOverridePrice( override.getOverridePrice() );
        storePriceOverrideResponse.setDiscountPercentage( override.getDiscountPercentage() );
        storePriceOverrideResponse.setEffectiveFrom( override.getEffectiveFrom() );
        storePriceOverrideResponse.setEffectiveTo( override.getEffectiveTo() );
        storePriceOverrideResponse.setIsActive( override.getIsActive() );
        storePriceOverrideResponse.setCreatedAt( override.getCreatedAt() );
        storePriceOverrideResponse.setUpdatedAt( override.getUpdatedAt() );

        return storePriceOverrideResponse;
    }

    @Override
    public void updateStorePriceOverrideFromRequest(StorePriceOverrideRequest request, StorePriceOverride override) {
        if ( request == null ) {
            return;
        }

        if ( request.getStoreId() != null ) {
            override.setStoreId( request.getStoreId() );
        }
        if ( request.getProductId() != null ) {
            override.setProductId( request.getProductId() );
        }
        if ( request.getOverridePrice() != null ) {
            override.setOverridePrice( request.getOverridePrice() );
        }
        if ( request.getDiscountPercentage() != null ) {
            override.setDiscountPercentage( request.getDiscountPercentage() );
        }
        if ( request.getEffectiveFrom() != null ) {
            override.setEffectiveFrom( request.getEffectiveFrom() );
        }
        if ( request.getEffectiveTo() != null ) {
            override.setEffectiveTo( request.getEffectiveTo() );
        }
        if ( request.getIsActive() != null ) {
            override.setIsActive( request.getIsActive() );
        }
    }
}
