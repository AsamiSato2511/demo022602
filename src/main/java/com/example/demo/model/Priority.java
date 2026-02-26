package com.example.demo.model;

public enum Priority {
    HIGH("高", "text-bg-danger"),
    MEDIUM("中", "text-bg-warning"),
    LOW("低", "text-bg-success");

    private final String label;
    private final String badgeClass;

    Priority(String label, String badgeClass) {
        this.label = label;
        this.badgeClass = badgeClass;
    }

    public String getLabel() {
        return label;
    }

    public String getBadgeClass() {
        return badgeClass;
    }
}