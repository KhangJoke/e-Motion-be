package com.swp391.e_Motion_be.dto.responses;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data

public class TransactionResponse {
    @SerializedName("vnp_ResponseId")
    private String responseId;

    @SerializedName("vnp_Command")
    private String command;

    @SerializedName("vnp_TmnCode")
    private String tmnCode;

    @SerializedName("vnp_TxnRef")
    private String txnRef;

    @SerializedName("vnp_Amount")
    private String amount;

    @SerializedName("vnp_OrderInfo")
    private String orderInfo;

    @SerializedName("vnp_ResponseCode")
    private String responseCode;

    @SerializedName("vnp_Message")
    private String message;

    @SerializedName("vnp_BankCode")
    private String bankCode;

    @SerializedName("vnp_PayDate")
    private String payDate;

    @SerializedName("vnp_TransactionNo")
    private String transactionNo;

    @SerializedName("vnp_TransactionType")
    private String transactionType;

    @SerializedName("vnp_TransactionStatus")
    private String transactionStatus;

    @SerializedName("vnp_SecureHash")
    private String secureHash;

    // getter/setter...
}


