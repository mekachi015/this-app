export interface StoreDTO {
  storeId?: number;
  storeName: string;
  storeDescription: string;
  storeAddress: string;
  storeEmail: string;
  storePhoneNumber: string;
  storeBusinessHours: string;
  storeLogo?: string;
  ownerId?: number;  // ← Fixed typo
}