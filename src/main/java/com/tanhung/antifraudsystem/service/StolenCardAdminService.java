package com.tanhung.antifraudsystem.service;

import com.tanhung.antifraudsystem.dto.request.StolenCardNumberRequestDto;
import com.tanhung.antifraudsystem.dto.response.StatusResponseDto;
import com.tanhung.antifraudsystem.dto.response.StolenCardResponseDto;
import com.tanhung.antifraudsystem.exception.InvalidCardNumberException;
import com.tanhung.antifraudsystem.exception.StolenCardNullException;
import com.tanhung.antifraudsystem.model.StolenCard;
import com.tanhung.antifraudsystem.validators.CardNumberValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StolenCardAdminService {
    private final StolenCardService stolenCardService;

    @Transactional
    public StolenCardResponseDto addStolenCardNumber(StolenCardNumberRequestDto stolenCard){
        if(stolenCard == null){
            throw new StolenCardNullException("Your card object must not be null!");
        }
        StolenCard cardEntity = proceedAddStolenCardNumber(stolenCard);
        return buildStolenCardResponse(cardEntity);
    }

    private StolenCard proceedAddStolenCardNumber(StolenCardNumberRequestDto requestCard){
        if(!CardNumberValidator.isValidCardNumber(requestCard.getCardNumber())){
            throw new InvalidCardNumberException(requestCard.getCardNumber() + " card number is invalid!");
        }
        return stolenCardService.addCard(requestCard);
    }

    private StolenCardResponseDto buildStolenCardResponse(StolenCard stolenCard){
        return stolenCardService.convertToDto(stolenCard);
    }

    @Transactional
    public StatusResponseDto deleteStolenCardNumber(String cardNumber){
        if(cardNumber == null) {
            throw new StolenCardNullException("Your card number must not be null!");
        }
        return proceedDeleteStolenCardNumber(cardNumber);
    }

    private StatusResponseDto proceedDeleteStolenCardNumber(String cardNumber){
        stolenCardService.deleteCard(cardNumber);
        return new StatusResponseDto("Card " + cardNumber + " successfully removed!");
    }

    public List<StolenCardResponseDto> getStolenCardList(){
        return stolenCardService.getAllStolenCards();
    }

}
