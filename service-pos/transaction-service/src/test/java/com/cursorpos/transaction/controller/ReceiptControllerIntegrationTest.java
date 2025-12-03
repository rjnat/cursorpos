package com.cursorpos.transaction.controller;

import com.cursorpos.shared.dto.ApiResponse;
import com.cursorpos.shared.security.TenantContext;
import com.cursorpos.transaction.dto.ReceiptResponse;
import com.cursorpos.transaction.entity.Receipt;
import com.cursorpos.transaction.entity.Transaction;
import com.cursorpos.transaction.entity.TransactionItem;
import com.cursorpos.transaction.repository.ReceiptRepository;
import com.cursorpos.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.cursorpos.transaction.config.TestSecurityConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for ReceiptController.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@SuppressWarnings("null") // Test code - suppress null safety warnings for TestRestTemplate responses
class ReceiptControllerIntegrationTest {

    @LocalServerPort
    private int port;

    private final TestRestTemplate restTemplate;
    private final ReceiptRepository receiptRepository;
    private final TransactionRepository transactionRepository;

    private static final String TENANT_ID = "tenant-receipt-test-001";
    private static final UUID BRANCH_ID = UUID.fromString("660e8400-e29b-41d4-a716-446655440001");
    @SuppressWarnings("java:S1075") // Test constants - URIs are intentionally hardcoded
    private static final String PATH_TRANSACTION = "/transaction/";
    private static final String PATH_PRINT = "/print";
    private static final BigDecimal AMOUNT_10 = new BigDecimal("10.00");
    private static final BigDecimal AMOUNT_110 = new BigDecimal("110.00");

    @Autowired
    ReceiptControllerIntegrationTest(TestRestTemplate restTemplate, ReceiptRepository receiptRepository,
            TransactionRepository transactionRepository) {
        this.restTemplate = restTemplate;
        this.receiptRepository = receiptRepository;
        this.transactionRepository = transactionRepository;
    }

    private String baseUrl;
    private HttpHeaders headers;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/receipts";
        headers = new HttpHeaders();
        headers.set("X-Tenant-ID", TENANT_ID);
        TenantContext.setTenantId(TENANT_ID);

        // Clean up test data
        receiptRepository.deleteAll();
        transactionRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        receiptRepository.deleteAll();
        transactionRepository.deleteAll();
        TenantContext.clear();
    }

    @Test
    void generateReceiptSuccess() {
        // Arrange
        Transaction transaction = createAndSaveTransaction();

        // Act
        ResponseEntity<ApiResponse<ReceiptResponse>> response = restTemplate.exchange(
                baseUrl + PATH_TRANSACTION + transaction.getId(),
                HttpMethod.POST,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<ApiResponse<ReceiptResponse>>() {
                });

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).contains("Receipt generated successfully");

        ReceiptResponse data = response.getBody().getData();
        assertThat(data.getId()).isNotNull();
        assertThat(data.getTransactionId()).isEqualTo(transaction.getId());
        assertThat(data.getReceiptNumber()).startsWith("RCP-");
        assertThat(data.getReceiptType()).isEqualTo("SALE");
        assertThat(data.getContent()).contains("SALES RECEIPT");
        assertThat(data.getContent()).contains(transaction.getTransactionNumber());
        assertThat(data.getContent()).contains("Test Product");
        assertThat(data.getPrintCount()).isZero();

        // Verify database
        List<Receipt> receipts = receiptRepository.findAll();
        assertThat(receipts).hasSize(1);
        assertThat(receipts.get(0).getTenantId()).isEqualTo(TENANT_ID);
    }

    @Test
    void generateReceiptTransactionNotFound() {
        // Act
        UUID nonExistentId = UUID.randomUUID();
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + PATH_TRANSACTION + nonExistentId,
                HttpMethod.POST,
                new HttpEntity<>(headers),
                String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void generateReceiptReceiptAlreadyExists() {
        // Arrange
        Transaction transaction = createAndSaveTransaction();
        createAndSaveReceipt(transaction);

        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + PATH_TRANSACTION + transaction.getId(),
                HttpMethod.POST,
                new HttpEntity<>(headers),
                String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Receipt already exists");
    }

    @Test
    void getReceiptByIdSuccess() {
        // Arrange
        Transaction transaction = createAndSaveTransaction();
        Receipt receipt = createAndSaveReceipt(transaction);

        // Act
        ResponseEntity<ApiResponse<ReceiptResponse>> response = restTemplate.exchange(
                baseUrl + "/" + receipt.getId(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<ApiResponse<ReceiptResponse>>() {
                });

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getId()).isEqualTo(receipt.getId());
        assertThat(response.getBody().getData().getReceiptNumber()).isEqualTo(receipt.getReceiptNumber());
    }

    @Test
    void getReceiptByIdNotFound() {
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + UUID.randomUUID(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getReceiptByTransactionSuccess() {
        // Arrange
        Transaction transaction = createAndSaveTransaction();
        Receipt receipt = createAndSaveReceipt(transaction);

        // Act
        ResponseEntity<ApiResponse<ReceiptResponse>> response = restTemplate.exchange(
                baseUrl + PATH_TRANSACTION + transaction.getId(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<ApiResponse<ReceiptResponse>>() {
                });

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData().getId()).isEqualTo(receipt.getId());
        assertThat(response.getBody().getData().getTransactionId()).isEqualTo(transaction.getId());
    }

    @Test
    void getReceiptByTransactionNotFound() {
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + PATH_TRANSACTION + UUID.randomUUID(),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void printReceiptSuccess() {
        // Arrange
        Transaction transaction = createAndSaveTransaction();
        Receipt receipt = createAndSaveReceipt(transaction);
        int initialPrintCount = receipt.getPrintCount();

        // Act
        ResponseEntity<ApiResponse<ReceiptResponse>> response = restTemplate.exchange(
                baseUrl + "/" + receipt.getId() + PATH_PRINT,
                HttpMethod.PUT,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<ApiResponse<ReceiptResponse>>() {
                });

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).contains("Receipt printed successfully");

        ReceiptResponse data = response.getBody().getData();
        assertThat(data.getPrintCount()).isEqualTo(initialPrintCount + 1);
        assertThat(data.getLastPrintedAt()).isNotNull();

        // Verify database
        Receipt updated = receiptRepository.findById(receipt.getId()).orElseThrow();
        assertThat(updated.getPrintCount()).isEqualTo(initialPrintCount + 1);
        assertThat(updated.getLastPrintedAt()).isNotNull();
    }

    @Test
    void printReceiptMultiplePrints() {
        // Arrange
        Transaction transaction = createAndSaveTransaction();
        Receipt receipt = createAndSaveReceipt(transaction);

        // Act - First print
        ResponseEntity<ApiResponse<ReceiptResponse>> response1 = restTemplate.exchange(
                baseUrl + "/" + receipt.getId() + PATH_PRINT,
                HttpMethod.PUT,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<ApiResponse<ReceiptResponse>>() {
                });

        // Act - Second print
        ResponseEntity<ApiResponse<ReceiptResponse>> response2 = restTemplate.exchange(
                baseUrl + "/" + receipt.getId() + PATH_PRINT,
                HttpMethod.PUT,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<ApiResponse<ReceiptResponse>>() {
                });

        // Assert
        assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response1.getBody().getData().getPrintCount()).isEqualTo(1);

        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response2.getBody().getData().getPrintCount()).isEqualTo(2);

        // Verify database
        Receipt updated = receiptRepository.findById(receipt.getId()).orElseThrow();
        assertThat(updated.getPrintCount()).isEqualTo(2);
    }

    @Test
    void printReceiptNotFound() {
        // Act
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + UUID.randomUUID() + PATH_PRINT,
                HttpMethod.PUT,
                new HttpEntity<>(headers),
                String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void multiTenantIsolationCannotAccessOtherTenantReceipt() {
        // Arrange
        Transaction transaction = createAndSaveTransaction();
        Receipt receipt = createAndSaveReceipt(transaction);

        // Act - Try to access with different tenant
        HttpHeaders otherTenantHeaders = new HttpHeaders();
        otherTenantHeaders.set("X-Tenant-ID", "tenant-other-001");

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + receipt.getId(),
                HttpMethod.GET,
                new HttpEntity<>(otherTenantHeaders),
                String.class);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // Helper methods
    private Transaction createAndSaveTransaction() {
        Transaction transaction = new Transaction();
        transaction.setTenantId(TENANT_ID);
        transaction.setTransactionNumber("TRX-" + System.currentTimeMillis());
        transaction.setBranchId(BRANCH_ID);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
        transaction.setType(Transaction.TransactionType.SALE);
        transaction.setSubtotal(new BigDecimal("100.00"));
        transaction.setTaxAmount(AMOUNT_10);
        transaction.setDiscountAmount(BigDecimal.ZERO);
        transaction.setTotalAmount(AMOUNT_110);
        transaction.setPaidAmount(AMOUNT_110);
        transaction.setChangeAmount(AMOUNT_10);
        transaction.setCashierName("John Doe");

        // Add transaction item
        TransactionItem item = new TransactionItem();
        item.setTenantId(TENANT_ID);
        item.setTransaction(transaction);
        item.setProductId(UUID.randomUUID());
        item.setProductCode("PROD-001");
        item.setProductName("Test Product");
        item.setQuantity(2);
        item.setUnitPrice(new BigDecimal("50.00"));
        item.setTaxRate(AMOUNT_10);
        item.setTaxAmount(AMOUNT_10);
        item.setSubtotal(new BigDecimal("100.00"));
        item.setTotalAmount(AMOUNT_110);

        List<TransactionItem> items = new ArrayList<>();
        items.add(item);
        transaction.setItems(items);

        return transactionRepository.save(transaction);
    }

    private Receipt createAndSaveReceipt(Transaction transaction) {
        Receipt receipt = new Receipt();
        receipt.setTenantId(TENANT_ID);
        receipt.setTransactionId(transaction.getId());
        receipt.setReceiptNumber("RCP-" + System.currentTimeMillis());
        receipt.setIssuedDate(LocalDateTime.now());
        receipt.setReceiptType("SALE");
        receipt.setContent("Test receipt content");
        receipt.setPrintCount(0);

        return receiptRepository.save(receipt);
    }
}
