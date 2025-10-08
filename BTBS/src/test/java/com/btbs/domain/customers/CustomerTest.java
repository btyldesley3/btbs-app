package com.btbs.domain.customers;

import com.btbs.domain.shared.id.CustomerId;
import com.btbs.domain.shared.value.PhoneNumber;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomerTest {

    @Test
    void VerifyKyc_Transitions_Status() {
        var c = new Customer(
                new CustomerId(UUID.randomUUID()),
                "Aaliyah Tester",
                LocalDate.of(1995, 1, 1),
                "aaliyah@example.com",
                PhoneNumber.of("+447911123456"),
                KycStatus.PENDING,
                true,
                0L
        );

        var verified = c.verifyKyc();
        assertEquals(KycStatus.VERIFIED, verified.kycStatus());
//        System.out.println(verified.kycStatus());
    }

}
