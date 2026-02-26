import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './components/auth/login/login.component';
import { SignUpComponent } from './components/auth/sign-up/sign-up.component';
import { AuthenticationPageComponent } from './pages/auth/authentication-page/authentication-page.component';
import { StorePageComponent } from './pages/store-front/store-page/store-page.component';
import { SelectedStoreComponent } from './pages/store-front/selected-store/selected-store.component';
import { CartPageComponent } from './pages/cart-page/cart-page.component';
import { StoreDashboardComponent } from './pages/store-dashboard/store-dashboard.component';
import { ProfileComponent } from './pages/profile/profile.component';
import { WishlistPageComponent } from './pages/wishlist-page/wishlist-page.component';
import { DriverComponentComponent } from './pages/driver-component/driver-component.component';
import { NewAuthComponent } from './components/auth/new-auth/new-auth.component';
import { ProductManagementComponent } from './pages/product-management/product-management.component';
import { AuthGuard } from './services/Auth-gaurds/auth-guard';
import { CustomerOrdersComponent } from './pages/customer-orders/customer-orders/customer-orders.component';
import { AddressPageComponent } from './pages/address-page/address-page.component';

export const routes: Routes = [
  { path: '', component: StorePageComponent },
  { path: 'login', component: NewAuthComponent },
  { path: 'sign-up', component: NewAuthComponent  },
  { path: 'login/admin', component: NewAuthComponent , canActivate: [AuthGuard], data: { roles: ['ADMIN'] } },
  { path: 'login/driver', component: NewAuthComponent , canActivate: [AuthGuard], data: { roles: ['DRIVER'] } },
  { path: 'sign-up/admin', component: NewAuthComponent, canActivate: [AuthGuard], data: { roles: ['ADMIN'] } },
  { path: 'sign-up/driver', component: NewAuthComponent , canActivate: [AuthGuard], data: { roles: ['DRIVER'] } },
  { path: 'stores', component: StorePageComponent },
  { path: 'store/:storeId', component: SelectedStoreComponent },
  { path: 'cart', component: CartPageComponent, canActivate: [AuthGuard], data: { roles: ['CUSTOMER'] } },
  { path: 'dashboard', component: StoreDashboardComponent, canActivate: [AuthGuard], data: { roles: ['ADMIN'] } },
  { path: 'profile', component: ProfileComponent  },
  { path: 'wishlist', component: WishlistPageComponent, canActivate: [AuthGuard], data: { roles: ['CUSTOMER'] } }, // Dynamic route for selected store
  { path: 'driver', component: DriverComponentComponent, canActivate: [AuthGuard], data: { roles: ['DRIVER'] } },
  { path: 'new', component: NewAuthComponent },
  { path: 'addresses', component: AddressPageComponent, canActivate: [AuthGuard], data: { roles: ['CUSTOMER'] } },
  { path: 'customer-orders', component: CustomerOrdersComponent, canActivate: [AuthGuard], data: { roles: ['CUSTOMER'] } },
  { path: 'product-management/:id', component: ProductManagementComponent, canActivate: [AuthGuard], data: { roles: ['ADMIN'] } },
  { path: '**', redirectTo: 'stores', pathMatch: 'full' }, // Wildcard route should be last
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
