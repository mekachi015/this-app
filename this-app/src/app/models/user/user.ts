export interface User {
    id: string;
    firstname: string;
    lastname:string;
    email: string;
    password: string;
    username: string;
    token: string;
    userType: UserType | string; // CUSTOMER, DRIVER, ADMIN
    profilePhotoUrl?: string; // Add this property
    createdAt: string;
}

export enum UserType {
  CUSTOMER = 'CUSTOMER',
  DRIVER = 'DRIVER',
  ADMIN = 'ADMIN'
}