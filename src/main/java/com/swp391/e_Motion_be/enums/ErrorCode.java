package com.swp391.e_Motion_be.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // Generic errors
    UNEXPECTED_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi không mong muốn"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Không có quyền truy cập"),
    INVALID_TIME_RANGE(HttpStatus.BAD_REQUEST, "Khoảng thời gian phải lớn hơn hoặc bằng 4"),

    // User errors
    ROLE_INVALID(HttpStatus.BAD_REQUEST, "Vai trò người dùng không hợp lệ"),
    PHONE_EXITS(HttpStatus.CONFLICT, "Số điện thoại đã tồn tại"),
    NEW_PASSWORD_SAME_AS_OLD(HttpStatus.BAD_REQUEST, "Mật khẩu mới phải khác mật khẩu cũ"),
    CANNOT_DELETE_OWN_ACCOUNT(HttpStatus.BAD_REQUEST, "Không thể xóa tài khoản của chính bạn"),
    ACCOUNT_BLOCKED(HttpStatus.FORBIDDEN, "Tài khoản đã bị khóa"),

    // Update password errors
    OLD_PASSWORD_NOT_MATCH(HttpStatus.BAD_REQUEST, "Mật khẩu cũ không đúng"),

    // Document errors
    DOCUMENT_NUMBER_EXISTS(HttpStatus.CONFLICT, "Số giấy tờ đã tồn tại"),
    DOCUMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy giấy tờ"),
    USER_ALREADY_HAS_DOCUMENT_OF_TYPE(HttpStatus.CONFLICT, "Người dùng đã có giấy tờ của loại này"),

    // Vehicle errors
    VEHICLE_EXIST(HttpStatus.CONFLICT, "Phương tiện đã tồn tại"),
    VEHICLE_NOT_EXIST(HttpStatus.NOT_FOUND, "Phương tiện không tồn tại"),
    VEHICLE_NOT_AVAILABLE(HttpStatus.NOT_FOUND, "Phương tiện không khả dụng"),
    VEHICLE_NOT_READY(HttpStatus.CONFLICT, "Phương tiện chưa sẵn sàng sử dụng"),
    VEHICLE_STATUS_INVALID(HttpStatus.CONFLICT, "Trạng thái phương tiện không hợp lệ"),
    INVALID_VEHICLE_BRAND(HttpStatus.CONFLICT, "Thương hiệu phương tiện không hợp lệ"),

    // Vehicle Log errors
    VEHICLE_LOG_NOT_EXIST(HttpStatus.NOT_FOUND, "Nhật ký phương tiện không tồn tại"),
    VEHICLE_ID_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy mã phương tiện"),
    VEHICLE_LOG_LIST_EMPTY(HttpStatus.NO_CONTENT, "Danh sách nhật ký phương tiện trống"),
    VEHICLE_LOG_TYPE_EMPTY(HttpStatus.BAD_REQUEST, "Loại nhật ký phương tiện trống"),
    VEHICLE_LOG_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Cập nhật nhật ký phương tiện thất bại"),
    VEHICLE_LOG_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Tạo nhật ký phương tiện thất bại"),
    VEHICLE_LOG_RENTAL_COMPLETED(HttpStatus.BAD_REQUEST, "Không thể cập nhật nhật ký phương tiện cho thuê đã hoàn thành"),

    // Img Vehicle
    IMG_VEHICLE_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy hình ảnh phương tiện"),

    //Rating
    RATING_ID_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy mã đánh giá"),

    // Verify errors
    VERIFY_EXPIRED(HttpStatus.BAD_REQUEST, "Mã xác thực đã hết hạn"),
    VERIFY_CODE_NOT_MATCH(HttpStatus.BAD_REQUEST, "Mã xác thực không đúng"),
    SEND_EMAIL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Gửi email thất bại"),

    // Register errors
    USER_EXISTS(HttpStatus.CONFLICT, "Người dùng đã tồn tại"),
    PHONE_ALREADY_EXISTS(HttpStatus.CONFLICT, "Số điện thoại đã tồn tại"),
    ACCOUNT_NOT_VERIFIED(HttpStatus.FORBIDDEN, "Tài khoản chưa được xác thực, vui lòng xác thực tài khoản của bạn"),
    ACCOUNT_ALREADY_VERIFIED(HttpStatus.BAD_REQUEST, "Tài khoản đã được xác thực, vui lòng đăng nhập"),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "Mật khẩu không hợp lệ"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "Email đã tồn tại"),

    // Login errors
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Token không hợp lệ hoặc đã hết hạn. Vui lòng đăng nhập lại"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "Token đã hết hạn. Vui lòng đăng nhập lại"),
    SIGNATURE_NOT_MATCH(HttpStatus.UNAUTHORIZED, "Chữ ký không khớp"),
    EXTRACT_USERNAME_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Trích xuất tên người dùng từ token thất bại"),
    NOT_LOGIN_YET(HttpStatus.UNAUTHORIZED, "Bạn chưa đăng nhập. Vui lòng đăng nhập để tiếp tục"),
    USER_NOT_EXISTS(HttpStatus.NOT_FOUND, "Người dùng không tồn tại"),
    REFRESH_TOKEN_IS_REUSED(HttpStatus.UNAUTHORIZED, "Refresh token đã được sử dụng. Vui lòng đăng nhập lại"),

    // Logout errors
    USER_HAS_BEEN_LOGOUT(HttpStatus.UNAUTHORIZED, "Tài khoản của bạn đã đăng xuất. Vui lòng đăng nhập để tiếp tục"),

    // Check image document errors
    DOCUMENT_NUMBER_MISMATCH(HttpStatus.BAD_REQUEST, "Số giấy tờ không khớp với hình ảnh"),
    NOT_FOUND_CCCD_IN_IMAGE(HttpStatus.BAD_REQUEST, "Không tìm thấy số CCCD hợp lệ trong hình ảnh"),
    UPLOAD_IMAGE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Tải lên hình ảnh thất bại"),
    FAIL_OCR(HttpStatus.INTERNAL_SERVER_ERROR, "Thực hiện OCR trên hình ảnh thất bại"),

    // OCR service errors
    NOT_FOUND_FOLDER_DATASET(HttpStatus.NOT_FOUND, "Không tìm thấy thư mục dữ liệu"),
    FAIL_COPY_DATASET(HttpStatus.INTERNAL_SERVER_ERROR, "Sao chép thư mục dữ liệu thất bại"),
    CREATE_FOLDER_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Tạo thư mục thất bại"),

    // Station errors
    STATION_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy trạm"),
    STATION_NAME_EXISTS(HttpStatus.CONFLICT, "Tên trạm đã tồn tại"),
    STATION_CITY_INVALID(HttpStatus.BAD_REQUEST, "Thành phố của trạm không hợp lệ"),
    STATION_GET_REVENUE_FAILED(HttpStatus.BAD_REQUEST, "Get revenue station failed"),
    STATION_NOT_IN_CITY(HttpStatus.BAD_REQUEST, "Trạm không thuộc thành phố đã chọn"),

    // Staff errors
    STAFF_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy nhân viên"),
    USER_ALREADY_ASSIGNED_AS_STAFF(HttpStatus.CONFLICT, "Người dùng đã được chỉ định làm nhân viên"),
    USER_NOT_A_STAFF(HttpStatus.BAD_REQUEST, "Người dùng không phải là nhân viên"),
    NOT_SAME_STAFF_EMAIL(HttpStatus.BAD_REQUEST, "Email nhân viên không khớp với email người tạo"),

    // Reservation errors
    RESERVATION_ENDTIME_INVALID(HttpStatus.BAD_REQUEST, "Thời gian kết thúc đặt chỗ phải trong tương lai"),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy đặt chỗ"),
    RESERVATION_TIME_INVALID(HttpStatus.BAD_REQUEST, "Thời gian đặt chỗ phải trong tương lai"),
    RESERVATION_TIME_INVALID_TO_CANCEL(HttpStatus.BAD_REQUEST, "Bạn chỉ có thể hủy đặt chỗ trước 5 ngày so với chuyến đi"),
    RESERVATION_ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "Đặt chỗ đã được hủy"),
    VEHICLE_STATION_MISMATCH(HttpStatus.BAD_REQUEST, "Phương tiện không thuộc trạm đã chọn"),
    TIME_MUST_BE_EXACT_HOUR(HttpStatus.BAD_REQUEST, "Thời gian đặt chỗ phải theo giờ chính xác (ví dụ: 1:00, 2:00)"),
    RESERVATION_EXTEND_TIME_INVALID(HttpStatus.BAD_REQUEST, "Thời gian gia hạn đặt chỗ phải sau thời gian kết thúc hiện tại"),
    RESERVATION_TIME_MUST_AFTER_NOW_3HOURS(HttpStatus.BAD_REQUEST, "Thời gian đặt chỗ phải sau thời điểm hiện tại ít nhất 3 giờ"),
    RENT_TIME_MUST_MINIMUM_4_HOURS(HttpStatus.BAD_REQUEST, "Thời gian thuê tối thiểu là 4 giờ"),
    RESERVATION_STATUS_INVALID(HttpStatus.BAD_REQUEST, "Trạng thái đặt chỗ không hợp lệ"),
    RESERVATION_END_TIME_INVALID(HttpStatus.BAD_REQUEST, "Thời gian 1 chuyến thuê tối đa là 1 tháng"),

    // Deposit errors
    DEPOSIT_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy tiền đặt cọc"),

    // Refresh token errors
    SENDED_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy refresh token đã gửi"),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy refresh token"),

    // RentalCheckList errors
    CHECKIN_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin nhận xe"),
    CHECKOUT_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin trả xe"),
    CHECKLIST_UNAUTHORIZED(HttpStatus.FORBIDDEN, "Bạn không có quyền chỉnh sửa danh sách kiểm tra này"),
    ALREADY_CHECKED_IN(HttpStatus.BAD_REQUEST, "Xe đã được nhận cho lần thuê này"),
    ALREADY_CHECKED_OUT(HttpStatus.BAD_REQUEST, "Xe đã được trả cho lần thuê này"),
    CHECKLIST_TYPE_INVALID(HttpStatus.BAD_REQUEST, "Loại danh sách kiểm tra không hợp lệ"),
    RENTAL_CHECKLIST_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy danh sách kiểm tra thuê xe"),
    DURATION_MINIUM(HttpStatus.BAD_REQUEST, "Thời gian thuê tối thiểu là 4 giờ"),
    RENTAL_NOT_IN_VALID_STATUS_FOR_CHECK(HttpStatus.BAD_REQUEST, "Trạng thái thuê xe không hợp lệ để thực hiện kiểm tra"),
    RENTAL_IS_NOT_ONGOING_OR_OVERDUE_FOR_CHECK_OUT(HttpStatus.BAD_REQUEST,"Trạng thái thuê xê chỉ hợp lệ cho thực hiện check out"),
    RENTAL_IS_NOT_CONFIRM_FOR_CHECK_IN(HttpStatus.BAD_REQUEST,"Trạng thái thuê xe chỉ hợp lệ cho thực hiện check in"),
    RENTAL_IS_NOT_OVERDUE_FOR_CHECK_OUT(HttpStatus.BAD_REQUEST,"Trạng thái thuê xê chỉ hợp lệ cho thực hiện check out"),
    RENTAL_LOG_RENTAL_COMPLETED(HttpStatus.BAD_REQUEST, "Không thể cập nhật danh sách kiểm tra cho thuê đã hoàn thành"),
    // Rental errors
    RENTAL_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy thông tin thuê xe"),
    RENTAL_HAS_CONFLICT(HttpStatus.CONFLICT, "Phương tiện đã có lịch thuê trong khoảng thời gian này, vui lòng chọn thời gian khác"),
    INVALID_RENTAL_STATUS(HttpStatus.BAD_REQUEST, "Trạng thái thuê xe không hợp lệ"),
    USER_HAS_ONGOING_RENTAL(HttpStatus.CONFLICT, "Người dùng đã thuê phương tiện"),
    USER_NEED_HAS_CCCD(HttpStatus.BAD_REQUEST, "Người thuê cần có CCCD"),
    USER_NEED_HAS_LICENSE(HttpStatus.BAD_REQUEST, "Người thuê cần có bằng lái xe"),
    RENTAL_EXTEND_TIME_INVALID(HttpStatus.BAD_REQUEST, "Thời gian gia hạn thuê xe phải sau thời gian kết thúc hiện tại"),
    NOT_SAME_RENTAL(HttpStatus.BAD_REQUEST, "Mã thuê xe không khớp với mã thuê xe trong danh sách kiểm tra"),

    // Payment errors
    PAYMENT_NOT_EXISTS(HttpStatus.NOT_FOUND, "Không tìm thấy thanh toán"),
    DEPOSIT_PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy thanh toán đặt cọc"),
    REFUND_RESPONSE_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy phản hồi hoàn tiền"),
    REFUND_RESPONSE_INVALID(HttpStatus.NOT_FOUND, "Phản hồi hoàn tiền không hợp lệ"),
    VNPAY_KEY_INVALID(HttpStatus.BAD_REQUEST, "Khóa VnPay không hợp lệ"),
    REFUND_FAILED(HttpStatus.BAD_REQUEST, "Hoàn tiền VnPay thất bại"),
    PAYMENT_CANNOT_BE_REFUNDED(HttpStatus.BAD_REQUEST, "Thanh toán không thể hoàn tiền"),
    REFUND_ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "Hoàn tiền đã được xử lý"),
    QUERY_RESPONSE_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy phản hồi truy vấn"),
    QUERY_FAILED(HttpStatus.BAD_REQUEST, "Truy vấn VnPay thất bại"),
    PAYMENT_PROCESSING_FAILED(HttpStatus.BAD_REQUEST, "Xử lý thanh toán thất bại"),
    RENTAL_CANNOT_BE_PAID(HttpStatus.BAD_REQUEST, "Không thể thanh toán thuê xe"),
    DEPOSIT_CANNOT_BE_PAID(HttpStatus.BAD_REQUEST, "Không thể thanh toán đặt cọc"),
    CREATE_PAYMENT_URL_FAILED(HttpStatus.BAD_REQUEST, "Tạo đường dẫn thanh toán thất bại"),
    REFUND_IS_PROCESSING(HttpStatus.BAD_REQUEST, "Yêu cầu hoàn tiền đang được xử lý"),
    REFUND_IS_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy giao dịch yêu cầu hoàn trả"),

    //Cloudinary errors
    DELETE_IMG_FAIL(HttpStatus.EXPECTATION_FAILED, "Xóa hình ảnh thất bại"),

    //Reservation email
    RESERVATION_EMAIL(HttpStatus.EXPECTATION_FAILED, "Gửi email thất bại"),

    //Contract errors
    DOCUSEAL_FETCH_FAILED(HttpStatus.BAD_REQUEST, "Lấy thông tin từ DocuSeal thất bại"),
    DOCUSEAL_DOCUMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy tài liệu trong DocuSeal");

    private final HttpStatus statusCode;
    private final String message;
}
