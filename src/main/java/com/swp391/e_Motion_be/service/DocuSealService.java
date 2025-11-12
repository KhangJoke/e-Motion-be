package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.entity.Rental;
import com.swp391.e_Motion_be.enums.ContractStatus;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.enums.RentalStatus;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DocuSealService {
    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${docuseal.api-key}")
    private String apiKey;
    @Value("${docuseal.template-id}")
    private String templateId;
    @Value("${docuseal.url}")
    private String url;
    @Autowired
    private RentalRepository rentalRepository;

    public String createContract(Rental rental) {

        Map<String, Object> renter = Map.of(
                "email", rental.getUser().getEmail(),
                "name", rental.getUser().getFullName(),
                "role", "Renter",
                "external_id", rental.getId().toString(),
                "send_email", true,
                "send_sms", false,
                "fields", List.of(
                        Map.of("name", "day", "default_value", LocalDate.now().getDayOfMonth(), "readonly", true),
                        Map.of("name", "month", "default_value", LocalDate.now().getMonthValue(), "readonly", true),
                        Map.of("name", "year", "default_value", LocalDate.now().getYear(), "readonly", true),
                        Map.of("name", "at", "default_value", rental.getStation().getName(), "readonly", true),
                        Map.of("name", "renterName", "default_value", rental.getUser().getFullName(), "readonly", true),
                        Map.of("name", "carName", "default_value", rental.getVehicle().getName(), "readonly", true),
                        Map.of("name", "carType", "default_value", rental.getVehicle().getCategory(), "readonly", true),
                        Map.of("name", "numberOfSeat", "default_value", rental.getVehicle().getSeats(), "readonly", true),
                        Map.of("name", "carBrand", "default_value", rental.getVehicle().getBrand(), "readonly", true),
                        Map.of("name", "plateNumber", "default_value", rental.getVehicle().getPlateNumber(), "readonly", true),
                        Map.of("name", "rentLength", "default_value", rental.getStartTime().getHour()-rental.getEndTime().getHour(), "readonly", true),
                        Map.of("name", "rentFee", "default_value", String.valueOf(rental.getRentFee()), "readonly", true),
                        Map.of("name", "paymentMethod", "default_value", "VNPay", "readonly", true),
                        Map.of("name", "payDate", "default_value", LocalDateTime.now(), "readonly", true)
                )
        );

        Map<String, Object> payload = Map.of(
                "template_id", templateId,
                "submitters", List.of(renter),
                "send_email", true,
                "send_sms", false,
                "order", "preserved",
                "message", Map.of(
                        "subject", "Ký hợp đồng thuê xe",
                        "body", "Xin chào, vui lòng ký hợp đồng thuê xe tại link dưới đây."
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Auth-Token", apiKey);

        ParameterizedTypeReference<List<Map<String, Object>>> responseType = new ParameterizedTypeReference<>() {};
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                "https://api.docuseal.com/submissions",
                HttpMethod.POST,
                entity,
                responseType
        );
        String contractUrl = (String) response.getBody().get(0).get("embed_src");
        rental.setContractStatus(ContractStatus.PENDING);
        rentalRepository.save(rental);
        return contractUrl;
    }


    public void handleWebhook(Map<String, Object> payload) {
        String eventType = (String) payload.get("event_type");
        if ("form.completed".equals(eventType)) {
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            String status = (String) data.get("status");
            String email = (String) data.get("email");
            String contractUrl = (String) data.get("submission_url");

            if ("completed".equals(status)) {
                Rental rental = rentalRepository.findTopByUserEmailOrderByCreatedAtDesc(email)
                        .orElseThrow(() -> new AppException(ErrorCode.RENTAL_NOT_FOUND));
                List<Map<String, Object>> documents = (List<Map<String, Object>>) data.get("documents");
                String documentUrl = null;
                if (documents != null && !documents.isEmpty()) {
                    documentUrl = (String) documents.get(0).get("url");
                }
                rental.setStatus(RentalStatus.CONFIRM);
                rental.setContractStatus(ContractStatus.SIGNED);
                rental.setContractDocumentUrl(documentUrl);
                rental.setContractUrl(contractUrl);
                rentalRepository.save(rental);
            }
        }else if("form.declined".equalsIgnoreCase(eventType)) {
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            String email = (String) data.get("email");

            Rental rental = rentalRepository.findTopByUserEmailOrderByCreatedAtDesc(email)
                    .orElseThrow(() -> new AppException(ErrorCode.RENTAL_NOT_FOUND));
            rental.setContractStatus(ContractStatus.DECLINED);
            rentalRepository.save(rental);
        }
    }
}
