export interface User {
    id: string;
    firstname: string;
    lastname:string;
    email: string;
    password: string;
    username: string;
    token: string;
    userType: string; // CUSTOMER, DRIVER, ADMIN
    createdAt: string;
}