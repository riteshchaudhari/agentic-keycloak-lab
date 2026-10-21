package lab.shop.catalog;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

/**
 * Catalog's HTTP API with fake tokens. Tokens are shaped like Keycloak's and run through the
 * service's own role mapping, so these tests cover how roles are read, not just the rules.
 */
@SpringBootTest(properties = "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://unused.invalid/certs")
@AutoConfigureMockMvc
class CatalogApiTest {

	@Autowired
	MockMvcTester mvc;

	@Autowired
	JwtAuthenticationConverter roleMapping;

	@Test
	void listsAllProductsWithTheirPricesForOrders() {
		var response = mvc.get().uri("/products").with(tokenWithRealmRoles("catalog-reader"));

		assertThat(response).hasStatusOk();
		assertThat(response).bodyJson().extractingPath("$.length()").isEqualTo(5);
		assertThat(response).bodyJson().extractingPath("$[0]").isEqualTo(
				Map.of("id", "p-mug", "name", "Coffee Mug", "priceCents", 1250));
	}

	@Test
	void refusesACustomer() {
		assertThat(mvc.get().uri("/products").with(tokenWithRealmRoles("customer"))).hasStatus(HttpStatus.FORBIDDEN);
	}

	@Test
	void refusesAnAdmin() {
		assertThat(mvc.get().uri("/products").with(tokenWithRealmRoles("admin"))).hasStatus(HttpStatus.FORBIDDEN);
	}

	@Test
	void refusesATokenWithoutRealmRoles() {
		assertThat(mvc.get().uri("/products").with(jwt().authorities(token -> roleMapping.convert(token).getAuthorities())))
			.hasStatus(HttpStatus.FORBIDDEN);
	}

	@Test
	void refusesARequestWithoutAToken() {
		assertThat(mvc.get().uri("/products")).hasStatus(HttpStatus.UNAUTHORIZED);
	}

	private JwtRequestPostProcessor tokenWithRealmRoles(String... roles) {
		return jwt().jwt(token -> token.claim("realm_access", Map.of("roles", List.of(roles))))
			.authorities(token -> roleMapping.convert(token).getAuthorities());
	}

}
