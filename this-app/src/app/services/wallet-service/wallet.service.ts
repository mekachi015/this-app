import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { WalletBalance } from '../../models/wallet-models/walletBalance';
import { Observable } from 'rxjs';
import { WalletTransaction } from '../../models/wallet-models/walletTransaction';

@Injectable({
  providedIn: 'root'
})
export class WalletService {
  private baseUrl = 'http://localhost:9091/api/wallet';

  constructor(
    private http: HttpClient
  ) { }

  getBalance(userId: number): Observable<WalletBalance>{
    return this.http.get<WalletBalance>(`${this.baseUrl}/${userId}/balance`);
  }

  getTransactions(userId: number): Observable<WalletTransaction[]> {
    return this.http.get<WalletTransaction[]>(`${this.baseUrl}/${userId}/transaction`);
  }
}
