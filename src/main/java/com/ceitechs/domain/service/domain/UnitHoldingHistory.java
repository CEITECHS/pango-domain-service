package com.ceitechs.domain.service.domain;

import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;

import com.ceitechs.domain.service.util.PangoUtility;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
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

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Transient String remaingTme;

    private UserTransactionHistory transactionHistory;

    private PendingPayment holdingPayment;
    
    private boolean holdingRequestAccepted = false;

    private String getRemainingTime(){
       return PangoUtility.remainingDurationBtnDateTimes(endDate, LocalDateTime.now());
    }
}
