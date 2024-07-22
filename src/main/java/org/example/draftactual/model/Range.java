package org.example.draftactual.model;

public record Range(double start, double end) {
    public boolean contains(double value) {
        return value >= start && value <= end;
    }

    public boolean overlap(Range other) {
        return contains(other.start) || contains(other.end) || other.contains(start) || other.contains(end);
    }
}