package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.requests.payment.CreatePaymentUrlRequest;
import com.swp391.e_Motion_be.dto.requests.payment.PayOSWebhookRequest;
import com.swp391.e_Motion_be.dto.requests.payment.PaymentRequest;
import com.swp391.e_Motion_be.dto.requests.payment.UpdatePaymentRequest;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.PayOSResponse;
import com.swp391.e_Motion_be.dto.responses.PaymentResponse;
import com.swp391.e_Motion_be.dto.responses.TransactionResponse;
import com.swp391.e_Motion_be.dto.responses.VnpayResponse;
import com.swp391.e_Motion_be.enums.payment.PaymentMethod;
import com.swp391.e_Motion_be.enums.payment.PaymentStatus;
import com.swp391.e_Motion_be.enums.payment.PaymentType;
import com.swp391.e_Motion_be.service.PayOSService;
import com.swp391.e_Motion_be.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    private final PaymentService paymentService;
    private final PayOSService payOSService;

    public PaymentController(PaymentService paymentService, PayOSService payOSService) {
        this.paymentService = paymentService;
        this.payOSService = payOSService;
    }

    @PostMapping("/vnpay")
    @PreAuthorize("permitAll()")
    public ApiResponse<VnpayResponse> createPaymentUrl(HttpServletRequest request,
                                                       @RequestBody @Valid CreatePaymentUrlRequest input) throws Exception
    {
        String ipAddr = request.getRemoteAddr();
        ApiResponse<VnpayResponse> response = new ApiResponse<>();
        response.setMessage("Create VnPay Url Successfully");
        response.setStatus(201);
        VnpayResponse data = paymentService.createPaymentUrl(input, ipAddr);
        response.setData(data);
        return response;
    }

    @GetMapping("/vnpay-return")
    @PreAuthorize("permitAll()")
    public void paymentVnPayReturn(@RequestParam Map<String, String> params, HttpServletResponse response) throws Exception {
        PaymentResponse paymentResponse = paymentService.handleReturn(params);
        String redirectUrl;
        if (paymentResponse.getStatus().equalsIgnoreCase(PaymentStatus.FAILED.toString())) {
//            redirectUrl = "http://localhost:5173/payments/payment-result?status=failed&type="+ paymentResponse.getType();
            redirectUrl = "https://e-motion-fe.vercel.app/payments/payment-result?status=failed&type="+ paymentResponse.getType();
        } else {
//            redirectUrl = "http://localhost:5173/payments/payment-result?status=success&txnRef=" + paymentResponse.getTxnRef() +"&type="+ paymentResponse.getType();
            redirectUrl = "https://e-motion-fe.vercel.app/payments/payment-result?status=success&txnRef=" + paymentResponse.getTxnRef() +"&type="+ paymentResponse.getType();
        }
        response.sendRedirect(redirectUrl);
    }

    @PostMapping("/payos/create-link/{reservationId}")
    @PreAuthorize("permitAll()")
    public ApiResponse<PayOSResponse> createPayOSPaymentLink(@PathVariable Long reservationId) {
        ApiResponse<PayOSResponse> response = new ApiResponse<>();
        response.setMessage("Create PayOS payment link successfully");
        response.setStatus(200);
        response.setData(payOSService.createPaymentLink(reservationId));
        return response;
    }

    @PostMapping("/payos/webhook")
    @PreAuthorize("permitAll()")
    public ApiResponse<PaymentResponse> payosWebhook(@RequestBody PayOSWebhookRequest request) {
        ApiResponse<PaymentResponse> response = new ApiResponse<>();
        response.setMessage("Process PayOS webhook successfully");
        response.setStatus(200);
        response.setData(payOSService.handlePayOSWebhook(request));
        return response;
    }

    @PostMapping("/payos/confirm/{reservationCode}")
    @PreAuthorize("permitAll()")
    public ApiResponse<PaymentResponse> confirmPayOSPayment(@PathVariable String reservationCode) {
        ApiResponse<PaymentResponse> response = new ApiResponse<>();
        response.setMessage("Confirm PayOS reservation payment successfully");
        response.setStatus(200);
        response.setData(payOSService.checkAndConfirmPayment(reservationCode));
        return response;
    }

    @PostMapping("/payos/check/{reservationCode}")
    @PreAuthorize("permitAll()")
    public ApiResponse<PaymentResponse> checkPayOSPayment(@PathVariable String reservationCode) {
        ApiResponse<PaymentResponse> response = new ApiResponse<>();
        response.setMessage("Check PayOS reservation payment successfully");
        response.setStatus(200);
        response.setData(payOSService.checkAndConfirmPayment(reservationCode));
        return response;
    }

    @PostMapping()
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ApiResponse<PaymentResponse> createPayment(@RequestBody @Valid PaymentRequest request){
        ApiResponse<PaymentResponse> response = new ApiResponse<>();
        response.setMessage("Create Payment Successfully");
        response.setStatus(201);
        response.setData(paymentService.createPayment(request));
        return response;
    }

    @GetMapping()
    @PreAuthorize("hasAnyRole()('ADMIN', 'STAFF')")
    public ApiResponse<List<PaymentResponse>> getAllPayment(){
        ApiResponse<List<PaymentResponse>> response = new ApiResponse<>();
        response.setMessage("Get All Payments Successfully");
        response.setStatus(201);
        response.setData(paymentService.getAllPayment());
        return response;
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> deletePayment(@PathVariable @Valid Long id){
        paymentService.deletePayment(id);
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Delete Payment By " + id + " Successfully");
        return response;
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<PaymentResponse> updatePayment(@PathVariable @Valid Long id, @RequestBody UpdatePaymentRequest request){
        ApiResponse<PaymentResponse> response = new ApiResponse<>();
        response.setMessage("Update Payment Successfully");
        response.setData(paymentService.updatePayment(request, id));
        return response;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<PaymentResponse> getPaymentById(@PathVariable @Valid Long id){
        ApiResponse<PaymentResponse> response = new ApiResponse<>();
        response.setMessage("Get Payment By " + id + " Successfully");
        response.setStatus(200);
        response.setData(paymentService.getPaymentById(id));
        return response;
    }

    @GetMapping("/vnpay/{txnRef}")
    @PreAuthorize("permitAll()")
    public ApiResponse<PaymentResponse> getPaymentById(@PathVariable @Valid String txnRef){
        ApiResponse<PaymentResponse> response = new ApiResponse<>();
        response.setMessage("Get Payment By " + txnRef + " Successfully");
        response.setStatus(200);
        response.setData(paymentService.findByTxnRef(txnRef));
        return response;
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<PaymentResponse>> getPaymentsByStatus(@PathVariable PaymentStatus status){
        ApiResponse<List<PaymentResponse>> response = new ApiResponse<>();
        response.setMessage("Get Payment By " + status + " Successfully");
        response.setStatus(200);
        response.setData(paymentService.getPaymentsByStatus(status));
        return response;
    }

    @GetMapping("/method/{method}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<PaymentResponse>> getPaymentsByMethod(@PathVariable PaymentMethod method){
        ApiResponse<List<PaymentResponse>> response = new ApiResponse<>();
        response.setMessage("Get Payment By " + method + " Successfully");
        response.setStatus(200);
        response.setData(paymentService.getPaymentsByMethod(method));
        return response;
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<PaymentResponse>> getPaymentsByType(@PathVariable PaymentType type){
        ApiResponse<List<PaymentResponse>> response = new ApiResponse<>();
        response.setMessage("Get Payment By " + type + " Successfully");
        response.setStatus(200);
        response.setData(paymentService.getPaymentsByType(type));
        return response;
    }

    @PostMapping("/query/{txnRef}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<TransactionResponse> queryTransaction(@PathVariable String txnRef,HttpServletRequest request) throws Exception {
        ApiResponse<TransactionResponse>response = new ApiResponse<>();
        response.setMessage("Query Transaction Successfully");
        response.setStatus(200);
        response.setData(paymentService.queryTransaction(txnRef, request));
        return response;
    }

    @GetMapping("/rental/{rentalId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<PaymentResponse> getPaymentByRentalId(@PathVariable Long rentalId){
        ApiResponse<PaymentResponse> response = new ApiResponse<>();
        response.setMessage("Get Payment By Rental Id " + rentalId + " Successfully");
        response.setStatus(200);
        response.setData(paymentService.getPaymentByRentalId(rentalId));
        return response;
    }
}
