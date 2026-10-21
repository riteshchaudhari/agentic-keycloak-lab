import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import {
  authInterceptor,
  LogLevel,
  provideAuth,
  withAppInitializerAuthCheck,
} from 'angular-auth-oidc-client';

import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor()])),
    provideAuth(
      {
        config: {
          // Public client with Authorization Code + PKCE; tokens live in the browser (ADR-0001).
          authority: 'http://localhost:8080/realms/shop',
          clientId: 'shop-frontend',
          redirectUrl: window.location.origin,
          postLogoutRedirectUri: window.location.origin,
          scope: 'openid profile',
          responseType: 'code',
          // Refresh tokens, never hidden-iframe silent renew (third-party cookies are blocked).
          silentRenew: true,
          useRefreshToken: true,
          renewTimeBeforeTokenExpiresInSeconds: 30,
          // Keycloak issues refresh tokens without offline_access; we don't want offline sessions.
          disableRefreshTokenOfflineAccessScopeWarning: true,
          // Only same-origin API calls get the Bearer token.
          secureRoutes: ['/api/'],
          logLevel: LogLevel.Warn,
        },
      },
      withAppInitializerAuthCheck(),
    ),
  ],
};
