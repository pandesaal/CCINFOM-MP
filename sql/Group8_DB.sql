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

