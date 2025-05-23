import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './components/auth/login/login.component';
import { SignUpComponent } from './components/auth/sign-up/sign-up.component';
import { AuthenticationPageComponent } from './pages/auth/authentication-page/authentication-page.component';
import { StorePageComponent } from './pages/store-front/store-page/store-page.component';
import { SelectedStoreComponent } from './pages/store-front/selected-store/selected-store.component';

export const routes: Routes = [
    { path: '', component: AuthenticationPageComponent },
    // {path :'login', component: LoginComponent},
    // {path :'sign-up', component: SignUpComponent},  
    {path : '**', redirectTo: 'login', pathMatch: 'full'},
    {path : 'stores', component: StorePageComponent},
    {path : 'selected-store', component: SelectedStoreComponent},
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
  })
  export class AppRoutingModule {}
