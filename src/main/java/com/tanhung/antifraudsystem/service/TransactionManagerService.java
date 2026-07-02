package com.tanhung.antifraudsystem.service;

import com.tanhung.antifraudsystem.dto.request.TransactionRequestDto;
import com.tanhung.antifraudsystem.dto.response.ActionResponseDto;
import com.tanhung.antifraudsystem.mapper.TransactionMapper;
import com.tanhung.antifraudsystem.model.Transaction;
import com.tanhung.antifraudsystem.repo.TransactionRepo;
import com.tanhung.antifraudsystem.validators.TransactionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionManagerService {
    private final TransactionRepo transactionRepo;
    private final TransactionMapper transactionMapper;
    private final TransactionValidator transactionValidator;

    public Transaction saveTransaction(Transaction transaction){
        return transactionRepo.save(transaction);
    }

    public Transaction convertToEntity(TransactionRequestDto request){
        return transactionMapper.toEntity(request);
    }

    public ActionResponseDto proceedTransaction(Transaction transaction){
        return transactionValidator.validate(transaction);
    }

}
