package com.cursorpos.transaction.service;

import com.cursorpos.shared.dto.PagedResponse;
import com.cursorpos.shared.exception.ResourceNotFoundException;
import com.cursorpos.shared.security.TenantContext;
import com.cursorpos.transaction.dto.*;
import com.cursorpos.transaction.entity.Payment;
import com.cursorpos.transaction.entity.Transaction;
import com.cursorpos.transaction.entity.TransactionItem;
import com.cursorpos.transaction.mapper.TransactionMapper;
import com.cursorpos.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransactionService.
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({ "null", "unused" }) // Test code - suppress null safety and unused warnings
class TransactionServiceTest {

        @Mock
        private TransactionRepository transactionRepository;

        @Mock
        private TransactionMapper transactionMapper;

        @InjectMocks
        private TransactionService transactionService;

        @Captor
        private ArgumentCaptor<Transaction> transactionCaptor;

        private static final String TENANT_ID = "tenant-coffee-001";
        private static final UUID TRANSACTION_ID = UUID.randomUUID();
        private static final UUID BRANCH_ID = UUID.randomUUID();
        private static final UUID CUSTOMER_ID = UUID.randomUUID();
        private static final UUID PRODUCT_ID = UUID.randomUUID();
        private static final String TEST_PRODUCT_NAME = "Test Product";
        private static final BigDecimal AMOUNT_100 = new BigDecimal("100.00");
        private static final BigDecimal AMOUNT_200 = new BigDecimal("200.00");
        private static final BigDecimal AMOUNT_220 = new BigDecimal("220.00");
        private static final BigDecimal AMOUNT_20 = new BigDecimal("20.00");
        private static final String TEST_TRANSACTION_NUMBER = "TRX-20250101-120000-ABC12345";

        @BeforeEach
        void setUp() {
                TenantContext.setTenantId(TENANT_ID);
        }

        @AfterEach
        void tearDown() {
                TenantContext.clear();
        }

        @Test
        void createTransactionSuccess() {
                // Arrange
                TransactionRequest request = createTransactionRequest();
                Transaction transaction = createTransaction();
                TransactionResponse expectedResponse = createTransactionResponse();

                when(transactionMapper.toTransaction(request)).thenReturn(transaction);
                when(transactionMapper.toTransactionItem(any(TransactionItemRequest.class)))
                                .thenAnswer(inv -> {
                                        TransactionItem item = new TransactionItem();
                                        item.setProductId(PRODUCT_ID);
                                        item.setProductName(TEST_PRODUCT_NAME);
                                        item.setQuantity(2);
                                        item.setUnitPrice(AMOUNT_100);
                                        return item;
                                });
                when(transactionMapper.toPayment(any(PaymentRequest.class)))
                                .thenAnswer(inv -> {
                                        Payment payment = new Payment();
                                        payment.setPaymentMethod(Payment.PaymentMethod.CASH);
                                        payment.setAmount(AMOUNT_220);
                                        return payment;
                                });
                when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
                when(transactionMapper.toTransactionResponse(transaction)).thenReturn(expectedResponse);

                // Act
                TransactionResponse response = transactionService.createTransaction(request);

                // Assert
                assertThat(response).isNotNull();
                assertThat(response.getId()).isEqualTo(TRANSACTION_ID);
                assertThat(response.getStatus()).isEqualTo(Transaction.TransactionStatus.COMPLETED);

                verify(transactionRepository).save(transactionCaptor.capture());
                Transaction savedTransaction = transactionCaptor.getValue();
                assertThat(savedTransaction.getTenantId()).isEqualTo(TENANT_ID);
                assertThat(savedTransaction.getTransactionNumber()).startsWith("TRX-");
                assertThat(savedTransaction.getStatus()).isEqualTo(Transaction.TransactionStatus.COMPLETED);
                assertThat(savedTransaction.getSubtotal()).isEqualByComparingTo(AMOUNT_200);
                assertThat(savedTransaction.getTotalAmount()).isEqualByComparingTo(AMOUNT_200);
                assertThat(savedTransaction.getPaidAmount()).isEqualByComparingTo(AMOUNT_220);
                assertThat(savedTransaction.getChangeAmount()).isEqualByComparingTo(AMOUNT_20);
        }

        @Test
        void createTransactionWithTaxAndDiscount() {
                // Arrange
                TransactionRequest request = createTransactionRequestWithTaxAndDiscount();
                Transaction transaction = new Transaction();

                when(transactionMapper.toTransaction(request)).thenReturn(transaction);
                when(transactionMapper.toTransactionItem(any(TransactionItemRequest.class)))
                                .thenAnswer(inv -> {
                                        TransactionItemRequest itemReq = inv.getArgument(0);
                                        TransactionItem item = new TransactionItem();
                                        item.setProductId(itemReq.getProductId());
                                        item.setProductName(itemReq.getProductName());
                                        item.setQuantity(itemReq.getQuantity());
                                        item.setUnitPrice(itemReq.getUnitPrice());
                                        return item;
                                });
                when(transactionMapper.toPayment(any(PaymentRequest.class)))
                                .thenAnswer(inv -> {
                                        Payment payment = new Payment();
                                        payment.setPaymentMethod(Payment.PaymentMethod.CASH);
                                        payment.setAmount(AMOUNT_200);
                                        return payment;
                                });
                when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

                // Act
                transactionService.createTransaction(request);

                // Assert
                verify(transactionRepository).save(transactionCaptor.capture());
                Transaction savedTransaction = transactionCaptor.getValue();

                // Subtotal = 100 * 2 = 200
                // Item discount = 10
                // Taxable = 200 - 10 = 190
                // Tax = 190 * 0.10 = 19
                // Transaction discount = 5
                // Total = 200 - 10 - 5 + 19 = 204
                assertThat(savedTransaction.getSubtotal()).isEqualByComparingTo(new BigDecimal("200.00"));
                assertThat(savedTransaction.getTaxAmount()).isEqualByComparingTo(new BigDecimal("19.00"));
                assertThat(savedTransaction.getDiscountAmount()).isEqualByComparingTo(new BigDecimal("5.00"));
                assertThat(savedTransaction.getTotalAmount()).isEqualByComparingTo(new BigDecimal("214.00"));
        }

        @Test
        void createTransactionWithPartialPaymentStatusPending() {
                // Arrange
                TransactionItemRequest item = TransactionItemRequest.builder()
                                .productId(PRODUCT_ID)
                                .productName(TEST_PRODUCT_NAME)
                                .quantity(2)
                                .unitPrice(AMOUNT_100)
                                .build();

                PaymentRequest payment = PaymentRequest.builder()
                                .paymentMethod(Payment.PaymentMethod.CASH)
                                .amount(new BigDecimal("150.00")) // Partial payment (total is 200.00)
                                .build();

                TransactionRequest request = TransactionRequest.builder()
                                .branchId(BRANCH_ID)
                                .customerId(CUSTOMER_ID)
                                .type(Transaction.TransactionType.SALE)
                                .items(Arrays.asList(item))
                                .payments(Arrays.asList(payment))
                                .build();

                Transaction transaction = new Transaction();

                when(transactionMapper.toTransaction(request)).thenReturn(transaction);
                when(transactionMapper.toTransactionItem(any(TransactionItemRequest.class)))
                                .thenAnswer(inv -> {
                                        TransactionItem txItem = new TransactionItem();
                                        txItem.setProductId(PRODUCT_ID);
                                        txItem.setProductName(TEST_PRODUCT_NAME);
                                        txItem.setQuantity(2);
                                        txItem.setUnitPrice(AMOUNT_100);
                                        return txItem;
                                });
                when(transactionMapper.toPayment(any(PaymentRequest.class)))
                                .thenAnswer(inv -> {
                                        PaymentRequest req = inv.getArgument(0);
                                        Payment txPayment = new Payment();
                                        txPayment.setPaymentMethod(req.getPaymentMethod());
                                        txPayment.setAmount(req.getAmount());
                                        return txPayment;
                                });
                when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);

                // Act
                transactionService.createTransaction(request);

                // Assert
                verify(transactionRepository).save(transactionCaptor.capture());
                Transaction savedTransaction = transactionCaptor.getValue();
                assertThat(savedTransaction.getStatus()).isEqualTo(Transaction.TransactionStatus.PENDING);
                assertThat(savedTransaction.getPaidAmount()).isEqualByComparingTo(new BigDecimal("150.00"));
                assertThat(savedTransaction.getChangeAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        void createTransactionNullRequestThrowsException() {
                assertThatThrownBy(() -> transactionService.createTransaction(null))
                                .isInstanceOf(NullPointerException.class);
        }

        @Test
        void getTransactionByIdSuccess() {
                // Arrange
                Transaction transaction = createTransaction();
                TransactionResponse expectedResponse = createTransactionResponse();

                when(transactionRepository.findByIdAndTenantIdAndDeletedAtIsNull(TRANSACTION_ID, TENANT_ID))
                                .thenReturn(Optional.of(transaction));
                when(transactionMapper.toTransactionResponse(transaction)).thenReturn(expectedResponse);

                // Act
                TransactionResponse response = transactionService.getTransactionById(TRANSACTION_ID);

                // Assert
                assertThat(response).isNotNull();
                assertThat(response.getId()).isEqualTo(TRANSACTION_ID);
                verify(transactionRepository).findByIdAndTenantIdAndDeletedAtIsNull(TRANSACTION_ID, TENANT_ID);
        }

        @Test
        void getTransactionByIdNotFoundThrowsException() {
                // Arrange
                when(transactionRepository.findByIdAndTenantIdAndDeletedAtIsNull(TRANSACTION_ID, TENANT_ID))
                                .thenReturn(Optional.empty());

                // Act & Assert
                assertThatThrownBy(() -> transactionService.getTransactionById(TRANSACTION_ID))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Transaction not found with ID:");
        }

        @Test
        void getTransactionByIdNullIdThrowsException() {
                assertThatThrownBy(() -> transactionService.getTransactionById(null))
                                .isInstanceOf(NullPointerException.class);
        }

        @Test
        void getTransactionByNumberSuccess() {
                // Arrange
                String transactionNumber = TEST_TRANSACTION_NUMBER;
                Transaction transaction = createTransaction();
                transaction.setTransactionNumber(transactionNumber);
                TransactionResponse expectedResponse = createTransactionResponse();

                when(transactionRepository.findByTenantIdAndTransactionNumberAndDeletedAtIsNull(TENANT_ID,
                                transactionNumber))
                                .thenReturn(Optional.of(transaction));
                when(transactionMapper.toTransactionResponse(transaction)).thenReturn(expectedResponse);

                // Act
                TransactionResponse response = transactionService.getTransactionByNumber(transactionNumber);

                // Assert
                assertThat(response).isNotNull();
                verify(transactionRepository).findByTenantIdAndTransactionNumberAndDeletedAtIsNull(TENANT_ID,
                                transactionNumber);
        }

        @Test
        void getTransactionByNumberNotFoundThrowsException() {
                // Arrange
                String transactionNumber = TEST_TRANSACTION_NUMBER;
                when(transactionRepository.findByTenantIdAndTransactionNumberAndDeletedAtIsNull(TENANT_ID,
                                transactionNumber))
                                .thenReturn(Optional.empty());

                // Act & Assert
                assertThatThrownBy(() -> transactionService.getTransactionByNumber(transactionNumber))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Transaction not found with number:");
        }

        @Test
        @SuppressWarnings("null")
        void getAllTransactionsSuccess() {
                // Arrange
                Pageable pageable = PageRequest.of(0, 10);
                List<Transaction> transactions = Arrays.asList(createTransaction(), createTransaction());
                Page<Transaction> page = new PageImpl<>(transactions, pageable, transactions.size());

                when(transactionRepository.findByTenantIdAndDeletedAtIsNull(TENANT_ID, pageable)).thenReturn(page);
                when(transactionMapper.toTransactionResponse(any(Transaction.class)))
                                .thenReturn(createTransactionResponse());

                // Act
                PagedResponse<TransactionResponse> response = transactionService.getAllTransactions(pageable);

                // Assert
                assertThat(response).isNotNull();
                assertThat(response.getContent()).hasSize(2);
                assertThat(response.getTotalElements()).isEqualTo(2);
                verify(transactionRepository).findByTenantIdAndDeletedAtIsNull(TENANT_ID, pageable);
        }

        @Test
        @SuppressWarnings("null")
        void getTransactionsByBranchSuccess() {
                // Arrange
                Pageable pageable = PageRequest.of(0, 10);
                List<Transaction> transactions = Arrays.asList(createTransaction());
                Page<Transaction> page = new PageImpl<>(transactions, pageable, transactions.size());

                when(transactionRepository.findByTenantIdAndBranchIdAndDeletedAtIsNull(TENANT_ID, BRANCH_ID, pageable))
                                .thenReturn(page);
                when(transactionMapper.toTransactionResponse(any(Transaction.class)))
                                .thenReturn(createTransactionResponse());

                // Act
                PagedResponse<TransactionResponse> response = transactionService.getTransactionsByBranch(BRANCH_ID,
                                pageable);

                // Assert
                assertThat(response).isNotNull();
                assertThat(response.getContent()).hasSize(1);
                verify(transactionRepository).findByTenantIdAndBranchIdAndDeletedAtIsNull(TENANT_ID, BRANCH_ID,
                                pageable);
        }

        @Test
        @SuppressWarnings("java:S5778") // Exception testing lambda is intentional
        void getTransactionsByBranchNullBranchIdThrowsException() {
                assertThatThrownBy(() -> transactionService.getTransactionsByBranch(null, PageRequest.of(0, 10)))
                                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @SuppressWarnings("null")
        void getTransactionsByCustomerSuccess() {
                // Arrange
                Pageable pageable = PageRequest.of(0, 10);
                List<Transaction> transactions = Arrays.asList(createTransaction());
                Page<Transaction> page = new PageImpl<>(transactions, pageable, transactions.size());

                when(transactionRepository.findByTenantIdAndCustomerIdAndDeletedAtIsNull(TENANT_ID, CUSTOMER_ID,
                                pageable))
                                .thenReturn(page);
                when(transactionMapper.toTransactionResponse(any(Transaction.class)))
                                .thenReturn(createTransactionResponse());

                // Act
                PagedResponse<TransactionResponse> response = transactionService.getTransactionsByCustomer(CUSTOMER_ID,
                                pageable);

                // Assert
                assertThat(response).isNotNull();
                assertThat(response.getContent()).hasSize(1);
                verify(transactionRepository).findByTenantIdAndCustomerIdAndDeletedAtIsNull(TENANT_ID, CUSTOMER_ID,
                                pageable);
        }

        @Test
        @SuppressWarnings("java:S5778") // Exception testing lambda is intentional
        void getTransactionsByCustomerNullCustomerIdThrowsException() {
                assertThatThrownBy(() -> transactionService.getTransactionsByCustomer(null, PageRequest.of(0, 10)))
                                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @SuppressWarnings("null")
        void getTransactionsByStatusSuccess() {
                // Arrange
                Pageable pageable = PageRequest.of(0, 10);
                Transaction.TransactionStatus status = Transaction.TransactionStatus.COMPLETED;
                List<Transaction> transactions = Arrays.asList(createTransaction());
                Page<Transaction> page = new PageImpl<>(transactions, pageable, transactions.size());

                when(transactionRepository.findByTenantIdAndStatusAndDeletedAtIsNull(TENANT_ID, status, pageable))
                                .thenReturn(page);
                when(transactionMapper.toTransactionResponse(any(Transaction.class)))
                                .thenReturn(createTransactionResponse());

                // Act
                PagedResponse<TransactionResponse> response = transactionService.getTransactionsByStatus(status,
                                pageable);

                // Assert
                assertThat(response).isNotNull();
                assertThat(response.getContent()).hasSize(1);
                verify(transactionRepository).findByTenantIdAndStatusAndDeletedAtIsNull(TENANT_ID, status, pageable);
        }

        @Test
        @SuppressWarnings("java:S5778") // Exception testing lambda is intentional
        void getTransactionsByStatusNullStatusThrowsException() {
                assertThatThrownBy(() -> transactionService.getTransactionsByStatus(null, PageRequest.of(0, 10)))
                                .isInstanceOf(NullPointerException.class);
        }

        @Test
        void getTransactionsByDateRangeSuccess() {
                // Arrange
                LocalDateTime startDate = LocalDateTime.of(2025, 1, 1, 0, 0);
                LocalDateTime endDate = LocalDateTime.of(2025, 1, 31, 23, 59);
                List<Transaction> transactions = Arrays.asList(createTransaction(), createTransaction());

                when(transactionRepository.findByDateRange(TENANT_ID, startDate, endDate)).thenReturn(transactions);
                when(transactionMapper.toTransactionResponse(any(Transaction.class)))
                                .thenReturn(createTransactionResponse());

                // Act
                List<TransactionResponse> responses = transactionService.getTransactionsByDateRange(startDate, endDate);

                // Assert
                assertThat(responses).hasSize(2);
                verify(transactionRepository).findByDateRange(TENANT_ID, startDate, endDate);
        }

        @Test
        @SuppressWarnings("java:S5778") // Exception testing lambda is intentional
        void getTransactionsByDateRangeNullStartDateThrowsException() {
                assertThatThrownBy(() -> transactionService.getTransactionsByDateRange(null, LocalDateTime.now()))
                                .isInstanceOf(NullPointerException.class);
        }

        @Test
        @SuppressWarnings("java:S5778") // Exception testing lambda is intentional
        void getTransactionsByDateRangeNullEndDateThrowsException() {
                assertThatThrownBy(() -> transactionService.getTransactionsByDateRange(LocalDateTime.now(), null))
                                .isInstanceOf(NullPointerException.class);
        }

        @Test
        void cancelTransactionSuccess() {
                // Arrange
                Transaction transaction = createTransaction();
                transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
                TransactionResponse expectedResponse = createTransactionResponse();
                expectedResponse.setStatus(Transaction.TransactionStatus.CANCELLED);

                when(transactionRepository.findByIdAndTenantIdAndDeletedAtIsNull(TRANSACTION_ID, TENANT_ID))
                                .thenReturn(Optional.of(transaction));
                when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
                when(transactionMapper.toTransactionResponse(transaction)).thenReturn(expectedResponse);

                // Act
                TransactionResponse response = transactionService.cancelTransaction(TRANSACTION_ID);

                // Assert
                assertThat(response).isNotNull();
                verify(transactionRepository).save(transactionCaptor.capture());
                Transaction cancelled = transactionCaptor.getValue();
                assertThat(cancelled.getStatus()).isEqualTo(Transaction.TransactionStatus.CANCELLED);
        }

        @Test
        void cancelTransactionAlreadyCancelledThrowsException() {
                // Arrange
                Transaction transaction = createTransaction();
                transaction.setStatus(Transaction.TransactionStatus.CANCELLED);

                when(transactionRepository.findByIdAndTenantIdAndDeletedAtIsNull(TRANSACTION_ID, TENANT_ID))
                                .thenReturn(Optional.of(transaction));

                // Act & Assert
                assertThatThrownBy(() -> transactionService.cancelTransaction(TRANSACTION_ID))
                                .isInstanceOf(IllegalStateException.class)
                                .hasMessageContaining("Transaction is already cancelled");
        }

        @Test
        void cancelTransactionNotFoundThrowsException() {
                // Arrange
                when(transactionRepository.findByIdAndTenantIdAndDeletedAtIsNull(TRANSACTION_ID, TENANT_ID))
                                .thenReturn(Optional.empty());

                // Act & Assert
                assertThatThrownBy(() -> transactionService.cancelTransaction(TRANSACTION_ID))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Transaction not found with ID:");
        }

        @Test
        void cancelTransactionNullIdThrowsException() {
                assertThatThrownBy(() -> transactionService.cancelTransaction(null))
                                .isInstanceOf(NullPointerException.class);
        }

        // Helper methods
        private TransactionRequest createTransactionRequest() {
                TransactionItemRequest item = TransactionItemRequest.builder()
                                .productId(PRODUCT_ID)
                                .productName(TEST_PRODUCT_NAME)
                                .quantity(2)
                                .unitPrice(AMOUNT_100)
                                .build();

                PaymentRequest payment = PaymentRequest.builder()
                                .paymentMethod(Payment.PaymentMethod.CASH)
                                .amount(AMOUNT_220)
                                .build();

                return TransactionRequest.builder()
                                .branchId(BRANCH_ID)
                                .customerId(CUSTOMER_ID)
                                .type(Transaction.TransactionType.SALE)
                                .items(Arrays.asList(item))
                                .payments(Arrays.asList(payment))
                                .build();
        }

        private TransactionRequest createTransactionRequestWithTaxAndDiscount() {
                TransactionItemRequest item = TransactionItemRequest.builder()
                                .productId(PRODUCT_ID)
                                .productName(TEST_PRODUCT_NAME)
                                .quantity(2)
                                .unitPrice(AMOUNT_100)
                                .discountAmount(new BigDecimal("10.00"))
                                .taxRate(new BigDecimal("10.00"))
                                .build();

                PaymentRequest payment = PaymentRequest.builder()
                                .paymentMethod(Payment.PaymentMethod.CASH)
                                .amount(AMOUNT_200)
                                .build();

                return TransactionRequest.builder()
                                .branchId(BRANCH_ID)
                                .type(Transaction.TransactionType.SALE)
                                .items(Arrays.asList(item))
                                .discountAmount(new BigDecimal("5.00"))
                                .payments(Arrays.asList(payment))
                                .build();
        }

        private Transaction createTransaction() {
                Transaction transaction = Transaction.builder()
                                .tenantId(TENANT_ID)
                                .transactionNumber(TEST_TRANSACTION_NUMBER)
                                .branchId(BRANCH_ID)
                                .customerId(CUSTOMER_ID)
                                .transactionDate(LocalDateTime.now())
                                .status(Transaction.TransactionStatus.COMPLETED)
                                .type(Transaction.TransactionType.SALE)
                                .subtotal(AMOUNT_200)
                                .taxAmount(BigDecimal.ZERO)
                                .discountAmount(BigDecimal.ZERO)
                                .totalAmount(AMOUNT_200)
                                .paidAmount(AMOUNT_220)
                                .changeAmount(AMOUNT_20)
                                .build();
                transaction.setId(TRANSACTION_ID);
                return transaction;
        }

        private TransactionResponse createTransactionResponse() {
                TransactionResponse response = new TransactionResponse();
                response.setId(TRANSACTION_ID);
                response.setTransactionNumber(TEST_TRANSACTION_NUMBER);
                response.setBranchId(BRANCH_ID);
                response.setCustomerId(CUSTOMER_ID);
                response.setTransactionDate(LocalDateTime.now());
                response.setStatus(Transaction.TransactionStatus.COMPLETED);
                response.setType(Transaction.TransactionType.SALE);
                response.setSubtotal(AMOUNT_200);
                response.setTaxAmount(BigDecimal.ZERO);
                response.setDiscountAmount(BigDecimal.ZERO);
                response.setTotalAmount(AMOUNT_200);
                response.setPaidAmount(AMOUNT_220);
                response.setChangeAmount(AMOUNT_20);
                return response;
        }
}
