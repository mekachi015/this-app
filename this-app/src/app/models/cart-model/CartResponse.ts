import { CartDTO } from "./CartDTO";

export interface CartResponse {
    success: boolean;
  message?: string;
  data?: CartDTO | CartDTO[];
  itemCount?: number;
  total?: number;
  count?: number;
  orderCount: number;
  grandTotal?: number;
}