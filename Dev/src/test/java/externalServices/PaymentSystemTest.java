package externalServices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PaymentSystemTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void pay() {
        PaymentData paymentData = new PaymentData();
        PaymentSystem paymentSystem = new PaymentSystem();
        paymentSystem.pay(paymentData);
    }
}