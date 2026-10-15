export interface CheckoutInitiateResponse {
    pendingCheckoutId: string;
    paymentUrl: string;
    totalAmount: number;
    storeName: string;
}