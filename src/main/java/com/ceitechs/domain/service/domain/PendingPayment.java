package com.ceitechs.domain.service.domain;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class PendingPayment {
    //Broken down(monthly) based on rental agreement start and end-date
    private LocalDate startDate;
    private LocalDate endDate;
    private double paymentAmount;
    private String paymentStatus;
    private String paymentPeriod;
    private String currencyType;
    private LocalDate paymentCreatedDate;
    private LocalDate paymentPostedDate;
    private String transactionType;
}