# e-Motion Backend - Electric Vehicle Rental Management System

<div align="center">

![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-brightgreen?style=for-the-badge&logo=spring-boot)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=for-the-badge&logo=mysql)
![Redis](https://img.shields.io/badge/Redis-7.0-red?style=for-the-badge&logo=redis)
![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)

**Modern REST API for Electric Vehicle Rental & Management Platform**

[Features](#-key-features) • [Tech Stack](#-technology-stack) • [Getting Started](#-getting-started) • [API Docs](#-api-documentation) • [Security](#-security-architecture)

</div>

---

## Introduction

**e-Motion** is an enterprise-grade electric vehicle (EV) rental management system built with Spring Boot 3. The platform enables seamless vehicle booking, reservation management, digital contract signing, integrated payment processing, and comprehensive fleet tracking across multiple stations.

### Project Objectives

- **Digitize EV Rental Process**: Transform traditional vehicle rental into a modern, paperless experience
- **Multi-Station Management**: Centralized platform for managing vehicles across multiple locations
- **Secure Payments**: Integrated VNPay payment gateway for safe transactions
- **Smart Automation**: Automated notifications, reminders, and contract generation
- **Real-time Tracking**: Monitor vehicle status, battery levels, and rental activities

---

## Key Features

### Authentication & Authorization
- **JWT-based Security**: Stateless authentication using access and refresh tokens
- **Role-Based Access Control (RBAC)**: Three-tier role system (Admin, Staff, User)
- **Email Verification**: Two-factor account verification with OTP
- **Password Recovery**: Secure forgot password flow with email verification
- **Session Management**: Redis-backed token storage with automatic expiration

### User Management
- **User Profiles**: Complete profile management with document uploads
- **Document Verification**: Support for ID cards (CCCD), Driver's License, and Passport
- **User Blocking**: Admin capability to block/unblock users
- **Activity Tracking**: Full audit trail of user activities

### Vehicle Management
- **Fleet Management**: Comprehensive CRUD for electric vehicles
- **Real-time Tracking**: Monitor battery levels, location, and status
- **Multi-category Support**: Cars, motorcycles, e-bikes with various categories
- **Vehicle Logs**: Maintenance history and usage tracking
- **Image Gallery**: Multiple images per vehicle with Cloudinary integration
- **Smart Availability**: Automatic status updates based on rentals

### Station Management
- **Multi-station Support**: Manage vehicles across different locations
- **City-based Organization**: Stations organized by cities
- **Capacity Management**: Track available slots per station
- **Station Assignment**: Flexible vehicle-to-station assignment

### Reservation System
- **Advanced Booking**: Reserve vehicles up to 30 days in advance
- **Reservation Codes**: Unique QR codes for each reservation
- **Auto-expiration**: Automatic cancellation of expired reservations
- **Flexible Modification**: Update or cancel reservations before rental starts
- **Email Notifications**: Automated confirmations and reminders

### Rental Management
- **Flexible Rental Periods**: 4h, 8h, 12h, or daily rentals
- **Check-in/Check-out**: Digital checklist with damage reporting
- **Contract Generation**: Automated digital contract via DocuSeal
- **Deposit Handling**: Automated deposit hold and release
- **Rental Extensions**: Extend rentals on-the-fly
- **Point System**: Loyalty points for discounts

### Payment Integration
- **VNPay Gateway**: Secure online payment processing
- **Multiple Payment Types**: Deposits, rentals, penalties
- **Payment History**: Complete transaction tracking
- **Auto-refunds**: Automated refund processing for cancellations
- **Receipt Generation**: Digital receipts via email

### Email Notifications
- **Welcome Emails**: Account verification emails
- **Booking Confirmations**: Reservation and rental confirmations
- **Reminders**: Upcoming rental/reservation reminders
- **Status Updates**: Rental status change notifications
- **Overdue Alerts**: Automated overdue rental warnings
- **Payment Receipts**: Transaction confirmations

### AI Integration
- **Smart Chat**: Gemini 2.5 Pro AI-powered customer support chatbot
- **OCR Document Reading**: Automatic ID card data extraction

### Reports & Analytics
- **User Reports**: Problem reporting with image uploads
- **Staff Dashboard**: Rental statistics and performance metrics
- **Rating System**: User feedback and vehicle ratings
- **Vehicle Analytics**: Usage patterns and maintenance needs

---

## 🛠 Technology Stack

### Backend Framework
| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 17 | Core programming language |
| **Spring Boot** | 3.5.5 | Application framework |
| **Spring Security** | 6.5.3 | Authentication & authorization |
| **Spring Data JPA** | 3.x | Database ORM with Hibernate |
| **Spring Validation** | 3.5.5 | Input validation |
| **Spring AI** | 1.0.3 | AI integration (Gemini) |

### Database & Caching
| Technology | Version | Purpose |
|------------|---------|---------|
| **MySQL** | 8.4.0 | Primary relational database |
| **Redis** | 7.0+ | Caching & session management |
| **Hibernate** | 6.x | ORM with auto-DDL |

### Security & Authentication
| Technology | Version | Purpose |
|------------|---------|---------|
| **JWT (JJWT)** | 0.11.5 | Token-based authentication |
| **BCrypt** | - | Password hashing |

### Third-Party Integrations
| Service | Purpose |
|---------|---------|
| **VNPay** | Payment gateway integration |
| **Cloudinary** | Image hosting and management |
| **SendGrid** | Transactional email service |
| **DocuSeal** | Digital contract signing |
| **Gemini AI** | Chatbot and AI features |
| **Tess4J** | OCR for document scanning |

### Documentation & Tools
| Technology | Version | Purpose |
|------------|---------|---------|
| **Springdoc OpenAPI** | 2.8.13 | Auto-generated API docs (Swagger) |
| **Lombok** | 1.18.30 | Reduce boilerplate code |
| **MapStruct** | 1.5.5 | DTO mapping |
| **Gson** | 2.11.0 | JSON processing |
| **Maven** | 3.11.0 | Build & dependency management |

### Other Libraries
- **Thymeleaf**: HTML email templates
- **ZXing**: QR code generation
- **Jackson**: JSON serialization/deserialization

---

## System Requirements

### Development Environment
- **JDK**: 17 or higher
- **Maven**: 3.9.11 or higher
- **MySQL**: 8.0+ (port 3306)
- **Redis**: 7.0+ (port 6379)
- **IDE**: IntelliJ IDEA (recommended), Eclipse, or VS Code

### Runtime Requirements
- **Memory**: Minimum 2GB RAM (4GB recommended for production)
- **Disk Space**: 1GB for application, dependencies, and logs
- **Network**: Internet connection required for:
    - VNPay payment processing
    - Email services (SendGrid/SMTP)
    - Cloudinary image hosting
    - AI services (Gemini)
    - DocuSeal contract generation

### External Services (Required)
1. **VNPay Merchant Account** (for payments)
2. **Cloudinary Account** (for image storage)
3. **SendGrid Account** or Gmail App Password (for emails)
4. **Google AI API Key** (for Gemini chatbot)
5. **DocuSeal API Key** (for contracts)
6. **Railway/Cloud Database** (for production deployment)

---

## Getting Started

### Prerequisites Checklist
- [ ] Java 17+ installed
- [ ] Maven 3.9.11+ installed
- [ ] MySQL 8.0+ running
- [ ] Redis server running
- [ ] API keys for external services ready

### 1️⃣ Clone the Repository

```bash
git clone https://github.com/your-username/e-Motion-be.git
cd e-Motion-be
```

### 2️⃣ Configure Environment Variables

**⚠️ IMPORTANT FOR PRODUCTION**: Never commit sensitive data to version control!

Create `.env` file in project root (or use environment variables):

```properties
# Database Configuration
DB_URL=jdbc:mysql://localhost:3306/e-motion?serverTimezone=Asia/Ho_Chi_Minh
DB_USERNAME=root
DB_PASSWORD=your_mysql_password

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# JWT Configuration (Generate a secure 256-bit key)
JWT_SECRET_KEY=your_very_long_and_secure_secret_key_min_256_bits
JWT_EXPIRATION_TIME=5200000

# Email Configuration (Gmail)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_specific_password

# SendGrid (Alternative)
SENDGRID_API_KEY=your_sendgrid_api_key

# Cloudinary Configuration
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret

# OCR Configuration
OCR_API_KEY=your_ocr_api_key

# VNPay Configuration
VNPAY_TMN_CODE=your_tmn_code
VNPAY_HASH_SECRET=your_hash_secret
VNPAY_RETURN_URL=http://localhost:8080/api/payment/vnpay-return

# AI Configuration (Gemini)
OPENAI_API_KEY=your_gemini_api_key

# DocuSeal Configuration
DOCUSEAL_API_KEY=your_docuseal_api_key
DOCUSEAL_TEMPLATE_ID=your_template_id
DOCUSEAL_RETURN_URL=http://localhost:8080/api/docuseal/return
```

### 3️⃣ Update Application Properties

Edit `src/main/resources/application.properties` to use environment variables:

```properties
# Application Name & Port
spring.application.name=e-Motion-be
server.port=8080

# MySQL Database
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Hibernate JPA
spring.jpa.hibernate.ddl-auto=update
spring.jackson.time-zone=Asia/Ho_Chi_Minh
spring.jpa.properties.hibernate.jdbc.time_zone=Asia/Ho_Chi_Minh

# Redis
spring.data.redis.host=${REDIS_HOST}
spring.data.redis.port=${REDIS_PORT}
spring.data.redis.password=${REDIS_PASSWORD:}

# JWT
security.jwt.secret-key=${JWT_SECRET_KEY}
security.jwt.expiration-time=${JWT_EXPIRATION_TIME:5200000}

# Email
spring.mail.host=${MAIL_HOST}
spring.mail.port=${MAIL_PORT}
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# SendGrid
sendgrid.api.key=${SENDGRID_API_KEY}

# Cloudinary
cloudinary.cloud.name=${CLOUDINARY_CLOUD_NAME}
cloudinary.api.key=${CLOUDINARY_API_KEY}
cloudinary.api.secret=${CLOUDINARY_API_SECRET}

# VNPay
vnpay.tmn-code=${VNPAY_TMN_CODE}
vnpay.hash-secret=${VNPAY_HASH_SECRET}
vnpay.url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
vnpay.return-url=${VNPAY_RETURN_URL}
vnpay.api-url=https://sandbox.vnpayment.vn/merchant_webapi/api/transaction

# AI
spring.ai.openai.api-key=${OPENAI_API_KEY}
spring.ai.openai.chat.base-url=https://generativelanguage.googleapis.com
spring.ai.openai.chat.completions-path=/v1beta/openai/chat/completions
spring.ai.openai.chat.options.model=gemini-2.5-flash

# DocuSeal
docuseal.api-key=${DOCUSEAL_API_KEY}
docuseal.url=https://api.docuseal.com/submissions
docuseal.template-id=${DOCUSEAL_TEMPLATE_ID}
docuseal.return-url=${DOCUSEAL_RETURN_URL}

# Business Rules
price.8h.rate=1.4
price.12h.rate=1.6
price.day.rate=2.0
price.per.battery=12000
penalty.fee.rate=0.06
vat.percentage=0.1
hold.fee.value=500000
```

### 4️⃣ Setup MySQL Database

```bash
# Login to MySQL
mysql -u root -p

# Create database
CREATE DATABASE e_motion CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# Exit MySQL
EXIT;
```

**Note**: Tables will be auto-created on first application startup (Hibernate Auto-DDL).

### 5️⃣ Start Redis Server

**Using Docker** (Recommended):
```bash
docker run -d --name e-motion-redis -p 6379:6379 redis:7-alpine
```

**Using Local Installation**:
```bash
# Windows
redis-server

# Linux/Mac
redis-server /usr/local/etc/redis.conf
```

Verify Redis is running:
```bash
redis-cli ping
# Expected output: PONG
```

### 6️⃣ Build the Application

```bash
# Clean and build
mvn clean install

# Skip tests (faster)
mvn clean install -DskipTests
```

### 7️⃣ Run the Application

**Option 1: Using Maven**
```bash
mvn spring-boot:run
```

**Option 2: Using JAR file**
```bash
java -jar target/e-Motion-be-0.0.1-SNAPSHOT.jar
```

**Option 3: Using IDE**
- Open project in IntelliJ IDEA
- Run `EMotionBeApplication.java`

### 8️⃣ Verify Installation

Once started, you should see:
```
Started EMotionBeApplication in X.XXX seconds
```

Access these URLs:
- **API Base URL**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **API Docs (JSON)**: http://localhost:8080/v3/api-docs

### 9️⃣ Test the API

**Health Check**:
```bash
curl http://localhost:8080/api/health
```

**Register a Test User**:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "userPassword": "Test123456",
    "fullName": "Test User",
    "phone": "0912345678"
  }'
```

---

## Project Structure

```
e-Motion-be/
├── src/
│   ├── main/
│   │   ├── java/com/swp391/e_Motion_be/
│   │   │   ├── config/               # Configuration classes
│   │   │   │   ├── CloudinaryConfig.java
│   │   │   │   ├── EmailConfig.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   ├── RedisConfiguration.java
│   │   │   │   ├── SwaggerConfig.java
│   │   │   │   ├── VNPayConfig.java
│   │   │   │   └── WebSecurityConfig.java
│   │   │   ├── controller/           # REST API controllers
│   │   │   │   ├── AuthenticationController.java
│   │   │   │   ├── UserController.java
│   │   │   │   ├── VehicleController.java
│   │   │   │   ├── ReservationController.java
│   │   │   │   ├── RentalController.java
│   │   │   │   ├── PaymentController.java
│   │   │   │   ├── StationController.java
│   │   │   │   ├── StaffController.java
│   │   │   │   ├── DocumentController.java
│   │   │   │   ├── DepositController.java
│   │   │   │   ├── RatingController.java
│   │   │   │   ├── ReportController.java
│   │   │   │   ├── ChatController.java
│   │   │   │   ├── ContractController.java
│   │   │   │   ├── VehicleLogController.java
│   │   │   │   ├── RentalCheckListController.java
│   │   │   │   └── ImgVehicleController.java
│   │   │   ├── entity/               # JPA entities (database models)
│   │   │   │   ├── User.java
│   │   │   │   ├── Vehicle.java
│   │   │   │   ├── Reservation.java
│   │   │   │   ├── Rental.java
│   │   │   │   ├── Payment.java
│   │   │   │   ├── Station.java
│   │   │   │   ├── Staff.java
│   │   │   │   ├── Document.java
│   │   │   │   ├── Deposit.java
│   │   │   │   ├── Rating.java
│   │   │   │   ├── Report.java
│   │   │   │   ├── VehicleLog.java
│   │   │   │   ├── RentalCheckList.java
│   │   │   │   ├── ImgVehicle.java
│   │   │   │   ├── RefreshToken.java
│   │   │   │   └── RedisToken.java
│   │   │   ├── service/              # Business logic layer
│   │   │   │   ├── auth/
│   │   │   │   ├── user/
│   │   │   │   ├── document/
│   │   │   │   ├── VehicleService.java
│   │   │   │   ├── ReservationService.java
│   │   │   │   ├── RentalService.java
│   │   │   │   ├── PaymentService.java
│   │   │   │   ├── StationService.java
│   │   │   │   ├── StaffService.java
│   │   │   │   ├── DepositService.java
│   │   │   │   ├── RatingService.java
│   │   │   │   ├── ReportService.java
│   │   │   │   ├── EmailService.java
│   │   │   │   ├── CloudinaryService.java
│   │   │   │   ├── ChatService.java
│   │   │   │   ├── DocuSealService.java
│   │   │   │   ├── VehicleLogService.java
│   │   │   │   ├── RentalCheckListService.java
│   │   │   │   └── ImgVehicleService.java
│   │   │   ├── repository/           # Data access layer (JPA repositories)
│   │   │   ├── dto/                  # Data Transfer Objects
│   │   │   │   ├── requests/
│   │   │   │   ├── responses/
│   │   │   │   ├── convert/
│   │   │   │   ├── email/
│   │   │   │   └── vehicleLog/
│   │   │   ├── enums/                # Enumerations
│   │   │   │   ├── Role.java
│   │   │   │   ├── ReservationStatus.java
│   │   │   │   ├── RentalStatus.java
│   │   │   │   ├── ContractStatus.java
│   │   │   │   ├── DepositStatus.java
│   │   │   │   ├── DocumentType.java
│   │   │   │   ├── ErrorCode.java
│   │   │   │   ├── CheckType.java
│   │   │   │   ├── payment/
│   │   │   │   ├── vehicle/
│   │   │   │   ├── station/
│   │   │   │   └── report/
│   │   │   ├── exception/            # Custom exceptions & handlers
│   │   │   ├── mapper/               # MapStruct mappers
│   │   │   ├── validator/            # Custom validators
│   │   │   ├── deserializer/         # Custom JSON deserializers
│   │   │   ├── scheduler/            # Scheduled tasks
│   │   │   ├── util/                 # Utility classes
│   │   │   └── EMotionBeApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── templates/            # Email HTML templates
│   │           ├── verify-email.html
│   │           ├── reservation-email.html
│   │           ├── reservation-expiring-email.html
│   │           ├── reservation-overdue-email.html
│   │           ├── reservation-cancel-email.html
│   │           ├── contract-email.html
│   │           ├── rental-expiring-email.html
│   │           ├── rental-overdue-email.html
│   │           ├── rental-cancel-email.html
│   │           ├── rental-returned-email.html
│   │           └── payment-status-email.html
│   └── test/                         # Unit and integration tests
├── target/                           # Compiled output
├── pom.xml                           # Maven configuration
├── README.md                         # This file
├── HELP.md                           # Spring Boot help
├── mvnw                              # Maven wrapper (Unix)
├── mvnw.cmd                          # Maven wrapper (Windows)
└── e-Motion-be.iml                   # IntelliJ IDEA module file
```

---

## Security Architecture

### Authentication Flow

```
┌─────────┐          ┌──────────────┐          ┌─────────┐          ┌──────┐
│ Client  │          │   Filter     │          │ Service │          │  DB  │
└────┬────┘          └──────┬───────┘          └────┬────┘          └───┬──┘
     │                      │                       │                   │
     │ POST /auth/login     │                       │                   │
     ├─────────────────────>│                       │                   │
     │                      │ loadUserByUsername()  │                   │
     │                      ├──────────────────────>│                   │
     │                      │                       │  findByEmail()    │
     │                      │                       ├──────────────────>│
     │                      │                       │<──────────────────┤
     │                      │<──────────────────────┤   User Entity     │
     │                      │   UserDetails         │                   │
     │                      │                       │                   │
     │                      │  Validate Password    │                   │
     │                      │  (BCrypt)             │                   │
     │                      │                       │                   │
     │                      │  Generate JWT Token   │                   │
     │                      │  Save Refresh Token   │                   │
     │                      │  to Redis             │                   │
     │                      │                       │                   │
     │<─────────────────────┤                       │                   │
     │   Access Token +     │                       │                   │
     │   Refresh Token      │                       │                   │
     │   (HttpOnly Cookie)  │                       │                   │
     │                      │                       │                   │
```

### Authorization Flow

```
┌─────────┐          ┌──────────────────────┐          ┌─────────────┐
│ Client  │          │  JwtAuthFilter       │          │  Secured    │
└────┬────┘          └──────┬───────────────┘          │  Endpoint   │
     │                      │                           └──────┬──────┘
     │ GET /api/vehicles    │                                  │
     │ Authorization: Bearer<token>                            │
     ├─────────────────────>│                                  │
     │                      │                                  │
     │                      │  1. Extract JWT from Header      │
     │                      │                                  │
     │                      │  2. Validate Token               │
     │                      │     - Signature                  │
     │                      │     - Expiration                 │
     │                      │     - Blacklist (Redis)          │
     │                      │                                  │
     │                      │  3. Extract User Email           │
     │                      │                                  │
     │                      │  4. Load UserDetails             │
     │                      │                                  │
     │                      │  5. Create Authentication        │
     │                      │     UsernamePasswordAuthToken    │
     │                      │                                  │
     │                      │  6. Set SecurityContext          │
     │                      │                                  │
     │                      ├─────────────────────────────────>│
     │                      │  Forward to Controller           │
     │                      │                                  │
     │                      │  7. Check @PreAuthorize          │
     │                      │     hasRole('USER')              │
     │                      │                                  │
     │                      │<─────────────────────────────────┤
     │<─────────────────────┤  Response                        │
     │                      │                                  │
```

### Security Features

#### 1. **Password Security**
- BCrypt hashing with salt (10 rounds)
- Password strength validation
- Secure password reset flow

#### 2. **Token Management**
- **Access Token**: Short-lived JWT (~1.5 hours)
- **Refresh Token**: Long-lived token (7 days) stored in HttpOnly cookie
- **Token Blacklisting**: Redis-based blacklist for logged-out tokens
- **Automatic Expiration**: Redis TTL for token cleanup

#### 3. **CORS Configuration**
```java
// Allowed origins: Frontend domains
allowedOrigins: ["http://localhost:3000", "https://yourdomain.com"]
allowedMethods: ["GET", "POST", "PUT", "DELETE", "PATCH"]
allowedHeaders: ["*"]
allowCredentials: true
```

#### 4. **CSRF Protection**
- Disabled for stateless REST API
- JWT used instead for CSRF protection

#### 5. **Rate Limiting** (Recommended to implement)
- TODO: Add rate limiting with Redis

#### 6. **Input Validation**
- Jakarta Validation annotations
- Custom validators for business rules
- SQL Injection prevention via JPA

---

## API Documentation

### Base URL
```
http://localhost:8080/api
```

### Interactive API Documentation
Visit **Swagger UI** for interactive API testing:
```
http://localhost:8080/swagger-ui/index.html
```

### Authentication Header
Most endpoints require JWT authentication:
```http
Authorization: Bearer <your_access_token>
```

### Standard Response Format
All API responses follow this structure:
```json
{
  "status": 200,
  "message": "Success message",
  "data": { /* Response payload */ }
}
```

### Date/Time Format
All timestamps use ISO 8601 format with Asia/Ho_Chi_Minh timezone:
```
yyyy-MM-dd'T'HH:mm:ss
Example: 2025-11-30T14:30:00
```

---

## API Endpoints Overview

### Authentication (`/api/auth`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/register` | Register new user account | ❌ |
| POST | `/login` | Login and get JWT token | ❌ |
| POST | `/logout` | Logout and invalidate tokens | ✅ |
| POST | `/refresh` | Refresh access token | 🍪 Cookie |
| POST | `/verify` | Verify email with OTP | ❌ |
| POST | `/resend` | Resend verification code | ❌ |
| POST | `/forgotPassword/sendVerify/{email}` | Send password reset code | ❌ |
| POST | `/forgotPassword/verify` | Verify reset code | ❌ |
| POST | `/forgotPassword/update` | Update forgotten password | ❌ |

### Users (`/api/users`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/me` | Get current user profile | ✅ |
| GET | `/` | Get all users | ✅ Admin |
| GET | `/{email}` | Get user by email | ✅ |
| DELETE | `/delete/{email}` | Delete user account | ✅ Admin |
| POST | `/me/change-password` | Change password | ✅ |
| POST | `/me/update-profile` | Update profile | ✅ |

### Vehicles (`/api/vehicles`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/` | Get all vehicles | ✅ |
| GET | `/id/{id}` | Get vehicle by ID | ✅ |
| GET | `/plate/{plateNumber}` | Get vehicle by plate number | ✅ |
| GET | `/booking` | Search available vehicles | ❌ Public |
| POST | `/` | Create new vehicle | ✅ Admin/Staff |
| PUT | `/{id}` | Update vehicle | ✅ Admin/Staff |
| DELETE | `/{id}` | Delete vehicle | ✅ Admin |

### Reservations (`/api/reservations`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/` | Create reservation | ✅ |
| GET | `/user` | Get user's reservations | ✅ |
| GET | `/{id}` | Get reservation details | ✅ |
| PUT | `/{id}/cancel` | Cancel reservation | ✅ |
| GET | `/code/{code}` | Get reservation by code | ✅ Staff |

### Rentals (`/api/rentals`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/` | Create rental from reservation | ✅ Staff |
| GET | `/user` | Get user's rental history | ✅ |
| GET | `/{id}` | Get rental details | ✅ |
| PUT | `/{id}/start` | Start rental (check-in) | ✅ Staff |
| PUT | `/{id}/return` | Return vehicle (check-out) | ✅ Staff |
| PUT | `/{id}/extend` | Extend rental period | ✅ |
| PUT | `/{id}/cancel` | Cancel rental | ✅ |

### Payments (`/api/payment`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/create` | Create VNPay payment URL | ✅ |
| GET | `/vnpay-return` | VNPay callback handler | ❌ |
| GET | `/user` | Get user's payment history | ✅ |
| GET | `/{id}` | Get payment details | ✅ |

### Stations (`/api/stations`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/` | Get all stations | ✅ |
| GET | `/{id}` | Get station by ID | ✅ |
| POST | `/` | Create station | ✅ Admin |
| PUT | `/{id}` | Update station | ✅ Admin |
| DELETE | `/{id}` | Delete station | ✅ Admin |

### Documents (`/api/documents`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/` | Upload user document | ✅ |
| GET | `/user/{userId}` | Get user's documents | ✅ |
| PUT | `/{docId}` | Update document | ✅ |
| DELETE | `/{docId}` | Delete document | ✅ |

### Ratings (`/api/ratings`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/` | Create vehicle rating | ✅ |
| GET | `/vehicle/{vehicleId}` | Get vehicle ratings | ✅ |
| GET | `/user` | Get user's ratings | ✅ |

### Reports (`/api/reports`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/` | Submit problem report | ✅ |
| GET | `/user` | Get user's reports | ✅ |
| GET | `/` | Get all reports | ✅ Staff |
| PUT | `/{id}/resolve` | Mark report as resolved | ✅ Staff |

### Chat (`/api/chat`)
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/` | Send message to AI chatbot | ✅ |

---

## Database Schema

### Core Entities

#### Users
```sql
users (
  user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  full_name VARCHAR(255) NOT NULL,
  phone VARCHAR(20) UNIQUE NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  role ENUM('USER', 'STAFF', 'ADMIN'),
  enabled BOOLEAN DEFAULT FALSE,
  blocked BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP,
  verification_code VARCHAR(6),
  verification_code_expires_at TIMESTAMP,
  forgot_password_code VARCHAR(6),
  point INT DEFAULT 0
)
```

#### Vehicles
```sql
vehicles (
  vehicle_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  vehicle_name VARCHAR(255) NOT NULL,
  description TEXT,
  brand ENUM('HONDA', 'YAMAHA', 'SUZUKI', 'VINFAST', 'BYD', ...),
  vehicle_status ENUM('AVAILABLE', 'RENTED', 'RESERVED', 'MAINTENANCE', 'OUT_OF_SERVICE'),
  vehicle_category ENUM('SEDAN', 'SUV', 'MPV', 'HATCHBACK', 'SCOOTER', ...),
  seats INT,
  price_4_hours DOUBLE,
  price_8_hours DOUBLE,
  price_12_hours DOUBLE,
  price_day DOUBLE,
  deposit_fee DOUBLE,
  consumption_rate DOUBLE,
  current_battery_level INT,
  battery_capacity DOUBLE,
  plate_number VARCHAR(20) UNIQUE,
  last_maintenance TIMESTAMP,
  is_delete BOOLEAN DEFAULT FALSE,
  point INT DEFAULT 0,
  station_id BIGINT FOREIGN KEY
)
```

#### Reservations
```sql
reservations (
  reservation_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  reservation_code VARCHAR(50) UNIQUE,
  reservation_status ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'EXPIRED', 'COMPLETED'),
  cancel_notified BOOLEAN DEFAULT FALSE,
  overdue_notified BOOLEAN DEFAULT FALSE,
  expiring_notified BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP,
  reserved_start_time TIMESTAMP,
  reserved_end_time TIMESTAMP,
  user_id BIGINT FOREIGN KEY,
  vehicle_id BIGINT FOREIGN KEY,
  station_id BIGINT FOREIGN KEY
)
```

#### Rentals
```sql
rentals (
  rental_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  rental_status ENUM('PENDING', 'ONGOING', 'COMPLETED', 'CANCELLED', 'OVERDUE'),
  start_time TIMESTAMP,
  end_time TIMESTAMP,
  cancel_notified BOOLEAN,
  overdue_notified BOOLEAN,
  expiring_notified BOOLEAN,
  rent_fee DOUBLE,
  discount_point INT DEFAULT 0,
  created_at TIMESTAMP,
  pending_end_time TIMESTAMP,
  pending_rent_fee DOUBLE,
  pre_status ENUM,
  submission_id BIGINT,
  contract_url VARCHAR(500),
  submission_url VARCHAR(500),
  contract_status ENUM('PENDING', 'SIGNED', 'REJECTED'),
  vehicle_id BIGINT FOREIGN KEY,
  reservation_id BIGINT FOREIGN KEY,
  station_id BIGINT FOREIGN KEY,
  user_id BIGINT FOREIGN KEY,
  staff_id BIGINT FOREIGN KEY
)
```

#### Payments
```sql
payments (
  payment_id BIGINT PRIMARY KEY AUTO_INCREMENT,
  total_amount DOUBLE,
  payment_method ENUM('VNPAY', 'CASH', 'BANK_TRANSFER'),
  payment_type ENUM('DEPOSIT', 'RENTAL', 'PENALTY', 'REFUND'),
  payment_status ENUM('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED'),
  created_at TIMESTAMP,
  is_delete BOOLEAN DEFAULT FALSE,
  txn_ref VARCHAR(100) UNIQUE,
  payment_description TEXT,
  response_code VARCHAR(10),
  transaction_no VARCHAR(50),
  bank_code VARCHAR(20),
  pay_date TIMESTAMP,
  user_id BIGINT FOREIGN KEY,
  rental_id BIGINT FOREIGN KEY,
  deposit_id BIGINT FOREIGN KEY
)
```

### Relationships
- **User** → **Reservations** (1:N)
- **User** → **Rentals** (1:N)
- **User** → **Payments** (1:N)
- **User** → **Documents** (1:N)
- **Vehicle** → **Reservations** (1:N)
- **Vehicle** → **Rentals** (1:N)
- **Vehicle** → **ImgVehicles** (1:N)
- **Station** → **Vehicles** (1:N)
- **Station** → **Reservations** (1:N)
- **Reservation** → **Rental** (1:1)
- **Rental** → **Deposit** (1:1)
- **Rental** → **RentalCheckList** (1:1)

---

## Email Templates

The system sends automated HTML emails for various events:

### Email Types
1. **Verification Email**: Account email verification
2. **Reservation Confirmation**: Successful reservation
3. **Reservation Expiring**: 1 hour before pickup
4. **Reservation Overdue**: Missed pickup time
5. **Reservation Cancelled**: Cancellation confirmation
6. **Contract Email**: Digital contract signing link
7. **Rental Expiring**: 1 hour before return time
8. **Rental Overdue**: Late return warning
9. **Rental Cancelled**: Rental cancellation
10. **Rental Returned**: Successful return confirmation
11. **Payment Status**: Payment confirmation/failure

### Email Service Providers
- **Primary**: SendGrid (transactional emails)
- **Fallback**: Gmail SMTP (development/testing)

---

## Scheduled Tasks

The system runs automated background jobs:

### 1. Reservation Expiration Checker
- **Frequency**: Every 5 minutes
- **Action**: Marks overdue reservations as EXPIRED

### 2. Rental Overdue Checker
- **Frequency**: Every 10 minutes
- **Action**: Marks late rentals as OVERDUE, calculates penalties

### 3. Email Reminder Scheduler
- **Frequency**: Every 30 minutes
- **Actions**:
    - Reservation expiring soon (1 hour before)
    - Rental return reminder (1 hour before)
    - Overdue notifications

### 4. Token Cleanup
- **Frequency**: Daily at 3:00 AM
- **Action**: Removes expired refresh tokens from Redis

---

## Testing

### Run Tests
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserServiceTest

# Run with coverage
mvn test jacoco:report
```

### Test Categories
- **Unit Tests**: Service layer business logic
- **Integration Tests**: Controller + Service + Repository
- **Security Tests**: Authentication & authorization
- **API Tests**: End-to-end REST API testing

---

## Deployment

### Railway Deployment (Current Setup)

The project is configured for Railway deployment:

**Railway Services**:
1. **MySQL Database** (Railway PostgreSQL/MySQL)
2. **Redis** (Railway Redis)
3. **Spring Boot App** (Railway Service)

**Environment Variables on Railway**:
Set all sensitive variables in Railway dashboard.

### Docker Deployment

**Create `Dockerfile`**:
```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/e-Motion-be-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Build and Run**:
```bash
# Build JAR
mvn clean package -DskipTests

# Build Docker image
docker build -t e-motion-be .

# Run container
docker run -p 8080:8080 \
  -e DB_URL="jdbc:mysql://host:port/db" \
  -e DB_USERNAME="user" \
  -e DB_PASSWORD="pass" \
  e-motion-be
```

### Production Checklist
- [ ] Change JWT secret key
- [ ] Update VNPay to production credentials
- [ ] Configure production database
- [ ] Set up SSL/TLS
- [ ] Configure production CORS origins
- [ ] Enable production logging
- [ ] Set up monitoring (Prometheus/Grafana)
- [ ] Configure backup strategy
- [ ] Implement rate limiting
- [ ] Set up CI/CD pipeline

---

## Security Best Practices for GitHub

### Before Pushing to Public Repository

#### 1. **Remove Sensitive Data from `application.properties`**

**Create `.env.example`**:
```properties
DB_URL=jdbc:mysql://localhost:3306/e-motion
DB_USERNAME=root
DB_PASSWORD=your_password
REDIS_HOST=localhost
REDIS_PORT=6379
JWT_SECRET_KEY=your_secret_key_here
# ... other variables
```

**Update `application.properties`** to use environment variables (see section 3️⃣ above).

#### 2. **Add `.gitignore`** entries:
```gitignore
# Sensitive files
application.properties
application-*.properties
.env
*.env

# Keystore files
*.p12
*.jks
keystore.*

# IDE
.idea/
*.iml
.vscode/

# Build
target/
*.class
*.jar
*.war

# Logs
logs/
*.log

# OS
.DS_Store
Thumbs.db
```

#### 3. **Remove Sensitive Data from Git History**

If you've already committed sensitive data:

```bash
# Install BFG Repo-Cleaner
# Download from: https://rtyley.github.io/bfg-repo-cleaner/

# Remove application.properties from history
bfg --delete-files application.properties

# Clean up
git reflog expire --expire=now --all
git gc --prune=now --aggressive

# Force push
git push origin --force --all
```

#### 4. **Rotate All Compromised Credentials**

If secrets were exposed, immediately change:
- Database passwords
- JWT secret key
- API keys (SendGrid, Cloudinary, VNPay, etc.)
- Email passwords
- All other sensitive credentials

#### 5. **Enable GitHub Secret Scanning**

GitHub automatically scans for known secret patterns. Enable this in your repository settings.

---

## Additional Resources

### Documentation
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/index.html)
- [VNPay Integration Guide](https://sandbox.vnpayment.vn/apis/docs/gioi-thieu/)
- [Cloudinary API Docs](https://cloudinary.com/documentation)
- [Gemini AI Documentation](https://ai.google.dev/docs)

### Tools
- **Postman Collection**: Import `postman_collection.json` for API testing
- **Database Client**: MySQL Workbench, DBeaver, or DataGrip
- **Redis Client**: RedisInsight or redis-cli
- **API Testing**: Swagger UI (included)

---

## Contributing

### Development Workflow
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style
- Follow Java naming conventions
- Use Lombok to reduce boilerplate
- Write meaningful commit messages
- Add JavaDoc for public methods
- Write unit tests for new features

---

## Troubleshooting

### Common Issues

#### 1. **Port 8080 already in use**
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8080 | xargs kill -9
```

#### 2. **MySQL Connection Refused**
- Verify MySQL is running: `mysql -u root -p`
- Check port 3306 is not blocked
- Verify credentials in `application.properties`

#### 3. **Redis Connection Error**
```bash
# Check Redis is running
redis-cli ping

# Start Redis
docker start e-motion-redis
```

#### 4. **JWT Token Invalid**
- Check system time is synchronized
- Verify JWT secret key matches
- Clear Redis token cache

#### 5. **Email Not Sending**
- Verify Gmail App Password (not regular password)
- Enable "Less secure app access" if using Gmail
- Check SendGrid API key is valid

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

<div align="center">

**Made with ❤️ by e-Motion Team**

⭐ Star this repository if you find it helpful!

</div>

