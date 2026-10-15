package com.example.This_App_Backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "SERVER_PORT=8080",
    "server.port=8080",
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.flyway.enabled=false",
    "platform.shipping.fee=20.00",
    "platform.commission.percent=10",
    "platform.delivery.cut=20",
    "payfast.merchant-id=test",
    "payfast.merchant-key=test",
    "payfast.passphrase=test",
    "payfast.sandbox=true",
    "payfast.return.url=http://localhost/return",
    "payfast.cancel.url=http://localhost/cancel",
    "payfast.notify.url=http://localhost/notify",
    "cloudinary.cloud-name=test",
    "cloudinary.api-key=test",
    "cloudinary.api-secret=test",
    "jwt.secret=test-secret-key-for-testing-only-32chars-min",
    "jwt.expiration=86400000",
    "ors.api.key=test-ors-key"
})
class ThisAppBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
