package com.cursorpos.transaction.service;

import com.cursorpos.shared.exception.ResourceNotFoundException;
import com.cursorpos.shared.security.TenantContext;
import com.cursorpos.transaction.dto.ReceiptResponse;
import com.cursorpos.transaction.entity.Receipt;
import com.cursorpos.transaction.entity.Transaction;
import com.cursorpos.transaction.entity.TransactionItem;
import com.cursorpos.transaction.mapper.TransactionMapper;
import com.cursorpos.transaction.repository.ReceiptRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReceiptService.
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null") // Test code - suppress null safety warnings for Mockito mocks
class ReceiptServiceTest {

        @Mock
        private ReceiptRepository receiptRepository;

        @Mock
        private TransactionRepository transactionRepository;

        @Mock
        private TransactionMapper transactionMapper;

        @InjectMocks
        private ReceiptService receiptService;

        @Captor
        private ArgumentCaptor<Receipt> receiptCaptor;

        private static final String TENANT_ID = "tenant-test-001";
        private static final UUID TRANSACTION_ID = UUID.randomUUID();
        private static final UUID RECEIPT_ID = UUID.randomUUID();

        @BeforeEach
        void setUp() {
                TenantContext.setTenantId(TENANT_ID);
        }

        @AfterEach
        void tearDown() {
                TenantContext.clear();
        }

        @Test
        void generateReceiptSuccess() {
                // Arrange
                Transaction transaction = createTransaction();
                Receipt savedReceipt = createReceipt();
                ReceiptResponse expectedResponse = createReceiptResponse();

                when(transactionRepository.findByIdAndTenantIdAndDeletedAtIsNull(TRANSACTION_ID, TENANT_ID))
                                .thenReturn(Optional.of(transaction));
                when(receiptRepository.findByTenantIdAndTransactionIdAndDeletedAtIsNull(TENANT_ID, TRANSACTION_ID))
                                .thenReturn(Optional.empty());
                when(receiptRepository.save(any(Receipt.class))).thenReturn(savedReceipt);
                when(transactionMapper.toReceiptResponse(savedReceipt)).thenReturn(expectedResponse);

                // Act
                ReceiptResponse result = receiptService.generateReceipt(TRANSACTION_ID);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.getId()).isEqualTo(RECEIPT_ID);
                assertThat(result.getReceiptNumber()).startsWith("RCP-");

                verify(receiptRepository).save(receiptCaptor.capture());
                Receipt capturedReceipt = receiptCaptor.getValue();
                assertThat(capturedReceipt.getTenantId()).isEqualTo(TENANT_ID);
                assertThat(capturedReceipt.getTransactionId()).isEqualTo(TRANSACTION_ID);
                assertThat(capturedReceipt.getReceiptType()).isEqualTo("SALE");
                assertThat(capturedReceipt.getPrintCount()).isZero();
                assertThat(capturedReceipt.getContent()).contains("SALES RECEIPT");
                assertThat(capturedReceipt.getContent()).contains("Test Product");
        }

        @Test
        void generateReceiptTransactionNotFound() {
                // Arrange
                when(transactionRepository.findByIdAndTenantIdAndDeletedAtIsNull(TRANSACTION_ID, TENANT_ID))
                                .thenReturn(Optional.empty());

                // Act & Assert
                assertThatThrownBy(() -> receiptService.generateReceipt(TRANSACTION_ID))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Transaction not found");

                verify(receiptRepository, never()).save(any());
        }

        @Test
        void generateReceiptReceiptAlreadyExists() {
                // Arrange
                Transaction transaction = createTransaction();
                Receipt existingReceipt = createReceipt();

                when(transactionRepository.findByIdAndTenantIdAndDeletedAtIsNull(TRANSACTION_ID, TENANT_ID))
                                .thenReturn(Optional.of(transaction));
                when(receiptRepository.findByTenantIdAndTransactionIdAndDeletedAtIsNull(TENANT_ID, TRANSACTION_ID))
                                .thenReturn(Optional.of(existingReceipt));

                // Act & Assert
                assertThatThrownBy(() -> receiptService.generateReceipt(TRANSACTION_ID))
                                .isInstanceOf(IllegalStateException.class)
                                .hasMessageContaining("Receipt already exists");

                verify(receiptRepository, never()).save(any());
        }

        @Test
        void generateReceiptNullTransactionId() {
                // Act & Assert
                assertThatThrownBy(() -> receiptService.generateReceipt(null))
                                .isInstanceOf(NullPointerException.class);
        }

        @Test
        void getReceiptByIdSuccess() {
                // Arrange
                Receipt receipt = createReceipt();
                ReceiptResponse expectedResponse = createReceiptResponse();

                when(receiptRepository.findByIdAndTenantIdAndDeletedAtIsNull(RECEIPT_ID, TENANT_ID))
                                .thenReturn(Optional.of(receipt));
                when(transactionMapper.toReceiptResponse(receipt)).thenReturn(expectedResponse);

                // Act
                ReceiptResponse result = receiptService.getReceiptById(RECEIPT_ID);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.getId()).isEqualTo(RECEIPT_ID);
        }

        @Test
        void getReceiptByIdNotFound() {
                // Arrange
                when(receiptRepository.findByIdAndTenantIdAndDeletedAtIsNull(RECEIPT_ID, TENANT_ID))
                                .thenReturn(Optional.empty());

                // Act & Assert
                assertThatThrownBy(() -> receiptService.getReceiptById(RECEIPT_ID))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Receipt not found");
        }

        @Test
        void getReceiptByIdNullId() {
                // Act & Assert
                assertThatThrownBy(() -> receiptService.getReceiptById(null))
                                .isInstanceOf(NullPointerException.class);
        }

        @Test
        void getReceiptByTransactionSuccess() {
                // Arrange
                Receipt receipt = createReceipt();
                ReceiptResponse expectedResponse = createReceiptResponse();

                when(receiptRepository.findByTenantIdAndTransactionIdAndDeletedAtIsNull(TENANT_ID, TRANSACTION_ID))
                                .thenReturn(Optional.of(receipt));
                when(transactionMapper.toReceiptResponse(receipt)).thenReturn(expectedResponse);

                // Act
                ReceiptResponse result = receiptService.getReceiptByTransaction(TRANSACTION_ID);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.getTransactionId()).isEqualTo(TRANSACTION_ID);
        }

        @Test
        void getReceiptByTransactionNotFound() {
                // Arrange
                when(receiptRepository.findByTenantIdAndTransactionIdAndDeletedAtIsNull(TENANT_ID, TRANSACTION_ID))
                                .thenReturn(Optional.empty());

                // Act & Assert
                assertThatThrownBy(() -> receiptService.getReceiptByTransaction(TRANSACTION_ID))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Receipt not found for transaction");
        }

        @Test
        void getReceiptByTransactionNullTransactionId() {
                // Act & Assert
                assertThatThrownBy(() -> receiptService.getReceiptByTransaction(null))
                                .isInstanceOf(NullPointerException.class);
        }

        @Test
        void printReceiptSuccess() {
                // Arrange
                Receipt receipt = createReceipt();
                receipt.setPrintCount(2);
                receipt.setLastPrintedAt(LocalDateTime.now().minusHours(1));

                Receipt updatedReceipt = createReceipt();
                updatedReceipt.setPrintCount(3);
                updatedReceipt.setLastPrintedAt(LocalDateTime.now());

                ReceiptResponse expectedResponse = createReceiptResponse();

                when(receiptRepository.findByIdAndTenantIdAndDeletedAtIsNull(RECEIPT_ID, TENANT_ID))
                                .thenReturn(Optional.of(receipt));
                when(receiptRepository.save(any(Receipt.class))).thenReturn(updatedReceipt);
                when(transactionMapper.toReceiptResponse(updatedReceipt)).thenReturn(expectedResponse);

                // Act
                ReceiptResponse result = receiptService.printReceipt(RECEIPT_ID);

                // Assert
                assertThat(result).isNotNull();

                verify(receiptRepository).save(receiptCaptor.capture());
                Receipt capturedReceipt = receiptCaptor.getValue();
                assertThat(capturedReceipt.getPrintCount()).isEqualTo(3);
                assertThat(capturedReceipt.getLastPrintedAt()).isNotNull();
        }

        @Test
        void printReceiptFirstPrint() {
                // Arrange
                Receipt receipt = createReceipt();
                receipt.setPrintCount(0);
                receipt.setLastPrintedAt(null);

                Receipt updatedReceipt = createReceipt();
                updatedReceipt.setPrintCount(1);
                updatedReceipt.setLastPrintedAt(LocalDateTime.now());

                ReceiptResponse expectedResponse = createReceiptResponse();

                when(receiptRepository.findByIdAndTenantIdAndDeletedAtIsNull(RECEIPT_ID, TENANT_ID))
                                .thenReturn(Optional.of(receipt));
                when(receiptRepository.save(any(Receipt.class))).thenReturn(updatedReceipt);
                when(transactionMapper.toReceiptResponse(updatedReceipt)).thenReturn(expectedResponse);

                // Act
                ReceiptResponse result = receiptService.printReceipt(RECEIPT_ID);

                // Assert
                assertThat(result).isNotNull();

                verify(receiptRepository).save(receiptCaptor.capture());
                Receipt capturedReceipt = receiptCaptor.getValue();
                assertThat(capturedReceipt.getPrintCount()).isEqualTo(1);
                assertThat(capturedReceipt.getLastPrintedAt()).isNotNull();
        }

        @Test
        void printReceiptNotFound() {
                // Arrange
                when(receiptRepository.findByIdAndTenantIdAndDeletedAtIsNull(RECEIPT_ID, TENANT_ID))
                                .thenReturn(Optional.empty());

                // Act & Assert
                assertThatThrownBy(() -> receiptService.printReceipt(RECEIPT_ID))
                                .isInstanceOf(ResourceNotFoundException.class)
                                .hasMessageContaining("Receipt not found");

                verify(receiptRepository, never()).save(any());
        }

        @Test
        void printReceiptNullId() {
                // Act & Assert
                assertThatThrownBy(() -> receiptService.printReceipt(null))
                                .isInstanceOf(NullPointerException.class);
        }

        // Helper methods
        private Transaction createTransaction() {
                Transaction transaction = new Transaction();
                transaction.setId(TRANSACTION_ID);
                transaction.setTenantId(TENANT_ID);
                transaction.setTransactionNumber("TRX-20251120-120000-TEST");
                transaction.setTransactionDate(LocalDateTime.now());
                transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
                transaction.setType(Transaction.TransactionType.SALE);
                transaction.setSubtotal(new BigDecimal("100.00"));
                transaction.setTaxAmount(new BigDecimal("10.00"));
                transaction.setDiscountAmount(BigDecimal.ZERO);
                transaction.setTotalAmount(new BigDecimal("110.00"));
                transaction.setPaidAmount(new BigDecimal("120.00"));
                transaction.setChangeAmount(new BigDecimal("10.00"));
                transaction.setCashierName("John Doe");

                List<TransactionItem> items = new ArrayList<>();
                TransactionItem item = new TransactionItem();
                item.setProductName("Test Product");
                item.setQuantity(2);
                item.setUnitPrice(new BigDecimal("50.00"));
                item.setTotalAmount(new BigDecimal("100.00"));
                items.add(item);

                transaction.setItems(items);

                return transaction;
        }

        private Receipt createReceipt() {
                Receipt receipt = new Receipt();
                receipt.setId(RECEIPT_ID);
                receipt.setTenantId(TENANT_ID);
                receipt.setTransactionId(TRANSACTION_ID);
                receipt.setReceiptNumber("RCP-20251120-120000-TEST");
                receipt.setIssuedDate(LocalDateTime.now());
                receipt.setReceiptType("SALE");
                receipt.setContent("Test receipt content");
                receipt.setPrintCount(0);
                return receipt;
        }

        private ReceiptResponse createReceiptResponse() {
                return ReceiptResponse.builder()
                                .id(RECEIPT_ID)
                                .transactionId(TRANSACTION_ID)
                                .receiptNumber("RCP-20251120-120000-TEST")
                                .issuedDate(LocalDateTime.now())
                                .receiptType("SALE")
                                .content("Test receipt content")
                                .printCount(0)
                                .build();
        }
}
