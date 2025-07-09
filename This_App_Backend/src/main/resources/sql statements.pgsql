-- =====================================================================
-- PostgreSQL Function for automatic 'updated_at' timestamp
-- This function is called by triggers to set the 'updated_at' column
-- =====================================================================
CREATE OR REPLACE FUNCTION update_timestamp_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- =====================================================================
-- Table Creation Statements (Ordered by Foreign Key Dependencies)
-- =====================================================================

-- 1. Users Table
-- Stores information for customers, admins, store owners, and potentially drivers.
CREATE TABLE Users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    phone_number VARCHAR(15),
    user_type VARCHAR(20) DEFAULT 'customer' NOT NULL, -- 'customer', 'admin', 'store_owner', 'driver'
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Store_Owners Table
-- Links specific users to their role as a store owner.
CREATE TABLE Store_Owners(
    owner_id SERIAL PRIMARY KEY,
    user_id INT UNIQUE NOT NULL, -- Foreign key to Users table
    -- Redundant fields (first_name, last_name, email, phone_number) are removed
    -- as they should be retrieved from the Users table via user_id.
    -- If you need specific owner details not in Users, add them here.
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

-- 3. Drivers Table
-- Stores information about delivery drivers.
CREATE TABLE Drivers (
    driver_id SERIAL PRIMARY KEY,
    user_id INT UNIQUE, -- Optional: Link to Users table if drivers are also system users
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone_number VARCHAR(15) NOT NULL,
    email VARCHAR(100) UNIQUE, -- Email can be unique for drivers
    /*current_location GEOMETRY(Point, 4326),  Using PostGIS for geospatial data */
    status VARCHAR(20) DEFAULT 'available', /* e.g., available, busy, offline, etc. */
    vehicle_type VARCHAR(50), /* e.g., car, bike, etc. */
    vehicle_license_plate VARCHAR(20) UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

-- 4. User_Addresses Table
-- Stores shipping and billing addresses for users.
CREATE TABLE User_Addresses(
    address_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL, -- Foreign key to Users table
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    address_line3 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(100) NOT NULL,
    address_type VARCHAR(20) DEFAULT 'shipping' NOT NULL, -- 'shipping', 'billing', 'both'
    is_default BOOLEAN DEFAULT FALSE, -- to mark the default address
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

-- 5. Stores Table
-- Stores information about individual stores.
CREATE TABLE Stores (
    store_id VARCHAR(100) PRIMARY KEY, -- Using VARCHAR for custom store IDs (e.g., UUIDs or short codes)
    owner_id INT NOT NULL, -- Foreign key to Store_Owners table
    store_name VARCHAR(100) NOT NULL,
    store_description TEXT,
    store_address VARCHAR(255) NOT NULL,
    store_phone_number VARCHAR(15),
    store_email VARCHAR(100) UNIQUE NOT NULL,
    store_business_hours VARCHAR(100), /* e.g., "Mon-Fri 9am-5pm" */
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_id) REFERENCES Store_Owners(owner_id)
);

-- 6. Products Table
-- Catalogue of products available in stores.
CREATE TABLE Products (
    product_id SERIAL PRIMARY KEY,
    store_id VARCHAR(100) NOT NULL, -- Foreign key to Stores table
    product_name VARCHAR(100) NOT NULL,
    product_description TEXT,
    product_price DECIMAL(10, 2) NOT NULL CHECK (product_price >= 0),
    stock_quantity INT DEFAULT 0 CHECK (stock_quantity >= 0),
    image_url VARCHAR(255),
    category VARCHAR(50),
    weight DECIMAL(10, 2), -- Added weight
    dimensions VARCHAR(100), -- Added dimensions
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (store_id) REFERENCES Stores(store_id)
);

-- 7. Payment_Methods Table
-- Stores user payment information.
CREATE TABLE Payment_Methods(
    method_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL, -- Foreign key for users table
    method_type VARCHAR(50) NOT NULL, -- e.g., 'credit_card', 'paypal', 'cash_on_delivery'
    -- For sensitive details, integrate with PCI-compliant payment gateways.
    -- These fields are for display/reference only (e.g., last 4 digits).
    card_last_four VARCHAR(4),
    card_brand VARCHAR(50),
    expiration_month INT,
    expiration_year INT,
    is_default BOOLEAN DEFAULT FALSE, -- to mark the default payment method
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    billing_address_id INT, -- Foreign key for user addresses table (optional)
    FOREIGN KEY (user_id) REFERENCES Users(user_id),
    FOREIGN KEY (billing_address_id) REFERENCES User_Addresses(address_id)
);

-- 8. Orders Table
-- Manages overall order details.
CREATE TABLE Orders(
    order_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL, -- Foreign key for users table
    store_id VARCHAR(100), -- Foreign key for stores table (can be NULL for multi-store carts)
    order_status VARCHAR(50) DEFAULT 'pending' NOT NULL, -- e.g., pending, processing, shipped, delivered, cancelled
    total_amount DECIMAL(10, 2) NOT NULL CHECK (total_amount >= 0),
    order_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    is_assigned_driver BOOLEAN DEFAULT FALSE, -- to check if a driver is assigned
    driver_id INT, -- Foreign key for drivers table, can be NULL if not assigned
    delivery_address_id INT NOT NULL, -- Foreign key for user addresses table
    estimated_delivery_date DATE,
    actual_delivery_date DATE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id),
    FOREIGN KEY (store_id) REFERENCES Stores(store_id),
    FOREIGN KEY (driver_id) REFERENCES Drivers(driver_id),
    FOREIGN KEY (delivery_address_id) REFERENCES User_Addresses(address_id)
);

-- 9. Order_Items Table (CRUCIAL NEW TABLE)
-- Details the individual products within each order.
CREATE TABLE Order_Items (
    order_item_id SERIAL PRIMARY KEY,
    order_id INT NOT NULL, -- Foreign key to Orders table
    product_id INT NOT NULL, -- Foreign key to Products table
    quantity INT NOT NULL CHECK (quantity > 0),
    price_at_purchase DECIMAL(10, 2) NOT NULL CHECK (price_at_purchase >= 0), -- Price when the order was placed
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES Orders(order_id),
    FOREIGN KEY (product_id) REFERENCES Products(product_id),
    UNIQUE (order_id, product_id) -- Ensures a product is only listed once per order
);

-- 10. Payments Table
-- Records user payment transactions.
CREATE TABLE Payments(
    payment_id SERIAL PRIMARY KEY,
    order_id INT NOT NULL, -- Foreign key for orders table
    payment_method_id INT NOT NULL, -- Foreign key to Payment_Methods
    payment_status VARCHAR(50) DEFAULT 'pending' NOT NULL, -- e.g., pending, completed, failed, refunded
    amount DECIMAL(10, 2) NOT NULL CHECK (amount >= 0),
    payment_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    transaction_id VARCHAR(255) UNIQUE, -- Unique ID from payment gateway
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES Orders(order_id),
    FOREIGN KEY (payment_method_id) REFERENCES Payment_Methods(method_id)
);

-- 11. Inventory_Transactions Table
-- Tracks stock changes for products in stores.
CREATE TABLE Inventory_Transactions(
    transaction_id SERIAL PRIMARY KEY,
    product_id INT NOT NULL, -- Foreign key for products table
    store_id VARCHAR(100) NOT NULL, -- Foreign key for stores table
    transaction_type VARCHAR(50) NOT NULL, -- e.g., 'stock_in', 'stock_out', 'adjustment', 'return'
    quantity_changed INT NOT NULL, -- Positive for 'in', negative for 'out'
    new_stock_level INT NOT NULL, -- The stock level after this transaction
    transaction_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    performed_by_user_id INT, -- Optional: User who performed the transaction (admin/store owner)
    comment_on_transaction TEXT,
    FOREIGN KEY (product_id) REFERENCES Products(product_id),
    FOREIGN KEY (store_id) REFERENCES Stores(store_id),
    FOREIGN KEY (performed_by_user_id) REFERENCES Users(user_id)
);

-- 12. Notifications Table
-- Stores notifications that go out to users.
CREATE TABLE Notifications(
    notification_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL, -- Foreign key for users table
    notification_type VARCHAR(50) NOT NULL, -- e.g., 'order_update', 'promotional', 'review_reminder'
    notification_message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE, -- to mark if the notification has been read
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

-- 13. Reviews_And_Ratings Table
-- Stores reviews and ratings for stores and products.
CREATE TABLE Reviews_And_Ratings(
    review_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL, -- Foreign key for users table
    product_id INT, -- Foreign key for products table (can be NULL if reviewing a store)
    store_id VARCHAR(100), -- Foreign key for stores table (can be NULL if reviewing a product)
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5), -- rating from 1 to 5
    review_text TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id),
    FOREIGN KEY (product_id) REFERENCES Products(product_id),
    FOREIGN KEY (store_id) REFERENCES Stores(store_id),
    -- A review must be either for a product OR a store
    CHECK (product_id IS NOT NULL OR store_id IS NOT NULL)
);

-- 14. Wishlists Table
-- Stores a user's favorite products or stores.
CREATE TABLE Wishlists(
    wishlist_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL, -- Foreign key for users table
    product_id INT, -- Foreign key for products table (can be NULL if wishing a store)
    store_id VARCHAR(100), -- Foreign key for stores table (can be NULL if wishing a product)
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id),
    FOREIGN KEY (product_id) REFERENCES Products(product_id),
    FOREIGN KEY (store_id) REFERENCES Stores(store_id),
    -- A wishlist item must be either for a product OR a store
    CHECK (product_id IS NOT NULL OR store_id IS NOT NULL),
    -- Ensure unique combination of user, product, and store in wishlist
    UNIQUE (user_id, product_id, store_id)
);

-- 15. Cart Table
-- Stores items currently in a user's shopping cart.
CREATE TABLE Cart(
    cart_item_id SERIAL PRIMARY KEY,
    user_id INT NOT NULL, -- Foreign key for users table
    product_id INT NOT NULL, -- Foreign key for products table
    store_id VARCHAR(100) NOT NULL, -- Foreign key for stores table
    quantity INT NOT NULL DEFAULT 1 CHECK (quantity > 0), -- quantity of the product in the cart
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id),
    FOREIGN KEY (product_id) REFERENCES Products(product_id),
    FOREIGN KEY (store_id) REFERENCES Stores(store_id),
    -- A user can only have one entry for a given product in their cart
    UNIQUE (user_id, product_id)
);


-- =====================================================================
-- Triggers for automatic 'updated_at' timestamp updates
-- =====================================================================

CREATE TRIGGER set_users_updated_at
BEFORE UPDATE ON Users
FOR EACH ROW
EXECUTE FUNCTION update_timestamp_column();

CREATE TRIGGER set_store_owners_updated_at
BEFORE UPDATE ON Store_Owners
FOR EACH ROW
EXECUTE FUNCTION update_timestamp_column();

CREATE TRIGGER set_drivers_updated_at
BEFORE UPDATE ON Drivers
FOR EACH ROW
EXECUTE FUNCTION update_timestamp_column();

CREATE TRIGGER set_products_updated_at
BEFORE UPDATE ON Products
FOR EACH ROW
EXECUTE FUNCTION update_timestamp_column();

CREATE TRIGGER set_stores_updated_at
BEFORE UPDATE ON Stores
FOR EACH ROW
EXECUTE FUNCTION update_timestamp_column();

CREATE TRIGGER set_user_addresses_updated_at
BEFORE UPDATE ON User_Addresses
FOR EACH ROW
EXECUTE FUNCTION update_timestamp_column();

CREATE TRIGGER set_orders_updated_at
BEFORE UPDATE ON Orders
FOR EACH ROW
EXECUTE FUNCTION update_timestamp_column();

-- No trigger for Order_Items as created_at is usually sufficient

-- No trigger for Payments as payment records are generally immutable after creation

CREATE TRIGGER set_payment_methods_updated_at
BEFORE UPDATE ON Payment_Methods
FOR EACH ROW
EXECUTE FUNCTION update_timestamp_column();

-- No trigger for Inventory_Transactions as transaction_date is usually sufficient

-- No trigger for Notifications as created_at is usually sufficient

CREATE TRIGGER set_reviews_and_ratings_updated_at
BEFORE UPDATE ON Reviews_And_Ratings
FOR EACH ROW
EXECUTE FUNCTION update_timestamp_column();

CREATE TRIGGER set_wishlists_updated_at
BEFORE UPDATE ON Wishlists
FOR EACH ROW
EXECUTE FUNCTION update_timestamp_column();

CREATE TRIGGER set_cart_updated_at
BEFORE UPDATE ON Cart
FOR EACH ROW
EXECUTE FUNCTION update_timestamp_column();
