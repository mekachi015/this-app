import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { provideClientHydration } from '@angular/platform-browser';
import { HttpClientModule, provideHttpClient , withInterceptors} from '@angular/common/http';
import { JwtInterceptor } from './services/jwt-interceptor/jwt.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [provideRouter(routes),
    provideClientHydration(),
      provideHttpClient(),
      HttpClientModule,
      {
        provide: 'HTTP_INTERCEPTORS',
        useClass: JwtInterceptor,
        multi: true
      }
    ],
};
