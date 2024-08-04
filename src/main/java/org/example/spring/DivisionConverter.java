package org.example.spring;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.example.pvp.model.Division;

@Converter(autoApply = true)
public class DivisionConverter implements AttributeConverter<Division, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Division division) {
        if (division == null) {
            return null;
        }

        return division.getId();
    }

    @Override
    public Division convertToEntityAttribute(Integer integer) {
        if (integer == null) {
            return null;
        }

        for (Division division : Division.values()) {
            if (division.getId() == integer) {
                return division;
            }
        }

        throw new IllegalArgumentException("Unknown id: " + integer);
    }
}
