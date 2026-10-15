export interface DeliveryStatus{
    id: string;
    status: 'pending' | 'in-progress' | 'delivered' | 'cancelled';
    timestamp: Date;
}