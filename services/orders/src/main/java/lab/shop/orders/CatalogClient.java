package lab.shop.orders;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Talks to the Catalog as the Orders service itself, never as the Customer (ADR-0003). The
 * client-credentials token is cached by the authorized client service and reused until it expires.
 */
@Component
class CatalogClient {

	private static final String REGISTRATION = "catalog";

	private final RestClient http;

	CatalogClient(RestClient.Builder builder, @Value("${catalog.base-url}") String baseUrl,
			ClientRegistrationRepository registrations, OAuth2AuthorizedClientService authorizedClients) {
		var manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(registrations, authorizedClients);
		var serviceToken = new OAuth2ClientHttpRequestInterceptor(manager);
		serviceToken.setClientRegistrationIdResolver(request -> REGISTRATION);
		// The token belongs to the service, so one cached token is shared by every Customer's request.
		serviceToken.setPrincipalResolver(request -> new AnonymousAuthenticationToken("orders-service",
				"orders-service", AuthorityUtils.createAuthorityList("ROLE_SERVICE")));
		this.http = builder.baseUrl(baseUrl).requestInterceptor(serviceToken).build();
	}

	List<Product> products() {
		return http.get().uri("/products").retrieve().body(new ParameterizedTypeReference<>() {
		});
	}

}
