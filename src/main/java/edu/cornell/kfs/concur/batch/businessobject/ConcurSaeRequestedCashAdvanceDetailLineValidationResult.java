package edu.cornell.kfs.concur.batch.businessobject;

import edu.cornell.kfs.concur.businessobjects.ValidationResult;

public class ConcurSaeRequestedCashAdvanceDetailLineValidationResult extends ValidationResult {
    private boolean cashAdvanceLine;
    private boolean cashAdvanceAdministratorApprovedCashAdvance;
    private boolean clonedCashAdvance;
    private boolean cashAdvanceUsedInExpenseReport;
    private boolean duplicatedCashAdvanceLine;
    private boolean validCashAdvanceLine;

    public ConcurSaeRequestedCashAdvanceDetailLineValidationResult() {
        super();
        this.cashAdvanceLine = false;
        this.cashAdvanceAdministratorApprovedCashAdvance = false;
        this.cashAdvanceUsedInExpenseReport = false;
        this.validCashAdvanceLine = false;
    }

    public boolean isCashAdvanceLine() {
        return cashAdvanceLine;
    }

    public boolean isNotCashAdvanceLine() {
        return !cashAdvanceLine;
    }

    public void setCashAdvanceLine(boolean cashAdvanceLine) {
        this.cashAdvanceLine = cashAdvanceLine;
    }

    public boolean isCashAdvanceAdministratorApprovedCashAdvance() {
        return cashAdvanceAdministratorApprovedCashAdvance;
    }
    
    public boolean isNotCashAdvanceAdministratorApprovedCashAdvance() {
        return !cashAdvanceAdministratorApprovedCashAdvance;
    }

    public void setCashAdvanceAdministratorApprovedCashAdvance(boolean cashAdvanceAdministratorApprovedCashAdvance) {
        this.cashAdvanceAdministratorApprovedCashAdvance = cashAdvanceAdministratorApprovedCashAdvance;
    }

    public boolean isCashAdvanceUsedInExpenseReport() {
        return cashAdvanceUsedInExpenseReport;
    }

    public boolean isCashAdvanceNotUsedInExpenseReport() {
        return !cashAdvanceUsedInExpenseReport;
    }

    public void setCashAdvanceUsedInExpenseReport(boolean cashAdvanceUsedInExpenseReport) {
        this.cashAdvanceUsedInExpenseReport = cashAdvanceUsedInExpenseReport;
    }

    public boolean isDuplicatedCashAdvanceLine() {
        return duplicatedCashAdvanceLine;
    }

    public boolean isNotDuplicatedCashAdvanceLine() {
        return !duplicatedCashAdvanceLine;
    }

    public void setDuplicatedCashAdvanceLine(boolean duplicatedCashAdvanceLine) {
        this.duplicatedCashAdvanceLine = duplicatedCashAdvanceLine;
    }

    public boolean isValidCashAdvanceLine() {
        return validCashAdvanceLine;
    }

    public boolean isNotValidCashAdvanceLine() {
        return !validCashAdvanceLine;
    }

    public void setValidCashAdvanceLine(boolean validCashAdvanceLine) {
        this.validCashAdvanceLine = validCashAdvanceLine;
    }

    public boolean isClonedCashAdvance() {
        return clonedCashAdvance;
    }
    
    public boolean isNotClonedCashAdvance() {
        return !clonedCashAdvance;
    }

    public void setClonedCashAdvance(boolean clonedCashAdvance) {
        this.clonedCashAdvance = clonedCashAdvance;
    }

}
