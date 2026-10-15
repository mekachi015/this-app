import { WishlistDto } from "./wishlistDTO";

export interface ProductWishlistItem extends WishlistDto {
  name: string;
  price: number;
  imageUrl: string;
  description: string;
}