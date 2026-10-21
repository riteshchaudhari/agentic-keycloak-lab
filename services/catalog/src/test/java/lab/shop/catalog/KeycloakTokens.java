package lab.shop.catalog;

import java.util.List;
import java.util.Map;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.keycloak.representations.idm.ClientRepresentation;

import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;

/**
 * Gets real tokens from a throwaway Keycloak running the shop realm.
 *
 * Customer tokens need the password grant, which no committed client allows. So this creates a
 * password-grant test client through the admin API, inside the test container only.
 */
final class KeycloakTokens {

	static final String IMAGE = "quay.io/keycloak/keycloak:26.8.1";

	static final String REALM = "shop";

	private static final String TEST_CLIENT = "test-password-grant";

	private final KeycloakContainer keycloak;

	private final RestClient http = RestClient.create();

	KeycloakTokens(KeycloakContainer keycloak) {
		this.keycloak = keycloak;
		createPasswordGrantTestClient();
	}

	static KeycloakContainer shopKeycloak() {
		return new KeycloakContainer(IMAGE).withRealmImportFile("shop-realm.json");
	}

	static String issuerUri(KeycloakContainer keycloak) {
		return keycloak.getAuthServerUrl() + "/realms/" + REALM;
	}

	static String jwkSetUri(KeycloakContainer keycloak) {
		return issuerUri(keycloak) + "/protocol/openid-connect/certs";
	}

	static String tokenUri(KeycloakContainer keycloak) {
		return issuerUri(keycloak) + "/protocol/openid-connect/token";
	}

	/** An access token for a seed account (alice, bob, ada). */
	String forAccount(String username) {
		return token(Map.of("grant_type", "password", "client_id", TEST_CLIENT, "username", username, "password", username));
	}

	/** An access token the Orders service would get for itself (client credentials, ADR-0003). */
	String forOrdersService() {
		return token(Map.of("grant_type", "client_credentials", "client_id", "orders-service", "client_secret",
				"lab-only-orders-service-secret"));
	}

	private String token(Map<String, String> form) {
		var body = new LinkedMultiValueMap<String, String>();
		form.forEach(body::add);
		Map<?, ?> response = http.post()
			.uri(tokenUri(keycloak))
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.body(body)
			.retrieve()
			.body(Map.class);
		return (String) response.get("access_token");
	}

	private void createPasswordGrantTestClient() {
		var client = new ClientRepresentation();
		client.setClientId(TEST_CLIENT);
		client.setPublicClient(true);
		client.setDirectAccessGrantsEnabled(true);
		client.setStandardFlowEnabled(false);
		client.setRedirectUris(List.of());
		try (var response = keycloak.getKeycloakAdminClient().realm(REALM).clients().create(client)) {
			if (response.getStatus() != 201) {
				throw new IllegalStateException("Could not create test client: HTTP " + response.getStatus());
			}
		}
	}

}
