import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';
import { initAuth } from './app/core/auth';

// Authentification Keycloak (si activée dans config.json) avant le démarrage de l'application
initAuth()
  .then(() => bootstrapApplication(App, appConfig))
  .catch((err) => console.error(err));
