package org.example.project2ver2;

public enum TypeOfTransaction {
    THU("Thu"),
    CHI("Chi");

    private final String displayName;

    TypeOfTransaction(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
