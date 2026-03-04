export interface WishlistDto {
  wishlistId: number;
  userId: number;
  productId?: number;
  productName?: string;
  productImage?: string;
  productPrice?: number;
  storeId?: number;
  storeName?: string;
  storeImage?: string; // Add this if your API provides it
  createdAt: string;
  updatedAt: string;
}