import { OrderDTO } from "./OrderDTO";

export interface OrderResponse {
  success: boolean;
  message: string;
  data?: OrderDTO | OrderDTO[];
  count?: number;
}