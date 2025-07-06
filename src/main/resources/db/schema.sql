CREATE TABLE address (
                         id LONG AUTO_INCREMENT PRIMARY KEY,
                         street VARCHAR(255) NOT NULL,
                         house_number VARCHAR(50) NOT NULL,
                         city VARCHAR(100) NOT NULL,
                         postal_code VARCHAR(20) NOT NULL
);

CREATE TABLE department (
                            id LONG AUTO_INCREMENT PRIMARY KEY,
                            code VARCHAR(50) NOT NULL UNIQUE,
                            name VARCHAR(255) NOT NULL
);

CREATE TABLE freelancer (
                            id LONG AUTO_INCREMENT PRIMARY KEY,
                            first_name VARCHAR(100) NOT NULL,
                            last_name VARCHAR(100) NOT NULL,
                            email VARCHAR(255) NOT NULL UNIQUE,
                            phone_number VARCHAR(50) NOT NULL UNIQUE,
                            address_id LONG NOT NULL,
                            business_name VARCHAR(255) NOT NULL,
                            business_id_no VARCHAR(50) NOT NULL,
                            bank_account VARCHAR(34) NOT NULL,
                            active BOOLEAN NOT NULL DEFAULT TRUE,
                            FOREIGN KEY(address_id) REFERENCES address(id)
);

CREATE TABLE employee (
                          id LONG AUTO_INCREMENT PRIMARY KEY,
                          first_name VARCHAR(100) NOT NULL,
                          last_name VARCHAR(100) NOT NULL,
                          email VARCHAR(255) NOT NULL UNIQUE,
                          phone_number VARCHAR(50) NOT NULL UNIQUE,
                          address_id LONG NOT NULL,
                          hire_date DATE NOT NULL,
                          termination_date DATE,
                          department_id LONG NOT NULL,
                          salary DECIMAL(15,2) NOT NULL,
                          FOREIGN KEY(address_id) REFERENCES address(id),
                          FOREIGN KEY(department_id) REFERENCES department(id)
);

CREATE TABLE invoice (
                         id LONG AUTO_INCREMENT PRIMARY KEY,
                         freelancer_id LONG NOT NULL,
                         invoice_date DATE NOT NULL,
                         due_date DATE NOT NULL,
                         paid BOOLEAN NOT NULL DEFAULT FALSE,
                         FOREIGN KEY(freelancer_id) REFERENCES freelancer(id)
);

CREATE TABLE service (
                         id LONG AUTO_INCREMENT PRIMARY KEY,
                         invoice_id LONG NOT NULL,
                         service_name VARCHAR(255) NOT NULL,
                         unit_fee DECIMAL(15,2) NOT NULL,
                         quantity INT NOT NULL,
                         FOREIGN KEY(invoice_id) REFERENCES invoice(id)
);

CREATE TABLE payment (
                         id LONG AUTO_INCREMENT PRIMARY KEY,
                         invoice_id LONG NOT NULL,
                         amount DECIMAL(15,2) NOT NULL,
                         paid_on TIMESTAMP NOT NULL,
                         transaction_id VARCHAR(100),
                         FOREIGN KEY(invoice_id) REFERENCES invoice(id)
);
