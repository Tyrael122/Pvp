package org.example;

public enum MenuOption {
    ME,
    MATCH,
    RANK;

    public static boolean isValid(String option) {
        try {
            MenuOption.valueOf(option);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
