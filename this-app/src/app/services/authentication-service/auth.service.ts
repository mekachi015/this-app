import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { map, catchError, tap, switchMap } from 'rxjs/operators';
import { User } from '../../models/user/user';
import { Router } from '@angular/router';
import { response } from 'express';


@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiUrl = 'http://localhost:8080/api'; // Your backend API URL
  private currentUserSubject: BehaviorSubject<User | null>;
  public currentUser: Observable<User | null>;

  constructor(private http: HttpClient, private router : Router) {
    // Initialize currentUserSubject with user from localStorage if exists
    const storedUser = localStorage.getItem('currentUser');
    this.currentUserSubject = new BehaviorSubject<User | null>(
      storedUser ? JSON.parse(storedUser) : null
    );
    this.currentUser = this.currentUserSubject.asObservable();
  }

  public get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  public get token(): string | null {
    const user = this.currentUserValue;
    return user && user.token ? user.token : null;
  }

  login(email: string, password: string): Observable<User> {
    return this.http.post<any>(`${this.apiUrl}/auth/login`, { 
      username: email, // Using email as username
      password: password 
    }).pipe(
      switchMap(response => {
        if (response.jwt) {
          // If the login response already contains user details, use them
          if (response.user) {
            const user: User = {
              ...response.user,
              token: response.jwt
            };
            localStorage.setItem('currentUser', JSON.stringify(user));
            this.currentUserSubject.next(user);
            return [user]; // Return as observable
          }
          
          // If not, fetch user details separately
          return this.getUserDetails(response.jwt).pipe(
            map(userDetails => {
              const user: User = {
                ...userDetails,
                token: response.jwt
              };
              localStorage.setItem('currentUser', JSON.stringify(user));
              this.currentUserSubject.next(user);
              return user;
            })
          );
        }
        throw new Error('No token received');
      }),
      catchError(error => {
        console.error('Login error:', error);
        return throwError(() => ({
          message: error.error?.message || 'Invalid credentials',
          status: error.status
        }));
      })
    );
  }

 register(userData: any): Observable<User> {
    // Split fullName into firstName and lastName
    const fullNameParts = userData.fullName.split(' ');
    const firstName = fullNameParts[0];
    const lastName = fullNameParts.slice(1).join(' ') || '';

    const registrationData = {
      firstName: firstName,
      lastName: lastName,
      username: userData.username,
      email: userData.email,
      phoneNumber: userData.phoneNumber || '',
      password: userData.password,
      userType: 'CUSTOMER'
    };

    // Fixed endpoint to match backend
    return this.http.post<any>(`${this.apiUrl}/auth/register`, registrationData)
      .pipe(
        map(response => {
          // After successful registration, return the user data
          const user: User = {
            id: response.id || response.userId,
            email: response.email,
            password: response.password,
            username: response.username,
            firstname: response.firstName || response.firstname,
            lastname: response.lastName || response.lastname,
            userType: response.userType,
            token: response.token, // This might not be provided on registration
            createdAt: response.createdAt
          };
          return user;
        }),
        catchError(error => {
          console.error('Registration error:', error);
          return throwError(() => ({
            message: error.error?.message || 'Registration failed',
            status: error.status
          }));
        })
      );
  }

  logout(): void {
     // Remove user from local storage and set current user to null
    localStorage.removeItem('currentUser');
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  // Helper method to add authorization header
  getAuthHeaders(): HttpHeaders {
    const token = this.token;
    if (token) {
      return new HttpHeaders({
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      });
    }
    return new HttpHeaders({ 'Content-Type': 'application/json' });
  }

  // Check if user is logged in
  isLoggedIn(): boolean {
   return !!this.currentUserValue && !!this.token;
  }

  // Get user role
  getUserRole(): string | null {
    const user = this.currentUserValue;
    return user ? user.userType : null;
  }

   // Helper method to get user details after login
  private getUserDetails(email: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/users/profile`, {
      headers: this.getAuthHeaders()
    });
  }

}
