package br.com.nextagon;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=jdbc:h2:mem:nextagon_test;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
		"spring.jpa.hibernate.ddl-auto=create-drop",

		"jwt.secret=chave-secreta-apenas-para-testes-nextagon",
		"jwt.expiration=900000",
		"jwt.refresh-expiration=604800000"
})
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}
}