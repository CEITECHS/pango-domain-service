package com.ceitechs.domain.service.domain;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Document(collection = "unit_rental_history")
@TypeAlias("unit_rental_history")
public class UnitRentalHistory {
    @Id
    private String rentalReferenceId;

    @DBRef
    private PropertyUnit propertyUnit;

    @Indexed
    private String ownerReferenceId;

    @DBRef
    private User user;

    private LocalDate startDate;

    private LocalDate endDate;

    private List<UserTransactionHistory> transactionHistory;

    private List<PendingPayment> pendingPayments;

    private boolean isActive;

}
