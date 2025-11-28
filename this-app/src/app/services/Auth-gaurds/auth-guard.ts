import { Injectable } from '@angular/core';
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
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return false;
    }

    // 2. Check for required roles (Authorization)
    const requiredRoles = route.data['roles'] as Array<string>;

    if (requiredRoles && requiredRoles.length > 0) {
      const currentUserRole = this.authService.getUserRole(); // You must implement this method in AuthService
      
      // If currentUserRole is null/undefined, treat as missing role and deny access
      if (currentUserRole == null || !requiredRoles.includes(currentUserRole)) {
        // User is logged in but lacks the required role (e.g., trying to access admin page)
        alert('Access Denied: You do not have the required role.'); 
        this.router.navigate(['/home']); // Redirect to a safe page
        return false;
      }
    }
    
    // User is logged in and has the required role (if specified)
    return true;
  }
}