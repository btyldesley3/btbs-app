package com.btbs.domain.shared.value;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MoneyTest {

    @Test
    void adds_And_Subtracts_With_Same_Currency() {
        var gbp = Currency.getInstance("GBP");
        var m1 = Money.of(new BigDecimal("10.00"), gbp);
        var m2 = Money.of(new BigDecimal("2.50"), gbp);

        assertEquals("GBP 12.50", m1.add(m2).toString());
//        System.out.println(m1.add(m2));
        assertEquals("GBP 7.50", m1.subtract(m2).toString());
//        System.out.println(m1.subtract(m2));
    }

    @Test
    void rejects_Currency_Mismatch() {
        var gbp = Currency.getInstance("GBP");
        var eur = Currency.getInstance("EUR");
        var m1 = Money.of(new BigDecimal("1.00"), gbp);
        var m2 = Money.of(new BigDecimal("1.00"), eur);

        assertThrows(IllegalArgumentException.class, () -> m1.add(m2));
//        System.out.println(m1.add(m2));
        assertThrows(IllegalArgumentException.class, () -> m1.subtract(m2));
//        System.out.println(m1.subtract(m2));
    }

    @Test
    void normalizes_Scale_To_Currency_Fraction_Digits() {
        var gbp = Currency.getInstance("GBP");
        var m = Money.of(new BigDecimal("1"), gbp);
        assertEquals(new BigDecimal("1.00"), m.amount());
//        System.out.println(m.currency().equals(gbp));
    }
}
