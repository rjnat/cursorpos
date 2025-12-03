package com.cursorpos.transaction.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Transaction entity.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-22
 */
class TransactionTest {

    private Transaction transaction;
    private static final String TENANT_ID = "test-tenant";
    private static final String TEST_PRODUCT = "Test Product";
    private static final String METHOD_NAME_PROPAGATE = "propagateTenantId";

    @BeforeEach
    void setUp() {
        transaction = new Transaction();
        transaction.setTenantId(TENANT_ID);
        transaction.setTransactionNumber("TRX-001");
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setBranchId(UUID.randomUUID());
        transaction.setStatus(Transaction.TransactionStatus.PENDING);
        transaction.setType(Transaction.TransactionType.SALE);
    }

    @Test
    void addItemSetsTransactionReference() {
        // Arrange
        TransactionItem item = new TransactionItem();
        item.setProductId(UUID.randomUUID());
        item.setProductName(TEST_PRODUCT);
        item.setQuantity(1);
        item.setUnitPrice(BigDecimal.TEN);
        item.setSubtotal(BigDecimal.TEN);
        item.setTotalAmount(BigDecimal.TEN);

        // Act
        transaction.addItem(item);

        // Assert
        assertThat(transaction.getItems()).hasSize(1);
        assertThat(transaction.getItems()).contains(item);
        assertThat(item.getTransaction()).isEqualTo(transaction);
    }

    @Test
    void removeItemRemovesItemAndClearsReference() {
        // Arrange
        TransactionItem item = new TransactionItem();
        item.setProductId(UUID.randomUUID());
        item.setProductName(TEST_PRODUCT);
        item.setQuantity(1);
        item.setUnitPrice(BigDecimal.TEN);
        item.setSubtotal(BigDecimal.TEN);
        item.setTotalAmount(BigDecimal.TEN);
        transaction.addItem(item);

        // Act
        transaction.removeItem(item);

        // Assert
        assertThat(transaction.getItems()).isEmpty();
        assertThat(item.getTransaction()).isNull();
    }

    @Test
    void addPaymentSetsTransactionReference() {
        // Arrange
        Payment payment = new Payment();
        payment.setPaymentMethod(Payment.PaymentMethod.CASH);
        payment.setAmount(BigDecimal.valueOf(100));
        payment.setPaymentDate(LocalDateTime.now());

        // Act
        transaction.addPayment(payment);

        // Assert
        assertThat(transaction.getPayments()).hasSize(1);
        assertThat(transaction.getPayments()).contains(payment);
        assertThat(payment.getTransaction()).isEqualTo(transaction);
    }

    @Test
    void removePaymentRemovesPaymentAndClearsReference() {
        // Arrange
        Payment payment = new Payment();
        payment.setPaymentMethod(Payment.PaymentMethod.CASH);
        payment.setAmount(BigDecimal.valueOf(100));
        payment.setPaymentDate(LocalDateTime.now());
        transaction.addPayment(payment);

        // Act
        transaction.removePayment(payment);

        // Assert
        assertThat(transaction.getPayments()).isEmpty();
        assertThat(payment.getTransaction()).isNull();
    }

    @Test
    void propagateTenantIdSetsItemTenantId() throws Exception {
        // Arrange
        TransactionItem item = new TransactionItem();
        item.setProductId(UUID.randomUUID());
        item.setProductName(TEST_PRODUCT);
        item.setQuantity(1);
        item.setUnitPrice(BigDecimal.TEN);
        item.setSubtotal(BigDecimal.TEN);
        item.setTotalAmount(BigDecimal.TEN);
        // Explicitly set tenant_id to null to test propagation
        item.setTenantId(null);

        transaction.addItem(item);

        // Act - call the protected propagateTenantId method using reflection
        Method method = Transaction.class.getDeclaredMethod(METHOD_NAME_PROPAGATE);
        method.invoke(transaction);

        // Assert
        assertThat(item.getTenantId()).isEqualTo(TENANT_ID);
    }

    @Test
    void propagateTenantIdSetsPaymentTenantId() throws Exception {
        // Arrange
        Payment payment = new Payment();
        payment.setPaymentMethod(Payment.PaymentMethod.CASH);
        payment.setAmount(BigDecimal.valueOf(100));
        payment.setPaymentDate(LocalDateTime.now());
        // Explicitly set tenant_id to null to test propagation
        payment.setTenantId(null);

        transaction.addPayment(payment);

        // Act - call the protected propagateTenantId method using reflection
        Method method = Transaction.class.getDeclaredMethod(METHOD_NAME_PROPAGATE);
        method.invoke(transaction);

        // Assert
        assertThat(payment.getTenantId()).isEqualTo(TENANT_ID);
    }

    @Test
    void propagateTenantIdDoesNotOverrideExistingTenantId() throws Exception {
        // Arrange
        String existingTenantId = "existing-tenant";
        TransactionItem item = new TransactionItem();
        item.setProductId(UUID.randomUUID());
        item.setProductName(TEST_PRODUCT);
        item.setQuantity(1);
        item.setUnitPrice(BigDecimal.TEN);
        item.setSubtotal(BigDecimal.TEN);
        item.setTotalAmount(BigDecimal.TEN);
        item.setTenantId(existingTenantId);

        transaction.addItem(item);

        // Act - call the protected propagateTenantId method using reflection
        Method method = Transaction.class.getDeclaredMethod(METHOD_NAME_PROPAGATE);
        method.invoke(transaction);

        // Assert - should keep existing tenant ID (tests the null check branch)
        assertThat(item.getTenantId()).isEqualTo(existingTenantId);
    }

    @Test
    void propagateTenantIdHandlesNullTransactionTenantId() throws Exception {
        // Arrange
        transaction.setTenantId(null);
        TransactionItem item = new TransactionItem();
        item.setProductId(UUID.randomUUID());
        item.setProductName(TEST_PRODUCT);
        item.setQuantity(1);
        item.setUnitPrice(BigDecimal.TEN);
        item.setSubtotal(BigDecimal.TEN);
        item.setTotalAmount(BigDecimal.TEN);
        item.setTenantId(null);

        transaction.addItem(item);

        // Act - call the protected propagateTenantId method using reflection
        Method method = Transaction.class.getDeclaredMethod(METHOD_NAME_PROPAGATE);
        method.invoke(transaction);

        // Assert - should remain null (tests the outer if branch)
        assertThat(item.getTenantId()).isNull();
    }
}
