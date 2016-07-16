package com.ceitechs.domain.service.domain;

import java.time.LocalDate;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UnitRentalHistory {
    private long rentalReferenceId;
    private PropertyUnit propertyUnit;
    private User user;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<UserTransactionHistory> transactionHistory;
    private List<PendingPayment> pendingPayments;
    private boolean isActive;
}
