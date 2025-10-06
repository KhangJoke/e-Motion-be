package com.swp391.e_Motion_be.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // Generic errors
    UNEXPECTED_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected exception"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized"),

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
    VEHICLE_NOT_AVAILABLE(HttpStatus.NOT_FOUND, "Vehicle is not available."),
    VEHICLE_NOT_READY(HttpStatus.CONFLICT, "Vehicle is not ready to use"),
    VEHICLE_STATUS_INVALID(HttpStatus.CONFLICT, "Vehicle status is invalid"),

    // Vehicle Log errors
    VEHICLE_LOG_NOT_EXIST(HttpStatus.NOT_FOUND, "Vehicle Log does not exist"),
    VEHICLE_ID_NOT_FOUND(HttpStatus.NOT_FOUND, "Vehicle id not found"),
    VEHICLE_LOG_LIST_EMPTY(HttpStatus.NO_CONTENT, "Vehicle log list is empty"),
    VEHICLE_LOG_TYPE_EMPTY(HttpStatus.BAD_REQUEST, "Vehicle log type is empty"),
    VEHICLE_LOG_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Vehicle log update failed"),
    VEHICLE_LOG_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Vehicle log creation failed"),

    // Img Vehicle
    IMG_VEHICLE_NOT_FOUND(HttpStatus.NOT_FOUND,"Img vehicle not found"),

    //Rating
    RATING_ID_NOT_FOUND(HttpStatus.NOT_FOUND, "Rating id not found"),

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

    // Logout errors
    USER_HAS_BEEN_LOGOUT(HttpStatus.UNAUTHORIZED, "Your Account has been logout. Please login to continue."),

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
    STATION_CITY_INVALID(HttpStatus.BAD_REQUEST, "Station city invalid"),

    // Staff errors
    STAFF_NOT_FOUND(HttpStatus.NOT_FOUND, "Staff not found"),
    USER_ALREADY_ASSIGNED_AS_STAFF(HttpStatus.CONFLICT, "User is already assigned as staff"),
    USER_NOT_A_STAFF(HttpStatus.BAD_REQUEST, "User is not assigned as staff"),

    // Reservation errors
    RESERVATION_ENDTIME_INVALID(HttpStatus.BAD_REQUEST, "Reservation end time must be in the future"),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "Reservation not found"),
    RESERVATION_TIME_INVALID(HttpStatus.BAD_REQUEST, "Reservation time must be in the future"),
    RESERVATION_TIME_INVALID_TO_CANCEL(HttpStatus.BAD_REQUEST, "You may cancel your reservation up to 5 days before your trip"),
    RESERVATION_ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "Reservation has already been cancelled"),
    VEHICLE_STATION_MISMATCH(HttpStatus.BAD_REQUEST, "Vehicle does not belong to the selected station"),

    // Deposit errors
    DEPOSIT_NOT_FOUND(HttpStatus.NOT_FOUND, "Deposit not found"),

    // Refresh token errors
    SENDED_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "The sent refresh token was not found"),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "Refresh token not found"),

    // RentalCheckList errors
    CHECKLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "Rental checklist not found"),
    CHECKLIST_UNAUTHORIZED(HttpStatus.FORBIDDEN, "You are not authorized to modify this checklist"),

    // Rental errors
    RENTAL_NOT_FOUND(HttpStatus.NOT_FOUND, "Rental not found"),
    RENTAL_HAS_CONFLICT(HttpStatus.CONFLICT, "This vehicle has rental in this range time, please choose other time"),
    INVALID_RENTAL_STATUS(HttpStatus.BAD_REQUEST, "Invalid Rental Status"),
    USER_HAS_ONGOING_RENTAL(HttpStatus.CONFLICT, "User already rental vehicle"),
    USER_NEED_HAS_CCCD(HttpStatus.BAD_REQUEST, "Renter need to has CCCD"),
    USER_NEED_HAS_LICENSE(HttpStatus.BAD_REQUEST, "Renter need to has LICENSE"),

    // Payment errors
    PAYMENT_NOT_EXISTS(HttpStatus.NOT_FOUND, "The payment was not found"),
    DEPOSIT_PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "The deposit payment was not found"),
    REFUND_RESPONSE_NOT_FOUND(HttpStatus.NOT_FOUND, "The refund response was not found"),
    REFUND_RESPONSE_INVALID(HttpStatus.NOT_FOUND, "The refund response was invalid"),
    VNPAY_KEY_INVALID(HttpStatus.BAD_REQUEST, "The VnPay Key was invalid"),
    REFUND_FAILED(HttpStatus.BAD_REQUEST, "The VnPay Refund Failed"),
    PAYMENT_CANNOT_BE_REFUNDED(HttpStatus.BAD_REQUEST, "The VnPay cannot be refunded"),
    REFUND_ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "The refund has already been processed"),
    QUERY_RESPONSE_NOT_FOUND(HttpStatus.NOT_FOUND, "The query response was not found"),
    QUERY_FAILED(HttpStatus.BAD_REQUEST, "The VnPay Query Failed"),
    PAYMENT_PROCESSING_FAILED(HttpStatus.BAD_REQUEST, "The payment is processing failed"),
    RENTAL_CANNOT_BE_PAID(HttpStatus.BAD_REQUEST, "The rental cannot be paid"),
    DEPOSIT_CANNOT_BE_PAID(HttpStatus.BAD_REQUEST, "The deposit cannot be paid"),

    //Cloudinary errors
    DELETE_IMG_FAIL(HttpStatus.EXPECTATION_FAILED, "Delete image failed"),

    //Check in errors
    ALREADY_CHECKED_IN(HttpStatus.BAD_REQUEST, "Vehicle has already been checked in for this rental"),

    //Reservation email
    RESERVATION_EMAIL(HttpStatus.EXPECTATION_FAILED, "Fail sending email");

    private final HttpStatus statusCode;
    private final String message;
}

