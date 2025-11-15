package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.entity.Rental;
import com.swp391.e_Motion_be.entity.User;
import com.swp391.e_Motion_be.enums.*;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.repository.DocumentRepository;
import com.swp391.e_Motion_be.repository.RentalRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
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
    @Autowired
    private EmailService emailService;
    @Autowired
    private DocumentRepository documentRepository;

    @Transactional
    public String createContract(Long id) {

        Rental rental = rentalRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RENTAL_NOT_FOUND));

        if(!rental.getStatus().equals(RentalStatus.PENDING)){
            throw new AppException(ErrorCode.INVALID_RENTAL_STATUS);
        }

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
                        Map.of("name", "renterName", "default_value", rental.getUser().getFullName(), "readonly", false),
                        Map.of("name", "carName", "default_value", rental.getVehicle().getName(), "readonly", true),
                        Map.of("name", "carType", "default_value", rental.getVehicle().getCategory(), "readonly", true),
                        Map.of("name", "numberOfSeat", "default_value", rental.getVehicle().getSeats(), "readonly", true),
                        Map.of("name", "carBrand", "default_value", rental.getVehicle().getBrand(), "readonly", true),
                        Map.of("name", "plateNumber", "default_value", rental.getVehicle().getPlateNumber(), "readonly", true),
                        Map.of("name", "rentLength", "default_value", rental.getStartTime().getHour()-rental.getEndTime().getHour(), "readonly", true),
                        Map.of("name", "rentFee", "default_value", String.valueOf(rental.getRentFee()), "readonly", true),
                        Map.of("name", "paymentMethod", "default_value", "VNPay", "readonly", true),
                        Map.of("name", "payDate", "default_value", LocalDateTime.now(), "readonly", true),
                        Map.of("name", "CCCD", "default_value", documentRepository.findByUser_EmailAndType(rental.getUser().getEmail(), DocumentType.CCCD).getNumber(), "readonly", false),
                        Map.of("name", "GPLX", "default_value", documentRepository.findByUser_EmailAndType(rental.getUser().getEmail(), DocumentType.LICENSE).getNumber(), "readonly", false)
                )
        );

        Map<String, Object> payload = Map.of(
                "template_id", templateId,
                "submitters", List.of(renter),
                "send_email", false,
                "send_sms", false,
                "order", "preserved",
                "message", Map.of()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Auth-Token", apiKey);

        ParameterizedTypeReference<List<Map<String, Object>>> responseType = new ParameterizedTypeReference<>() {};
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
        ResponseEntity<List<Map<String, Object>>> response;
        try{
            response = restTemplate.exchange(
                    "https://api.docuseal.com/submissions",
                    HttpMethod.POST,
                    entity,
                    responseType
            );
        }catch (Exception e){
            throw new AppException(ErrorCode.DOCUSEAL_CREATE_FAILED);
        }
        String contractUrl = (String) response.getBody().get(0).get("embed_src");
        emailService.sendContractEmail(rental, contractUrl);
        rental.setContractStatus(ContractStatus.PENDING);
        rentalRepository.save(rental);
        return contractUrl;
    }

    @Transactional
    public void handleWebhook(Map<String, Object> payload) {
        String eventType = (String) payload.get("event_type");
        if ("form.completed".equals(eventType)) {
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            String status = (String) data.get("status");
            String email = (String) data.get("email");
            String submissionUrl = (String) data.get("submission_url");

            if ("completed".equals(status)) {
                Rental rental = rentalRepository.findTopByUserEmailOrderByCreatedAtDesc(email)
                        .orElseThrow(() -> new AppException(ErrorCode.RENTAL_NOT_FOUND));
                Map<String, Object> submission = (Map<String, Object>) data.get("submission");
                long submissionId = 0;
                if (submission != null && submission.get("id") != null) {
                    submissionId = ((Number) submission.get("id")).longValue();
                }
                rental.setStatus(RentalStatus.CONFIRM);
                rental.setContractStatus(ContractStatus.SIGNED);
                rental.setSubmissionId(submissionId);
                rental.setSubmissionUrl(submissionUrl);
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

    @Transactional
    public String getContractUrl(long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new AppException(ErrorCode.RENTAL_NOT_FOUND));

        User loginUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!rental.getUser().getId().equals(loginUser.getId()) && loginUser.getRole().equals(Role.ROLE_USER)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", apiKey);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response;
        try{
            response = restTemplate.exchange(
                    "https://api.docuseal.com/submissions/" + rental.getSubmissionId(),
                    HttpMethod.GET,
                    entity,
                    Map.class
            );
        }catch (Exception e){
            throw new AppException(ErrorCode.CONTRACT_NOT_FOUND);
        }

        Map<String, Object> body = response.getBody();
        if (body == null) {
            throw new AppException(ErrorCode.DOCUSEAL_FETCH_FAILED);
        }

        // Lấy danh sách document trong JSON
        List<Map<String, Object>> documents = (List<Map<String, Object>>) body.get("documents");
        if (documents != null && !documents.isEmpty()) {
            return (String) documents.get(0).get("url");
        }

        throw new AppException(ErrorCode.DOCUSEAL_DOCUMENT_NOT_FOUND);
    }

}
