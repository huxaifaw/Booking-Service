# Booking Service

A Spring Boot REST API service for managing bookings, workers/cleaning professionals, and vehicles. This service provides functionality to create bookings, check availability, and manage workers and vehicles.

## 🚀 Features

- **Booking Management**: Create and manage bookings with time slots
- **Worker Management**: Manage workers with availability and working hours
- **Vehicle Management**: Manage vehicles assigned to workers
- **Availability Checking**: Check worker and time slot availability
- **RESTful APIs**: Complete REST API with Swagger documentation
- **Database Integration**: PostgreSQL database with JPA/Hibernate

## 🛠️ Technology Stack

- **Java 17**
- **Spring Boot 3.3.13**
- **Spring Data JPA**
- **PostgreSQL**
- **Maven**
- **Swagger**

## 🗄️ Database Setup

### 1. Install PostgreSQL
Make sure PostgreSQL is installed and running on your system or you can run the shell script below on docker:

```bash
docker run --name my-postgres -e POSTGRES_USER=root -e POSTGRES_PASSWORD=admin -e POSTGRES_DB=bookingdb -p 5432:5432 -d postgres:15
```

### 2. Create Database and Schema

Connect to PostgreSQL and run the following SQL commands:

```sql
-- Create database
CREATE DATABASE bookingdb;

-- Connect to the database
\c bookingdb;

-- Create schema
CREATE SCHEMA IF NOT EXISTS bookingschema;

-- Set default schema
SET search_path TO bookingschema;
```

### 3. Database Schema

The application will automatically create the following tables when you run it (due to `spring.jpa.hibernate.ddl-auto=update`):

- `vehicle` - Stores vehicle information
- `worker` - Stores worker information with vehicle assignments
- `booking` - Stores booking information
- `booking_detail` - Links bookings with assigned workers

## 📊 Sample Data Scripts

### Insert Sample Vehicles

```sql
-- Switch to the correct schema
SET search_path TO bookingschema;

-- Insert sample vehicles
INSERT INTO vehicle (name) VALUES
                               ('Toyota Corolla'),
                               ('Honda Civic'),
                               ('Ford Focus'),
                               ('Nissan Sentra'),
                               ('Hyundai Elantra');
```

### Insert Sample Workers

```sql
-- Workers for Vehicle 1 (Toyota Corolla)
INSERT INTO worker (name, available, working_hours, working_on_fridays, vehicle_id) VALUES
                                                                                        ('Alice Johnson', true, '08:00-22:00', false, 1),
                                                                                        ('Bob Smith', true, '08:00-22:00', true, 1),
                                                                                        ('Carol Davis', true, '08:00-22:00', false, 1),
                                                                                        ('David Wilson', true, '08:00-22:00', true, 1),
                                                                                        ('Emma Brown', true, '08:00-22:00', false, 1);

-- Workers for Vehicle 2 (Honda Civic)
INSERT INTO worker (name, available, working_hours, working_on_fridays, vehicle_id) VALUES
                                                                                        ('Frank Miller', true, '08:00-22:00', true, 2),
                                                                                        ('Grace Taylor', true, '08:00-22:00', false, 2),
                                                                                        ('Henry Anderson', true, '08:00-22:00', true, 2),
                                                                                        ('Ivy Thomas', true, '08:00-22:00', false, 2),
                                                                                        ('Jack Garcia', true, '08:00-22:00', true, 2);

-- Workers for Vehicle 3 (Ford Focus)
INSERT INTO worker (name, available, working_hours, working_on_fridays, vehicle_id) VALUES
                                                                                        ('Kate Martinez', true, '08:00-22:00', false, 3),
                                                                                        ('Liam Rodriguez', true, '08:00-22:00', true, 3),
                                                                                        ('Maya Lee', true, '08:00-22:00', false, 3),
                                                                                        ('Noah White', true, '08:00-22:00', true, 3),
                                                                                        ('Olivia Harris', true, '08:00-22:00', false, 3);

-- Workers for Vehicle 4 (Nissan Sentra)
INSERT INTO worker (name, available, working_hours, working_on_fridays, vehicle_id) VALUES
                                                                                        ('Paul Clark', true, '08:00-22:00', true, 4),
                                                                                        ('Quinn Lewis', true, '08:00-22:00', false, 4),
                                                                                        ('Rachel Walker', true, '08:00-22:00', true, 4),
                                                                                        ('Sam Hall', true, '08:00-22:00', false, 4),
                                                                                        ('Tina Allen', true, '08:00-22:00', true, 4);

-- Workers for Vehicle 5 (Hyundai Elantra)
INSERT INTO worker (name, available, working_hours, working_on_fridays, vehicle_id) VALUES
                                                                                        ('Uma Young', true, '08:00-22:00', false, 5),
                                                                                        ('Victor King', true, '08:00-22:00', true, 5),
                                                                                        ('Wendy Wright', true, '08:00-22:00', false, 5),
                                                                                        ('Xavier Lopez', true, '08:00-22:00', true, 5),
                                                                                        ('Yara Hill', true, '08:00-22:00', false, 5);
```

### Insert Sample Bookings


```sql

-- Insert sample bookings

INSERT INTO booking (start_time, end_time, duration, required_workers) VALUES 

('2024-01-15 09:00:00', '2024-01-15 12:00:00', 4, 2),

('2024-01-15 14:00:00', '2024-01-15 17:00:00', 2, 1),

('2024-01-16 10:00:00', '2024-01-16 15:00:00', 2, 3),

('2024-01-17 08:00:00', '2024-01-17 11:00:00', 4, 1);


-- Insert booking details (linking bookings with workers)

INSERT INTO booking_detail (booking_id, worker_id) VALUES 

(1, 1), (1, 3),  -- Booking 1 has workers 1 and 3

(2, 2),          -- Booking 2 has worker 2

(3, 1), (3, 5), (3, 7),  -- Booking 3 has workers 1, 5, and 7

(4, 6);          -- Booking 4 has worker 6

```

## ⚙️ Configuration

Please refer to [application.properties](src/main/resources/application.properties) for default configurations.

**Note**: Update the database credentials (`username` and `password`) according to your PostgreSQL setup.

## 🚀 Running the Application

### 1. Clone the repository
```bash
git clone https://github.com/huxaifaw/Booking-Service.git
cd booking-service
```

### 2. Update database configuration
Edit `src/main/resources/application.properties` and update the database credentials if needed.

### 3. Build the application
```bash
./mvnw clean install
```

### 4. Run the application
```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

## 📚 API Documentation

### Swagger UI
Once the application is running, you can access the interactive API documentation at:

**🔗 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**