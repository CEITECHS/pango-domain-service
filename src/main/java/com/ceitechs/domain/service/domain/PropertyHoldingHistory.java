package com.ceitechs.domain.service.domain;

import java.time.LocalDateTime;

import com.ceitechs.domain.service.util.PangoUtility;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "unit_holding_history")
@TypeAlias("unit_holding_history")
public class PropertyHoldingHistory {

    @Id
    private String holdingReferenceId;

    @DBRef
    private PropertyUnit propertyUnit;

    @Indexed
    private String ownerReferenceId;

    @DBRef
    private User user;

    private LocalDateTime createdDate = LocalDateTime.now();

    private LocalDateTime startDate; // decided date

    private LocalDateTime endDate;

    @Transient String remainingTme;

    private UserTransactionHistory transactionHistory;

    private PendingPayment holdingPayment;
    
    private boolean holdingRequestAccepted = false;  

    private String decisionDetails;

    private HoldingPhase phase = HoldingPhase.INITIATED;

    private String getRemainingTime(){
       return PangoUtility.remainingDurationBtnDateTimes(endDate, LocalDateTime.now());
    }

    public enum HoldingPhase {
        INITIATED, // can only allow cancellation in this phase
        CANCELLED,
        DECIDED,
        EXPIRED
    }


}
