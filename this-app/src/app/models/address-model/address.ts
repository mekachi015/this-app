export interface Address {
  addressId: number;
  streetNumber: string;
  streetName?: string;
  suburb?: string;
  city: string;
  province: string;
  postalCode: string;
  addressType: string;
  isDefault: boolean;
}