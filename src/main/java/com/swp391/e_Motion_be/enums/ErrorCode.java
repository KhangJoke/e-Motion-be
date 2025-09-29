package com.swp391.e_Motion_be.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // Generic errors
    UNEXPECTED_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected exception"),

    // Update password errors
    PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST, "New password and confirm new password do not match"),
    OLD_PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST, "Old password does not match"),
    CONFIRM_PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST, "Confirm password does not match"),

    // Document errors
    DOCUMENT_NUMBER_EXISTS(HttpStatus.CONFLICT, "Document number already exists"),
    DOCUMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Document not found"),

    // Vehicle errors
    VEHICLE_EXIST(HttpStatus.CONFLICT, "Vehicle already exists"),
    VEHICLE_NOT_EXIST(HttpStatus.NOT_FOUND, "Vehicle does not exist"),

    // Vehicle Log errors
    VEHICLE_LOG_NOT_EXIST(HttpStatus.NOT_FOUND, "Vehicle Log does not exist"),
    VEHICLE_ID_NOT_FOUND(HttpStatus.NOT_FOUND, "Vehicle id not found"),
    VEHICLE_LOG_LIST_EMPTY(HttpStatus.NO_CONTENT, "Vehicle log list is empty"),
    VEHICLE_LOG_TYPE_EMPTY(HttpStatus.BAD_REQUEST, "Vehicle log type is empty"),
    VEHICLE_LOG_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Vehicle log update failed"),
    VEHICLE_LOG_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Vehicle log creation failed"),

    // Img Vehicle
    IMG_VEHICLE_NOT_FOUND(HttpStatus.NOT_FOUND,"Img vehicle not found"),

    // Verify errors
    VERIFY_EXPIRED(HttpStatus.BAD_REQUEST, "Verification code expired"),
    VERIFY_CODE_NOT_MATCH(HttpStatus.BAD_REQUEST, "Verification code does not match"),
    SEND_EMAIL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Send email failed"),

    // Register errors
    USER_EXISTS(HttpStatus.CONFLICT, "User already exists"),
    PHONE_ALREADY_EXISTS(HttpStatus.CONFLICT, "Phone number already exists"),
    ACCOUNT_NOT_VERIFIED(HttpStatus.FORBIDDEN, "Account not verified, Please verify your account"),
    ACCOUNT_ALREADY_VERIFIED(HttpStatus.BAD_REQUEST, "Account already verified, Please login"),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "Invalid password"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "Email already exists"),

    // Login errors
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Token is invalid or expired. Please login again."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "Token is expired. Please login again."),
    SIGNATURE_NOT_MATCH(HttpStatus.UNAUTHORIZED, "Signature not match"),
    EXTRACT_USERNAME_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Extract username from token failed"),
    NOT_LOGIN_YET(HttpStatus.UNAUTHORIZED, "You are not logged in. Please login to continue."),
    USER_NOT_EXISTS(HttpStatus.NOT_FOUND, "User does not exist"),

    // Check image document errors
    DOCUMENT_NUMBER_MISMATCH(HttpStatus.BAD_REQUEST, "Document number mismatch with image"),
    DOCUMENT_IMAGE_USED(HttpStatus.CONFLICT, "This image has already been used for another document"),
    CREATE_DOCUMENT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create document"),
    NOT_FOUND_CCCD_IN_IMAGE(HttpStatus.BAD_REQUEST, "Could not find valid CCCD number in the provided image"),
    UPLOAD_IMAGE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image"),
    DELETE_IMAGE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete image"),
    FAIL_OCR(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to perform OCR on the image"),

    // OCR service errors
    NOT_FOUND_FOLDER_DATASET(HttpStatus.NOT_FOUND, "Dataset folder not found"),
    FAIL_COPY_DATASET(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to copy dataset folder"),
    CREATE_FOLDER_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create folder"),

    // Station errors
    STATION_NOT_FOUND(HttpStatus.NOT_FOUND, "Station not found"),
    STATION_NAME_EXISTS(HttpStatus.CONFLICT, "Station name already exists"),

    // Staff errors
    STAFF_NOT_FOUND(HttpStatus.NOT_FOUND, "Staff not found"),
    USER_ALREADY_ASSIGNED_AS_STAFF(HttpStatus.CONFLICT, "User is already assigned as staff"),

    // Reservation errors
    RESERVATION_ENDTIME_INVALID(HttpStatus.BAD_REQUEST, "Reservation end time must be in the future"),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "Reservation not found"),
    RESERVATION_TIME_INVALID(HttpStatus.BAD_REQUEST, "Reservation time must be in the future"),

    // Deposit errors
    DEPOSIT_NOT_FOUND(HttpStatus.NOT_FOUND, "Deposit not found"),

    // Refresh token errors
    SENDED_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "The sent refresh token was not found"),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "Refresh token not found");

    private final HttpStatus statusCode;
    private final String message;
}

