package com.tanhung.antifraudsystem.service;

import com.tanhung.antifraudsystem.dto.request.TransactionRequestDto;
import com.tanhung.antifraudsystem.dto.response.ActionResponseDto;
import com.tanhung.antifraudsystem.exception.CardNumberNullException;
import com.tanhung.antifraudsystem.exception.InvalidCardNumberException;
import com.tanhung.antifraudsystem.exception.InvalidRegionException;
import com.tanhung.antifraudsystem.model.Region;
import com.tanhung.antifraudsystem.model.Transaction;
import com.tanhung.antifraudsystem.repo.RegionRepo;
import com.tanhung.antifraudsystem.validators.CardNumberValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionReviewService {

    private final RegionRepo regionRepo;
    private final TransactionManagerService transactionManager;

    public ActionResponseDto reviewRequest(TransactionRequestDto request){
        validateCardNumberIsValid(request.getCardNumber());
        return proceedTransactionRequest(request);
    }

    private void validateCardNumberIsValid(String cardNumber){
        if(!isValidCardNumber(cardNumber)){
            throw new InvalidCardNumberException("Card number is invalid!");
        }
    }

    private boolean isValidCardNumber(String cardNumber){
        return CardNumberValidator.isValidCardNumber(cardNumber);
    }

    private ActionResponseDto proceedTransactionRequest(TransactionRequestDto requestDto){
        Transaction transaction = transactionManager.convertToEntity(requestDto);
        Region foundRegion = findRegion(requestDto.getRegion());
        if(foundRegion == null){
            throw new InvalidRegionException(requestDto.getRegion() + " is invalid!");
        }
        transaction.setRegion(foundRegion);
        ActionResponseDto response = transactionManager.proceedTransaction(transaction);
        transactionManager.saveTransaction(transaction);
        return response;
    }

    private Region findRegion(String region){
        return regionRepo.findByCode(region);
    }

}
