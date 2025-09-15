package it.unicam.cs.mpgc.jbudget123718.jbudget.model;

public enum LoanType {
    MORTGAGE("🏠"),
    CAR_LOAN("🚗"),
    PERSONAL_LOAN("💰"),
    STUDENT_LOAN("🎓"),
    BUSINESS_LOAN("🏢"),
    OTHER("📋");

    private final String emoji;

    LoanType(String emoji) {
        this.emoji = emoji;
    }

    public String getEmoji() {
        return emoji;
    }
}
