package com.swp391.e_Motion_be.controller;

import com.swp391.e_Motion_be.dto.requests.payment.CreatePaymentUrlRequest;
import com.swp391.e_Motion_be.dto.requests.payment.PaymentRequest;
import com.swp391.e_Motion_be.dto.responses.ApiResponse;
import com.swp391.e_Motion_be.dto.responses.PaymentResponse;
import com.swp391.e_Motion_be.enums.PaymentMethod;
import com.swp391.e_Motion_be.enums.PaymentStatus;
import com.swp391.e_Motion_be.enums.PaymentType;
import com.swp391.e_Motion_be.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/vnpay")
    public ApiResponse<String> createPaymentUrl(HttpServletRequest request,
                                        @RequestBody @Valid CreatePaymentUrlRequest input) throws Exception
    {
        String ipAddr = request.getRemoteAddr();
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Create VnPay Url Successfully");
        response.setStatus(201);
        response.setData(paymentService.createPaymentUrl(input, ipAddr));
        return response;
    }

    @GetMapping("/vnpay-return")
    public String paymentVnPayReturn(HttpServletRequest request) {
        return paymentService.handleReturn(request);
    }

    @PostMapping()
    public ApiResponse<PaymentResponse> createPayment(@RequestBody @Valid PaymentRequest request){
        ApiResponse<PaymentResponse> response = new ApiResponse<>();
        response.setMessage("Create VnPay Url Successfully");
        response.setStatus(201);
        response.setData(paymentService.createPayment(request));
        return response;
    }

    @GetMapping()
    public ApiResponse<List<PaymentResponse>> getAllPayment(){
        ApiResponse<List<PaymentResponse>> response = new ApiResponse<>();
        response.setMessage("Create VnPay Url Successfully");
        response.setStatus(201);
        response.setData(paymentService.getAllPayment());
        return response;
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<String> deletePayment(@PathVariable @Valid Long id){
        paymentService.deletePayment(id);
        ApiResponse<String> response = new ApiResponse<>();
        response.setMessage("Delete Payment By " + id + " Successfully");
        return response;
    }

    @PutMapping("/update/{id}")
    public ApiResponse<PaymentResponse> updatePayment(@PathVariable @Valid Long id, @RequestBody PaymentRequest request){
        ApiResponse<PaymentResponse> response = new ApiResponse<>();
        response.setMessage("Update Payment Successfully");
        response.setData(paymentService.updatePayment(request, id));
        return response;
    }

    @GetMapping("/{id}")
    public ApiResponse<PaymentResponse> getPaymentById(@PathVariable @Valid Long id){
        ApiResponse<PaymentResponse> response = new ApiResponse<>();
        response.setMessage("Get Payment By " + id + " Successfully");
        response.setStatus(200);
        response.setData(paymentService.getPaymentById(id));
        return response;
    }

    @GetMapping("/vnpay/{txnRef}")
    public ApiResponse<PaymentResponse> getPaymentById(@PathVariable @Valid String txnRef){
        ApiResponse<PaymentResponse> response = new ApiResponse<>();
        response.setMessage("Get Payment By " + txnRef + " Successfully");
        response.setStatus(200);
        response.setData(paymentService.findByTxnRef(txnRef));
        return response;
    }

    @GetMapping("/status/{status}")
    public ApiResponse<List<PaymentResponse>> getPaymentsByStatus(@PathVariable PaymentStatus status){
        ApiResponse<List<PaymentResponse>> response = new ApiResponse<>();
        response.setMessage("Get Payment By " + status + " Successfully");
        response.setStatus(200);
        response.setData(paymentService.getPaymentsByStatus(status));
        return response;
    }

    @GetMapping("/method/{method}")
    public ApiResponse<List<PaymentResponse>> getPaymentsByMethod(@PathVariable PaymentMethod method){
        ApiResponse<List<PaymentResponse>> response = new ApiResponse<>();
        response.setMessage("Get Payment By " + method + " Successfully");
        response.setStatus(200);
        response.setData(paymentService.getPaymentsByMethod(method));
        return response;
    }

    @GetMapping("/type/{type}")
    public ApiResponse<List<PaymentResponse>> getPaymentsByType(@PathVariable PaymentType type){
        ApiResponse<List<PaymentResponse>> response = new ApiResponse<>();
        response.setMessage("Get Payment By " + type + " Successfully");
        response.setStatus(200);
        response.setData(paymentService.getPaymentsByType(type));
        return response;
    }
}
