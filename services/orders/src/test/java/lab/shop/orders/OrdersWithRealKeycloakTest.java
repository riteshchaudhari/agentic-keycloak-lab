package lab.shop.orders;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Orders' HTTP API against a real Keycloak running the committed shop realm: real Customer tokens
 * in, a real client-credentials token out to the (stubbed) Catalog.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class OrdersWithRealKeycloakTest {

	@Container
	static final KeycloakContainer keycloak = KeycloakTokens.shopKeycloak();

	@RegisterExtension
	static WireMockExtension catalog = WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

	static KeycloakTokens tokens;

	@BeforeAll
	static void createTokens() {
		tokens = new KeycloakTokens(keycloak);
	}

	@DynamicPropertySource
	static void realKeycloak(DynamicPropertyRegistry registry) {
		registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> KeycloakTokens.issuerUri(keycloak));
		registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", () -> KeycloakTokens.jwkSetUri(keycloak));
		registry.add("spring.security.oauth2.client.provider.keycloak.token-uri", () -> KeycloakTokens.tokenUri(keycloak));
		registry.add("spring.security.oauth2.client.registration.catalog.client-secret", () -> "lab-only-orders-service-secret");
		registry.add("catalog.base-url", catalog::baseUrl);
	}

	@Autowired
	MockMvcTester mvc;

	@BeforeEach
	void stubCatalog() {
		catalog.stubFor(get("/products").willReturn(okJson("""
				[{"id": "p-mug", "name": "Coffee Mug", "priceCents": 1250}]""")));
	}

	@Test
	void aCustomerSeesProductsAndOrdersCallsTheCatalogWithItsOwnServiceToken() {
		var response = mvc.get().uri("/api/products").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.forAccount("alice"));

		assertThat(response).hasStatusOk();
		assertThat(response).bodyJson().extractingPath("$[0].name").isEqualTo("Coffee Mug");

		String sentToCatalog = catalog.getAllServeEvents().getFirst().getRequest().getHeader("Authorization");
		Map<?, ?> claims = claimsOf(sentToCatalog.substring("Bearer ".length()));
		assertThat(claims.get("azp")).isEqualTo("orders-service");
		List<Object> roles = List.copyOf((List<?>) ((Map<?, ?>) claims.get("realm_access")).get("roles"));
		assertThat(roles).contains("catalog-reader");
		catalog.verify(getRequestedFor(urlEqualTo("/products")).withHeader("Authorization", matching("Bearer .+")));
	}

	@Test
	void refusesAnAdminsRealToken() {
		assertThat(mvc.get().uri("/api/products").header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.forAccount("ada")))
			.hasStatus(HttpStatus.FORBIDDEN);
	}

	@Test
	void refusesARequestWithoutAToken() {
		assertThat(mvc.get().uri("/api/products")).hasStatus(HttpStatus.UNAUTHORIZED);
	}

	private static Map<?, ?> claimsOf(String jwt) {
		byte[] payload = Base64.getUrlDecoder().decode(jwt.split("\\.")[1]);
		return JsonMapper.shared().readValue(payload, Map.class);
	}

}
