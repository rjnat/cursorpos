package com.cursorpos.transaction.controller;

import com.cursorpos.shared.dto.ApiResponse;
import com.cursorpos.shared.dto.PagedResponse;
import com.cursorpos.shared.security.TenantContext;
import com.cursorpos.transaction.dto.*;
import com.cursorpos.transaction.entity.Payment;
import com.cursorpos.transaction.entity.Transaction;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.cursorpos.transaction.config.TestSecurityConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for TransactionController.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@SuppressWarnings("null") // Test code - suppress null safety warnings for TestRestTemplate responses
class TransactionControllerIntegrationTest {

        @LocalServerPort
        private int port;

        private final TestRestTemplate restTemplate;
        private final TransactionRepository transactionRepository;

        private static final String TENANT_ID = "tenant-coffee-integration-001";
        private static final UUID BRANCH_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        private static final UUID CUSTOMER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
        private static final UUID PRODUCT_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440003");
        private static final BigDecimal AMOUNT_200 = new BigDecimal("200.00");
        private static final BigDecimal AMOUNT_220 = new BigDecimal("220.00");
        private static final String PAGE_QUERY = "?page=0&size=10";

        @Autowired
        TransactionControllerIntegrationTest(TestRestTemplate restTemplate,
                        TransactionRepository transactionRepository) {
                this.restTemplate = restTemplate;
                this.transactionRepository = transactionRepository;
        }

        private String baseUrl;
        private HttpHeaders headers;

        @BeforeEach
        void setUp() {
                baseUrl = "http://localhost:" + port + "/api/transactions";
                headers = new HttpHeaders();
                headers.set("X-Tenant-ID", TENANT_ID);
                TenantContext.setTenantId(TENANT_ID);

                // Clean up test data
                transactionRepository.deleteAll();
        }

        @AfterEach
        void tearDown() {
                transactionRepository.deleteAll();
                TenantContext.clear();
        }

        @Test
        void createTransactionSuccess() {
                // Arrange
                TransactionRequest request = createTransactionRequest();

                // Act
                ResponseEntity<ApiResponse<TransactionResponse>> response = restTemplate.exchange(
                                baseUrl,
                                HttpMethod.POST,
                                new HttpEntity<>(request, headers),
                                new ParameterizedTypeReference<ApiResponse<TransactionResponse>>() {
                                });

                // Assert
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().isSuccess()).isTrue();
                assertThat(response.getBody().getData()).isNotNull();

                TransactionResponse data = response.getBody().getData();
                assertThat(data.getId()).isNotNull();
                assertThat(data.getTransactionNumber()).startsWith("TRX-");
                assertThat(data.getStatus()).isEqualTo(Transaction.TransactionStatus.COMPLETED);
                assertThat(data.getBranchId()).isEqualTo(BRANCH_ID);
                assertThat(data.getTotalAmount()).isEqualByComparingTo(AMOUNT_200);
                assertThat(data.getPaidAmount()).isEqualByComparingTo(AMOUNT_220);
                assertThat(data.getChangeAmount()).isEqualByComparingTo(new BigDecimal("20.00"));

                // Verify database
                List<Transaction> transactions = transactionRepository.findAll();
                assertThat(transactions).hasSize(1);
                assertThat(transactions.get(0).getTenantId()).isEqualTo(TENANT_ID);
        }

        @Test
        void createTransactionWithTaxAndDiscount() {
                // Arrange
                TransactionItemRequest item = TransactionItemRequest.builder()
                                .productId(PRODUCT_ID)
                                .productCode("PROD-001")
                                .productName("Test Product")
                                .quantity(2)
                                .unitPrice(new BigDecimal("100.00"))
                                .discountAmount(new BigDecimal("10.00"))
                                .taxRate(new BigDecimal("10.00"))
                                .build();

                PaymentRequest payment = PaymentRequest.builder()
                                .paymentMethod(Payment.PaymentMethod.CREDIT_CARD)
                                .amount(new BigDecimal("250.00"))
                                .referenceNumber("REF-12345")
                                .build();

                TransactionRequest request = TransactionRequest.builder()
                                .branchId(BRANCH_ID)
                                .type(Transaction.TransactionType.SALE)
                                .items(Arrays.asList(item))
                                .discountAmount(new BigDecimal("5.00"))
                                .payments(Arrays.asList(payment))
                                .notes("Test transaction with tax and discount")
                                .cashierId(UUID.randomUUID())
                                .cashierName("John Doe")
                                .build();

                // Act
                ResponseEntity<ApiResponse<TransactionResponse>> response = restTemplate.exchange(
                                baseUrl,
                                HttpMethod.POST,
                                new HttpEntity<>(request, headers),
                                new ParameterizedTypeReference<ApiResponse<TransactionResponse>>() {
                                });

                // Assert
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                TransactionResponse data = response.getBody().getData();

                // Subtotal = 100 * 2 = 200
                // Item discount = 10, Transaction discount = 5
                // Taxable = 200 - 10 = 190
                // Tax = 190 * 0.10 = 19
                // Total = 200 - 10 - 5 + 19 = 204
                assertThat(data.getSubtotal()).isEqualByComparingTo(AMOUNT_200);
                assertThat(data.getTaxAmount()).isEqualByComparingTo(new BigDecimal("19.00"));
                assertThat(data.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("5.00"));
                assertThat(data.getTotalAmount()).isEqualByComparingTo(new BigDecimal("214.00"));
        }

        @Test
        void createTransactionInvalidRequestReturnsBadRequest() {
                // Arrange - Empty items list
                TransactionRequest request = TransactionRequest.builder()
                                .branchId(BRANCH_ID)
                                .type(Transaction.TransactionType.SALE)
                                .items(Arrays.asList())
                                .payments(Arrays.asList())
                                .build();

                // Act
                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl,
                                HttpMethod.POST,
                                new HttpEntity<>(request, headers),
                                String.class);

                // Assert
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        void getTransactionByIdSuccess() {
                // Arrange
                Transaction transaction = createAndSaveTransaction();

                // Act
                ResponseEntity<ApiResponse<TransactionResponse>> response = restTemplate.exchange(
                                baseUrl + "/" + transaction.getId(),
                                HttpMethod.GET,
                                new HttpEntity<>(headers),
                                new ParameterizedTypeReference<ApiResponse<TransactionResponse>>() {
                                });

                // Assert
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getData().getId()).isEqualTo(transaction.getId());
        }

        @Test
        void getTransactionByIdNotFound() {
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
        void getTransactionByNumberSuccess() {
                // Arrange
                Transaction transaction = createAndSaveTransaction();

                // Act
                ResponseEntity<ApiResponse<TransactionResponse>> response = restTemplate.exchange(
                                baseUrl + "/number/" + transaction.getTransactionNumber(),
                                HttpMethod.GET,
                                new HttpEntity<>(headers),
                                new ParameterizedTypeReference<ApiResponse<TransactionResponse>>() {
                                });

                // Assert
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getData().getTransactionNumber())
                                .isEqualTo(transaction.getTransactionNumber());
        }

        @Test
        void getAllTransactionsSuccess() {
                // Arrange
                createAndSaveTransaction();
                createAndSaveTransaction();

                // Act
                ResponseEntity<PagedResponse<TransactionResponse>> response = restTemplate.exchange(
                                baseUrl + PAGE_QUERY,
                                HttpMethod.GET,
                                new HttpEntity<>(headers),
                                new ParameterizedTypeReference<PagedResponse<TransactionResponse>>() {
                                });

                // Assert
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().getContent()).hasSize(2);
                assertThat(response.getBody().getTotalElements()).isEqualTo(2);
        }

        @Test
        void getTransactionsByBranchSuccess() {
                // Arrange
                createAndSaveTransaction();

                // Act
                ResponseEntity<PagedResponse<TransactionResponse>> response = restTemplate.exchange(
                                baseUrl + "/branch/" + BRANCH_ID + PAGE_QUERY,
                                HttpMethod.GET,
                                new HttpEntity<>(headers),
                                new ParameterizedTypeReference<PagedResponse<TransactionResponse>>() {
                                });

                // Assert
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).hasSize(1);
        }

        @Test
        void getTransactionsByCustomerSuccess() {
                // Arrange
                createAndSaveTransaction();

                // Act
                ResponseEntity<PagedResponse<TransactionResponse>> response = restTemplate.exchange(
                                baseUrl + "/customer/" + CUSTOMER_ID + PAGE_QUERY,
                                HttpMethod.GET,
                                new HttpEntity<>(headers),
                                new ParameterizedTypeReference<PagedResponse<TransactionResponse>>() {
                                });

                // Assert
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).hasSize(1);
        }

        @Test
        void getTransactionsByStatusSuccess() {
                // Arrange
                createAndSaveTransaction();

                // Act
                ResponseEntity<PagedResponse<TransactionResponse>> response = restTemplate.exchange(
                                baseUrl + "/status/COMPLETED" + PAGE_QUERY,
                                HttpMethod.GET,
                                new HttpEntity<>(headers),
                                new ParameterizedTypeReference<PagedResponse<TransactionResponse>>() {
                                });

                // Assert
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getContent()).hasSize(1);
        }

        @Test
        void getTransactionsByDateRangeSuccess() {
                // Arrange
                createAndSaveTransaction();
                LocalDateTime now = LocalDateTime.now();
                String startDate = now.minusDays(1).toString();
                String endDate = now.plusDays(1).toString();

                // Act
                ResponseEntity<ApiResponse<List<TransactionResponse>>> response = restTemplate.exchange(
                                baseUrl + "/date-range?startDate=" + startDate + "&endDate=" + endDate,
                                HttpMethod.GET,
                                new HttpEntity<>(headers),
                                new ParameterizedTypeReference<ApiResponse<List<TransactionResponse>>>() {
                                });

                // Assert
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getData()).hasSize(1);
        }

        @Test
        void cancelTransactionSuccess() {
                // Arrange
                Transaction transaction = createAndSaveTransaction();

                // Act
                ResponseEntity<ApiResponse<TransactionResponse>> response = restTemplate.exchange(
                                baseUrl + "/" + transaction.getId() + "/cancel",
                                HttpMethod.PUT,
                                new HttpEntity<>(headers),
                                new ParameterizedTypeReference<ApiResponse<TransactionResponse>>() {
                                });

                // Assert
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(response.getBody().getData().getStatus())
                                .isEqualTo(Transaction.TransactionStatus.CANCELLED);

                // Verify database
                Transaction updated = transactionRepository.findById(transaction.getId()).orElseThrow();
                assertThat(updated.getStatus()).isEqualTo(Transaction.TransactionStatus.CANCELLED);
        }

        @Test
        void cancelTransactionAlreadyCancelledReturnsBadRequest() {
                // Arrange
                Transaction transaction = createAndSaveTransaction();
                transaction.setStatus(Transaction.TransactionStatus.CANCELLED);
                transactionRepository.save(transaction);

                // Act
                ResponseEntity<String> response = restTemplate.exchange(
                                baseUrl + "/" + transaction.getId() + "/cancel",
                                HttpMethod.PUT,
                                new HttpEntity<>(headers),
                                String.class);

                // Assert
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        // Helper methods
        private TransactionRequest createTransactionRequest() {
                TransactionItemRequest item = TransactionItemRequest.builder()
                                .productId(PRODUCT_ID)
                                .productCode("PROD-001")
                                .productName("Test Product")
                                .quantity(2)
                                .unitPrice(new BigDecimal("100.00"))
                                .build();

                PaymentRequest payment = PaymentRequest.builder()
                                .paymentMethod(Payment.PaymentMethod.CASH)
                                .amount(new BigDecimal("220.00"))
                                .build();

                return TransactionRequest.builder()
                                .branchId(BRANCH_ID)
                                .customerId(CUSTOMER_ID)
                                .type(Transaction.TransactionType.SALE)
                                .items(Arrays.asList(item))
                                .payments(Arrays.asList(payment))
                                .cashierId(UUID.randomUUID())
                                .cashierName("Jane Smith")
                                .build();
        }

        private Transaction createAndSaveTransaction() {
                Transaction transaction = new Transaction();
                transaction.setTenantId(TENANT_ID);
                transaction.setTransactionNumber("TRX-" + System.currentTimeMillis());
                transaction.setBranchId(BRANCH_ID);
                transaction.setCustomerId(CUSTOMER_ID);
                transaction.setTransactionDate(LocalDateTime.now());
                transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
                transaction.setType(Transaction.TransactionType.SALE);
                transaction.setSubtotal(AMOUNT_200);
                transaction.setTaxAmount(BigDecimal.ZERO);
                transaction.setDiscountAmount(BigDecimal.ZERO);
                transaction.setTotalAmount(AMOUNT_200);
                transaction.setPaidAmount(AMOUNT_220);
                transaction.setChangeAmount(new BigDecimal("20.00"));

                return transactionRepository.save(transaction);
        }
}
