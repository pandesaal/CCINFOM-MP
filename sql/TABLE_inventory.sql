CREATE DATABASE IF NOT EXISTS `s17_group8`;
USE `s17_group8`;

DROP TABLE IF EXISTS inventory;
CREATE TABLE IF NOT EXISTS inventory (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    category VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL CHECK (price > 0),
    quantity INT DEFAULT 0 CHECK (quantity >= 0),
    last_restock TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CHECK (category IN ('Main Course', 'Desserts', 'Beverages', 'Sides'))
);

INSERT INTO inventory (product_name, category, price, quantity) 
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
