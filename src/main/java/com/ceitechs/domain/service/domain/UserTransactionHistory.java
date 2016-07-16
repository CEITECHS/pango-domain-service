package com.ceitechs.domain.service.domain;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserTransactionHistory {

    public enum TransactionStatus {
        PENDING,
        PROCESSED,
        SUCCESS,
        FAILED,
        CANCELLED
    }

    private String transactionId;
    private User user;
    // private UnitRentalHistory rentalHistory;
    private UnitHoldingHistory holdingHistory;
    private TransactionType transactionType;
    private String paymentReferenceId;
    private String payingAccount;
    private String transactionDesc;
    private String statusReason;
    private double transactionAmount;
    private LocalDate transactionDate;
}