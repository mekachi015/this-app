export interface WalletBalance{
    walletId: number;
    balance: number;
    ownerName: string;
    ownerType: 'ADMIN' | 'DRIVER';
    updatedAt: string;
}