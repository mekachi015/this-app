import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, BehaviorSubject, throwError, of } from 'rxjs';
import { map, catchError, tap, switchMap } from 'rxjs/operators';
import { User } from '../../models/user/user';
import { Router } from '@angular/router';


@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private apiUrl = 'http://localhost:9091/api'; // Your backend API URL
  private currentUserSubject: BehaviorSubject<User | null>;
  public currentUser: Observable<User | null>;

  constructor(private http: HttpClient, private router: Router) {
    // Check if we're in a browser environment before accessing localStorage
    let storedUser = null;
    if (typeof window !== 'undefined' && window.localStorage) {
      const storedUserString = localStorage.getItem('currentUser');
      storedUser = storedUserString ? JSON.parse(storedUserString) : null;
    }

    this.currentUserSubject = new BehaviorSubject<User | null>(storedUser);
    this.currentUser = this.currentUserSubject.asObservable();
  }

  public get currentUserValue(): User | null {
    return this.currentUserSubject.value;
  }

  public get token(): string | null {
    const user = this.currentUserValue;
    return user && user.token ? user.token : null;
  }

  login(
    username: string,
    password: string,
    userType: string = 'CUSTOMER'
  ): Observable<User> {
    console.log('AuthService.login called with:', {
      username,
      password: password,
    });

    let endpoint = `${this.apiUrl}/auth/login`;
    if (userType === 'DRIVER') {
      endpoint = `${this.apiUrl}/auth/login/driver`;
    } else if (userType === 'ADMIN') {
      endpoint = `${this.apiUrl}/auth/login/admin`;
    }
    return this.http
      .post<any>(endpoint, {
        username: username,
        password: password,
      })
      .pipe(
        tap((response) => console.log('Login response:', response)),
        map((response) => {
          if (response.jwt) {
            // Create user object from the response
            const user: User = {
              id: response.id,
              email: response.email,
              username: response.username,
              password: response.password,
              firstname: response.firstName,
              lastname: response.lastName,
              userType: response.userType,
              profilePhotoUrl: response.profilePhotoUrl,
              token: response.jwt,
              createdAt: response.createdAt,
            };

            // Save to localStorage safely
            if (typeof window !== 'undefined' && window.localStorage) {
              localStorage.setItem('currentUser', JSON.stringify(user));
            }

            this.currentUserSubject.next(user);
            console.log('User set as current user:', user);
            return user;
          }
          throw new Error('No token received');
        }),
        catchError((error) => {
          console.error('Login error:', error);
          return throwError(() => ({
            message: error.error?.message || 'Invalid credentials',
            status: error.status,
          }));
        })
      );
  }

  // Specific login methods for different user types
  loginDriver(username: string, password: string): Observable<User> {
    return this.login(username, password, 'DRIVER');
  }

  loginAdmin(username: string, password: string): Observable<User> {
    return this.login(username, password, 'ADMIN');
  }

  loginCustomer(username: string, password: string): Observable<User> {
    return this.login(username, password, 'CUSTOMER');
  }

  registerDriver(userData: any): Observable<User> {
    return this.register(userData, 'DRIVER');
  }

  registerAdmin(userData: any): Observable<User> {
    return this.register(userData, 'ADMIN');
  }

  register(userData: any, userType: string = 'CUSTOMER'): Observable<User> {
    console.log('AuthService.register called with:', userData);

    // Split fullName into firstName and lastName
    const fullNameParts = userData.fullName.split(' ');
    const firstName = fullNameParts[0];
    const lastName = fullNameParts.slice(1).join(' ') || '';

    // Determine the endpoint based on userType
    let endpoint = `${this.apiUrl}/auth/register`;
    if (userType === 'DRIVER') {
      endpoint = `${this.apiUrl}/auth/register/driver`;
    } else if (userType === 'ADMIN') {
      endpoint = `${this.apiUrl}/auth/register/admin`;
    }

    const registrationData = {
      firstName: firstName,
      lastName: lastName,
      username: userData.username,
      email: userData.email,
      phoneNumber: userData.phoneNumber || '',
      password: userData.password,
      userType: userType,
    };

    console.log('Registration data being sent:', registrationData);

    return this.http.post<any>(endpoint, registrationData).pipe(
      tap((response) => console.log('Registration response:', response)),
      map((response) => {
        // After successful registration, return the user data
        const user: User = {
          id: response.id || response.userId,
          email: response.email,
          password: response.password,
          username: response.username,
          firstname: response.firstName || response.firstname,
          lastname: response.lastName || response.lastname,
          userType: response.userType,
          profilePhotoUrl: response.profilePhotoUrl,
          token: response.token || response.jwt, // Check for both token formats
          createdAt: response.createdAt,
        };

        // If registration returns a token, set as current user
        if (user.token) {
          if (typeof window !== 'undefined' && window.localStorage) {
            localStorage.setItem('currentUser', JSON.stringify(user));
          }
          this.currentUserSubject.next(user);
          console.log('User registered and set as current user:', user);
        }

        return user;
      }),
      catchError((error) => {
        console.error('Registration error:', error);
        return throwError(() => ({
          message: error.error?.message || 'Registration failed',
          status: error.status,
        }));
      })
    );
  }

  getLatestUserProfile(): Observable<User> {
  const headers = this.getAuthHeaders();
  return this.http.get<User>(`${this.apiUrl}/profile/me`, { headers });
}

  logout(): void {
    // Remove user from local storage and set current user to null
    if (typeof window !== 'undefined' && window.localStorage) {
      localStorage.removeItem('currentUser');
    }
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

getAuthHeadersForMultipart(token?: string): HttpHeaders {
  const authToken = token || this.token;
  if (authToken) {
    return new HttpHeaders({
      'Authorization': `Bearer ${authToken}`
      // No Content-Type - browser sets it for multipart
    });
  }
  return new HttpHeaders();
}

// Or modify existing method to accept a flag
getAuthHeaders(token?: string, includeContentType: boolean = true): HttpHeaders {
  const authToken = token || this.token;
  const headers: any = {};

  if (authToken) {
    headers['Authorization'] = `Bearer ${authToken}`;
  }

  if (includeContentType) {
    headers['Content-Type'] = 'application/json';
  }

  return new HttpHeaders(headers);
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

  // // Helper method to get user details after login
  // private getUserDetails(token: string): Observable<any> {
  //   return this.http.get<any>(`${this.apiUrl}/users/profile`, {
  //     headers: this.getAuthHeaders(token),
  //   });
  // }

  uploadProfilePhoto(formData: FormData): Observable<{ photoUrl: string }> {
    const token = this.getToken();
    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`,
    });

    return this.http.post<{ photoUrl: string }>(
      `${this.apiUrl}/profile/upload-photo`,
      formData,
      { headers }
    );
  }

//Refactored method for uploading profile photo 
uploadProfilePhotoRefactored(formData: FormData): Observable<{photoUrl: string}>{
  const token = this.getToken();
  const userId = this.currentUserValue?.id; //get from current user

  if(!userId){
    return throwError(() => new Error('User not logged in'));
  }

  const headers = new HttpHeaders({
    Authorization: `Bearer ${token}`,
  });

  return this.http.post<{photoUrl: string}>(
    `${this.apiUrl}/users/${userId}/profile-photo`,
    formData,
    {headers}
  );
}

  // Helper method to get token
  private getToken(): string {
    const user = this.currentUserValue;
    return user?.token || '';
  }

  /**
   * Redirects the user to the login page.
   * Optionally, a return URL can be passed to redirect the user back after login.
   */
  redirectToLogin(returnUrl: string = '/'): void {
    this.router.navigate(['/login'], { queryParams: { returnUrl } });
  }

 getUserProfilePicture(userId: number, headers: HttpHeaders): Observable<{ profilePhotoUrl: string }> {
  // const token = this.getToken();
  return this.http.get<{ profilePhotoUrl: string }>(
    `${this.apiUrl}/users/${userId}/profile-picture`,
    { headers }
  );
}
}
