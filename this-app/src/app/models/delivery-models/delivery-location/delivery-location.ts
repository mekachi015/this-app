export interface DeliveryLocation{
    latitude: number;
    longitude: number;
    address: string; // Full address of the delivery location
    city: string;
    postalCode: string; // Postal code for the delivery location
}