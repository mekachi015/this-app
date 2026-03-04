import { DeliveryAddressDTO } from "./DeliveryAddressDTO";
import { DriverDTO } from "./DriverDTO";
import { OrderItemDTO } from "./OrderItemDTO";

export interface OrderDTO{
  id: any;
  orderId: number;
  orderStatus: string;
  totalAmount: number;
  shippingAmount: number;
  subtotal: number;
  orderDate: string;
  estimatedDeliveryDate?: string;
  actualDeliveryDate?: string;
  isAssignedDriver: boolean;
  storeId: number;
  storeName: string;
  storeAddress?: string;   // store's street address used for route origin
  deliveryAddress: DeliveryAddressDTO;
  driver?: DriverDTO;
  items: OrderItemDTO[];
  itemCount: number;
  createdAt: string;
  updatedAt: string;
  date: string;
}