# e-Motion Backend API Documentation

## 📋 Table of Contents

- [Introduction](#introduction)
- [Technology Stack](#technology-stack)
- [System Requirements](#system-requirements)
- [Getting Started](#getting-started)
- [Database Configuration](#database-configuration)
- [API Documentation](#api-documentation)
  - [Authentication](#authentication-apiauth)
  - [Users](#users-apiusers)
  - [User Documents](#user-documents-apidocuments)
  - [Vehicles](#vehicle-api-apivehicles)
  - [Staff](#staff-api-apistaffs)
  - [Stations](#station-api-apistations)
  - [Rentals](#rental-api-apirentals)
  - [Reservations](#reservation-api-apireservations)
  - [Payments](#payment-api-apipayment)
  - [Vehicle Logs](#vehiclelog-api-apivehicle-logs)
  - [Rental Checklists](#rentalchecklist-api-apirental-checklists)
- [Error Handling](#error-response-format)
- [Developer Notes](#notes-for-frontend-developers)

---

## Introduction

**e-Motion** is a comprehensive electric vehicle rental management system designed to streamline the process of renting electric vehicles across multiple stations.

### Key Features

- 🔐 **JWT-based Authentication** - Secure user authentication with access and refresh tokens
- 👥 **Role-based Access Control** - Support for Admin, Staff, and User roles
- 🚗 **Vehicle Management** - Complete CRUD operations for electric vehicles
- 📍 **Multi-station Support** - Manage vehicles across different locations
- 💳 **VNPay Integration** - Seamless payment processing with Vietnam's leading payment gateway
- 📧 **Email Notifications** - Automated email alerts for reservations, rentals, and reminders
- 📊 **Comprehensive Tracking** - Monitor vehicle logs, rental checklists, and maintenance records

### User Roles

- **Admin**: Full system administration and management capabilities
- **Staff**: Station operations, vehicle management, and rental processing
- **User (Renter)**: Vehicle browsing, reservation, and rental services

---

## Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 17+ | Core programming language |
| Spring Boot | 3.5.5 | Application framework |
| Spring Security | 6.x | Authentication & authorization |
| Spring Data JPA | 3.x | Database ORM |
| MySQL | 8.0 | Primary database |
| Redis | 7.0 | Caching & session management |
| JWT | - | Token-based authentication |
| JavaMail | - | Email notifications |
| Springdoc OpenAPI | 2.x | API documentation (Swagger) |
| Maven | 3.9.11+ | Build & dependency management |
| VNPay API | 2.1.0 | Payment gateway integration |

---

## System Requirements

### Development Environment

- **JDK**: 17 or higher
- **Maven**: 3.9.11 or higher
- **MySQL**: 8.0 or higher (running on port `3306`)
- **Redis**: 7.0 or higher (running on port `6379`)
- **IDE**: IntelliJ IDEA, Eclipse, or VS Code (recommended)

### Runtime Environment

- **Memory**: Minimum 2GB RAM recommended
- **Disk Space**: 500MB for application and dependencies
- **Network**: Internet connection for VNPay integration and email services

---

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd e-Motion-be
```

### 2. Configure Application Properties

Edit `src/main/resources/application.properties`:

```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/e_motion
spring.datasource.username=your_username
spring.datasource.password=your_password

# JWT Configuration
security.jwt.secret-key=your_secret_key_here
security.jwt.expiration-time=86400000

# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password

# VNPay Configuration
vnpay.tmnCode=your_tmn_code
vnpay.hashSecret=your_hash_secret
vnpay.url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
vnpay.returnUrl=http://localhost:8080/api/payment/vnpay-return

# Redis Configuration
spring.redis.host=localhost
spring.redis.port=6379
```

### 3. Start MySQL Database

Ensure MySQL 8 is running on your local machine:

```bash
# Start MySQL service
# Windows: Start from Services or MySQL Workbench
# Linux/Mac: sudo systemctl start mysql
```

The application uses **Code First approach** (Hibernate Auto DDL), so database tables will be automatically created on first run.

### 4. Start Redis Server

Run Redis using Docker:

```bash
docker run -d --name e-motion-redis -p 6379:6379 redis:7
```

Or start Redis locally if installed:

```bash
# Windows: redis-server
# Linux/Mac: redis-server /path/to/redis.conf
```

### 5. Build the Application

```bash
mvn clean package
```

This will:
- Clean previous builds
- Compile source code
- Run tests
- Package as JAR file in `target/` directory

### 6. Run the Application

```bash
java -jar target/e-Motion-0.0.1-SNAPSHOT.jar
```

Or run directly with Maven:

```bash
mvn spring-boot:run
```

### 7. Access the Application

Once started, the application will be available at:

- **API Base URL**: `http://localhost:8080`
- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
- **API Docs (JSON)**: `http://localhost:8080/v3/api-docs`

---

## Database Configuration

### Database Schema

The application uses **Hibernate Auto DDL** (Code First approach). Database tables are automatically generated based on JPA entity classes.

**Database Name**: `e_motion`

### Initial Setup

1. Create the database:
```sql
CREATE DATABASE e_motion;
```

2. The application will automatically create all required tables on first startup.

### Redis Configuration

Redis is used for:
- Session management
- Refresh token storage
- Caching frequently accessed data

**Default Configuration**:
- Host: `localhost`
- Port: `6379`
- No password (development)

---

# API Documentation

This section provides comprehensive documentation for all API endpoints in the e-Motion system.

## General Information

### Base URL
```
http://localhost:8080/api
```

### Standard Response Format

All API responses follow this consistent structure:

```json
{
  "status": 200,
  "message": "Success message",
  "data": { }
}
```

**Response Fields**:
- `status` (integer): HTTP status code
- `message` (string): Human-readable message describing the result
- `data` (object/array): Response payload (can be null for some operations)

### Authentication

Most endpoints require JWT authentication. Include the access token in the request header:

```http
Authorization: Bearer <your_jwt_token>
```

### Date/Time Format

All date and time values use **ISO 8601** format:
```
yyyy-MM-ddTHH:mm:ss
```

Example: `2025-10-12T14:30:00`

---

## Authentication (`/api/auth`)

Base URL: `http://localhost:8080/api/auth`

### 1. Register a New User

**Endpoint:** `POST /api/auth/register`

**Description:** Creates a new user account and sends verification email.

**Authentication:** None required

**Request Body:**
```json
{
  "email": "user@example.com",
  "userPassword": "password123",
  "fullName": "John Doe",
  "phone": "0912345678"
}
```

**Success Response (201):**
```json
{
  "status": 201,
  "message": "User registered successfully. Please check your email for verification code.",
  "data": "user@example.com"
}
```

---

### 2. Login

**Endpoint:** `POST /api/auth/login`

**Description:** Authenticates user credentials and returns JWT access token. Refresh token is set in HTTP-only cookie.

**Authentication:** None required

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Success Response (200):**
```json
{
  "status": 200,
  "message": "User logged in successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 1678886400000
  }
}
```

**Response Headers:**
```http
Set-Cookie: refreshToken=<refresh_token>; HttpOnly; Path=/; Max-Age=604800
```

---

### 3. Refresh Token

**Endpoint:** `POST /api/auth/refresh`

**Description:** Issues a new access token and refresh token using the refresh token from cookie.

**Authentication:** Requires refresh token in cookie

**Request:** No body required (uses cookie)

**Success Response (200):**
```json
{
  "status": 200,
  "message": "Token refreshed successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 1678886400000
  }
}
```

**Response Headers:**
```http
Set-Cookie: refresh_token=<new_refresh_token>; HttpOnly; Secure; Path=/api/auth/refresh; Max-Age=604800
```

---

### 4. Logout

**Endpoint:** `POST /api/auth/logout`

**Description:** Logs out user, invalidates access token, and clears refresh token cookie.

**Authentication:** Requires JWT token

**Request Headers:**
```http
Authorization: Bearer <access_token>
```

**Success Response (200):**
```json
{
  "status": 200,
  "message": "User logout in successfully",
  "data": null
}
```

**Response Headers:**
```http
Set-Cookie: refreshToken=; HttpOnly; Path=/; Max-Age=0
```

---

### 5. Verify User Account

**Endpoint:** `POST /api/auth/verify`

**Description:** Verifies user's email address using the 6-digit verification code.

**Authentication:** None required

**Request Body:**
```json
{
  "email": "user@example.com",
  "verificationCode": "123456"
}
```

**Success Response (200):**
```json
{
  "status": 200,
  "message": "User verified successfully",
  "data": "user@example.com"
}
```

---

### 6. Resend Verification Code

**Endpoint:** `POST /api/auth/resend`

**Description:** Resends the verification code to user's email.

**Authentication:** None required

**Query Parameters:**
- `email` (string, required): User's email address

**Example Request:**
```http
POST /api/auth/resend?email=user@example.com
```

**Success Response (200):**
```json
{
  "status": 200,
  "message": "Verification code resent successfully",
  "data": "user@example.com"
}
```

---

### 7. Send Forgot Password Verification

**Endpoint:** `POST /api/auth/forgotPassword/sendVerify/{email}`

**Description:** Sends a password reset verification code to user's email.

**Authentication:** None required

**Path Parameter:**
- `email` (string): User's email address

**Example Request:**
```http
POST /api/auth/forgotPassword/sendVerify/user@example.com
```

**Success Response (200):**
```json
{
  "status": 200,
  "message": "Verification code sent successfully",
  "data": "user@example.com"
}
```

---

### 8. Verify Forgot Password Code

**Endpoint:** `POST /api/auth/forgotPassword/verify`

**Description:** Verifies the password reset code.

**Authentication:** None required

**Request Body:**
```json
{
  "email": "user@example.com",
  "verificationCode": "123456"
}
```

**Success Response (200):**
```json
{
  "status": 200,
  "message": "User verified successfully",
  "data": "user@example.com"
}
```

---

### 9. Update Password

**Endpoint:** `POST /api/auth/forgotPassword/update`

**Description:** Updates user's password after successful verification.

**Authentication:** None required

**Request Body:**
```json
{
  "email": "user@example.com",
  "newPassword": "newPassword456",
  "confirmNewPassword": "newPassword456",
  "forgotPasswordCode": "123456"
}
```

**Success Response (200):**
```json
{
  "status": 200,
  "message": "Password updated successfully",
  "data": "user@example.com"
}
```

---

## Users (`/api/users`)

Base URL: `http://localhost:8080/api/users`

### 1. Get Current User Details

**Endpoint:** `GET /api/users/me`

**Description:** Retrieves the profile details of the currently authenticated user.

**Authentication:** Requires JWT token

**Success Response (200):**
```json
{
  "status": 200,
  "message": "Get current user successfully",
  "data": {
    "fullName": "John Doe",
    "email": "user@example.com",
    "phone": "0912345678",
    "role": "USER",
    "userDocuments": [
      {
        "imgUrl": "http://example.com/image.jpg",
        "docType": "CCCD",
        "docNumber": "123456789012",
        "email": "user@example.com"
      }
    ]
  }
}
```

---

### 2. Get All Users

**Endpoint:** `GET /api/users`

**Description:** Retrieves a list of all registered users.

**Authentication:** Requires JWT token (ADMIN role)

**Success Response (200):**
```json
{
  "status": 200,
  "message": "Get all users successfully",
  "data": [
    {
      "fullName": "John Doe",
      "email": "user@example.com",
      "phone": "0912345678",
      "role": "USER",
      "userDocuments": []
    }
  ]
}
```

---

### 3. Get User by Email

**Endpoint:** `GET /api/users/{email}`

**Description:** Retrieves a specific user's details by email address.

**Authentication:** Requires JWT token

**Path Parameter:**
- `email` (string): The user's email address

**Example Request:**
```http
GET /api/users/user@example.com
```

**Success Response (200):**
```json
{
  "status": 200,
  "message": "Get user by email successfully",
  "data": {
    "fullName": "John Doe",
    "email": "user@example.com",
    "phone": "0912345678",
    "role": "USER",
    "userDocuments": []
  }
}
```

---

### 4. Delete User

**Endpoint:** `DELETE /api/users/delete/{email}`

**Description:** Deletes a user account by email address.

**Authentication:** Requires JWT token (ADMIN role)

**Path Parameter:**
- `email` (string): The user's email address

**Example Request:**
```http
DELETE /api/users/delete/user@example.com
```

**Success Response (200):**
```json
{
  "status": 200,
  "message": "Delete user by email successfully",
  "data": null
}
```

---

### 5. Change Password

**Endpoint:** `POST /api/users/me/change-password`

**Description:** Allows authenticated user to change their password.

**Authentication:** Requires JWT token

**Request Body:**
```json
{
  "oldPassword": "password123",
  "newPassword": "newPassword456",
  "confirmNewPassword": "newPassword456"
}
```

**Success Response (200):**
```json
{
  "status": 200,
  "message": "Change password successfully",
  "data": null
}
```

---

### 6. Update User Profile

**Endpoint:** `POST /api/users/me/update-profile`

**Description:** Updates the authenticated user's profile information.

**Authentication:** Requires JWT token

**Request Body:**
```json
{
  "fullName": "Johnathan Doe",
  "phone": "0987654321"
}
```

**Success Response (200):**
```json
{
  "status": 200,
  "message": "Update profile successfully",
  "data": {
    "fullName": "Johnathan Doe",
    "email": "user@example.com",
    "phone": "0987654321",
    "role": "USER",
    "userDocuments": []
  }
}
```

---

## User Documents (`/api/documents`)

Base URL: `http://localhost:8080/api/documents`

### 1. Create Document

**Endpoint:** `POST /api/documents`

**Description:** Creates a new document (ID card, driver's license, etc.) for a user.

**Authentication:** Requires JWT token

**Request Body:**
```json
{
  "imgUrl": "http://example.com/new_doc.jpg",
  "docType": "PASSPORT",
  "docNumber": "C1234567",
  "email": "user@example.com"
}
```

**Available Document Types:**
- `CCCD`: Citizen Identification Card
- `LICENSE`: Driver's License
- `PASSPORT`: Passport

**Success Response (200):**
```json
{
  "status": 200,
  "message": null,
  "data": {
    "imgUrl": "http://example.com/new_doc.jpg",
    "docType": "PASSPORT",
    "docNumber": "C1234567",
    "email": "user@example.com"
  }
}
```

---

### 2. Get All Documents

**Endpoint:** `GET /api/documents`

**Description:** Retrieves all documents for all users.

**Authentication:** Requires JWT token (ADMIN role)

**Success Response (200):**
```json
{
  "status": 200,
  "message": null,
  "data": [
    {
      "imgUrl": "http://example.com/image.jpg",
      "docType": "CCCD",
      "docNumber": "123456789012",
      "email": "user@example.com"
    }
  ]
}
```

---

### 3. Get Document by ID

**Endpoint:** `GET /api/documents/{docId}`

**Description:** Retrieves a specific document by its ID.

**Authentication:** Requires JWT token

**Path Parameter:**
- `docId` (long): Document ID

**Example Request:**
```http
GET /api/documents/1
```

**Success Response (200):**
```json
{
  "status": 200,
  "message": null,
  "data": {
    "imgUrl": "http://example.com/image.jpg",
    "docType": "CCCD",
    "docNumber": "123456789012",
    "email": "user@example.com"
  }
}
```

---

### 4. Get Documents by User ID

**Endpoint:** `GET /api/documents/user/{userId}`

**Description:** Retrieves all documents for a specific user.

**Authentication:** Requires JWT token

**Path Parameter:**
- `userId` (long): User ID

**Example Request:**
```http
GET /api/documents/user/5
```

**Success Response (200):**
```json
{
  "status": 200,
  "message": null,
  "data": [
    {
      "imgUrl": "http://example.com/image.jpg",
      "docType": "CCCD",
      "docNumber": "123456789012",
      "email": "user@example.com"
    }
  ]
}
```

---

### 5. Update Document

**Endpoint:** `PUT /api/documents/{docId}`

**Description:** Updates an existing document.

**Authentication:** Requires JWT token

**Path Parameter:**
- `docId` (long): Document ID

**Request Body:**
```json
{
  "imgUrl": "http://example.com/updated_image.jpg",
  "docType": "CCCD",
  "docNumber": "098765432109"
}
```

**Success Response (200):**
```json
{
  "status": 200,
  "message": null,
  "data": {
    "imgUrl": "http://example.com/updated_image.jpg",
    "docType": "CCCD",
    "docNumber": "098765432109",
    "email": "user@example.com"
  }
}
```

---

### 6. Delete Document

**Endpoint:** `DELETE /api/documents/{docId}`

**Description:** Deletes a document by its ID.

**Authentication:** Requires JWT token

**Path Parameter:**
- `docId` (long): Document ID

**Example Request:**
```http
DELETE /api/documents/1
```

**Success Response (200):**
```json
{
  "status": 200,
  "message": "Document deleted successfully",
  "data": null
}
```

---

## Vehicle API (`/api/vehicles`)

Base URL: `http://localhost:8080/api/vehicles`

### 1. Get All Vehicles

**Endpoint:** `GET /api/vehicles`

**Description:** Retrieves a list of all vehicles.

**Authentication:** Requires JWT token

**Success Response (200):**
```json
{
  "status": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "Tesla Model X",
      "type": "ELECTRIC",
      "status": "AVAILABLE",
      "category": "SUV",
      "seats": 5,
      "pricePerDay": 150.0,
      "consumptionRate": 20.0,
      "batteryCapacity": 100.0,
      "stationId": 2,
      "city": "Hanoi"
    }
  ]
}
```

---

### 2. Find Vehicle by ID

**Endpoint:** `GET /api/vehicles/id/{id}`

**Description:** Retrieves a vehicle by its ID.

**Authentication:** Requires JWT token

**Path Parameter:**
- `id` (Long): Vehicle ID

**Example Request:**
```http
GET /api/vehicles/id/1
```

**Success Response (200):**
```json
{
  "status": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "BYD M6 2024",
    "description": "BYD M6 2024 là mẫu MPV hiện đại dành cho khách hàng yêu thích sự thoải mái và công nghệ tiên tiến. Không gian rộng rãi 7 chỗ, cửa trượt điện tiện lợi và điều hòa tự động mang đến trải nghiệm như xe gia đình cao cấp. Xe vận hành êm ái, tiết kiệm nhiên liệu, phù hợp cho cả đi phố lẫn hành trình dài. Hệ thống giải trí thông minh, màn hình cảm ứng lớn và kết nối đa phương tiện giúp mọi chuyến đi thêm phần thú vị. Thiết kế mạnh mẽ, sang trọng phù hợp với khách hàng chú trọng cả tiện nghi lẫn hình ảnh.",
    "type": "CAR",
    "category": "MPV",
    "status": "AVAILABLE",
    "seats": 6,
    "pricePerHour": 54000.0,
    "pricePerDay": 1300000.0,
    "depositFee": 3000000.0,
    "consumptionRate": 6.3,
    "batteryLevel": 0.85,
    "batteryCapacity": 75.0,
    "plateNumber": "51H-2025",
    "lastMaintenance": "2025-09-20T10:00:00",
    "stationId": 1
  }
}
```

---

### 3. Find Vehicle by Plate Number

**Endpoint:** `GET /api/vehicles/plate/{plateNumber}`

**Description:** Retrieves a vehicle by its plate number.

**Authentication:** Requires JWT token

**Path Parameter:**
- `plateNumber` (string): Vehicle plate number

**Example Request:**
```http
GET /api/vehicles/plate/59A-77777
```

**Success Response (200):**
```json
{
  "status": 200,
  "message": "success",
  "data": {
    "id": 12,
    "name": "VINFAST VF8 2023",
    "description": "Khám phá đỉnh cao trải nghiệm tự lái với VinFast VF8 2024 – VF8 mang lại cảm giác êm ái, sang trọng và hiện đại với thiết kế tinh tế và công nghệ an toàn tiên tiến.",
    "type": "CAR",
    "category": "SUV",
    "status": "AVAILABLE",
    "seats": 5,
    "pricePerHour": 165000.0,
    "pricePerDay": 1300000.0,
    "depositFee": 3000000.0,
    "consumptionRate": 19.5,
    "batteryLevel": 1.0,
    "batteryCapacity": 82.0,
    "plateNumber": "59A-77777",
    "lastMaintenance": "2025-09-25T10:00:00",
    "stationId": 4
  }
}
```

---

### 4. Create a New Vehicle

**Endpoint:** `POST /api/vehicles`

**Description:** Adds a new vehicle to the system.

**Authentication:** Requires JWT token (ADMIN role)

**Request Body:**
```json
{
  "name": "VINFAST VF8 2023",
  "description": "Khám phá đỉnh cao trải nghiệm tự lái với VinFast VF8 2024 – VF8 mang lại cảm giác êm ái, sang trọng và hiện đại với thiết kế tinh tế và công nghệ an toàn tiên tiến.",
  "vehicleType": "CAR",
  "vehicleStatus": "AVAILABLE",
  "category": "SEDAN",
  "seats": 5,
  "pricePerHour": 165000,
  "pricePerDay": 1300000,
  "depositFee": 3000000,
  "consumptionRate": 19.5,
  "batteryLevel": 1.0,
  "batteryCapacity": 82.0,
  "plateNumber": "59A-77777",
  "lastMaintenance": "2025-09-25T10:00:00",
  "stationId": 4
}
```

**Success Response (201):**
```json
{
  "status": 201,
  "message": "Vehicle created successfully",
  "data": {
    "id": 13,
    "name": "VINFAST VF8 2023",
    "description": "Khám phá đỉnh cao trải nghiệm tự lái với VinFast VF8 2024 – VF8 mang lại cảm giác êm ái, sang trọng và hiện đại với thiết kế tinh tế và công nghệ an toàn tiên tiến.",
    "type": "CAR",
    "category": "SEDAN",
    "status": "AVAILABLE",
    "seats": 5,
    "pricePerHour": 165000,
    "pricePerDay": 1300000,
    "depositFee": 3000000,
    "consumptionRate": 19.5,
    "batteryLevel": 1.0,
    "batteryCapacity": 82.0,
    "plateNumber": "59A-77777",
    "lastMaintenance": "2025-09-25T10:00:00",
    "stationId": 4
  }
}
```

---

### 5. Update Vehicle by ID

**Endpoint:** `PUT /api/vehicles/{id}`

**Description:** Updates a vehicle's information by its ID.

**Authentication:** Requires JWT token (ADMIN role)

**Path Parameter:**
- `id` (Long): Vehicle ID

**Request Body:**
```json
{
  "name": "Updated Vehicle Name",
  "description": "Updated description",
  "pricePerDay": 1400000
}
```

**Success Response (200):**
```json
{
  "status": 200,
  "message": "Vehicle updated successfully",
  "data": null
}
```

---

### 6. Delete Vehicle by ID

**Endpoint:** `DELETE /api/vehicles/{id}`

**Description:** Deletes a vehicle by its ID.

**Authentication:** Requires JWT token (ADMIN role)

**Path Parameter:**
- `id` (Long): Vehicle ID

**Example Request:**
```http
DELETE /api/vehicles/5
```

**Success Response (200):**
```json
{
  "status": 200,
  "message": "Vehicle deleted successfully",
  "data": null
}
```

---

## Rental API (`/api/rentals`)

Base URL: `http://localhost:8080/api/rentals`

### 1. Get All Rentals

**Endpoint:** `GET /api/rentals`

**Description:** Retrieves a list of all rentals.

**Authentication:** Requires JWT token

**Success Response (200):**
```json
{
  "status": 200,
  "message": null,
  "data": [
    {
      "id": 1,
      "status": "ACTIVE",
      "startTime": "2025-10-15T10:00:00",
      "endTime": "2025-10-20T10:00:00",
      "rentFee": 6500000.0,
      "createdAt": "2025-10-12T09:30:00",
      "vehicleId": 5,
      "reservationId": 10,
      "userId": 3,
      "stationId": 2,
      "staffId": 7
    }
  ]
}
```

---

### 2. Create Rental from Reservation

**Endpoint:** `POST /api/rentals/reservation`

**Description:** Creates a new rental from an existing reservation.

**Authentication:** Requires JWT token (STAFF or ADMIN role)

**Request Body:**
```json
{
  "reservationCode": "123456",
  "staffId": 7
}
```

**Success Response (200):**
```json
{
  "status": 200,
  "message": null,
  "data": {
    "id": 15,
    "status": "PENDING",
    "startTime": "2025-10-15T10:00:00",
    "endTime": "2025-10-20T10:00:00",
    "rentFee": 6500000.0,
    "createdAt": "2025-10-12T10:30:00",
    "vehicleId": 5,
    "reservationId": 10,
    "userId": 3,
    "stationId": 2,
    "staffId": 7
  }
}
```

---

### 3. Create Rental (Direct)

**Endpoint:** `POST /api/rentals`

**Description:** Creates a new rental directly without a reservation.

**Authentication:** Requires JWT token (STAFF or ADMIN role)

**Request Body:**
```json
{
  "startTime": "2025-10-15T10:00:00",
  "endTime": "2025-10-20T10:00:00",
  "vehicleId": 5,
  "stationId": 2,
  "email": "user@example.com",
  "staffId": 7
}
```

**Success Response (200):**
```json
{
  "status": 200,
  "message": null,
  "data": {
    "id": 16,
    "status": "PENDING",
    "startTime": "2025-10-15T10:00:00",
    "endTime": "2025-10-20T10:00:00",
    "rentFee": 6500000.0,
    "createdAt": "2025-10-12T11:00:00",
    "vehicleId": 5,
    "reservationId": null,
    "userId": 3,
    "stationId": 2,
    "staffId": 7
  }
}
```

---

### 4. Get Rentals by Status

**Endpoint:** `GET /api/rentals/status/{status}`

**Description:** Retrieves rentals filtered by status.

**Authentication:** Requires JWT token

**Path Parameter:**
- `status` (string): Rental status (e.g., ACTIVE, COMPLETED)

**Example Request:**
```http
GET /api/rentals/status/ACTIVE
```

**Success Response (200):**
```json
{
  "status": 200,
  "message": null,
  "data": [
    {
      "id": 1,
      "status": "ACTIVE",
      "startTime": "2025-10-15T10:00:00",
      "endTime": "2025-10-20T10:00:00",
      "rentFee": 6500000.0,
      "createdAt": "2025-10-12T09:30:00",
      "vehicleId": 5,
      "reservationId": 10,
      "userId": 3,
      "stationId": 2,
      "staffId": 7
    }
  ]
}
```

---

### 5. Update Rental Status

**Endpoint:** `PATCH /api/rentals/status`

**Description:** Updates the status of a rental.

**Authentication:** Requires JWT token (STAFF or ADMIN role)

**Request Body:**
```json
{
  "id": 15,
  "status": "ACTIVE"
}
```

**Success Response (200):**
```json
{
  "status": 200,
  "message": null,
  "data": {
    "id": 15,
    "status": "ACTIVE",
    "startTime": "2025-10-15T10:00:00",
    "endTime": "2025-10-20T10:00:00",
    "rentFee": 6500000.0,
    "createdAt": "2025-10-12T10:30:00",
    "vehicleId": 5,
    "reservationId": 10,
    "userId": 3,
    "stationId": 2,
    "staffId": 7
  }
}
```

---

### 6. Get Rental Details by ID

**Endpoint:** `GET /api/rentals/{id}/details`

**Description:** Retrieves detailed information about a specific rental.

**Authentication:** Requires JWT token

**Path Parameter:**
- `id` (long): Rental ID

**Example Request:**
```http
GET /api/rentals/15/details
```

**Success Response (200):**
```json
{
  "status": 200,
  "message": null,
  "data": {
    "id": 15,
    "status": "ACTIVE",
    "startTime": "2025-10-15T10:00:00",
    "endTime": "2025-10-20T10:00:00",
    "rentFee": 6500000.0,
    "createdAt": "2025-10-12T10:30:00",
    "vehicleId": 5,
    "reservationId": 10,
    "userId": 3,
    "stationId": 2,
    "staffId": 7
  }
}
```

---

### 7. Get Rental Overview by ID

**Endpoint:** `GET /api/rentals/{id}/overview`

**Description:** Retrieves comprehensive overview of a rental including deposits, fees, and damages.

**Authentication:** Requires JWT token

**Path Parameter:**
- `id` (long): Rental ID

**Example Request:**
```http
GET /api/rentals/15/overview
```

**Success Response (200):**
```json
{
  "status": 200,
  "message": null,
  "data": {
    "rentalResponse": {
      "id": 15,
      "status": "ACTIVE",
      "startTime": "2025-10-15T10:00:00",
      "endTime": "2025-10-20T10:00:00",
      "rentFee": 6500000.0,
      "createdAt": "2025-10-12T10:30:00",
      "vehicleId": 5,
      "reservationId": 10,
      "userId": 3,
      "stationId": 2,
      "staffId": 7
    },
    "reservationDeposit": 3000000.0,
    "rentalDeposit": 3000000.0,
    "checkListFee": 500000.0,
    "vehicleDamages": {
      "Scratched bumper": 200000.0,
      "Broken mirror": 300000.0
    },
    "vehicleDamageFee": 500000.0,
    "refundEligible": false
  }
}
```

---

### 8. Process Check-In Payment

**Endpoint:** `POST /api/rentals/{id}/check-inpayment`

**Description:** Processes the check-in payment for a rental and generates VNPay payment URL.

**Authentication:** Requires JWT token

**Path Parameter:**
- `id` (long): Rental ID

**Example Request:**
```http
POST /api/rentals/15/check-inpayment
```

**Success Response:**
```json
{
  "status": 200,
  "message": null,
  "data": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=..."
}
```

---

### 9. Process Check-Out Payment

**Endpoint:** `POST /api/rentals/{id}/check-outpayment`

**Description:** Processes the check-out payment for a rental and determines if additional payment is required.

**Authentication:** Requires JWT token

**Path Parameter:**
- `id` (long): Rental ID

**Example Request:**
```http
POST /api/rentals/15/check-outpayment
```

**Success Response (Payment Required):**
```json
{
  "status": 200,
  "message": null,
  "data": {
    "processStatus": "PAYMENT_REQUIRED",
    "paymentUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=...",
    "rental": {
      "id": 15,
      "status": "COMPLETED",
      "startTime": "2025-10-15T10:00:00",
      "endTime": "2025-10-20T10:00:00",
      "rentFee": 6500000.0,
      "createdAt": "2025-10-12T10:30:00",
      "vehicleId": 5,
      "reservationId": 10,
      "userId": 3,
      "stationId": 2,
      "staffId": 7
    }
  }
}
```

**Success Response (Completed - No Additional Payment):**
```json
{
  "status": 200,
  "message": null,
  "data": {
    "processStatus": "COMPLETED",
    "paymentUrl": null,
    "rental": {
      "id": 15,
      "status": "COMPLETED",
      "startTime": "2025-10-15T10:00:00",
      "endTime": "2025-10-20T10:00:00",
      "rentFee": 6500000.0,
      "createdAt": "2025-10-12T10:30:00",
      "vehicleId": 5,
      "reservationId": 10,
      "userId": 3,
      "stationId": 2,
      "staffId": 7
    }
  }
}
```

---

## Reservation API (/api/reservations)

Base URL: `http://localhost:8080/api/reservations`

### 1. Create Reservation

**Endpoint:** `POST /api/reservations`

**Description:** Creates a new vehicle reservation and generates payment URL.

**Authentication:** Requires JWT token

**Request Body:**
```json
{
  "userEmail": "user@example.com",
  "vehicleId": 5,
  "stationId": 2,
  "startTime": "2025-10-15T10:00:00",
  "endTime": "2025-10-20T10:00:00"
}
```

**Success Response (201):**
```json
{
  "status": 201,
  "message": "Reservation created successfully",
  "data": {
    "vnpayUrl": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=...",
    "reservation": {
      "code": "123456",
      "status": "PENDING",
      "createdAt": "2025-10-12T12:00:00",
      "endTime": "2025-10-20T10:00:00",
      "overdueNotified": false,
      "expiringNotified": false,
      "userEmail": "user@example.com",
      "vehicleId": 5,
      "stationId": 2
    },
    "deposit": {
      "id": 10,
      "status": "PENDING",
      "amount": 500000.0,
      "createdAt": "2025-10-12T12:00:00",
      "reservationId": 15,
      "rentalId": null
    }
  }
}
```

---

### 2. Get All Reservations

**Endpoint:** `GET /api/reservations`

**Description:** Retrieves a list of all reservations.

**Authentication:** Requires JWT token (STAFF or ADMIN role)

**Success Response (200):**
```json
{
  "status": 200,
  "message": "Fetched all reservations successfully",
  "data": [
    {
      "code": "123456",
      "status": "CONFIRMED",
      "createdAt": "2025-10-12T12:00:00",
      "endTime": "2025-10-20T10:00:00",
      "overdueNotified": false,
      "expiringNotified": false,
      "userEmail": "user@example.com",
      "vehicleId": 5,
      "stationId": 2
    }
  ]
}
```

---

### 3. Get Reservation by Code

**Endpoint:** `GET /api/reservations/{code}`

**Description:** Retrieves a specific reservation by its 6-digit code.

**Authentication:** Requires JWT token

**Path Parameter:**
- `code` - 6-digit reservation code

**Success Response (200):**
```json
{
  "status": 200,
  "message": "Fetched reservation successfully",
  "data": {
    "code": "123456",
    "status": "CONFIRMED",
    "createdAt": "2025-10-12T12:00:00",
    "endTime": "2025-10-20T10:00:00",
    "overdueNotified": false,
    "expiringNotified": false,
    "userEmail": "user@example.com",
    "vehicleId": 5,
    "stationId": 2
  }
}
```

---

### 4. Update Reservation Status

**Endpoint:** `PATCH /api/reservations/update-status`

**Description:** Updates the status of a reservation.

**Authentication:** Requires JWT token (STAFF or ADMIN role)

**Request Body:**
```json
{
  "reservationCode": "123456",
  "newStatus": "CONFIRMED"
}
```

**Available Status Values:** `PENDING`, `CONFIRMED`, `CANCELLED`, `EXPIRED`, `COMPLETED`

**Success Response (200):**
```json
{
  "status": 200,
  "message": "Updated reservation status successfully",
  "data": {
    "code": "123456",
    "status": "CONFIRMED",
    "createdAt": "2025-10-12T12:00:00",
    "endTime": "2025-10-20T10:00:00",
    "overdueNotified": false,
    "expiringNotified": false,
    "userEmail": "user@example.com",
    "vehicleId": 5,
    "stationId": 2
  }
}
```

---

### 5. Delete Reservation

**Endpoint:** `DELETE /api/reservations/{code}`

**Description:** Deletes a reservation by its code.

**Authentication:** Requires JWT token (STAFF or ADMIN role)

**Path Parameter:**
- `code` - 6-digit reservation code

**Success Response (204):**
```json
{
  "status": 204,
  "message": "Deleted reservation successfully",
  "data": null
}
```

---

### 6. Get Reservations by Status

**Endpoint:** `GET /api/reservations/status/{status}`

**Description:** Retrieves reservations filtered by status.

**Authentication:** Requires JWT token

**Path Parameter:**
- `status` - Reservation status

**Available Status Values:** `PENDING`, `CONFIRMED`, `CANCELLED`, `EXPIRED`, `COMPLETED`

**Success Response (200):**
```json
{
  "status": 200,
  "message": "Fetched reservations by status successfully",
  "data": [
    {
      "code": "123456",
      "status": "CONFIRMED",
      "createdAt": "2025-10-12T12:00:00",
      "endTime": "2025-10-20T10:00:00",
      "overdueNotified": false,
      "expiringNotified": false,
      "userEmail": "user@example.com",
      "vehicleId": 5,
      "stationId": 2
    }
  ]
}
```

---

### 7. Get Reservations by User Email

**Endpoint:** `GET /api/reservations/email/{email}`

**Description:** Retrieves all reservations for a specific user.

**Authentication:** Requires JWT token

**Path Parameter:**
- `email` - User email address

**Success Response (200):**
```json
{
  "status": 200,
  "message": "Fetched reservations by user email successfully",
  "data": [
    {
      "code": "123456",
      "status": "CONFIRMED",
      "createdAt": "2025-10-12T12:00:00",
      "endTime": "2025-10-20T10:00:00",
      "overdueNotified": false,
      "expiringNotified": false,
      "userEmail": "user@example.com",
      "vehicleId": 5,
      "stationId": 2
    }
  ]
}
```

---

### 8. Get Reservations by Station Name

**Endpoint:** `GET /api/reservations/station/{stationName}`

**Description:** Retrieves all reservations for a specific station.

**Authentication:** Requires JWT token

**Path Parameter:**
- `stationName` - Station name

**Success Response (200):**
```json
{
  "status": 200,
  "message": "Fetched reservations by station name successfully",
  "data": [
    {
      "code": "123456",
      "status": "CONFIRMED",
      "createdAt": "2025-10-12T12:00:00",
      "endTime": "2025-10-20T10:00:00",
      "overdueNotified": false,
      "expiringNotified": false,
      "userEmail": "user@example.com",
      "vehicleId": 5,
      "stationId": 2
    }
  ]
}
```

---

### 9. Get Reservations by Vehicle ID

**Endpoint:** `GET /api/reservations/vehicle/{vehicleId}`

**Description:** Retrieves all reservations for a specific vehicle.

**Authentication:** Requires JWT token

**Path Parameter:**
- `vehicleId` - Vehicle ID

**Success Response (200):**
```json
{
  "status": 200,
  "message": "Fetched reservations by vehicle ID successfully",
  "data": [
    {
      "code": "123456",
      "status": "CONFIRMED",
      "createdAt": "2025-10-12T12:00:00",
      "endTime": "2025-10-20T10:00:00",
      "overdueNotified": false,
      "expiringNotified": false,
      "userEmail": "user@example.com",
      "vehicleId": 5,
      "stationId": 2
    }
  ]
}
```

---

### 10. Get Valid Reservations Before Time

**Endpoint:** `GET /api/reservations/time/{time}`

**Description:** Retrieves valid reservations before a specified time.

**Authentication:** Requires JWT token

**Path Parameter:**
- `time` - LocalDateTime in format `yyyy-MM-ddTHH:mm:ss`

**Success Response (200):**
```json
{
  "status": 200,
  "message": "Fetched reservations before specified time successfully",
  "data": [
    {
      "code": "123456",
      "status": "CONFIRMED",
      "createdAt": "2025-10-12T12:00:00",
      "endTime": "2025-10-20T10:00:00",
      "overdueNotified": false,
      "expiringNotified": false,
      "userEmail": "user@example.com",
      "vehicleId": 5,
      "stationId": 2
    }
  ]
}
```

---

### 11. Cancel Reservation

**Endpoint:** `POST /api/reservations/{code}/cancel`

**Description:** Cancels a reservation and processes refund if applicable.

**Authentication:** Requires JWT token

**Path Parameter:**
- `code` - 6-digit reservation code

**Success Response (200):**
```json
{
  "status": 200,
  "message": "Cancelled reservation successfully",
  "data": true
}
```

---

## Payment API (/api/payment)

Base URL: `http://localhost:8080/api/payment`

All responses are wrapped in:
```json
{
  "status": 200,
  "message": "success",
  "data": { ... }
}
```

### 1. Create VNPay Payment URL

Endpoint: POST /api/payment/vnpay

Description: Generate a VNPay payment URL for processing payments.

Authentication: Requires JWT token.

Request Body: http://localhost:8080/api/payment/vnpay

```json
{
  "amount": 3000000.0,
  "description": "Deposit for reservation 123456",
  "userEmail": "user@example.com",
  "type": "DEPOSIT",
  "depositId": 10,
  "rentalId": null
}
```

Available payment types: DEPOSIT, RENTAL, REFUND

Success Response:
```json
{
  "status": 201,
  "message": "Create VnPay Url Successfully",
  "data": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=300000000&vnp_BankCode=&vnp_Command=pay&vnp_CreateDate=20251012120000&vnp_CurrCode=VND&vnp_IpAddr=127.0.0.1&vnp_Locale=vn&vnp_OrderInfo=Deposit+for+reservation+123456&vnp_OrderType=other&vnp_ReturnUrl=http%3A%2F%2Flocalhost%3A8080%2Fapi%2Fpayment%2Fvnpay-return&vnp_TmnCode=ABCD1234&vnp_TxnRef=PAY1697097600000&vnp_Version=2.1.0&vnp_SecureHash=..."
}
```

### 2. VNPay Payment Return

Endpoint: GET /api/payment/vnpay-return

Description: Callback endpoint for VNPay to return payment results. Redirects to frontend.

Authentication: Not required (VNPay callback).

Query Parameters: VNPay payment response parameters

Success Redirect:
```
http://localhost:5173/booking/payment-result?status=success&txnRef=PAY1697097600000
```

Failed Redirect:
```
http://localhost:5173/booking/payment-result?status=failed
```

### 3. Create Payment Record

Endpoint: POST /api/payment

Description: Create a payment record manually (for internal use).

Authentication: Requires JWT token (STAFF or ADMIN role).

Request Body: http://localhost:8080/api/payment

```json
{
  "amount": 3000000,
  "description": "Manual payment for rental",
  "userEmail": "user@example.com",
  "paymentType": "RENTAL",
  "depositId": null,
  "rentalId": 15
}
```

Success Response:
```json
{
  "status": 201,
  "message": "Create VnPay Url Successfully",
  "data": {
    "paymentId": 25,
    "amount": 3000000.0,
    "method": "VNPAY",
    "type": "RENTAL",
    "status": "PENDING",
    "createdAt": "2025-10-12T12:30:00",
    "txnRef": "PAY1697098200000",
    "description": "Manual payment for rental",
    "responseCode": null,
    "transactionNo": null,
    "bankCode": null,
    "payDate": null,
    "userEmail": "user@example.com",
    "rentalResponse": {
      "id": 15,
      "status": "ACTIVE",
      "startTime": "2025-10-15T10:00:00",
      "endTime": "2025-10-20T10:00:00",
      "rentFee": 6500000.0,
      "createdAt": "2025-10-12T10:30:00",
      "vehicleId": 5,
      "reservationId": 10,
      "userId": 3,
      "stationId": 2,
      "staffId": 7
    },
    "depositResponse": null
  }
}
```

### 4. Get All Payments

Endpoint: GET /api/payment

Description: Retrieves a list of all payment records.

Authentication: Requires JWT token (STAFF or ADMIN role).

Path Parameter: http://localhost:8080/api/payment

Success Response:
```json
{
  "status": 201,
  "message": "Create VnPay Url Successfully",
  "data": [
    {
      "paymentId": 25,
      "amount": 3000000.0,
      "method": "VNPAY",
      "type": "DEPOSIT",
      "status": "SUCCESS",
      "createdAt": "2025-10-12T12:30:00",
      "txnRef": "PAY1697098200000",
      "description": "Deposit for reservation 123456",
      "responseCode": "00",
      "transactionNo": "14234567",
      "bankCode": "NCB",
      "payDate": "2025-10-12T12:35:00",
      "userEmail": "user@example.com",
      "rentalResponse": null,
      "depositResponse": {
        "id": 10,
        "status": "PAID",
        "amount": 3000000.0,
        "createdAt": "2025-10-12T12:00:00",
        "reservationId": 5,
        "rentalId": null
      }
    }
  ]
}
```

### 5. Delete Payment

Endpoint: DELETE /api/payment/delete/{id}

Description: Deletes a payment record by its ID.

Authentication: Requires JWT token (ADMIN role).

Path Parameter: http://localhost:8080/api/payment/delete/25

Success Response:
```json
{
  "status": 200,
  "message": "Delete Payment By 25 Successfully",
  "data": null
}
```

### 6. Update Payment

Endpoint: PUT /api/payment/update/{id}

Description: Updates a payment record.

Authentication: Requires JWT token (STAFF or ADMIN role).

Request Body: http://localhost:8080/api/payment/update/25

```json
{
  "method": "VNPAY",
  "status": "SUCCESS",
  "type": "DEPOSIT",
  "amount": 3000000,
  "description": "Updated deposit payment",
  "userEmail": "user@example.com",
  "depositId": 10,
  "rentalId": null
}
```

Success Response:
```json
{
  "status": 200,
  "message": "Update Payment Successfully",
  "data": {
    "paymentId": 25,
    "amount": 3000000.0,
    "method": "VNPAY",
    "type": "DEPOSIT",
    "status": "SUCCESS",
    "createdAt": "2025-10-12T12:30:00",
    "txnRef": "PAY1697098200000",
    "description": "Updated deposit payment",
    "responseCode": "00",
    "transactionNo": "14234567",
    "bankCode": "NCB",
    "payDate": "2025-10-12T12:35:00",
    "userEmail": "user@example.com",
    "rentalResponse": null,
    "depositResponse": {
      "id": 10,
      "status": "PAID",
      "amount": 3000000.0,
      "createdAt": "2025-10-12T12:00:00",
      "reservationId": 5,
      "rentalId": null
    }
  }
}
```

### 7. Get Payment by ID

Endpoint: GET /api/payment/{id}

Description: Retrieves a specific payment by its ID.

Authentication: Requires JWT token.

Path Parameter: http://localhost:8080/api/payment/25

Success Response:
```json
{
  "status": 200,
  "message": "Get Payment By 25 Successfully",
  "data": {
    "paymentId": 25,
    "amount": 3000000.0,
    "method": "VNPAY",
    "type": "DEPOSIT",
    "status": "SUCCESS",
    "createdAt": "2025-10-12T12:30:00",
    "txnRef": "PAY1697098200000",
    "description": "Deposit for reservation 123456",
    "responseCode": "00",
    "transactionNo": "14234567",
    "bankCode": "NCB",
    "payDate": "2025-10-12T12:35:00",
    "userEmail": "user@example.com",
    "rentalResponse": null,
    "depositResponse": {
      "id": 10,
      "status": "PAID",
      "amount": 3000000.0,
      "createdAt": "2025-10-12T12:00:00",
      "reservationId": 5,
      "rentalId": null
    }
  }
}
```

### 8. Get Payment by Transaction Reference

Endpoint: GET /api/payment/vnpay/{txnRef}

Description: Retrieves a payment by its VNPay transaction reference.

Authentication: Requires JWT token.

Path Parameter: http://localhost:8080/api/payment/vnpay/PAY1697098200000

Success Response:
```json
{
  "status": 200,
  "message": "Get Payment By PAY1697098200000 Successfully",
  "data": {
    "paymentId": 25,
    "amount": 3000000.0,
    "method": "VNPAY",
    "type": "DEPOSIT",
    "status": "SUCCESS",
    "createdAt": "2025-10-12T12:30:00",
    "txnRef": "PAY1697098200000",
    "description": "Deposit for reservation 123456",
    "responseCode": "00",
    "transactionNo": "14234567",
    "bankCode": "NCB",
    "payDate": "2025-10-12T12:35:00",
    "userEmail": "user@example.com",
    "rentalResponse": null,
    "depositResponse": {
      "id": 10,
      "status": "PAID",
      "amount": 3000000.0,
      "createdAt": "2025-10-12T12:00:00",
      "reservationId": 5,
      "rentalId": null
    }
  }
}
```

### 9. Get Payments by Status

Endpoint: GET /api/payment/status/{status}

Description: Retrieves payments filtered by status.

Authentication: Requires JWT token.

Path Parameter: http://localhost:8080/api/payment/status/SUCCESS

Available status values: PENDING, SUCCESS, FAILED, REFUNDED

Success Response:
```json
{
  "status": 200,
  "message": "Get Payment By SUCCESS Successfully",
  "data": [
    {
      "paymentId": 25,
      "amount": 3000000.0,
      "method": "VNPAY",
      "type": "DEPOSIT",
      "status": "SUCCESS",
      "createdAt": "2025-10-12T12:30:00",
      "txnRef": "PAY1697097600000",
      "description": "Deposit for reservation 123456",
      "responseCode": "00",
      "transactionNo": "14234567",
      "bankCode": "NCB",
      "payDate": "2025-10-12T12:35:00",
      "userEmail": "user@example.com",
      "rentalResponse": null,
      "depositResponse": {
        "id": 10,
        "status": "PAID",
        "amount": 3000000.0,
        "createdAt": "2025-10-12T12:00:00",
        "reservationId": 5,
        "rentalId": null
      }
    }
  ]
}
```

### 10. Get Payments by Method

Endpoint: GET /api/payment/method/{method}

Description: Retrieves payments filtered by payment method.

Authentication: Requires JWT token.

Path Parameter: http://localhost:8080/api/payment/method/VNPAY

Available methods: VNPAY, CASH

Success Response:
```json
{
  "status": 200,
  "message": "Get Payment By VNPAY Successfully",
  "data": [
    {
      "paymentId": 25,
      "amount": 3000000.0,
      "method": "VNPAY",
      "type": "DEPOSIT",
      "status": "SUCCESS",
      "createdAt": "2025-10-12T12:30:00",
      "txnRef": "PAY1697098200000",
      "description": "Deposit for reservation 123456",
      "responseCode": "00",
      "transactionNo": "14234567",
      "bankCode": "NCB",
      "payDate": "2025-10-12T12:35:00",
      "userEmail": "user@example.com",
      "rentalResponse": null,
      "depositResponse": {
        "id": 10,
        "status": "PAID",
        "amount": 3000000.0,
        "createdAt": "2025-10-12T12:00:00",
        "reservationId": 5,
        "rentalId": null
      }
    }
  ]
}
```

### 11. Get Payments by Type

Endpoint: GET /api/payment/type/{type}

Description: Retrieves payments filtered by payment type.

Authentication: Requires JWT token.

Path Parameter: http://localhost:8080/api/payment/type/DEPOSIT

Available types: DEPOSIT, RENTAL, REFUND

Success Response:
```json
{
  "status": 200,
  "message": "Get Payment By DEPOSIT Successfully",
  "data": [
    {
      "paymentId": 25,
      "amount": 3000000.0,
      "method": "VNPAY",
      "type": "DEPOSIT",
      "status": "SUCCESS",
      "createdAt": "2025-10-12T12:30:00",
      "txnRef": "PAY1697098200000",
      "description": "Deposit for reservation 123456",
      "responseCode": "00",
      "transactionNo": "14234567",
      "bankCode": "NCB",
      "payDate": "2025-10-12T12:35:00",
      "userEmail": "user@example.com",
      "rentalResponse": null,
      "depositResponse": {
        "id": 10,
        "status": "PAID",
        "amount": 3000000.0,
        "createdAt": "2025-10-12T12:00:00",
        "reservationId": 5,
        "rentalId": null
      }
    }
  ]
}
```

### 12. Query VNPay Transaction

Endpoint: POST /api/payment/query/{txnRef}

Description: Queries VNPay for transaction details and status.

Authentication: Requires JWT token.

Path Parameter: http://localhost:8080/api/payment/query/PAY1697098200000

Success Response:
```json
{
  "status": 200,
  "message": "Query Transaction Successfully",
  "data": {
    "responseId": "19685ef160274c268769e37ad9f44c0b",
    "command": "querydr",
    "tmnCode": "ABCD1234",
    "txnRef": "PAY1697098200000",
    "amount": "300000000",
    "orderInfo": "Deposit for reservation 123456",
    "responseCode": "00",
    "message": "Giao dịch thành công",
    "bankCode": "NCB",
    "payDate": "20251012123500",
    "transactionNo": "14234567",
    "transactionType": "01",
    "transactionStatus": "00",
    "secureHash": "..."
  }
}
```

---

## VehicleLog API (/api/vehicle-logs)

Base URL: `http://localhost:8080/api/vehicle-logs`

All responses are wrapped in:

```json
{
  "status": 200,
  "message": "success",
  "data": { ... }
}
```

### 1. Get All Vehicle Logs

**Endpoint:** `GET /api/vehicle-logs`

**Description:** Retrieves a list of all vehicle maintenance and service logs.

**Authentication:** Requires JWT token (STAFF or ADMIN role).

Success Response:
```json
{
  "status": 200,
  "message": "Get all vehicle logs successfully",
  "data": [
    {
      "id": 1,
      "logType": "MAINTENANCE",
      "description": "Regular oil change and tire rotation",
      "logDate": "2025-10-10T14:00:00",
      "cost": 500000.0,
      "performedBy": "Mechanic John",
      "vehicleId": 5,
      "createdAt": "2025-10-10T14:00:00"
    }
  ]
}
```

---

### 2. Create Vehicle Log

**Endpoint:** `POST /api/vehicle-logs`

**Description:** Creates a new vehicle log entry.

**Authentication:** Requires JWT token (STAFF or ADMIN role).

Request Body:
```json
{
  "logType": "MAINTENANCE",
  "description": "Brake pad replacement",
  "logDate": "2025-10-12T10:00:00",
  "cost": 800000.0,
  "performedBy": "Mechanic Smith",
  "vehicleId": 5
}
```

Success Response:
```json
{
  "status": 200,
  "message": "Create vehicle log successfully",
  "data": {
    "id": 15,
    "logType": "MAINTENANCE",
    "description": "Brake pad replacement",
    "logDate": "2025-10-12T10:00:00",
    "cost": 800000.0,
    "performedBy": "Mechanic Smith",
    "vehicleId": 5,
    "createdAt": "2025-10-12T10:00:00"
  }
}
```

---

### 3. Get Vehicle Log by ID

**Endpoint:** `GET /api/vehicle-logs/{id}`

**Description:** Retrieves a specific vehicle log by its ID.

**Authentication:** Requires JWT token.

Success Response:
```json
{
  "status": 200,
  "message": "Get vehicle log successfully",
  "data": {
    "id": 15,
    "logType": "MAINTENANCE",
    "description": "Brake pad replacement",
    "logDate": "2025-10-12T10:00:00",
    "cost": 800000.0,
    "performedBy": "Mechanic Smith",
    "vehicleId": 5,
    "createdAt": "2025-10-12T10:00:00"
  }
}
```

---

### 4. Update Vehicle Log

**Endpoint:** `PUT /api/vehicle-logs/{id}`

**Description:** Updates an existing vehicle log.

**Authentication:** Requires JWT token (STAFF or ADMIN role).

Request Body:
```json
{
  "logType": "MAINTENANCE",
  "description": "Brake pad replacement and alignment",
  "logDate": "2025-10-12T10:00:00",
  "cost": 1000000.0,
  "performedBy": "Mechanic Smith",
  "vehicleId": 5
}
```

Success Response:
```json
{
  "status": 200,
  "message": "Update vehicle log successfully",
  "data": {
    "id": 15,
    "logType": "MAINTENANCE",
    "description": "Brake pad replacement and alignment",
    "logDate": "2025-10-12T10:00:00",
    "cost": 1000000.0,
    "performedBy": "Mechanic Smith",
    "vehicleId": 5,
    "createdAt": "2025-10-12T10:00:00"
  }
}
```

---

### 5. Delete Vehicle Log

**Endpoint:** `DELETE /api/vehicle-logs/{id}`

**Description:** Deletes a vehicle log by its ID.

**Authentication:** Requires JWT token (ADMIN role).

Success Response:
```json
{
  "status": 200,
  "message": "Delete vehicle log successfully",
  "data": null
}
```

---

### 6. Get Vehicle Logs by Vehicle ID

**Endpoint:** `GET /api/vehicle-logs/vehicle/{vehicleId}`

**Description:** Retrieves all logs for a specific vehicle.

**Authentication:** Requires JWT token

**Path Parameter:**
- `vehicleId` (long): Vehicle ID

**Example Request:**
```http
GET /api/vehicle-logs/vehicle/5
```

**Success Response:**
```json
{
  "status": 200,
  "message": "Get vehicle logs by vehicle ID successfully",
  "data": [
    {
      "id": 1,
      "logType": "MAINTENANCE",
      "description": "Regular oil change and tire rotation",
      "logDate": "2025-10-10T14:00:00",
      "cost": 500000.0,
      "performedBy": "Mechanic John",
      "vehicleId": 5,
      "createdAt": "2025-10-10T14:00:00"
    },
    {
      "id": 15,
      "logType": "MAINTENANCE",
      "description": "Brake pad replacement and alignment",
      "logDate": "2025-10-12T10:00:00",
      "cost": 1000000.0,
      "performedBy": "Mechanic Smith",
      "vehicleId": 5,
      "createdAt": "2025-10-12T10:00:00"
    }
  ]
}
```

---

## Staff API (/api/staffs)

Base URL: `http://localhost:8080/api/staffs`

All responses are wrapped in:
```json
{
  "status": 200,
  "message": "success",
  "data": { ... }
}
```

### 1. Create Staff

**Endpoint:** `POST /api/staffs`

**Description:** Create a new staff member and assign them to a station.

**Authentication:** Requires JWT token (ADMIN role).

**Request Body:**
```json
{
  "email": "nguyen1112894@gmail.com",
  "stationName": "E-Motion Station Cầu Giấy"
}
```

**Success Response:**
```json
{
  "status": 200,
  "message": "Create staff successfully",
  "data": {
    "email": "nguyen1112894@gmail.com",
    "stationName": "E-Motion Station Cầu Giấy",
    "fullName": "Nguyen"
  }
}
```

---

### 2. Find Staff by Email

**Endpoint:** `GET /api/staffs/{email}`

**Description:** Retrieve staff information by email address.

**Authentication:** Requires JWT token (ADMIN role).

**Path Parameter:**
- `email` (string): Staff email address

**Example:** `GET http://localhost:8080/api/staffs/nguyen1112894@gmail.com`

**Success Response:**
```json
{
  "status": 200,
  "message": "Get staff by user email successfully",
  "data": {
    "email": "nguyen1112894@gmail.com",
    "stationName": "E-Motion Station Cầu Giấy",
    "fullName": "Nguyen"
  }
}
```

---

### 3. Find All Staffs

**Endpoint:** `GET /api/staffs`

**Description:** Retrieve a list of all staff members.

**Authentication:** Requires JWT token (ADMIN role).

**Example:** `GET http://localhost:8080/api/staffs`

**Success Response:**
```json
{
  "status": 200,
  "message": "Get all staffs successfully",
  "data": [
    {
      "email": "trungbeat7749@gmail.com",
      "stationName": "E-Motion Station Hoàn Kiếm",
      "fullName": "Hồ Thơm"
    },
    {
      "email": "khangngoc3082005@gmail.com",
      "stationName": "E-Motion Station Hoàn Kiếm",
      "fullName": "Khang"
    },
    {
      "email": "voquangtrungyb@gmail.com",
      "stationName": "E-Motion Station Cầu Giấy",
      "fullName": "vua Quang Trung"
    }
  ]
}
```

---

### 4. Delete Staff by Email

**Endpoint:** `DELETE /api/staffs/{email}`

**Description:** Remove a staff member from the system.

**Authentication:** Requires JWT token (ADMIN role).

**Path Parameter:**
- `email` (string): Staff email address

**Example:** `DELETE http://localhost:8080/api/staffs/nguyen1112894@gmail.com`

**Success Response:**
```json
{
  "status": 200,
  "message": "Delete staff by email successfully",
  "data": null
}
```

---

### 5. Update Staff by Email

**Endpoint:** `PUT /api/staffs/{email}`

**Description:** Update staff information (primarily station assignment).

**Authentication:** Requires JWT token (ADMIN role).

**Path Parameter:**
- `email` (string): Staff email address

**Request Body:**
```json
{
  "email": "voquangtrungtiktok@gmail.com",
  "oldStationName": "E-Motion Station Cầu Giấy",
  "newStationName": "E-Motion Station Thủ Đức"
}
```

**Example:** `PUT http://localhost:8080/api/staffs/voquangtrungtiktok@gmail.com`

**Success Response:**
```json
{
  "status": 200,
  "message": "Update staff by id successfully",
  "data": {
    "email": "voquangtrungtiktok@gmail.com",
    "stationName": "E-Motion Station Thủ Đức",
    "fullName": "Võ Quang Trung"
  }
}
```

---

## Station API (/api/stations)

Base URL: `http://localhost:8080/api/stations`

All responses are wrapped in:
```json
{
  "status": 200,
  "message": "success",
  "data": { ... }
}
```

### 1. Create Station

**Endpoint:** `POST /api/stations`

**Description:** Create a new station location.

**Authentication:** Requires JWT token (ADMIN role).

**Request Body:**
```json
{
  "name": "E-Motion Station Example6",
  "address": "123 Example Street",
  "city": "Hà Nội",
  "status": "ACTIVE",
  "latitude": 10.762622,
  "longitude": 106.660172
}
```

**Success Response:**
```json
{
  "status": 200,
  "message": "Create station successfully",
  "data": {
    "name": "E-Motion Station Example6",
    "address": "123 Example Street",
    "city": "HANOI",
    "latitude": 10.762622,
    "longitude": 106.660172,
    "status": "ACTIVE"
  }
}
```

**Notes:**
- `city` accepts values: "Hà Nội" or "TP HCM" (will be converted to enum: HANOI, TP_HCM)
- `status` can be: ACTIVE, INACTIVE, MAINTENANCE

---

### 2. Find Station by Name

**Endpoint:** `GET /api/stations/name/{name}`

**Description:** Retrieve station information by name.

**Authentication:** Requires JWT token.

**Path Parameter:**
- `name` (string): Station name

**Example:** `GET http://localhost:8080/api/stations/name/E-Motion Station Example6`

**Success Response:**
```json
{
  "status": 200,
  "message": "Get station by name successfully",
  "data": {
    "name": "E-Motion Station Example6",
    "address": "123 Example Street",
    "city": "HANOI",
    "latitude": 10.762622,
    "longitude": 106.660172,
    "status": "ACTIVE"
  }
}
```

---

### 3. Find Station by Address

**Endpoint:** `GET /api/stations/address/{address}`

**Description:** Retrieve stations matching a specific address.

**Authentication:** Requires JWT token.

**Path Parameter:**
- `address` (string): Station address

**Example:** `GET http://localhost:8080/api/stations/address/123 Example Street`

**Success Response:**
```json
{
  "status": 200,
  "message": "Get stations by address successfully",
  "data": [
    {
      "name": "E-Motion Station Example6",
      "address": "123 Example Street",
      "city": "HANOI",
      "latitude": 10.762622,
      "longitude": 106.660172,
      "status": "ACTIVE"
    }
  ]
}
```

---

### 4. Find Station by City

**Endpoint:** `GET /api/stations/city/{city}`

**Description:** Retrieve all stations in a specific city.

**Authentication:** Requires JWT token.

**Path Parameter:**
- `city` (string): City name (HANOI or TP_HCM)

**Example:** `GET http://localhost:8080/api/stations/city/HANOI`

**Success Response:**
```json
{
  "status": 200,
  "message": "Get stations by city successfully",
  "data": [
    {
      "name": "E-Motion Station Hoàn Kiếm",
      "address": "66 Tràng Tiền, Hoàn Kiếm",
      "city": "HANOI",
      "latitude": 21.02552,
      "longitude": 105.85335,
      "status": "ACTIVE"
    },
    {
      "name": "E-Motion Station Cầu Giấy",
      "address": "69 P. Vũ Phạm Hàm, Trung Hoà, Cầu Giấy",
      "city": "HANOI",
      "latitude": 21.0201,
      "longitude": 105.80095,
      "status": "ACTIVE"
    },
    {
      "name": "E-Motion Station Thanh Xuân",
      "address": "183 Đ. Nguyễn Trãi, Thượng Đình, Thanh Xuân",
      "city": "HANOI",
      "latitude": 20.99851,
      "longitude": 105.8139,
      "status": "ACTIVE"
    }
  ]
}
```

---

### 5. Find All Stations

**Endpoint:** `GET /api/stations`

**Description:** Retrieve a list of all stations.

**Authentication:** Requires JWT token.

**Example:** `GET http://localhost:8080/api/stations`

**Success Response:**
```json
{
  "status": 200,
  "message": "Get all stations successfully",
  "data": [
    {
      "name": "E-Motion Station Hoàn Kiếm",
      "address": "66 Tràng Tiền, Hoàn Kiếm",
      "city": "HANOI",
      "latitude": 21.02552,
      "longitude": 105.85335,
      "status": "ACTIVE"
    },
    {
      "name": "E-Motion Station Tân Bình",
      "address": "396 Đ. Lý Thường Kiệt, Phường 7, Tân Bình",
      "city": "TP_HCM",
      "latitude": 10.78463,
      "longitude": 106.65434,
      "status": "ACTIVE"
    },
    {
      "name": "E-Motion Station Thủ Đức",
      "address": "5 Đ. Đỗ Xuân Hợp, Phước Long B, Thủ Đức",
      "city": "TP_HCM",
      "latitude": 10.82967,
      "longitude": 106.7679,
      "status": "ACTIVE"
    }
  ]
}
```

---

### 6. Delete Station by Name

**Endpoint:** `DELETE /api/stations/{name}`

**Description:** Remove a station from the system.

**Authentication:** Requires JWT token (ADMIN role).

**Path Parameter:**
- `name` (string): Station name

**Example:** `DELETE http://localhost:8080/api/stations/E-Motion Station Example6`

**Success Response:**
```json
{
  "status": 200,
  "message": "Delete station successfully",
  "data": null
}
```

---

### 7. Update Station by Name

**Endpoint:** `PUT /api/stations/{name}`

**Description:** Update station information.

**Authentication:** Requires JWT token (ADMIN role).

**Path Parameter:**
- `name` (string): Station name

**Request Body:**
```json
{
  "name": "E-Motion Station Example6",
  "address": "124 Example Street ",
  "city": "Hà Nội",
  "status": "ACTIVE",
  "latitude": 10.762672,
  "longitude": 106.660172
}
```

**Example:** `PUT http://localhost:8080/api/stations/E-Motion Station Example6`

**Success Response:**
```json
{
  "status": 200,
  "message": "Update station successfully",
  "data": {
    "name": "E-Motion Station Example6",
    "address": "124 Example Street ",
    "city": "HANOI",
    "latitude": 10.762672,
    "longitude": 106.660172,
    "status": "ACTIVE"
  }
}
```

---

## RentalCheckList API (/api/rental-checklists)

Base URL: `http://localhost:8080/api/rental-checklists`

All responses are wrapped in:
```json
{
  "status": 200,
  "message": "success",
  "data": { ... }
}
```

### 1. Create Check-In Checklist

**Endpoint:** `POST /api/rental-checklists`

**Description:** Creates a check-in checklist for a rental (records vehicle condition at pick-up).

**Authentication:** Requires JWT token (STAFF role).

**Request Body:**
```json
{
  "rentalId": 3,
  "currentBattery": 85.0,
  "staffEmail": "staff@example.com",
  "type": "CHECK_IN",
  "img": "http://example.com/checkin_image.png"
}
```

**Success Response:**
```json
{
  "status": 200,
  "message": null,
  "data": {
    "id": 18,
    "type": "CHECK_IN",
    "fee": 0.0,
    "currentBattery": 85.0,
    "img": "http://example.com/checkin_image.png",
    "rentalId": 3,
    "staffEmail": "staff@example.com",
    "createdAt": "2025-10-12T12:56:25"
  }
}
```

---

### 2. Create Check-Out Checklist

**Endpoint:** `POST /api/rental-checklists`

**Description:** Creates a check-out checklist for a rental (records vehicle condition at return and calculates fees).

**Authentication:** Requires JWT token (STAFF role).

**Request Body:**
```json
{
  "rentalId": 3,
  "currentBattery": 10.0,
  "staffEmail": "staff@example.com",
  "type": "CHECK_OUT",
  "img": "http://example.com/checkout_image.png"
}
```

**Success Response:**
```json
{
  "status": 200,
  "message": null,
  "data": {
    "id": 19,
    "type": "CHECK_OUT",
    "fee": 7470000.0,
    "currentBattery": 10.0,
    "img": "http://example.com/checkout_image.png",
    "rentalId": 3,
    "staffEmail": "staff@example.com",
    "createdAt": "2025-10-12T13:01:00"
  }
}
```

