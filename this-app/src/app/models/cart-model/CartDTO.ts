export interface CartDTO{
    cartItemId: number;
  userId: number;
  productId: number;
  productName: string;
  productImage: string;
  productPrice: number;
  storeId: number;
  storeName: string;
  quantity: number;
  subtotal: number;
  createdAt: string;
  updatedAt: string;
}