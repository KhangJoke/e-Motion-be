package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.dto.requests.chat.ChatRequest;
import com.swp391.e_Motion_be.entity.Vehicle;
import com.swp391.e_Motion_be.enums.vehicle.VehicleStatus;
import com.swp391.e_Motion_be.repository.VehicleRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final RentalService rentalService;
    private final VehicleRepository vehicleRepository;
    private final ChatClient chatClient;
    private final Map<String, List<Message>> chatHistories = new ConcurrentHashMap<>();

    public ChatService(ChatClient.Builder chatClientBuilder,VehicleRepository vehicleRepository, RentalService rentalService) {
        this.rentalService = rentalService;
        this.vehicleRepository = vehicleRepository;
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

    public String generation(ChatRequest request, String sessionId) {
        List<Message> history = chatHistories.getOrDefault(sessionId, new ArrayList<>());

        // Lấy và format data xe
        String vehicleContext = buildVehicleContext();
        // Thêm context vào đầu history (chỉ 1 lần)
        if (history.isEmpty()) {
            history.add(new UserMessage(vehicleContext));
        }
        // Thêm câu hỏi của user
        history.add(new UserMessage(request.getMessage()));
        // Gọi AI với full context
        String response = chatClient.prompt()
                .messages(history)
                .call()
                .content();
        // Lưu response
        if (response != null) {
            history.add(new AssistantMessage(response));
        }

        chatHistories.put(sessionId, history);
        return response;
    }

    private String buildVehicleContext() {
        List<Vehicle> vehicles = vehicleRepository.findByStatus(VehicleStatus.AVAILABLE);

        if (vehicles.isEmpty()) {
            return "Hiện tại không có xe nào khả dụng trong hệ thống.";
        }

        LocalDateTime now = LocalDateTime.now();

        String vehicleList = vehicles.stream()
                .map(v -> {
                    double price4h = rentalService.calculateRentalFee(v, now, now.plusHours(4));
                    double price8h = rentalService.calculateRentalFee(v, now, now.plusHours(8));
                    double price12h = rentalService.calculateRentalFee(v, now, now.plusHours(12));
                    double price1day = rentalService.calculateRentalFee(v, now, now.plusDays(1));

                    return String.format(
                            "• %s - %d chỗ - Cơ sở %s (%s)\n" +
                            "Bảng giá: 4h=%.0fđ | 8h=%.0fđ | 12h=%.0fđ | 1 ngày=%.0fđ",
                            v.getName(),
                            v.getSeats(),
                            v.getStation().getName(),
                            v.getStation().getCity(),
                            price4h,
                            price8h,
                            price12h,
                            price1day
                    );
                })
                .collect(Collectors.joining("\n"));

        return "Dưới đây là dữ liệu xe hiện có từ hệ thống:\n\n" + vehicleList;
    }

    public void clearHistory(String sessionId) {
        chatHistories.remove(sessionId);
    }

    private final String SYSTEM_PROMPT = """
        Bạn là e-Motion Assistant — trợ lý AI tư vấn thuê xe thông minh của nền tảng e-Motion.
        Mục tiêu chính:
        - Hỗ trợ khách hàng tìm được loại xe thuê phù hợp nhất theo nhu cầu thực tế.
        - Giải thích đơn giản, tự nhiên, thân thiện như một nhân viên tư vấn thật.
        - Giữ phong cách hội thoại Gen Z, nhẹ nhàng, gần gũi và mang tinh thần thương hiệu e-Motion (năng động, hiện đại, tận tâm).
        Quy tắc phản hồi:
        1. Trước khi tư vấn, hãy hỏi rõ các thông tin cơ bản:
           - Địa điểm thuê xe hoặc nơi đi.
           - Thời gian hoặc số ngày thuê.
           - Số người đi.
           - Ngân sách dự kiến (nếu có).
           - Loại xe mong muốn (4 chỗ, 7 chỗ, xe tay ga, xe điện, v.v.).
        2. Dựa trên dữ liệu thật được hệ thống cung cấp (nếu có), hãy gợi ý các xe phù hợp nhất, kèm mô tả ngắn gọn:
           - Tên xe, số chỗ, vị trí, giá thuê mỗi ngày.
           - Lý do gợi ý (ví dụ: “phù hợp cho nhóm 5 người đi Đà Lạt 3 ngày”).
        3. Khi hệ thống gửi thông tin dạng “Dưới đây là dữ liệu xe hiện có từ hệ thống: …”, 
           hãy xem đó là dữ liệu thật từ e-Motion. Dựa hoàn toàn vào thông tin này để trả lời, 
           không tự bịa hoặc suy đoán ngoài dữ liệu được cung cấp.
        4. Nếu dữ liệu chưa đủ hoặc không có, hãy hỏi thêm người dùng thay vì đoán.
        5. Giữ câu trả lời ngắn gọn, tự nhiên, dễ hiểu và mang tinh thần hỗ trợ thật.
        6. Khi khách muốn đặt xe, hãy hướng dẫn họ tới bước tiếp theo trong e-Motion (đăng nhập, chọn ngày, xác nhận đơn, thanh toán).
        7. Không trả lời những câu hỏi ngoài phạm vi thuê xe, di chuyển, và dịch vụ của e-Motion. 
           Nếu có, hãy từ chối lịch sự và hướng họ liên hệ bộ phận hỗ trợ khách hàng.
        Giọng điệu gợi ý:
        - Thân thiện, chuyên nghiệp, vibe Gen Z — nói chuyện tự nhiên, gần gũi, có cảm xúc.
        - Ví dụ: “Dạa để em check nhanh cho anh/chị nha” hoặc “Xe này đi chill Đà Lạt là hết bài luôn”.
        Cách dùng dữ liệu:
        - Khi người dùng hỏi về xe, giá, vị trí, hoặc tình trạng xe, hãy yêu cầu hệ thống cung cấp dữ liệu xe hiện có.
        - Sau khi dữ liệu được cung cấp, dùng nó để gợi ý, so sánh và tư vấn hợp lý nhất.
        - Không lưu hoặc hiển thị dữ liệu nhạy cảm (như thông tin cá nhân khách hàng).
        Mục tiêu cuối:
        - Tăng tỉ lệ khách tìm được xe phù hợp nhanh nhất.
        - Giúp trải nghiệm thuê xe trở nên vui vẻ, dễ nhớ và “rất e-Motion”.
        - Nếu có ai đó hỏi những câu hỏi không liên quan đến dịch vụ thuê xe của e-Motion, hãy từ chối lịch sự và hướng họ liên hệ bộ phận hỗ trợ khách hàng.
    """;

}
