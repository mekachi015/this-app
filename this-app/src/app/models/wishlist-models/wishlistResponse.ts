import { WishlistDto } from "./wishlistDTO";

export interface WishlistResponse {
  success: boolean;
  message?: string;
  data?: WishlistDto | WishlistDto[];
  itemCount?: number;
  count?: number;
  isInWishlist?: boolean;
}
