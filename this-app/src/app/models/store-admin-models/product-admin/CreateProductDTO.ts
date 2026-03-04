// export interface CreateProductDTO {
//     productName: string;
//     productDescription?: string;
//     productPrice: number;
//     stockQuantity: number;
//     category?: string;
//     weight?: number;
//     dimensions?: string;
//     imageUrl?: string;
//     storeId: number;
// }

export interface CreateProductDTO {
  productName: string;
  productDescription: string;
  productPrice: number;
  stockQuantity: number;
  category: string;
  storeId: number;
  userId: number;
  imageUrl?: string;
}
