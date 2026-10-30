package toubani.badr.transactionservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"spring.cloud.config.enabled=false", "bank.transaction.max-amount=1000"})
class TransactionServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
