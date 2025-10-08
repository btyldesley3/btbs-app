package com.btbs.infrastructure.persistence.jpa.converters;

import com.btbs.domain.shared.value.PhoneNumber;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PhoneNumberConverter implements AttributeConverter<PhoneNumber, String> {

    @Override
    public String convertToDatabaseColumn(PhoneNumber attribute) { //Convert PhoneNumber -> String to store in DB
        return attribute == null ? null : attribute.value();
    }

    @Override
    public PhoneNumber convertToEntityAttribute(String dbData) { //Convert String -> PhoneNumber if needed to take out of DB
        return dbData == null ? null : PhoneNumber.of(dbData);
    }
}
