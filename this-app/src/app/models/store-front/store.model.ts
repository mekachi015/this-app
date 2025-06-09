export interface Store {
  id: string;
  name: string;
  imageUrl: string;
  description?: string; // Add this field
  bestseller?: string;
  category?: string;
  rating?: number;
  operatingHours?: string;
}
