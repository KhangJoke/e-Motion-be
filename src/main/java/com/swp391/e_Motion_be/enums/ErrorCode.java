package com.swp391.e_Motion_be.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // Generic errors
    UNEXPECTED_EXCEPTION(9999, "Unexpected exception"),

    // Update password errors
    PASSWORD_NOT_MATCH(2002, "New password and confirm new password do not match"),
    OLD_PASSWORD_NOT_MATCH(2007, "Old password does not match"),
    CONFIRM_PASSWORD_NOT_MATCH(2008, "Confirm password does not match"),

    // Document errors
    DOCUMENT_NUMBER_EXISTS(1001, "Document number already exists"),
    DOCUMENT_NOT_FOUND(1002, "Document not found"),

    // Vehicle errors
    VEHICLE_EXIST(2013,"Vehicle is already exist"),
    VEHICLE_NOT_EXIST(2014,"Vehicle does not exist"),

    // Verify errors
    VERIFY_EXPIRED(2004, "Verification code expired"),
    VERIFY_CODE_NOT_MATCH(2005, "Verification code does not match"),
    SEND_EMAIL_FAILED(3001, "Send email failed"),

    // Register errors
    USER_NOT_EXISTS(2001, "User does not exist"),
    ACCOUNT_NOT_VERIFIED(2003, "Account not verified, Please verify your account"),
    ACCOUNT_ALREADY_VERIFIED(2006, "Account already verified, Please login"),
    INVALID_PASSWORD(2009, "Invalid password"),

    //Login errors
    TOKEN_EXPIRED(2010, "Token has been expired. Please login again."),
    INVALID_TOKEN(2011, "Invalid token. Please login again."),
    NOT_LOGIN_YET(2012, "You are not logged in. Please login to continue."),

    // Check image document errors
    DOCUMENT_NUMBER_MISMATCH(3001, "Document number does not match the one extracted from the image"),
    DOCUMENT_IMAGE_USED(3002, "This image has already been used for another document"),
    CREATE_DOCUMENT_FAILED(3002, "Failed to create document"),
    NOT_FOUND_CCCD_IN_IMAGE(3003, "Could not find a valid CCCD number in the provided image"),
    UPLOAD_IMAGE_FAILED(3004, "Failed to upload image"),
    DELETE_IMAGE_FAILED(3005, "Failed to delete image"),
    FAIL_OCR(3006, "Failed to perform OCR on the image"),

    // OCR service errors
    NOT_FOUND_FOLDER_DATASET(4001, "Could not find dataset folder"),
    FAIL_COPY_DATASET(4002, "Failed to copy dataset folder"),
    CREATE_FOLDER_FAILED(4003, "Failed to create folder"),

    // Station errors
    STATION_NOT_FOUND(4001, "Station not found"),
    STATION_NAME_EXISTS(4002, "Station name already exists"),
    STAFF_NOT_FOUND(5001, "Staff not found");

    private final int code;
    private final String message;
}
