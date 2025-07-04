import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of , throwError } from 'rxjs';
import { map,delay } from 'rxjs';
import { User } from '../../models/user/user';
import { response } from 'express';


@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private mockUserUrl = 'assets/test-data/user/user.json'; // replace with api url

  constructor(private http: HttpClient) { }

  login(email: string, password: string): Observable<User> {
    return this.http.get<{users: User[]}>(this.mockUserUrl).pipe(
      delay(1000), // Simulate network delay
      map(response => {
        const user = response.users.find(u => 
          u.email === email && u.password === password
        );
        
        if (user) {
          return user;
        }
        throw new Error('Invalid credentials');
      })
    );
  }

  register(userData: Omit<User, 'id' | 'createdAt'>): Observable<User> {
    return this.http.get<{users: User[]}>(this.mockUserUrl).pipe(
      delay(1000),
      map(response => {
        // Check if user already exists
        if (response.users.some(u => u.email === userData.email)) {
          throw new Error('User already exists');
        }

        // Create new user
        const newUser: User = {
          ...userData,
          id: (response.users.length + 1).toString(),
          createdAt: new Date().toISOString()
        };

        return newUser;
      })
    );
  }

}
