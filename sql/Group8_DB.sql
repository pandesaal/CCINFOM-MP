CREATE DATABASE IF NOT EXISTS `s17_group8`;
USE `s17_group8`;

DROP TABLE IF EXISTS Inventory;
CREATE TABLE IF NOT EXISTS Inventory (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    category VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL CHECK (price > 0),
    quantity INT DEFAULT 0 CHECK (quantity >= 0),
    last_restock TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (category IN ('Main Course', 'Desserts', 'Beverages', 'Sides'))
);

INSERT INTO Inventory (product_name, category, price, quantity) 
VALUES 	('Pork Sinigang', 'Main Course', 250.00, 50),
		('Kare-kare', 'Main Course', 350.00, 50),
		('Chicken Adobo', 'Main Course', 220.00, 50),
		('Sisig', 'Main Course', 280.00, 50),
		('Burger Steak', 'Main Course', 190.00, 50),
		('Pancit Palabok', 'Main Course', 180.00, 50),
		('Pancit Bihon', 'Main Course', 170.00, 50),
		('Sinigang', 'Main Course', 240.00, 50),
		('Halo-halo', 'Desserts', 120.00, 50),
		('Leche Flan', 'Desserts', 90.00, 50),
		('Dulce de Leche Cake', 'Desserts', 150.00, 50),
		('Bibingka', 'Desserts', 100.00, 50),
		('Peach Mango Pie', 'Desserts', 110.00, 50),
		('Buko Pandan', 'Desserts', 130.00, 50),
		('Mango Shake', 'Beverages', 85.00, 50),
		('Bottomless Iced Tea', 'Beverages', 60.00, 50),
		('Buko Juice', 'Beverages', 70.00, 50),
		('Sago\'t Gulaman', 'Beverages', 50.00, 50),
		('Steamed Rice', 'Sides', 40.00, 50),
		('Mashed Potatoes', 'Sides', 70.00, 50),
		('Garlic Fried Rice', 'Sides', 60.00, 50),
		('Macaroni Salad', 'Sides', 50.00, 50);


DROP TABLE IF EXISTS Employee;
DROP TABLE IF EXISTS Roles;
DROP TABLE IF EXISTS TimeShift;

CREATE TABLE IF NOT EXISTS Roles (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS TimeShift (
    time_shiftid INT AUTO_INCREMENT PRIMARY KEY,
    shift_type VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS Employee (
    employee_id INT AUTO_INCREMENT PRIMARY KEY,
	first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    role_id INT NOT NULL,
    time_shiftid INT NOT NULL,
    FOREIGN KEY (role_id) REFERENCES Roles(role_id),
    FOREIGN KEY (time_shiftid) REFERENCES TimeShift(time_shiftid)
);

INSERT INTO Roles (role_name) VALUES
('Waiter'),
('Chef'),
('Cleaner'),
('Manager'),
('Cashier');

INSERT INTO TimeShift (shift_type) VALUES
('Morning'),
('Afternoon'),
('Night');

INSERT INTO Employee (first_name, last_name, role_id, time_shiftid) VALUES
('John', 'Wick', 1, 1),
('Sabrina', 'Carpenter', 2, 2),
('Spongebob', 'Squarepants', 1, 3),
('Bruno', 'Mars', 3, 1),
('Jennie', 'Kim', 2, 3),
('Ariana', 'Grande', 4, 2),
('Nicki', 'Minaj', 5, 1),
('Peter', 'Parker', 1, 2),
('Donald', 'Trump', 3, 3),
('Patrick', 'Star', 4, 1);

	
DROP TABLE IF EXISTS Orders;
DROP TABLE IF EXISTS OrderItem;
DROP TABLE IF EXISTS Assigned_Employee_to_Order;

CREATE TABLE IF NOT EXISTS Orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    total_amount DECIMAL(10, 2),
    order_type ENUM('Dine-In', 'Takeout', 'Delivery') NOT NULL, 
    order_status ENUM('In Progress', 'Ready', 'Served', 'Completed') NOT NULL,
    order_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES Customers(customer_id)
);

INSERT INTO Orders (customer_id, total_amount, order_type, order_status) 
-- di ko sure pano dapat dummy values ng customer_id kasi scope yun ni yonna
-- im using it as a foreign key kasi
VALUES 	(1, 495.00, 'Dine-In', 'In Progress'),
		(2, 570.00, 'Takeout', 'Ready'),
		(3, 480.00, 'Delivery', 'Served'),
		(4, 530.00, 'Dine-In', 'Completed'),
		(5, 425.00, 'Takeout', 'In Progress');

CREATE TABLE IF NOT EXISTS Order_Item (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
	product_id INT NOT NULL,
    quantity INT DEFAULT 0 CHECK (quantity > 0),
    price_per_unit DECIMAL(10, 2) NOT NULL CHECK (price_per_unit > 0),
    subtotal DECIMAL(10, 2) NOT NULL CHECK (subtotal > 0),
	FOREIGN KEY (order_id) REFERENCES Orders(order_id),
    FOREIGN KEY (product_id) REFERENCES Inventory(product_id)
);

-- Trigger to calculate subtotal before inserting an order item
-- activated when INSERT INTO is used
DELIMITER $$	-- delimiter allows you to change the character or string that signifies the end of a SQL statement
				-- so that MySQL does not end BEGIN...END blocks prematurely
CREATE TRIGGER calculate_subtotal_before_insert
BEFORE INSERT ON Order_Item
FOR EACH ROW
BEGIN
    SET NEW.subtotal = NEW.quantity * NEW.price_per_unit;
    -- NEW refers to the new row currently being inserted or updated
END$$

-- Trigger to calculate subtotal after updating an order item
-- activated when UPDATE is used
CREATE TRIGGER calculate_subtotal_after_update
AFTER UPDATE ON Order_Item
FOR EACH ROW
BEGIN
    IF OLD.quantity != NEW.quantity OR OLD.price_per_unit != NEW.price_per_unit THEN
        UPDATE Order_Item
        SET subtotal = NEW.quantity * NEW.price_per_unit
        WHERE order_item_id = NEW.order_item_id;
    END IF;
END$$

DELIMITER ;  -- Reset delimiter back to default


INSERT INTO Order_Item (order_id, product_id, quantity, price_per_unit)
VALUES 
    -- Order 1
    (1, 1, 1, 250.00),  -- Pork Sinigang: 1 * 250.00 = 250.00
    (1, 19, 1, 40.00),  -- Steamed Rice: 1 * 40.00 = 40.00
    (1, 9, 1, 120.00),  -- Halo-halo: 1 * 120.00 = 120.00
    (1, 15, 1, 85.00),  -- Mango Shake: 1 * 85.00 = 85.00
    
    -- Order 2
    (2, 2, 1, 350.00),  -- Kare-kare: 1 * 350.00 = 350.00
    (2, 20, 1, 70.00),  -- Mashed Potatoes: 1 * 70.00 = 70.00
    (2, 10, 1, 90.00),  -- Leche Flan: 1 * 90.00 = 90.00
    (2, 16, 1, 60.00),  -- Bottomless Iced Tea: 1 * 60.00 = 60.00
    
    -- Order 3
    (3, 3, 1, 220.00),  -- Chicken Adobo: 1 * 220.00 = 220.00
    (3, 21, 1, 60.00),  -- Garlic Fried Rice: 1 * 60.00 = 60.00
    (3, 11, 1, 150.00), -- Dulce de Leche Cake: 1 * 150.00 = 150.00
    (3, 18, 1, 50.00),  -- Sago't Gulaman: 1 * 50.00 = 50.00

    -- Order 4
    (4, 4, 1, 280.00),  -- Sisig: 1 * 280.00 = 280.00
    (4, 22, 1, 50.00),  -- Macaroni Salad: 1 * 50.00 = 50.00
    (4, 14, 1, 130.00), -- Buko Pandan: 1 * 130.00 = 130.00
    (4, 17, 1, 70.00),  -- Buko Juice: 1 * 70.00 = 70.00

    -- Order 5
    (5, 5, 1, 190.00),  -- Burger Steak: 1 * 190.00 = 190.00
    (5, 19, 1, 40.00),  -- Steamed Rice: 1 * 40.00 = 40.00
    (5, 13, 1, 110.00), -- Peach Mango Pie: 1 * 110.00 = 110.00
    (5, 15, 1, 85.00);  -- Mango Shake: 1 * 85.00 = 85.00


CREATE TABLE IF NOT EXISTS Assigned_Employee_to_Order (
    order_id INT NOT NULL,
    employee_id INT NOT NULL,
    PRIMARY KEY (order_id, employee_id),
    FOREIGN KEY (order_id) REFERENCES Orders(order_id),
    FOREIGN KEY (employee_id) REFERENCES Employee(employee_id)
);

INSERT INTO Assigned_Employee_to_Order (customer_id, total_amount, order_type, order_status) 
VALUES
-- Order 1: Dine-In, Shift: Afternoon
    (1, 2),  -- Sabrina Carpenter (Chef)
    (1, 8),  -- Peter Parker (Waiter)

-- Order 2: Takeout, Shift: Night
    (2, 5),  -- Jennie Kim (Chef)
    (2, 3),  -- Spongebob Squarepants (Waiter)

-- Order 3: Delivery, Shift: Morning
    (3, 7),  -- Nicki Minaj (Cashier)
    (3, 10), -- Patrick Star (Manager)

-- Order 4: Dine-In, Shift: Morning
    (4, 2),  -- Sabrina Carpenter (Chef)
    (4, 1),  -- John Wick (Waiter)
    (4, 4),  -- Bruno Mars (Cleaner)
    (4, 10), -- Patrick Star (Manager)

-- Order 5: Takeout, Shift: Afternoon
    (5, 2),  -- Sabrina Carpenter (Chef)
    (5, 8);  -- Peter Parker (Waiter)

