export interface Address {
  addressId: number;
  addressLine1: string;
  addressLine2?: string;
  addressLine3?: string;
  city: string;
  province: string;
  postalCode: string;
  addressType: string;
  isDefault: boolean;
}