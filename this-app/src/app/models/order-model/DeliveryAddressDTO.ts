export interface DeliveryAddressDTO {
  addressId: number;
  addressLine1: string;
  addressLine2?: string;
  city: string;
  province: string;
  postalCode: string;
}