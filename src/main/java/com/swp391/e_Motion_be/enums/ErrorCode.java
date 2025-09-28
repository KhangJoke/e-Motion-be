package com.swp391.e_Motion_be.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // Generic errors
    UNEXPECTED_EXCEPTION(9999, "Unexpected exception"),

    // Update password errors
    PASSWORD_NOT_MATCH(421, "New password and confirm new password do not match"),
    OLD_PASSWORD_NOT_MATCH(422, "Old password does not match"),
    CONFIRM_PASSWORD_NOT_MATCH(423, "Confirm password does not match"),

    // Document errors
    DOCUMENT_NUMBER_EXISTS(424, "Document number already exists"),
    DOCUMENT_NOT_FOUND(425, "Document not found"),

    // Vehicle errors
    VEHICLE_EXIST(431,"Vehicle is already exist"),
    VEHICLE_NOT_EXIST(432,"Vehicle does not exist"),

    // Vehicle Log erros
    VEHICLE_LOG_NOT_EXIST(433,"Vehicle Log does not exist"),
    VEHICLE_ID_NOT_FOUND(434,"Vehicle id is not found"),
    VEHICLE_LOG_LIST_EMPTY(435,"Vehicle log list is empty"),
    VEHICLE_LOG_TYPE_EMPTY(436,"Vehicle log type is empty"),
    VEHICLE_LOG_UPDATE_FAILED(437,"Vehicle log update failed"),
    VEHICLE_LOG_CREATION_FAILED(438,"Vehicle log creation failed"),

    // Img Vehicle
    IMG_VEHICLE_NOT_FOUND(439,"Img vehicle not found"),

    // Verify errors
    VERIFY_EXPIRED(440, "Verification code expired"),
    VERIFY_CODE_NOT_MATCH(441, "Verification code does not match"),
    SEND_EMAIL_FAILED(442, "Send email failed"),

    // Register errors
    USER_EXISTS(450, "User already exists"),
    PHONE_ALREADY_EXISTS(451, "Phone number already exists"),
    ACCOUNT_NOT_VERIFIED(452, "Account not verified, Please verify your account"),
    ACCOUNT_ALREADY_VERIFIED(453, "Account already verified, Please login"),
    INVALID_PASSWORD(454, "Invalid password"),
    EMAIL_ALREADY_EXISTS(455, "Email already exists"),

    //Login errors
    TOKEN_EXPIRED(456, "Token has been expired. Please login again."),
    INVALID_TOKEN(457, "Invalid token. Please login again."),
    NOT_LOGIN_YET(458, "You are not logged in. Please login to continue."),
    USER_NOT_EXISTS(459, "User does not exist"),

    // Check image document errors
    DOCUMENT_NUMBER_MISMATCH(460, "Document number does not match the one extracted from the image"),
    DOCUMENT_IMAGE_USED(461, "This image has already been used for another document"),
    CREATE_DOCUMENT_FAILED(462, "Failed to create document"),
    NOT_FOUND_CCCD_IN_IMAGE(463, "Could not find a valid CCCD number in the provided image"),
    UPLOAD_IMAGE_FAILED(464, "Failed to upload image"),
    DELETE_IMAGE_FAILED(465, "Failed to delete image"),
    FAIL_OCR(466, "Failed to perform OCR on the image"),

    // OCR service errors
    NOT_FOUND_FOLDER_DATASET(467, "Could not find dataset folder"),
    FAIL_COPY_DATASET(468, "Failed to copy dataset folder"),
    CREATE_FOLDER_FAILED(469, "Failed to create folder"),

    // Station errors
    STATION_NOT_FOUND(470, "Station not found"),
    STATION_NAME_EXISTS(471, "Station name already exists"),

    // Staff errors
    STAFF_NOT_FOUND(475, "Staff not found"),
    USER_ALREADY_ASSIGNED_AS_STAFF(476, "User is already assigned as staff"),

    // Reservation errors
    RESERVATION_ENDTIME_INVALID(480, "Reservation end time must be in the future"),
    RESERVATION_NOT_FOUND(481, "Reservation not found"),
    RESERVATION_TIME_INVALID(482, "Reservation time must be in the future"),

    // Deposit errors
    DEPOSIT_NOT_FOUND(485, "Deposit not found"),

    //Refresh token errors
    SENDED_TOKEN_NOT_FOUND (490, "The sent refresh token was not found"),
    REFRESH_TOKEN_NOT_FOUND (491, "Refresh token not found");

    private final int code;
    private final String message;
}
