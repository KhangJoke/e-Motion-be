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
- **MySQL** (đang chạy sẵn trên máy, port `1433`)

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
- **Description:** Xóa refresh token trong cookie.
- **Request Body:**

  ```json
  {
    "token": "jwt.token.string"
  }
  ```

- **Success Response (200):**

  ```json
  {
    "status": 200,
    "message": "User logout in successfully",
    "data": null
  }
  ```
- **Cookie (Set-Cookie):**

  ```text
    "Set-Cookie": "refreshToken=jwt.refresh.token.string; HttpOnly; Path=/; Max-Age=604800"
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
