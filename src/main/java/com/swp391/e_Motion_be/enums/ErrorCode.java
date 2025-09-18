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
    PASSWORD_NOT_MATCH(2002, "New password and confirm new password do not match");
    private final int code;
    private final String message;
}
