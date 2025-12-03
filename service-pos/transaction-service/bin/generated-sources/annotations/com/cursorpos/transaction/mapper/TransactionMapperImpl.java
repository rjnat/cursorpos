package com.cursorpos.transaction.mapper;

import com.cursorpos.transaction.dto.PaymentRequest;
import com.cursorpos.transaction.dto.PaymentResponse;
import com.cursorpos.transaction.dto.ReceiptResponse;
import com.cursorpos.transaction.dto.TransactionItemRequest;
import com.cursorpos.transaction.dto.TransactionItemResponse;
import com.cursorpos.transaction.dto.TransactionRequest;
import com.cursorpos.transaction.dto.TransactionResponse;
import com.cursorpos.transaction.entity.Payment;
import com.cursorpos.transaction.entity.Receipt;
import com.cursorpos.transaction.entity.Transaction;
import com.cursorpos.transaction.entity.TransactionItem;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-03T23:17:19+0700",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.44.0.v20251118-1623, environment: Java 21.0.8 (Oracle Corporation)"
)
@Component
public class TransactionMapperImpl implements TransactionMapper {

    @Override
    public Transaction toTransaction(TransactionRequest request) {
        if ( request == null ) {
            return null;
        }

        Transaction.TransactionBuilder transaction = Transaction.builder();

        transaction.branchId( request.getBranchId() );
        transaction.cashierId( request.getCashierId() );
        transaction.cashierName( request.getCashierName() );
        transaction.customerId( request.getCustomerId() );
        transaction.discountAmount( request.getDiscountAmount() );
        transaction.notes( request.getNotes() );
        transaction.type( request.getType() );

        return transaction.build();
    }

    @Override
    public TransactionResponse toTransactionResponse(Transaction transaction) {
        if ( transaction == null ) {
            return null;
        }

        TransactionResponse.TransactionResponseBuilder transactionResponse = TransactionResponse.builder();

        transactionResponse.branchId( transaction.getBranchId() );
        transactionResponse.cashierId( transaction.getCashierId() );
        transactionResponse.cashierName( transaction.getCashierName() );
        transactionResponse.changeAmount( transaction.getChangeAmount() );
        transactionResponse.createdAt( transaction.getCreatedAt() );
        transactionResponse.customerId( transaction.getCustomerId() );
        transactionResponse.discountAmount( transaction.getDiscountAmount() );
        transactionResponse.id( transaction.getId() );
        transactionResponse.items( transactionItemListToTransactionItemResponseList( transaction.getItems() ) );
        transactionResponse.notes( transaction.getNotes() );
        transactionResponse.paidAmount( transaction.getPaidAmount() );
        transactionResponse.payments( paymentListToPaymentResponseList( transaction.getPayments() ) );
        transactionResponse.status( transaction.getStatus() );
        transactionResponse.subtotal( transaction.getSubtotal() );
        transactionResponse.taxAmount( transaction.getTaxAmount() );
        transactionResponse.totalAmount( transaction.getTotalAmount() );
        transactionResponse.transactionDate( transaction.getTransactionDate() );
        transactionResponse.transactionNumber( transaction.getTransactionNumber() );
        transactionResponse.type( transaction.getType() );
        transactionResponse.updatedAt( transaction.getUpdatedAt() );

        return transactionResponse.build();
    }

    @Override
    public TransactionItem toTransactionItem(TransactionItemRequest request) {
        if ( request == null ) {
            return null;
        }

        TransactionItem.TransactionItemBuilder transactionItem = TransactionItem.builder();

        transactionItem.discountAmount( request.getDiscountAmount() );
        transactionItem.notes( request.getNotes() );
        transactionItem.productCode( request.getProductCode() );
        transactionItem.productId( request.getProductId() );
        transactionItem.productName( request.getProductName() );
        transactionItem.quantity( request.getQuantity() );
        transactionItem.taxRate( request.getTaxRate() );
        transactionItem.unitPrice( request.getUnitPrice() );

        return transactionItem.build();
    }

    @Override
    public TransactionItemResponse toTransactionItemResponse(TransactionItem item) {
        if ( item == null ) {
            return null;
        }

        TransactionItemResponse.TransactionItemResponseBuilder transactionItemResponse = TransactionItemResponse.builder();

        transactionItemResponse.discountAmount( item.getDiscountAmount() );
        transactionItemResponse.id( item.getId() );
        transactionItemResponse.notes( item.getNotes() );
        transactionItemResponse.productCode( item.getProductCode() );
        transactionItemResponse.productId( item.getProductId() );
        transactionItemResponse.productName( item.getProductName() );
        transactionItemResponse.quantity( item.getQuantity() );
        transactionItemResponse.subtotal( item.getSubtotal() );
        transactionItemResponse.taxAmount( item.getTaxAmount() );
        transactionItemResponse.taxRate( item.getTaxRate() );
        transactionItemResponse.totalAmount( item.getTotalAmount() );
        transactionItemResponse.unitPrice( item.getUnitPrice() );

        return transactionItemResponse.build();
    }

    @Override
    public Payment toPayment(PaymentRequest request) {
        if ( request == null ) {
            return null;
        }

        Payment.PaymentBuilder payment = Payment.builder();

        payment.amount( request.getAmount() );
        payment.notes( request.getNotes() );
        payment.paymentMethod( request.getPaymentMethod() );
        payment.referenceNumber( request.getReferenceNumber() );

        return payment.build();
    }

    @Override
    public PaymentResponse toPaymentResponse(Payment payment) {
        if ( payment == null ) {
            return null;
        }

        PaymentResponse.PaymentResponseBuilder paymentResponse = PaymentResponse.builder();

        paymentResponse.amount( payment.getAmount() );
        paymentResponse.id( payment.getId() );
        paymentResponse.notes( payment.getNotes() );
        paymentResponse.paymentDate( payment.getPaymentDate() );
        paymentResponse.paymentMethod( payment.getPaymentMethod() );
        paymentResponse.referenceNumber( payment.getReferenceNumber() );

        return paymentResponse.build();
    }

    @Override
    public ReceiptResponse toReceiptResponse(Receipt receipt) {
        if ( receipt == null ) {
            return null;
        }

        ReceiptResponse.ReceiptResponseBuilder receiptResponse = ReceiptResponse.builder();

        receiptResponse.content( receipt.getContent() );
        receiptResponse.id( receipt.getId() );
        receiptResponse.issuedDate( receipt.getIssuedDate() );
        receiptResponse.lastPrintedAt( receipt.getLastPrintedAt() );
        receiptResponse.printCount( receipt.getPrintCount() );
        receiptResponse.receiptNumber( receipt.getReceiptNumber() );
        receiptResponse.receiptType( receipt.getReceiptType() );
        receiptResponse.transactionId( receipt.getTransactionId() );

        return receiptResponse.build();
    }

    protected List<TransactionItemResponse> transactionItemListToTransactionItemResponseList(List<TransactionItem> list) {
        if ( list == null ) {
            return null;
        }

        List<TransactionItemResponse> list1 = new ArrayList<TransactionItemResponse>( list.size() );
        for ( TransactionItem transactionItem : list ) {
            list1.add( toTransactionItemResponse( transactionItem ) );
        }

        return list1;
    }

    protected List<PaymentResponse> paymentListToPaymentResponseList(List<Payment> list) {
        if ( list == null ) {
            return null;
        }

        List<PaymentResponse> list1 = new ArrayList<PaymentResponse>( list.size() );
        for ( Payment payment : list ) {
            list1.add( toPaymentResponse( payment ) );
        }

        return list1;
    }
}
