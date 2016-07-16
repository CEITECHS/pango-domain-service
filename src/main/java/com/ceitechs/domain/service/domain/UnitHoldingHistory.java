package com.ceitechs.domain.service.domain;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UnitHoldingHistory {
    private long holdingReferenceId;
    private PropertyUnit propertyUnit;
    private User user;
    private LocalDate startDate;
    private LocalDate endDate;
    private UserTransactionHistory transactionHistory;
    private PendingPayment holdingPayment;
}

