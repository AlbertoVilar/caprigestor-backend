package com.devmaster.goatfarm.milk.domain;

import com.devmaster.goatfarm.milk.enums.MilkProductionStatus;
import com.devmaster.goatfarm.milk.enums.MilkingShift;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Framework-free domain representation of one milk production record. */
public final class MilkProduction {
    private Long id;
    private Long farmId;
    private String goatId;
    private Long goatTechnicalId;
    private Long lactationId;
    private LocalDate date;
    private MilkingShift shift;
    private BigDecimal volumeLiters;
    private String notes;
    private MilkProductionStatus status;
    private LocalDateTime canceledAt;
    private String canceledReason;
    private boolean recordedDuringMilkWithdrawal;
    private Long milkWithdrawalEventId;
    private LocalDate milkWithdrawalEndDate;
    private String milkWithdrawalSource;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private MilkProduction() {
    }

    public static MilkProduction record(Long farmId, String goatId, Long lactationId,
                                        LocalDate date, MilkingShift shift, BigDecimal volumeLiters,
                                        String notes) {
        MilkProduction production = new MilkProduction();
        production.farmId = farmId;
        production.goatId = goatId;
        production.lactationId = lactationId;
        production.date = date;
        production.shift = shift;
        production.volumeLiters = volumeLiters;
        production.notes = notes;
        production.status = MilkProductionStatus.ACTIVE;
        production.recordedDuringMilkWithdrawal = false;
        return production;
    }

    public static MilkProduction rehydrate(Long id, Long farmId, String goatId, Long goatTechnicalId,
                                           Long lactationId, LocalDate date, MilkingShift shift,
                                           BigDecimal volumeLiters, String notes, MilkProductionStatus status,
                                           LocalDateTime canceledAt, String canceledReason,
                                           boolean recordedDuringMilkWithdrawal, Long milkWithdrawalEventId,
                                           LocalDate milkWithdrawalEndDate, String milkWithdrawalSource,
                                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        MilkProduction p = new MilkProduction();
        p.id = id; p.farmId = farmId; p.goatId = goatId; p.goatTechnicalId = goatTechnicalId;
        p.lactationId = lactationId; p.date = date; p.shift = shift; p.volumeLiters = volumeLiters;
        p.notes = notes; p.status = status; p.canceledAt = canceledAt; p.canceledReason = canceledReason;
        p.recordedDuringMilkWithdrawal = recordedDuringMilkWithdrawal; p.milkWithdrawalEventId = milkWithdrawalEventId;
        p.milkWithdrawalEndDate = milkWithdrawalEndDate; p.milkWithdrawalSource = milkWithdrawalSource;
        p.createdAt = createdAt; p.updatedAt = updatedAt;
        return p;
    }

    public void updateDetails(BigDecimal volumeLiters, String notes) {
        if (status == MilkProductionStatus.CANCELED) {
            throw new IllegalStateException("Registro cancelado não pode ser alterado.");
        }
        if (volumeLiters != null) this.volumeLiters = volumeLiters;
        if (notes != null) this.notes = notes;
    }

    public void cancel(LocalDateTime canceledAt, String reason) {
        if (status == MilkProductionStatus.CANCELED) return;
        status = MilkProductionStatus.CANCELED;
        this.canceledAt = canceledAt;
        this.canceledReason = reason;
    }

    public void applyWithdrawalSnapshot(Long eventId, LocalDate endDate, String source) {
        recordedDuringMilkWithdrawal = eventId != null || endDate != null || (source != null && !source.isBlank());
        milkWithdrawalEventId = eventId;
        milkWithdrawalEndDate = endDate;
        milkWithdrawalSource = source;
    }

    public Long getId() { return id; }
    public Long getFarmId() { return farmId; }
    public String getGoatId() { return goatId; }
    public Long getGoatTechnicalId() { return goatTechnicalId; }
    public Long getLactationId() { return lactationId; }
    public LocalDate getDate() { return date; }
    public MilkingShift getShift() { return shift; }
    public BigDecimal getVolumeLiters() { return volumeLiters; }
    public String getNotes() { return notes; }
    public MilkProductionStatus getStatus() { return status; }
    public LocalDateTime getCanceledAt() { return canceledAt; }
    public String getCanceledReason() { return canceledReason; }
    public boolean isRecordedDuringMilkWithdrawal() { return recordedDuringMilkWithdrawal; }
    public Long getMilkWithdrawalEventId() { return milkWithdrawalEventId; }
    public LocalDate getMilkWithdrawalEndDate() { return milkWithdrawalEndDate; }
    public String getMilkWithdrawalSource() { return milkWithdrawalSource; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

}
