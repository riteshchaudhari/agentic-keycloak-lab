import { Component, computed, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { OidcSecurityService } from 'angular-auth-oidc-client';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  template: `
    <header>
      <strong>Agentic Keycloak Lab Shop</strong>
      @if (isAuthenticated()) {
        <span class="who" data-testid="username">{{ username() }}</span>
      } @else {
        <button type="button" (click)="login()">Log in</button>
      }
    </header>
    @if (isAuthenticated()) {
      <main><router-outlet /></main>
    }
  `,
})
export class App {
  private readonly oidc = inject(OidcSecurityService);

  protected readonly isAuthenticated = computed(() => this.oidc.authenticated().isAuthenticated);
  protected readonly username = computed(() => this.oidc.userData().userData?.preferred_username ?? '');

  protected login(): void {
    this.oidc.authorize();
  }
}
