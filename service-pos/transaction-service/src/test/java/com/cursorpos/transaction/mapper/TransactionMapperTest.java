package com.cursorpos.transaction.mapper;

import com.cursorpos.transaction.dto.*;
import com.cursorpos.transaction.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TransactionMapper.
 * 
 * @author rjnat
 * @version 1.0.0
 * @since 2025-11-22
 */
class TransactionMapperTest {

    private TransactionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(TransactionMapper.class);
    }

    private static final String TEST_TENANT = "test-tenant";
    private static final String PROD_001 = "PROD-001";

    @Test
    void toTransactionWithValidRequestMapsCorrectly() {
        // Arrange
        TransactionRequest request = TransactionRequest.builder()
                .branchId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .type(Transaction.TransactionType.SALE)
                .cashierId(UUID.randomUUID())
                .cashierName("John Doe")
                .notes("Test notes")
                .discountAmount(BigDecimal.TEN)
                .items(new ArrayList<>())
                .payments(new ArrayList<>())
                .build();

        // Act
        Transaction result = mapper.toTransaction(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getBranchId()).isEqualTo(request.getBranchId());
        assertThat(result.getCustomerId()).isEqualTo(request.getCustomerId());
        assertThat(result.getType()).isEqualTo(request.getType());
        assertThat(result.getCashierId()).isEqualTo(request.getCashierId());
        assertThat(result.getCashierName()).isEqualTo(request.getCashierName());
        assertThat(result.getNotes()).isEqualTo(request.getNotes());
    }

    @Test
    void toTransactionWithNullRequestReturnsNull() {
        // Act
        Transaction result = mapper.toTransaction(null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void toTransactionResponseWithValidTransactionMapsCorrectly() {
        // Arrange
        Transaction transaction = new Transaction();
        transaction.setTenantId(TEST_TENANT);
        transaction.setTransactionNumber("TRX-001");
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setBranchId(UUID.randomUUID());
        transaction.setCustomerId(UUID.randomUUID());
        transaction.setType(Transaction.TransactionType.SALE);
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setSubtotal(BigDecimal.valueOf(100));
        transaction.setTaxAmount(BigDecimal.valueOf(10));
        transaction.setDiscountAmount(BigDecimal.ZERO);
        transaction.setTotalAmount(BigDecimal.valueOf(110));
        transaction.setPaidAmount(BigDecimal.valueOf(110));
        transaction.setChangeAmount(BigDecimal.ZERO);
        transaction.setCashierId(UUID.randomUUID());
        transaction.setCashierName("Jane Smith");
        transaction.setNotes("Test notes");

        // Act
        TransactionResponse result = mapper.toTransactionResponse(transaction);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(transaction.getId());
        assertThat(result.getTransactionNumber()).isEqualTo(transaction.getTransactionNumber());
        assertThat(result.getBranchId()).isEqualTo(transaction.getBranchId());
        assertThat(result.getType()).isEqualTo(transaction.getType());
        assertThat(result.getStatus()).isEqualTo(transaction.getStatus());
    }

    @Test
    void toTransactionResponseWithNullTransactionReturnsNull() {
        // Act
        TransactionResponse result = mapper.toTransactionResponse(null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void toTransactionItemWithValidRequestMapsCorrectly() {
        // Arrange
        TransactionItemRequest request = TransactionItemRequest.builder()
                .productId(UUID.randomUUID())
                .productCode(PROD_001)
                .productName("Test Product")
                .quantity(2)
                .unitPrice(BigDecimal.valueOf(50))
                .discountAmount(BigDecimal.valueOf(5))
                .taxRate(BigDecimal.valueOf(10))
                .notes("Item notes")
                .build();

        // Act
        TransactionItem result = mapper.toTransactionItem(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(request.getProductId());
        assertThat(result.getProductCode()).isEqualTo(request.getProductCode());
        assertThat(result.getProductName()).isEqualTo(request.getProductName());
        assertThat(result.getQuantity()).isEqualTo(request.getQuantity());
        assertThat(result.getUnitPrice()).isEqualByComparingTo(request.getUnitPrice());
    }

    @Test
    void toTransactionItemWithNullRequestReturnsNull() {
        // Act
        TransactionItem result = mapper.toTransactionItem(null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void toTransactionItemResponseWithValidItemMapsCorrectly() {
        // Arrange
        TransactionItem item = new TransactionItem();
        item.setId(UUID.randomUUID());
        item.setTenantId(TEST_TENANT);
        item.setProductId(UUID.randomUUID());
        item.setProductCode(PROD_001);
        item.setProductName("Test Product");
        item.setQuantity(2);
        item.setUnitPrice(BigDecimal.valueOf(50));
        item.setDiscountAmount(BigDecimal.valueOf(5));
        item.setTaxRate(BigDecimal.valueOf(10));
        item.setTaxAmount(BigDecimal.valueOf(9));
        item.setSubtotal(BigDecimal.valueOf(100));
        item.setTotalAmount(BigDecimal.valueOf(104));
        item.setNotes("Item notes");

        // Act
        TransactionItemResponse result = mapper.toTransactionItemResponse(item);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(item.getId());
        assertThat(result.getProductId()).isEqualTo(item.getProductId());
        assertThat(result.getProductCode()).isEqualTo(item.getProductCode());
        assertThat(result.getQuantity()).isEqualTo(item.getQuantity());
    }

    @Test
    void toTransactionItemResponseWithNullItemReturnsNull() {
        // Act
        TransactionItemResponse result = mapper.toTransactionItemResponse(null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void toPaymentWithValidRequestMapsCorrectly() {
        // Arrange
        PaymentRequest request = PaymentRequest.builder()
                .paymentMethod(Payment.PaymentMethod.CASH)
                .amount(BigDecimal.valueOf(100))
                .referenceNumber("REF-001")
                .notes("Payment notes")
                .build();

        // Act
        Payment result = mapper.toPayment(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getPaymentMethod()).isEqualTo(request.getPaymentMethod());
        assertThat(result.getAmount()).isEqualByComparingTo(request.getAmount());
        assertThat(result.getReferenceNumber()).isEqualTo(request.getReferenceNumber());
        assertThat(result.getNotes()).isEqualTo(request.getNotes());
    }

    @Test
    void toPaymentWithNullRequestReturnsNull() {
        // Act
        Payment result = mapper.toPayment(null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void toPaymentResponseWithValidPaymentMapsCorrectly() {
        // Arrange
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID());
        payment.setTenantId(TEST_TENANT);
        payment.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD);
        payment.setAmount(BigDecimal.valueOf(100));
        payment.setPaymentDate(LocalDateTime.now());
        payment.setReferenceNumber("REF-001");
        payment.setNotes("Payment notes");

        // Act
        PaymentResponse result = mapper.toPaymentResponse(payment);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(payment.getId());
        assertThat(result.getPaymentMethod()).isEqualTo(payment.getPaymentMethod());
        assertThat(result.getAmount()).isEqualByComparingTo(payment.getAmount());
        assertThat(result.getReferenceNumber()).isEqualTo(payment.getReferenceNumber());
    }

    @Test
    void toPaymentResponseWithNullPaymentReturnsNull() {
        // Act
        PaymentResponse result = mapper.toPaymentResponse(null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void toReceiptResponseWithValidReceiptMapsCorrectly() {
        // Arrange
        Receipt receipt = new Receipt();
        receipt.setId(UUID.randomUUID());
        receipt.setTenantId(TEST_TENANT);
        receipt.setReceiptNumber("REC-001");
        receipt.setIssuedDate(LocalDateTime.now());
        receipt.setContent("Receipt content");
        receipt.setPrintCount(1);
        receipt.setLastPrintedAt(LocalDateTime.now());

        // Act
        ReceiptResponse result = mapper.toReceiptResponse(receipt);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(receipt.getId());
        assertThat(result.getReceiptNumber()).isEqualTo(receipt.getReceiptNumber());
        assertThat(result.getContent()).isEqualTo(receipt.getContent());
        assertThat(result.getPrintCount()).isEqualTo(receipt.getPrintCount());
    }

    @Test
    void toReceiptResponseWithNullReceiptReturnsNull() {
        // Act
        ReceiptResponse result = mapper.toReceiptResponse(null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void toTransactionItemResponseListWithValidListMapsCorrectly() {
        // Arrange
        TransactionItem item1 = new TransactionItem();
        item1.setId(UUID.randomUUID());
        item1.setProductCode(PROD_001);
        item1.setProductName("Product 1");
        item1.setQuantity(1);
        item1.setUnitPrice(BigDecimal.TEN);
        item1.setSubtotal(BigDecimal.TEN);
        item1.setTotalAmount(BigDecimal.TEN);

        TransactionItem item2 = new TransactionItem();
        item2.setId(UUID.randomUUID());
        item2.setProductCode("PROD-002");
        item2.setProductName("Product 2");
        item2.setQuantity(2);
        item2.setUnitPrice(BigDecimal.valueOf(20));
        item2.setSubtotal(BigDecimal.valueOf(40));
        item2.setTotalAmount(BigDecimal.valueOf(40));

        List<TransactionItem> items = Arrays.asList(item1, item2);

        // Act - Access via interface (to trigger generated code)
        Transaction transaction = new Transaction();
        transaction.getItems().addAll(items);
        TransactionResponse response = mapper.toTransactionResponse(transaction);

        // Assert
        assertThat(response.getItems()).hasSize(2);
    }

    @Test
    void toPaymentResponseListWithValidListMapsCorrectly() {
        // Arrange
        Payment payment1 = new Payment();
        payment1.setId(UUID.randomUUID());
        payment1.setPaymentMethod(Payment.PaymentMethod.CASH);
        payment1.setAmount(BigDecimal.valueOf(50));
        payment1.setPaymentDate(LocalDateTime.now());

        Payment payment2 = new Payment();
        payment2.setId(UUID.randomUUID());
        payment2.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD);
        payment2.setAmount(BigDecimal.valueOf(50));
        payment2.setPaymentDate(LocalDateTime.now());

        List<Payment> payments = Arrays.asList(payment1, payment2);

        // Act
        Transaction transaction = new Transaction();
        transaction.getPayments().addAll(payments);
        TransactionResponse response = mapper.toTransactionResponse(transaction);

        // Assert
        assertThat(response.getPayments()).hasSize(2);
    }
}
