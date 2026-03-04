import { WishlistDto } from "./wishlistDTO";

export interface StoreWishlistItem extends WishlistDto {
    id: string;
  name: string;
  imageUrl?: string;
  bestseller?: string;
  category?: string;
  rating?: number;
  operatingHours?: string;
  description?: string;
}