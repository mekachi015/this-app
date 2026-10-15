export interface OrderItemDTO {
  orderItemId: number;
  productId: number;
  productName: string;
  productImage: string;
  productPrice: number;
  quantity: number;
  priceAtPurchase: number;
  itemTotal: number;
}