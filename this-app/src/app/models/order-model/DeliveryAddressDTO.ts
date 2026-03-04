export interface DeliveryAddressDTO {
  addressId: number;
  streetNumber: string;
  streetName?: string;
  city: string;
  province: string;
  postalCode: string;
}