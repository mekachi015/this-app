import { DeliveryItem } from '../delivery-item/DeliveryItem';
import { DeliveryStatus } from '../delivery-status/delivery-status';
import { DeliveryLocation } from '../delivery-location/delivery-location';

export interface Delivery{
    id: string;
    customerId: string;
    customerName: string;
    customerPhone?: string; // Optional field for customer phone number
    location: DeliveryLocation;
    //eta: string; // Estimated Time of Arrival
    status:DeliveryStatus
    items: DeliveryItem[];
    estimatedDeliveryTime: Date;
    createdAt: Date; // Timestamp when the delivery was created
    updatedAt: Date; // Timestamp when the delivery was last updated
}