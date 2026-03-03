export interface WalletTransaction{
    transactionId: number;
    orderId: number | null;
    amount : number;
    type: 'CREDIT' | 'DEBIT';
    description: string;
    createdAt: string;
}