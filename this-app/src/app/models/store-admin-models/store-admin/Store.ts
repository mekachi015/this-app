export interface Store {
  storeId?: string | number;
  storeName: string;
  storeDescription: string;
  storeAddress: string;
  storeEmail: string;
  storePhoneNumber: string;
  storeBusinessHours: string;
  storeLogo?: string;
  createdAt: Date;
  updatedAt: Date;
  ownerId?: string;
}