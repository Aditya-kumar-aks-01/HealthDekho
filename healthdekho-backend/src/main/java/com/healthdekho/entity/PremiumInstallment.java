package com.healthdekho.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "premium_installment")
public class PremiumInstallment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "installment_id")
    private Long installmentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private CustomerPolicy policy;

    @Column(name = "installment_number", nullable = false)
    private Integer installmentNumber;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "grace_period_end_date", nullable = false)
    private LocalDate gracePeriodEndDate;

    @Column(name = "scheduled_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal scheduledAmount;

    @Column(name = "paid_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "payment_status", nullable = false)
    private String paymentStatus;

    @OneToMany(mappedBy = "installment", fetch = FetchType.LAZY)
    private List<PremiumPayment> payments = new ArrayList<>();

    protected PremiumInstallment() {
    }

    public PremiumInstallment(
        CustomerPolicy policy,
        Integer installmentNumber,
        LocalDate dueDate,
        LocalDate gracePeriodEndDate,
        BigDecimal scheduledAmount,
        BigDecimal paidAmount,
        String paymentStatus
) {
    this.policy = policy;
    this.installmentNumber = installmentNumber;
    this.dueDate = dueDate;
    this.gracePeriodEndDate = gracePeriodEndDate;
    this.scheduledAmount = scheduledAmount;
    this.paidAmount = paidAmount;
    this.paymentStatus = paymentStatus;
}
    public Long getInstallmentId() {
        return installmentId;
    }

    public void setInstallmentId(Long installmentId) {
        this.installmentId = installmentId;
    }

    public CustomerPolicy getPolicy() {
        return policy;
    }

    public void setPolicy(CustomerPolicy policy) {
        this.policy = policy;
    }

    public Integer getInstallmentNumber() {
        return installmentNumber;
    }

    public void setInstallmentNumber(Integer installmentNumber) {
        this.installmentNumber = installmentNumber;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getGracePeriodEndDate() {
        return gracePeriodEndDate;
    }

    public void setGracePeriodEndDate(LocalDate gracePeriodEndDate) {
        this.gracePeriodEndDate = gracePeriodEndDate;
    }

    public BigDecimal getScheduledAmount() {
        return scheduledAmount;
    }

    public void setScheduledAmount(BigDecimal scheduledAmount) {
        this.scheduledAmount = scheduledAmount;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public List<PremiumPayment> getPayments() {
        return payments;
    }

    public void setPayments(List<PremiumPayment> payments) {
        this.payments = payments;
    }
}