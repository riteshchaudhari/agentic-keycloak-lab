package lab.shop.orders;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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

	/** Key under which the service's own token is cached; carries no authorities and grants nothing. */
	private static final Authentication SERVICE = UsernamePasswordAuthenticationToken.unauthenticated("orders-service", null);

	private final RestClient http;

	CatalogClient(RestClient.Builder builder, @Value("${catalog.base-url}") String baseUrl,
			ClientRegistrationRepository registrations, OAuth2AuthorizedClientService authorizedClients) {
		var manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(registrations, authorizedClients);
		var serviceToken = new OAuth2ClientHttpRequestInterceptor(manager);
		serviceToken.setClientRegistrationIdResolver(request -> REGISTRATION);
		// The token belongs to the service, not the Customer, so one cached token serves every request.
		serviceToken.setPrincipalResolver(request -> SERVICE);
		this.http = builder.baseUrl(baseUrl).requestInterceptor(serviceToken).build();
	}

	List<Product> products() {
		return http.get().uri("/products").retrieve().body(new ParameterizedTypeReference<>() {
		});
	}

}
