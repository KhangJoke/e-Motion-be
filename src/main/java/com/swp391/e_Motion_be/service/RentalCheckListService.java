package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.dto.requests.checklist.PageAndFilterCheckListRequest;
import com.swp391.e_Motion_be.dto.requests.checklist.RentalCheckListCreateRequest;
import com.swp391.e_Motion_be.dto.requests.checklist.RentalCheckListUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.checkList.PageAndFilterCheckListResponse;
import com.swp391.e_Motion_be.dto.responses.checkList.RentalCheckListListResponse;
import com.swp391.e_Motion_be.dto.responses.checkList.RentalCheckListResponse;
import com.swp391.e_Motion_be.entity.*;
import com.swp391.e_Motion_be.enums.CheckType;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.enums.RentalStatus;
import com.swp391.e_Motion_be.enums.vehicle.VehicleStatus;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.mapper.RentalCheckListMapper;
import com.swp391.e_Motion_be.repository.RentalCheckListRepository;
import com.swp391.e_Motion_be.repository.RentalRepository;
import com.swp391.e_Motion_be.repository.StaffRepository;
import com.swp391.e_Motion_be.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RentalCheckListService {
    @Value("${price.per.battery}")
    private double pricePerBattery;
    @Value("${penalty.fee.rate}")
    private double penaltyFeeRate;
    @Value("${price.day.rate}")
    private double priceDayRate;

    private final RentalCheckListRepository rentalCheckListRepository;
    private final RentalCheckListMapper rentalCheckListMapper;
    private final RentalRepository rentalRepository;
    private final StaffRepository staffRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;

    public RentalCheckListResponse createCheckList(RentalCheckListCreateRequest request) {
        // --- Kiểm tra trùng check ---
        boolean alreadyExists = rentalCheckListRepository.findByRental_Id(request.getRentalId())
                .stream()
                .anyMatch(c -> c.getType() == request.getType());
        if (alreadyExists) {
            if (request.getType() == CheckType.CHECK_IN) throw new AppException(ErrorCode.ALREADY_CHECKED_IN);
            if (request.getType() == CheckType.CHECK_OUT) throw new AppException(ErrorCode.ALREADY_CHECKED_OUT);
        }

        // lấy ra các entity liên quan
        Rental rental = rentalRepository.findById(request.getRentalId())
                .orElseThrow(() -> new AppException(ErrorCode.RENTAL_NOT_FOUND));
        Staff staff = staffRepository.findByUser_EmailAndIsDeleteFalse(request.getStaffEmail())
                .orElseThrow(() -> new AppException(ErrorCode.STAFF_NOT_FOUND));

        // check if check in and rental is confirmed
        if (request.getType() == CheckType.CHECK_IN) {
            if (!rental.getStatus().equals(RentalStatus.CONFIRM)) {
                throw new AppException(ErrorCode.RENTAL_IS_NOT_CONFIRM_FOR_CHECK_IN);
            }
        }
        // check if check out and rental is ongoing or overdue
        else if (request.getType() == CheckType.CHECK_OUT) {
            if (!rental.getStatus().equals(RentalStatus.ONGOING) && !rental.getStatus().equals(RentalStatus.OVERDUE)) {
                throw new AppException(ErrorCode.RENTAL_IS_NOT_ONGOING_OR_OVERDUE_FOR_CHECK_OUT);
            }
            Vehicle vehicle = rental.getVehicle();
            vehicle.setBatteryLevel(request.getCurrentBattery());
        }

        RentalCheckList checkList = rentalCheckListMapper.toCheckListEntity(request);
        checkList.setRental(rental);
        checkList.setStaff(staff);
        // lưu vào db trước
        rentalCheckListRepository.save(checkList);

        if(request.getType().equals(CheckType.CHECK_IN)){
            // update status vehicle, rental khi bắt đầu thuê
            rental.setStatus(RentalStatus.ONGOING);
            rental.getVehicle().setStatus(VehicleStatus.ONGOING);
            rentalCheckListRepository.save(checkList);
        }else{
            // lưu phí phát sinh và cập nhật status rental, vehicle
            double fee = calculateFee(rental.getId());
            checkList.setFee(fee);

            rental.setStatus(RentalStatus.PENDING_FEE);
            rental.getVehicle().setStatus(VehicleStatus.CHECKING);

            long hours = Duration.between(rental.getStartTime(), rental.getEndTime()).toHours();
            int pointPerHour = rental.getVehicle().getPoint();
            int earnedPoints = (int) (hours * pointPerHour);

            User user = rental.getUser();
            user.setPoint(user.getPoint() + earnedPoints);
            userRepository.save(user);

            rentalCheckListRepository.save(checkList);

            emailService.sendRentalReturnedNotification(rental);
        }

        return rentalCheckListMapper.toRentalCheckListResponse(checkList);
    }

    // tính phí phát sinh
    public double calculateFee(Long rentalId){
        List<RentalCheckList> checkLists = rentalCheckListRepository.findByRental_Id(rentalId);
        RentalCheckList checkIn = checkLists.stream()
                .filter(c -> c.getType() == CheckType.CHECK_IN)
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.CHECKIN_NOT_FOUND));

        RentalCheckList checkOut = checkLists.stream()
                .filter(c -> c.getType() == CheckType.CHECK_OUT)
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.CHECKOUT_NOT_FOUND));

        double  fee = 0.0;

        double batteryDiff = checkOut.getCurrentBattery() - checkIn.getCurrentBattery();
        if(batteryDiff < 0){
            fee += Math.abs(batteryDiff) * pricePerBattery;
        }

        Rental rental = checkOut.getRental();
        double pricePerDay = rental.getVehicle().getPricePer4Hours()*priceDayRate;

        LocalDateTime actualReturnTime = checkOut.getCreatedAt();
        LocalDateTime expectedReturnTime = rental.getEndTime();

        if (actualReturnTime.isAfter(expectedReturnTime)) {
            long lateHours = Math.max(0, Duration.between(expectedReturnTime, actualReturnTime).toHours());
            fee += lateHours * (penaltyFeeRate * pricePerDay);
        }

        return fee;
    }

    public List<RentalCheckListResponse> getAllCheckLists(){
        List<RentalCheckList> checkLists = rentalCheckListRepository.findAll();
        return checkLists.stream()
                .map(rentalCheckListMapper::toRentalCheckListResponse)
                .toList();
    }

    public List<RentalCheckListListResponse> getListCheckLists(){
        List<RentalCheckList> checkLists = rentalCheckListRepository.findLatestChecklistPerRental();
        return checkLists.stream()
                .map(rentalCheckListMapper::toRentalCheckListListResponse)
                .toList();
    }

    public List<RentalCheckListResponse>  getCheckListByRentalId(Long rentalId){
        return rentalCheckListRepository.findByRental_Id(rentalId)
                .stream()
                .map(rentalCheckListMapper::toRentalCheckListResponse)
                .toList();
    }

    //Search by type and rental id + staff email
    public  List<RentalCheckListResponse> getCheckListByFilter(String keyword, List<CheckType> type){
        List<CheckType> typeList = (type == null || type.isEmpty())
                ? List.of(CheckType.CHECK_IN,CheckType.CHECK_OUT)
                : type;

        List<RentalCheckList> checkLists = (keyword == null || keyword.isBlank())
                ? rentalCheckListRepository.findByTypeIn(typeList)
                : (keyword.matches(".*[a-zA-Z@._].*")
                ? rentalCheckListRepository.findByStaff_User_EmailContainsAndTypeIn(keyword, typeList)
                : rentalCheckListRepository.findByRental_IdAndTypeIn(Long.parseLong(keyword), typeList));

        return checkLists
                .stream()
                .map(rentalCheckListMapper::toRentalCheckListResponse)
                .toList();
    }

    public PageAndFilterCheckListResponse findByPageAndFilterAndSearch(PageAndFilterCheckListRequest request) {
        List<CheckType> typeList = (request.getType() == null || request.getType().isEmpty())
                ? Arrays.asList(CheckType.values())
                : request.getType();

        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getLimit(), Sort.by("rental.id").ascending());
        List<RentalCheckList> latest = rentalCheckListRepository.findLatestChecklistPerRental();

        List<Long> ids = latest.stream()
                .map(RentalCheckList::getId)
                .toList();

        Page<RentalCheckList> checkListPage = rentalCheckListRepository
                .findByIdInAndTypeInAndStaff_User_EmailContaining(ids, typeList, request.getSearch(), pageable);

        List<RentalCheckListListResponse> checkLists = checkListPage.getContent().stream()
                .map(rentalCheckListMapper::toRentalCheckListListResponse)
                .toList();

        return new PageAndFilterCheckListResponse(checkLists, checkListPage.getTotalPages());
    }

    public RentalCheckListResponse updateRentalCheckList(Long id, @Valid RentalCheckListUpdateRequest request) {
        RentalCheckList rentalCheckList = rentalCheckListRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RENTAL_CHECKLIST_NOT_FOUND));

        if(rentalCheckList.getRental().getStatus().equals(RentalStatus.COMPLETED)) {
            throw new AppException(ErrorCode.RENTAL_LOG_RENTAL_COMPLETED);
        }
        if(staffRepository.findByUser_EmailAndIsDeleteFalse(request.getStaffEmail()).isEmpty()) {
            throw new AppException(ErrorCode.STAFF_NOT_FOUND);
        }
        if(request.getStaffEmail().equalsIgnoreCase(rentalCheckList.getStaff().getUser().getEmail())) {
            throw new AppException(ErrorCode.NOT_SAME_STAFF_EMAIL);
        }
        if(rentalRepository.findById(request.getRentalId()).isEmpty()) {
            throw new AppException(ErrorCode.RENTAL_NOT_FOUND);
        }
        if(!request.getRentalId().equals(rentalCheckList.getRental().getId())) {
            throw new AppException(ErrorCode.NOT_SAME_RENTAL);
        }
        rentalCheckList.setCurrentBattery(request.getCurrentBattery());
        rentalCheckList.setImg(request.getImg());

        rentalCheckListRepository.save(rentalCheckList);
        return rentalCheckListMapper.toRentalCheckListResponse(rentalCheckList);
    }
}
