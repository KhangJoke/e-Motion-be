package com.swp391.e_Motion_be.service.document;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp391.e_Motion_be.enums.DocumentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class OcrService {

    @Value("${ocr.api.key}")
    private String apiKey;

    private static final String OCR_API_URL = "https://api.ocr.space/parse/image";

    private static final Set<String> CAR_CLASSES = new HashSet<>();
    static {
        CAR_CLASSES.add("B1");
        CAR_CLASSES.add("B2");
        CAR_CLASSES.add("BE");
        CAR_CLASSES.add("C");
        CAR_CLASSES.add("D");
        CAR_CLASSES.add("E");
    }

    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\b\\d{12}\\b");
    private static final Pattern CLASS_PATTERN = Pattern.compile("Hạng\\s*(?:/Class)?\\s*[:\\-]?\\s*([A-Za-z0-9]{1,3})", Pattern.CASE_INSENSITIVE);
    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{1,2}[\\/\\-]\\d{1,2}[\\/\\-]\\d{2,4})");

    private static final DateTimeFormatter[] DATE_FORMATTERS = new DateTimeFormatter[]{
            DateTimeFormatter.ofPattern("d/M/yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("d-M-yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy")
    };

    public String extractTextFromUrl(String imageUrl) {
        try {
            // Gửi request dạng multipart/form-data
            RestTemplate restTemplate = new RestTemplate();

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("url", imageUrl);
            body.add("language", "vnm");
            body.add("isOverlayRequired", "false");
            body.add("OCREngine", "2");
            body.add("filetype", "png");

            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", apiKey);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(OCR_API_URL, requestEntity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                JsonNode parsedResults = root.path("ParsedResults");

                if (parsedResults.isArray() && !parsedResults.isEmpty()) {
                    return parsedResults.get(0).path("ParsedText").asText();
                } else {
                    return "Không tìm thấy văn bản trong ảnh.";
                }
            } else {
                return "Lỗi kết nối OCR API: " + response.getStatusCode();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Lỗi khi xử lý OCR: " + e.getMessage();
        }
    }

    public boolean isCCCD(String text){
        return text.toLowerCase().contains("căn cước công dân") || text.toLowerCase().contains("citizen identity card");
    }

    public boolean isGPLX(String text){
        return text.toLowerCase().contains("giấy phép lái xe") || text.toLowerCase().contains("driver's license");
    }

    public boolean hasValidIdNumber(String text){
        Matcher m = NUMBER_PATTERN.matcher(text);
        return m.find();
    }

    public String extractIdNumber(String text){
        Matcher m = NUMBER_PATTERN.matcher(text);
        if(m.find()){
            return m.group();
        }
        return null;
    }

    public boolean isExpired(String text){
        Pattern expiryLabel = Pattern.compile("Có giá trị đến|Expires|Date of expiry", Pattern.CASE_INSENSITIVE);
        Matcher mLabel = expiryLabel.matcher(text);
        if (mLabel.find()){
            // Get the position after the expiry label
            int labelEndPos = mLabel.end();
            // Extract text after the expiry label (next 100 characters to find the date)
            String textAfterLabel = text.substring(labelEndPos, Math.min(labelEndPos + 100, text.length()));
            // Check if there's "không thời hạn" after the expiry label
            if(textAfterLabel.toLowerCase().contains("không thời hạn")){
                return false;
            }
            Matcher m = DATE_PATTERN.matcher(textAfterLabel);
            if(m.find()){
                LocalDate d = parseDate(m.group(1));
                if(d != null){
                    return d.isBefore(LocalDate.now());
                }
            }
        }
        return true;
    }

    // ----- Check GPLX class cho thuê xe -----
    public boolean isAllowedToRentCar(String text){
        Matcher m = CLASS_PATTERN.matcher(text);
        if(m.find()){
            String cls = m.group(1).toUpperCase();
            String[] tokens = cls.split("[,;\\s]+");
            for(String t: tokens){
                if(CAR_CLASSES.contains(t)) return true;
            }
        }
        return false;
    }

    private LocalDate parseDate(String dateStr){
        dateStr = dateStr.replaceAll("[^0-9/\\-]", "");
        for(DateTimeFormatter fmt: DATE_FORMATTERS){
            return LocalDate.parse(dateStr, fmt);
        }
        return null;
    }
}
