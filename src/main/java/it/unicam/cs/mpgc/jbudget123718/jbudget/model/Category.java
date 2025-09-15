package it.unicam.cs.mpgc.jbudget123718.jbudget.model;

public enum Category { FOOD("🍎"), TRANSPORT("🚗"), UTILITIES("💡"), OTHER("🔖");
    private final String emoji; Category(String emoji) { this.emoji = emoji; }
    public String getEmoji() { return emoji; }}