package com.btbs.domain.shared.value;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PhoneNumberTest {

    @Test
    void accepts_E164_And_00_Prefix() {
        assertEquals("+447911123456", PhoneNumber.of("+447911123456").value());
        assertEquals("+447911123456", PhoneNumber.of("00447911123456").value());
//        PhoneNumber pn = PhoneNumber.of("+447911123456");
//        System.out.println(pn);
//        PhoneNumber pn = PhoneNumber.of("00447911123456");
//        System.out.println(pn);
    }

    @Test
    void rejects_Invalid_Format() {
        assertThrows(IllegalArgumentException.class, () -> PhoneNumber.of("07911 123"));
//        System.out.println(PhoneNumber.of("07911 123"));
        assertThrows(IllegalArgumentException.class, () -> PhoneNumber.of("abc"));
//        System.out.println(PhoneNumber.of("abc"));
    }
}
