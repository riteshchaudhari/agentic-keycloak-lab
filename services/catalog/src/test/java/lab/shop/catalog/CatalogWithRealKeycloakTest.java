package lab.shop.catalog;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

/** Catalog's HTTP API with tokens signed by a real Keycloak running the committed shop realm. */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class CatalogWithRealKeycloakTest {

	@Container
	static final KeycloakContainer keycloak = KeycloakTokens.shopKeycloak();

	static KeycloakTokens tokens;

	@BeforeAll
	static void createTokens() {
		tokens = new KeycloakTokens(keycloak);
	}

	@DynamicPropertySource
	static void keycloakProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> KeycloakTokens.issuerUri(keycloak));
		registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", () -> KeycloakTokens.jwkSetUri(keycloak));
	}

	@Autowired
	MockMvcTester mvc;

	@Test
	void acceptsTheOrdersServiceToken() {
		assertThat(mvc.get().uri("/products").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.forOrdersService()))
			.hasStatusOk();
	}

	@Test
	void refusesACustomersToken() {
		assertThat(mvc.get().uri("/products").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.forAccount("alice")))
			.hasStatus(HttpStatus.FORBIDDEN);
	}

	@Test
	void refusesAnAdminsToken() {
		assertThat(mvc.get().uri("/products").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.forAccount("ada")))
			.hasStatus(HttpStatus.FORBIDDEN);
	}

	@Test
	void refusesARequestWithoutAToken() {
		assertThat(mvc.get().uri("/products")).hasStatus(HttpStatus.UNAUTHORIZED);
	}

}
