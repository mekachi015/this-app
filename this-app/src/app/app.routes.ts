import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './components/auth/login/login.component';
import { SignUpComponent } from './components/auth/sign-up/sign-up.component';
import { AuthenticationPageComponent } from './pages/auth/authentication-page/authentication-page.component';
import { StorePageComponent } from './pages/store-front/store-page/store-page.component';
import { SelectedStoreComponent } from './pages/store-front/selected-store/selected-store.component';
import { CartPageComponent } from './pages/cart-page/cart-page.component';
import { StoreDashboardComponent } from './pages/store-dashboard/store-dashboard.component';

export const routes: Routes = [
     { path: '', component: AuthenticationPageComponent },
    { path: 'login', component: LoginComponent },
    { path: 'sign-up', component: SignUpComponent },
    { path: 'stores', component: StorePageComponent },
    { path: 'selected-store', component: SelectedStoreComponent },
    { path: 'cart', component: CartPageComponent },
    { path: 'dashboard', component: StoreDashboardComponent },
    { path: '**', redirectTo: 'stores', pathMatch: 'full' } // Wildcard route should be last
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
  })
  export class AppRoutingModule {}
