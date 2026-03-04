import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { WalletBalance } from '../../../models/wallet-models/walletBalance';
import { WalletTransaction } from '../../../models/wallet-models/walletTransaction';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/authentication-service/auth.service';
import { WalletService } from '../../../services/wallet-service/wallet.service';

@Component({
  selector: 'app-wallet-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './wallet-page.component.html',
  styleUrl: './wallet-page.component.scss'
})
export class WalletPageComponent implements OnInit {

  wallet: WalletBalance | null = null;
  transactions: WalletTransaction[] = [];

  isLoading= true;
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private walletService: WalletService,
    private router: Router
  ){}

  ngOnInit(): void{
    const user = this.authService.currentUserValue;
    if(!user?.id){
      this.router.navigate(['/login']);
      return;
    }
    this.loadWallet(Number(user.id));
  }

  private loadWallet(userId: number): void{
    this.isLoading = true

    //call get wallet
    this.walletService.getBalance(userId).subscribe({
      next: (data) => {
        this.wallet = data;
        this.loadTransaction(userId);
      },
      error: () => {
        this.errorMessage = 'Could not load wallet, please try again.';
        this.isLoading = false;
      }
    });
  }

  private loadTransaction(userId: number): void {
    this.walletService.getTransactions(userId).subscribe({
      next: (data) => {
        this.transactions = data;
        this.isLoading = false;
      },
      error: () => {
        this.transactions = [];
        this.isLoading = false;
      }
    });
  }

  goBack(): void{
    // this.router.navigate(['/profile']);
    window.history.back();

  }

  //Maps CREDIT or DEBIT to a css class for colour coding
  txClass(type: string): string {
    return type === 'CREDIT' ? 'credit' : 'debit';
  }
}
