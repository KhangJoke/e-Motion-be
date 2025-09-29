package com.swp391.e_Motion_be.service;

import com.swp391.e_Motion_be.dto.requests.deposit.DepositCreateRequest;
import com.swp391.e_Motion_be.dto.requests.deposit.DepositUpdateRequest;
import com.swp391.e_Motion_be.dto.responses.DepositResponse;
import com.swp391.e_Motion_be.entity.Deposit;
import com.swp391.e_Motion_be.entity.User;
import com.swp391.e_Motion_be.enums.ErrorCode;
import com.swp391.e_Motion_be.exception.AppException;
import com.swp391.e_Motion_be.mapper.DepositMapper;
import com.swp391.e_Motion_be.repository.DepositRepository;
import com.swp391.e_Motion_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepositService {

    private final DepositRepository depositRepository;
    private final DepositMapper depositMapper;
    private final UserRepository userRepository;

    public List<DepositResponse> getAllDeposits() {
        return depositRepository.findAll().stream()
                .map(depositMapper:: toDespositResponse)
                .toList();
    }

    public DepositResponse createDeposit(DepositCreateRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTS));
        Deposit deposit = depositMapper.toDespositEntity(request);
        deposit.setUser(user);
        depositRepository.save(deposit);
        return depositMapper.toDespositResponse(deposit);
    }

    public List<DepositResponse> getDepositsByUserEmail(String email){
        return depositRepository.findByUser_Email(email).stream()
                .map(depositMapper:: toDespositResponse)
                .toList();
    }

    public DepositResponse updateDeposit(DepositUpdateRequest request){
        Deposit deposit = depositRepository.findByUser_EmailAndTypeAndStatus(request.getEmail(), request.getType(), request.getOldStatus())
                .orElseThrow(() -> new AppException(ErrorCode.DEPOSIT_NOT_FOUND));
        deposit.setStatus(request.getNewStatus());
        return depositMapper.toDespositResponse((depositRepository.save(deposit)));
    }
}
