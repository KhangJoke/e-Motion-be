# e-Motion Backend

## Giới thiệu

**e-Motion** là hệ thống web app quản lý **dịch vụ thuê xe điện**, hỗ trợ các vai trò:

- **Admin**: quản trị hệ thống
- **Staff**: quản lý xe & dịch vụ
- **Renter**: khách hàng thuê xe

Ứng dụng backend được xây dựng trên **Spring Boot 3.5.5**, kết nối **MySQL 8**, sử dụng **JWT Authentication** và hỗ trợ **Mail Sender** cho việc gửi email xác thực/thông báo.

---

## Công nghệ sử dụng

- Java 17 (JDK 17)
- Spring Boot 3.5.5
- Spring Security (JWT)
- Hibernate JPA
- MySQL 8
- Java Mail Sender
- Springdoc OpenAPI (Swagger)
- Maven 3.9.11

---

## Yêu cầu môi trường

- **JDK 17+**
- **Maven 3.9.11+**
- **MySQL 8** (đang chạy sẵn trên máy, port `1433`)
- **Redis 7** (port `6379`)

---

## Cách chạy project (JAR)

**1. Build JAR**

cd e-Motion-be

mvn clean package

java -jar target/e-Motion-0.0.1-SNAPSHOT.jar

**2. Run JAR**

java -jar target/e-Motion-0.0.1-SNAPSHOT.jar

**3. Truy cập ứng dụng**

API: http://localhost:8080

Swagger UI: http://localhost:8080/swagger-ui/index.html

## Database Local

MySQL 8

Dùng Code First (Hibernate) → khi chạy lần đầu, các bảng sẽ tự sinh trong database e-Motion.

Redis 7

Dùng Docker chạy
```json
docker run -d --name e-motion-redis -p 6379:6379 redis:7
```

## Config (application.properties)

**8Database**: spring.datasource.\*

**JWT**: security.jwt.secret-key, security.jwt.expiration-time

**Mail Sender**: spring.mail.\*

# API Documentation

This document provides detailed information about the API endpoints for the e-Motion application.

## General Information

- All API endpoints are prefixed with `/api`.
- The standard response format is a JSON object containing `status`, `message`, and `data`.

```json
{
  "status": 200,
  "message": "Success",
  "data": { ... }
}
```

---

## Authentication (`/api/auth`)

### 1. Register a new user

- **Endpoint:** `POST /api/auth/register`
- **Description:** Creates a new user account.
- **Request Body:**

  ```json
  {
    "email": "user@example.com",
    "userPassword": "password123",
    "fullName": "John Doe",
    "phone": "0912345678"
  }
  ```

- **Success Response (201):**

  ```json
  {
    "status": 201,
    "message": "User registered successfully. Please check your email for verification code.",
    "data": "user@example.com"
  }
  ```

### 2. Log in

- **Endpoint:** `POST /api/auth/login`
- **Description:** Đăng nhập, trả về access token và set refresh token trong cookie.
- **Request Body:**

  ```json
  {
    "email": "user@example.com",
    "password": "password123"
  }
  ```

- **Success Response (200):**

  ```json
  {
    "status": 200,
    "message": "User logged in successfully",
    "data": {
      "token": "jwt.token.string",
      "expiresIn": 1678886400000
    }
  }
  ```

- **Cookie (Set-Cookie):**

  ```text
    "Set-Cookie": "refreshToken=jwt.refresh.token.string; HttpOnly; Path=/; Max-Age=604800"
  ```

### 3. Refresh Token

- **Endpoint:** `POST /api/auth/refresh`
- **Description:** Lấy refresh token từ cookie → phát hành access token mới và refresh token mới.
- **Request Body:**

  ```text
  Không cần body, chỉ cần cookie refresh_token
  ```

- **Success Response (200):**

  ```json
  {
    "status": 200,
    "message": "Token refreshed successfully",
    "data": {
      "token": "new-access-token",
      "expiresIn": 1678886400000
    }
  }
  ```

- **Cookie (Set-Cookie):**

  ```text
    "Set-Cookie": "refresh_token=new-refresh-token; HttpOnly; Secure; Path=/api/auth/refresh; Max-Age=604800"
  ```  

### 4. Logout

- **Endpoint:** `POST /api/auth/logout`
- **Description:** Đăng xuất user, xóa refresh token trong cookie và vô hiệu hóa access token hiện tại.
- **Request Header:**

    ```json
    Authorization: Bearer <access_token>
    ```
- **Request Body:**
  - Không cần


- **Cookie (Set-Cookie) Request:**

  ```text
    "Set-Cookie": "refreshToken=jwt.refresh.token.string; HttpOnly; Path=/; Max-Age=604800"
  ```
- **Success Response (200):**

  ```json
  {
    "status": 200,
    "message": "User logout in successfully",
    "data": null
  }
  ```
- **Cookie (Set-Cookie) Response:**

  ```text
    "Set-Cookie": "refreshToken=jwt.refresh.token.string; HttpOnly; Path=/; Max-Age=0"
  ```
  
### 5. Verify user account

- **Endpoint:** `POST /api/auth/verify`
- **Description:** Verifies a user's email address using the verification code.
- **Request Body:**

  ```json
  {
    "email": "user@example.com",
    "verificationCode": "123456"
  }
  ```

- **Success Response (200):**

  ```json
  {
    "status": 200,
    "message": "User verified successfully",
    "data": "user@example.com"
  }
  ```

### 6. Resend verification code

- **Endpoint:** `POST /api/auth/resend`
- **Description:** Resends the verification code to the user's email.
- **Query Parameters:**
    - `email` (string): The user's email address.
- **Success Response (200):**

  ```json
  {
    "status": 200,
    "message": "Verification code resent successfully",
    "data": "user@example.com"
  }
  ```

### 7. Send verification code for password update

- **Endpoint:** `POST /api/auth/forgotPassword/sendVerify/{email}`
- **Description:** Sends a verification code to the user's email to initiate a password update.
- **Path Variable:**
    - `email` (string): The user's email address.
- **Success Response (200):**

  ```json
  {
    "status": 200,
    "message": "Verification code sent successfully",
    "data": "user@example.com"
  }
  ```

### 8. Verify forgot password request

- **Endpoint:** `POST /api/auth/forgotPassword/verify`
- **Description:** Verifies the code for a forgot password request.
- **Request Body:**

  ```json
  {
    "email": "user@example.com",
    "verificationCode": "123456"
  }
  ```

- **Success Response (200):**

  ```json
  {
    "status": 200,
    "message": "User verified successfully",
    "data": "user@example.com"
  }
  ```

### 9. Update password after forgetting

- **Endpoint:** `POST /api/auth/forgotPassword/update`
- **Description:** Updates the user's password after a successful verification.
- **Request Body:**

  ```json
  {
    "email": "user@example.com",
    "newPassword": "newPassword456",
    "confirmNewPassword": "newPassword456",
    "forgotPasswordCode": "123456"
  }
  ```

- **Success Response (200):**

  ```json
  {
    "status": 200,
    "message": "Password updated successfully",
    "data": "user@example.com"
  }
  ```

---

## Users (`/api/users`)

### 1. Get current user details

- **Endpoint:** `GET /api/users/me`
- **Description:** Retrieves the details of the currently authenticated user.
- **Authentication:** Requires JWT token.
- **Success Response (200):**

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

### 2. Get all users

- **Endpoint:** `GET /api/users`
- **Description:** Retrieves a list of all users (Admin only).
- **Authentication:** Requires JWT token with ADMIN role.
- **Success Response (200):**

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

### 3. Get user by email

- **Endpoint:** `GET /api/users/{email}`
- **Description:** Retrieves a specific user by their email address.
- **Authentication:** Requires JWT token.
- **Path Variable:**
    - `email` (string): The email of the user to retrieve.
- **Success Response (200):**

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

### 4. Delete user by email

- **Endpoint:** `DELETE /api/users/delete/{email}`
- **Description:** Deletes a user by their email address (Admin only).
- **Authentication:** Requires JWT token with ADMIN role.
- **Path Variable:**
    - `email` (string): The email of the user to delete.
- **Success Response (200):**

  ```json
  {
    "status": 200,
    "message": "Delete user by email successfully",
    "data": null
  }
  ```

### 5. Change password

- **Endpoint:** `POST /api/users/me/change-password`
- **Description:** Allows the authenticated user to change their password.
- **Authentication:** Requires JWT token.
- **Request Body:**

  ```json
  {
    "oldPassword": "password123",
    "newPassword": "newPassword456",
    "confirmNewPassword": "newPassword456"
  }
  ```

- **Success Response (200):**

  ```json
  {
    "status": 200,
    "message": "Change password successfully",
    "data": null
  }
  ```

### 6. Update user profile

- **Endpoint:** `POST /api/users/me/update-profile`
- **Description:** Allows the authenticated user to update their profile information.
- **Authentication:** Requires JWT token.
- **Request Body:**

  ```json
  {
    "fullName": "Johnathan Doe",
    "phone": "0987654321"
  }
  ```

- **Success Response (200):**

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

### 1. Create a new document

- **Endpoint:** `POST /api/documents`
- **Description:** Creates a new document for a user.
- **Authentication:** Requires JWT token.
- **Request Body:**

  ```json
  {
    "imgUrl": "http://example.com/new_doc.jpg",
    "docType": "PASSPORT",
    "docNumber": "C1234567",
    "email": "user@example.com"
  }
  ```

- **Success Response (200):**

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

### 2. Get all documents

- **Endpoint:** `GET /api/documents`
- **Description:** Retrieves all documents for all users (Admin only).
- **Authentication:** Requires JWT token with ADMIN role.
- **Success Response (200):**

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

### 3. Get document by ID

- **Endpoint:** `GET /api/documents/{docId}`
- **Description:** Retrieves a specific document by its ID.
- **Authentication:** Requires JWT token.
- **Path Variable:**
    - `docId` (long): The ID of the document to retrieve.
- **Success Response (200):**

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

### 4. Get documents by User ID

- **Endpoint:** `GET /api/documents/user/{userId}`
- **Description:** Retrieves all documents for a specific user by their user ID.
- **Authentication:** Requires JWT token.
- **Path Variable:**
    - `userId` (long): The ID of the user.
- **Success Response (200):**

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

### 5. Update a document

- **Endpoint:** `PUT /api/documents/{docId}`
- **Description:** Updates an existing document.
- **Authentication:** Requires JWT token.
- **Path Variable:**
    - `docId` (long): The ID of the document to update.
- **Request Body:**

  ```json
  {
    "imgUrl": "http://example.com/updated_image.jpg",
    "docType": "CCCD",
    "docNumber": "098765432109"
  }
  ```

- **Success Response (200):**

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

### 6. Delete a document

- **Endpoint:** `DELETE /api/documents/{docId}`
- **Description:** Deletes a document by its ID.
- **Authentication:** Requires JWT token.
- **Path Variable:**
    - `docId` (long): The ID of the document to delete.
- **Success Response (200):**

  ```json
  {
    "status": 200,
    "message": "Document deleted successfully",
    "data": null
  }
  ```


## Vehicle API (/api/vehicles)

## Base URL: http://localhost:8080/api/vehicles

## All responses are wrapped in:

{
"status": 200,
"message": "success",
"data": { ... }
}

## 1. Get All Vehicles
Endpoint: GET /api/vehicles

Description: Retrieve a list of all vehicles.

Success Response:

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
## 2. Find Vehicle by ID

Endpoint: GET /api/vehicles/id/{id}

Description: Retrieve a vehicle by its ID.

Path Parameter:http://localhost:8080/api/vehicles/id/1

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

Success Response: Vehicle object (same as above).

## 3. Find Vehicle by Plate Number

Endpoint: GET /api/vehicles/plate/{plateNumber}

Description: Retrieve a vehicle by its plate number.

Path Parameter: http://localhost:8080/api/vehicles/plate/59A-77777

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

## 4. Create a New Vehicle

Endpoint: POST /api/vehicles

Description: Add a new vehicle.

Request Body:http://localhost:8080/api/vehicles

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

Success Response: Newly created Vehicle object.

## 5.Update Vehicle by ID

Endpoint: PUT /api/vehicles/{id}

Description: Update a vehicle by its ID.

Path Parameter:http://localhost:8080/api/vehicles/1

id (Long) – Vehicle ID

Request Body: Same as create vehicle (can update only required fields).

Success Response: Updated Vehicle object.

```json
{
"status": 200,
"message": "Vehicle updated successfully",
"data": null
}
```

## 6.Delete Vehicle by ID

Endpoint: DELETE /api/vehicles/{id}

Description: Delete a vehicle by its ID.

Path Parameter:http://localhost:8080/api/vehicles/5

id (Long) – Vehicle ID

Success Response:

```json
{
"status": 200,
"message": "Vehicle deleted successfully",
"data": null
}
```

## RentalCheckList API(/api/rental-checklists)

Base URL: http://localhost:8080/api/rental-checklists

All responses are wrapped in:

{ "status": 200, "message": "success", "data": { ... } }

## 1.CreateCheckList (CHECK_IN)

Endpoint: POST /api/vehicles/rental-checklists

Description: Create a new checklist check in.

Request Body: http://localhost:8080/api/rental-checklists

```json
{
"rentalId": 3,
"currentBattery": 10.0,
"staffEmail": "voquangtrungyb@gmail.com",
"type": "CHECK_IN",
"img": "http://example.com/updated_image.png"
}
```

Success Response:
```json
{
  "status": 200,
  "message": null,
  "data": {
    "id": 18,
    "type": "CHECK_IN",
    "fee": 0.0,
    "currentBattery": 70.0,
    "img": "http://example.com/updated_image.png",
    "rentalId": 3,
    "staffEmail": "voquangtrungyb@gmail.com",
    "createdAt": "2025-10-12T12:56:25.565433"
  }
}
```

## 2.CreateCheckList (CHECK_OUT)

Endpoint: POST /api/vehicles/rental-checklists

Description: Create a new checklist check out.

Request Body: http://localhost:8080/api/rental-checklists

```json
{
  "rentalId": 3,
  "currentBattery": 10.0,
  "staffEmail": "voquangtrungyb@gmail.com",
  "type": "CHECK_OUT",
  "img": "http://example.com/updated_image.png"
}
```

Success Response:
```json
{
  "status": 200,
  "message": null,
  "data": {
    "id": 19,
    "type": "CHECK_OUT",
    "fee": 7470000.0,
    "currentBattery": 10.0,
    "img": "http://example.com/updated_image.png",
    "rentalId": 3,
    "staffEmail": "voquangtrungyb@gmail.com",
    "createdAt": "2025-10-12T13:01:00.996125"
  }
}
```

## Staff API (/api/staffs)

Base URL: http://localhost:8080/api/staffs 
All responses are wrapped in:

{ "status": 200, "message": "success", "data": { ... } }

## 1. Create Staff

Endpoint: POST /api/staffs

Description: Create a new staff.

Request Body: http://localhost:8080/api/staffs

```json
{
    "email": "nguyen1112894@gmail.com",
    "stationName": "E-Motion Station Cầu Giấy"
}
```

Success Response:
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

## 2. Find Staff by Email

Endpoint: GET /api/staffs/{email}

Description: Find staff by email.

Path Parameter: http://localhost:8080/api/staffs/nguyen1112894@gmail.com

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

## 3. Find all staffs.

Endpoint: GET /api/staffs

Description: Find all staffs.

Path Parameter: http://localhost:8080/api/staffs

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
      "email": "le4035040@gmail.com",
      "stationName": "E-Motion Station Tân Bình",
      "fullName": "nam"
    },
    {
      "email": "myapah7605@gmail.com",
      "stationName": "E-Motion Station Tân Bình",
      "fullName": "phat"
    },
    {
      "email": "myapah2005@gmail.com",
      "stationName": "E-Motion Station Thủ Đức",
      "fullName": "phat"
    },
    {
      "email": "voquangtrung04022005@gmail.com",
      "stationName": "E-Motion Station Thủ Đức",
      "fullName": "Quang Trung Đại Đế"
    },
    {
      "email": "barrysmunozeg1ef@gmail.com",
      "stationName": "E-Motion Station Trần Hưng Đạo",
      "fullName": "Quang Trung Đại Đế"
    },
    {
      "email": "trunho05@gmail.com",
      "stationName": "E-Motion Station Cầu Giấy",
      "fullName": "Nguyễn Huệ"
    },
    {
      "email": "voquangtrungyb@gmail.com",
      "stationName": "E-Motion Station Cầu Giấy",
      "fullName": "vua Quang Trung"
    },
    {
      "email": "voquangtrung11a2locan2021@gmail.com",
      "stationName": "E-Motion Station Thanh Xuân",
      "fullName": "Bắc Bình Vương"
    },
    {
      "email": "nhatnam13112005@gmail.com",
      "stationName": "E-Motion Station Thanh Xuân",
      "fullName": "Nguyen Quang Nhat Nam"
    },
    {
      "email": "voquangtrungtiktok@gmail.com",
      "stationName": "E-Motion Station Cầu Giấy",
      "fullName": "Võ Quang Trung"
    },
    {
      "email": "nguyen1112894@gmail.com",
      "stationName": "E-Motion Station Cầu Giấy",
      "fullName": "Nguyen"
    }
  ]
}
```

## 4. Delete Staff by Email

Endpoint: DELETE /api/staffs/{email}

Description: Delete staff by email.

Path Parameter: http://localhost:8080/api/staffs/nguyen1112894@gmail.com

```json
{
  "status": 200,
  "message": "Delete staff by email successfully",
  "data": null
}
```

## 5. Update Staff by Email

Endpoint: PUT /api/staffs/{email}

Description: Update staff by email.

Request Body: http://localhost:8080/api/staffs/voquangtrungtiktok@gmail.com

```json
{
  "email": "voquangtrungtiktok@gmail.com",
  "oldStationName": "E-Motion Station Cầu Giấy",
  "newStationName": "E-Motion Station Thủ Đức"
}
```

Success Response:
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

## Station API (/api/stations)

Base URL: http://localhost:8080/api/stations
All responses are wrapped in:

{ "status": 200, "message": "success", "data": { ... } }

## 1. Create Station

Endpoint: POST /api/stations

Description: Create a new station.

Request Body: http://localhost:8080/api/stations

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

Success Response:
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

## 2. Find Station by name

Endpoint: GET /api/stations/name/{name}

Description: Find station by name.

Path Parameter: http://localhost:8080/api/stations/name/E-Motion Station Example6

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

## 3. Find Station by address

Endpoint: GET /api/stations/address/{address}

Description: Find station by address.

Path Parameter: http://localhost:8080/api/stations/address/123 Example Street
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
## 4. Find Station by city

Endpoint: GET /api/stations/city/{city}

Description: Find station by city.

Path Parameter: http://localhost:8080/api/stations/city/HANOI
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
    },
    {
      "name": "E-Motion Station Example",
      "address": "123 Example Street",
      "city": "HANOI",
      "latitude": null,
      "longitude": null,
      "status": null
    },
    {
      "name": "E-Motion Station Example1",
      "address": "123 Example Street",
      "city": "HANOI",
      "latitude": null,
      "longitude": null,
      "status": null
    },
    {
      "name": "E-Motion Station Example2",
      "address": "123 Example Street",
      "city": "HANOI",
      "latitude": null,
      "longitude": null,
      "status": null
    },
    {
      "name": "E-Motion Station Example3",
      "address": "123 Example Street",
      "city": "HANOI",
      "latitude": 10.762622,
      "longitude": 106.660172,
      "status": null
    },
    {
      "name": "E-Motion Station Example4",
      "address": "123 Example Street",
      "city": "HANOI",
      "latitude": 10.762622,
      "longitude": 106.660172,
      "status": null
    },
    {
      "name": "E-Motion Station Example5",
      "address": "123 Example Street",
      "city": "HANOI",
      "latitude": 10.762622,
      "longitude": 106.660172,
      "status": null
    },
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
## 5. Find all stations.

Endpoint: GET /api/stations

Description: Find all stations.

Path Parameter: http://localhost:8080/api/stations

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
    },
    {
      "name": "E-Motion Station Trần Hưng Đạo",
      "address": "34 Trần Hưng Đạo, P. Phạm Ngũ Lão, Q1",
      "city": "TP_HCM",
      "latitude": 10.76723,
      "longitude": 106.69443,
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
    },
    {
      "name": "E-Motion Station Example",
      "address": "123 Example Street",
      "city": "HANOI",
      "latitude": null,
      "longitude": null,
      "status": null
    },
    {
      "name": "E-Motion Station Example1",
      "address": "123 Example Street",
      "city": "HANOI",
      "latitude": null,
      "longitude": null,
      "status": null
    },
    {
      "name": "E-Motion Station Example2",
      "address": "123 Example Street",
      "city": "HANOI",
      "latitude": null,
      "longitude": null,
      "status": null
    },
    {
      "name": "E-Motion Station Example3",
      "address": "123 Example Street",
      "city": "HANOI",
      "latitude": 10.762622,
      "longitude": 106.660172,
      "status": null
    },
    {
      "name": "E-Motion Station Example4",
      "address": "123 Example Street",
      "city": "HANOI",
      "latitude": 10.762622,
      "longitude": 106.660172,
      "status": null
    },
    {
      "name": "E-Motion Station Example5",
      "address": "123 Example Street",
      "city": "HANOI",
      "latitude": 10.762622,
      "longitude": 106.660172,
      "status": null
    },
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

## 6. Delete Staff by Email

Endpoint: DELETE /api/stations/{name}

Description: Delete station by name.

Path Parameter: http://localhost:8080/api/stations/E-Motion Station Example6

```json
{
  "status": 200,
  "message": "Delete station successfully",
  "data": null
}
```

## 7. Update Station by name

Endpoint: PUT /api/stations/{name}

Description: Update station by name.

Request Body: http://localhost:8080/api/stations/E-Motion Station Example6

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

Success Response:
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