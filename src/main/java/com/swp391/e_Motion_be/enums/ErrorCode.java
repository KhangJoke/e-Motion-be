package com.swp391.e_Motion_be.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized exception"),
    DOCUMENT_NUMBER_EXISTS(1001, "Document number already exists"),
    DOCUMENT_NOT_FOUND(1002, "Document not found"),
    USER_NOT_EXISTS(2001, "User does not exist"),
    PASSWORD_NOT_MATCH(2002, "New password and confirm new password do not match"),
    ACCOUNT_NOT_VERIFIED(2003, "Account not verified, Please verify your account"),
    VERIFY_EXPIRED(2004, "Verification code expired"),
    VERIFY_CODE_NOT_MATCH(2005, "Verification code does not match"),
    ACCOUNT_ALREADY_VERIFIED(2006, "Account already verified, Please login"),
    SEND_EMAIL_FAILED(3001, "Send email failed"),
    OLD_PASSWORD_NOT_MATCH(2007, "Old password does not match"),
    CONFIRM_PASSWORD_NOT_MATCH(2008, "Confirm password does not match"),
    INVALID_PASSWORD(2009, "Invalid password"),
    TOKEN_EXPIRED(2010, "Token has been expired. Please login again."),
    INVALID_TOKEN(2011, "Invalid token. Please login again."),
    NOT_LOGIN_YET(2012, "You are not logged in. Please login to continue."),
    VEHICLE_EXIST(2013,"Vehicle is already exist"),
    VEHICLE_NOT_EXIST(2014,"Vehicle does not exist"),
    // Check image document errors
    DOCUMENT_NUMBER_MISMATCH(3001, "Document number does not match the one extracted from the image"),
    DOCUMENT_IMAGE_USED(3002, "This image has already been used for another document"),
    CREATE_DOCUMENT_FAILED(3002, "Failed to create document"),
    NOT_FOUND_CCCD_IN_IMAGE(3003, "Could not find a valid CCCD number in the provided image"),
    UPLOAD_IMAGE_FAILED(3004, "Failed to upload image"),
    DELETE_IMAGE_FAILED(3005, "Failed to delete image"),
    FAIL_OCR(3006, "Failed to perform OCR on the image");

    private final int code;
    private final String message;
}
