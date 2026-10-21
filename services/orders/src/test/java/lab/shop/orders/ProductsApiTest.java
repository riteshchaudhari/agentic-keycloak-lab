package lab.shop.orders;

import java.util.List;
import java.util.Map;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.lessThanOrExactly;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

/**
 * Orders' HTTP API with fake Customer tokens. Keycloak's token endpoint and the Catalog are stubbed
 * over HTTP, so Orders' real Catalog client runs and we can see what it sends.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ProductsApiTest {

	@RegisterExtension
	static WireMockExtension keycloak = WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

	@RegisterExtension
	static WireMockExtension catalog = WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

	@DynamicPropertySource
	static void stubbedBoundaries(DynamicPropertyRegistry registry) {
		registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", () -> "http://unused.invalid/certs");
		registry.add("spring.security.oauth2.client.provider.keycloak.token-uri",
				() -> keycloak.baseUrl() + "/realms/shop/protocol/openid-connect/token");
		registry.add("catalog.base-url", catalog::baseUrl);
	}

	@Autowired
	MockMvcTester mvc;

	@Autowired
	JwtAuthenticationConverter roleMapping;

	@BeforeEach
	void stubKeycloakAndCatalog() {
		keycloak.stubFor(post(urlEqualTo("/realms/shop/protocol/openid-connect/token"))
			.willReturn(okJson("""
					{"access_token": "orders-service-token", "token_type": "Bearer", "expires_in": 300}""")));
		catalog.stubFor(get("/products").willReturn(okJson("""
				[{"id": "p-mug", "name": "Coffee Mug", "priceCents": 1250},
				 {"id": "p-tee", "name": "Lab T-Shirt", "priceCents": 2400}]""")));
	}

	@Test
	void aCustomerSeesTheCatalogsProductsFetchedWithTheOrdersServiceToken() {
		var response = mvc.get().uri("/api/products").with(tokenWithRealmRoles("customer"));

		assertThat(response).hasStatusOk();
		assertThat(response).bodyJson().isEqualTo("""
				[{"id": "p-mug", "name": "Coffee Mug", "priceCents": 1250},
				 {"id": "p-tee", "name": "Lab T-Shirt", "priceCents": 2400}]""");
		catalog.verify(getRequestedFor(urlEqualTo("/products"))
			.withHeader("Authorization", equalTo("Bearer orders-service-token")));
	}

	@Test
	void reusesOneOrdersServiceTokenForEveryCustomer() {
		assertThat(mvc.get().uri("/api/products").with(tokenFor("alice-sub", "customer"))).hasStatusOk();
		assertThat(mvc.get().uri("/api/products").with(tokenFor("bob-sub", "customer"))).hasStatusOk();

		// At most one: the shared Spring context may already hold the token from an earlier test.
		keycloak.verify(lessThanOrExactly(1), postRequestedFor(urlEqualTo("/realms/shop/protocol/openid-connect/token")));
		catalog.verify(2, getRequestedFor(urlEqualTo("/products")));
	}

	@Test
	void refusesAnAdminWhoIsNotACustomer() {
		assertThat(mvc.get().uri("/api/products").with(tokenWithRealmRoles("admin"))).hasStatus(HttpStatus.FORBIDDEN);
	}

	@Test
	void refusesARequestWithoutAToken() {
		assertThat(mvc.get().uri("/api/products")).hasStatus(HttpStatus.UNAUTHORIZED);
		catalog.verify(0, getRequestedFor(urlEqualTo("/products")));
	}

	private JwtRequestPostProcessor tokenWithRealmRoles(String... roles) {
		return tokenFor("some-customer-sub", roles);
	}

	/** A Keycloak-shaped token: roles live in realm_access.roles and go through the service's own role mapping. */
	private JwtRequestPostProcessor tokenFor(String subject, String... roles) {
		return jwt().jwt(token -> token.subject(subject).claim("realm_access", Map.of("roles", List.of(roles))))
			.authorities(token -> roleMapping.convert(token).getAuthorities());
	}

}
