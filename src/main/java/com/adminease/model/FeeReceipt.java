package com.adminease.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeReceipt {
    private String id;
    private double amount;
    private String paymentDate;
    private String paymentBy;
    private String transactionId;
    private String tuitionFee;
    private String examFee;
    private String admissionFee;
    private String enrollmentFee;
    private String developmentFee;
    private String libraryFee;
    private String iCardFee;
    private String sportsFee;
    private String gymnasiumFee;
    private String medicalInsuranceFee;
    private String hostelFee;
    private String practicalsFee;
}
