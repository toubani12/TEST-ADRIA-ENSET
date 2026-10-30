import Keycloak from 'keycloak-js';
import { HttpInterceptorFn } from '@angular/common/http';

export interface AppConfig { securityEnabled: boolean; keycloakUrl: string; realm: string; clientId: string; }

/** Keycloak (PKCE) activé si config.json -> securityEnabled = true ; sinon mode développement sans login. */
export const auth = {
  keycloak: null as Keycloak | null,
  get enabled() { return this.keycloak !== null; },
  get username(): string { return (this.keycloak?.tokenParsed as any)?.preferred_username ?? ''; },
  get roles(): string[] { return (this.keycloak?.tokenParsed as any)?.realm_access?.roles ?? []; },
  get isAdmin() { return !this.enabled || this.roles.includes('ADMIN'); },
  logout() { this.keycloak?.logout({ redirectUri: location.origin }); },
};

export async function initAuth(): Promise<void> {
  const cfg: AppConfig = await fetch('/config.json').then(r => r.json()).catch(() => ({ securityEnabled: false }));
  if (!cfg.securityEnabled) return;
  const kc = new Keycloak({ url: cfg.keycloakUrl, realm: cfg.realm, clientId: cfg.clientId });
  await kc.init({ onLoad: 'login-required', pkceMethod: 'S256', checkLoginIframe: false });
  setInterval(() => kc.updateToken(60).catch(() => kc.login()), 30000);
  auth.keycloak = kc;
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = auth.keycloak?.token;
  return next(token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req);
};
