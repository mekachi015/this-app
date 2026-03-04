export interface Product {
    productId?: number;
    //storeName?: string;
    productName: string;
    productDescription?: string;
    productPrice: number;
    stockQuantity: number;
    imageUrl?: string;
    category?: string;
    weight?: number;
    dimensions?: string;
    createdAt?: Date;
    updatedAt?: Date,
    storeId?: number;
    userId?: number;
}