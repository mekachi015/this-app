import { Injectable } from '@angular/core';
import Swal from 'sweetalert2';
import { Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthService } from '../authentication-service/auth.service';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> | Promise<boolean> | boolean {
    
    // 1. Check if the user is logged in
    const isLoggedIn = this.authService.isLoggedIn();
    const currentUserRole = this.authService.getUserRole();
    const url = state.url;

    // Define role-specific login/signup routes
    const roleRoutes = {
      ADMIN: ['/login/admin', '/sign-up/admin'],
      DRIVER: ['/login/driver', '/sign-up/driver'],
      CUSTOMER: ['/login', '/sign-up']
    };

    // If not logged in, allow access only to login/signup pages
    if (!isLoggedIn) {
      return true;
    }

    // If logged in, prevent access to login/signup pages for other roles
    if (currentUserRole) {
      // Flatten all login/signup routes
      const allLoginSignupRoutes = [
        ...roleRoutes.ADMIN,
        ...roleRoutes.DRIVER,
        ...roleRoutes.CUSTOMER
      ];
      // Get allowed login/signup routes for current role
      const allowedRoutes = roleRoutes[currentUserRole as keyof typeof roleRoutes] || [];
      // If trying to access a login/signup route not for their role, block
      if (
        allLoginSignupRoutes.includes(url) &&
        !allowedRoutes.includes(url)
      ) {
        Swal.fire({
          icon: 'error',
          title: 'Access Denied',
          text: 'You cannot access this page'
        });
        this.router.navigate(['/home']);
        return false;
      }
    }

    // 2. Check for required roles (Authorization)
    const requiredRoles = route.data['roles'] as Array<string>;
    if (requiredRoles && requiredRoles.length > 0) {
      if (currentUserRole == null || !requiredRoles.includes(currentUserRole)) {
        Swal.fire({
          icon: 'error',
          title: 'Access Denied',
          text: 'You do not have the required role.'
        });
        this.router.navigate(['/home']);
        return false;
      }
    }

    // User is logged in and has the required role (if specified)
    return true;
  }
}