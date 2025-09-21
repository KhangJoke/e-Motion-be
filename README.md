# e-Motion Backend

## Giới thiệu

**e-Motion** là hệ thống web app quản lý **dịch vụ thuê xe điện**, hỗ trợ các vai trò:

- **Admin**: quản trị hệ thống
- **Staff**: quản lý xe & dịch vụ
- **Renter**: khách hàng thuê xe

Ứng dụng backend được xây dựng trên **Spring Boot 3.5.5**, kết nối **SQL Server 19**, sử dụng **JWT Authentication** và hỗ trợ **Mail Sender** cho việc gửi email xác thực/thông báo.

---

## Công nghệ sử dụng

- Java 17 (JDK 17)
- Spring Boot 3.5.5
- Spring Security (JWT)
- Hibernate JPA
- SQL Server 19
- Java Mail Sender
- Springdoc OpenAPI (Swagger)
- Maven 3.9.11

---

## Yêu cầu môi trường

- **JDK 17+**
- **Maven 3.9.11+**
- **SQL Server 19** (đang chạy sẵn trên máy, port `1433`)

---

## Cách chạy project (JAR)

1. **Build JAR**

cd e-Motion-be

mvn clean package

java -jar target/e-Motion-0.0.1-SNAPSHOT.jar

2. **Run JAR**

java -jar target/e-Motion-0.0.1-SNAPSHOT.jar

3. **Truy cập ứng dụng**

API: http://localhost:8080

Swagger UI: http://localhost:8080/swagger-ui/index.html

## Database

SQL Server 19

Dùng Code First (Hibernate) → khi chạy lần đầu, các bảng sẽ tự sinh trong database e-Motion.

## Cấu hình (application.properties)

**8Database**: spring.datasource.\*

**JWT**: security.jwt.secret-key, security.jwt.expiration-time

**Mail Sender**: spring.mail.\*

## API Overview

**Auth**

POST /api/auth/register → Register Account

POST /api/auth/verify → Verify Email

POST /api/auth/login → Login (JWT)

POST /api/auth/resend → Resend verification code

POST /api/auth/forgotPassword/sendVerify/{email} → Send forgot password code

POST /api/auth/forgotPassword/verify → Verify forgot password code

POST /api/auth/forgotPassword/update → Update password

**User**

GET /api/users → Lấy danh sách user

GET /api/users/me → Xem chi tiết user đang login

GET /api/users/{email} → Xem chi tiết user với email

POST /api/users/me/change-password → Đổi mật khẩu cho user

POST /api/users/me/update-profile → Cập nhật profile cho user

DELETE /api/users/{email} → Xóa user

**User Documents**

POST /api/documents → Tạo User Document mới

GET /api/documents → Lấy hết User Document
