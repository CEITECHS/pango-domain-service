package com.ceitechs.domain.service.domain;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "unit_holding_history")
@TypeAlias("unit_holding_history")
public class UnitHoldingHistory {

    @Id
    private String holdingReferenceId;

    @DBRef
    private PropertyUnit propertyUnit;

    @DBRef
    private User user;

    private LocalDate startDate;

    private LocalDate endDate;

    private UserTransactionHistory transactionHistory;

    private PendingPayment holdingPayment;
}
