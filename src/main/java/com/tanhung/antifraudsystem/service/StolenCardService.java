package com.tanhung.antifraudsystem.service;

import com.tanhung.antifraudsystem.dto.request.StolenCardNumberRequestDto;
import com.tanhung.antifraudsystem.dto.response.StolenCardResponseDto;
import com.tanhung.antifraudsystem.exception.StolenCardConflictException;
import com.tanhung.antifraudsystem.exception.StolenCardNumberNotFoundException;
import com.tanhung.antifraudsystem.mapper.StolenCardMapper;
import com.tanhung.antifraudsystem.model.StolenCard;
import com.tanhung.antifraudsystem.repo.StolenCardRepo;
import com.tanhung.antifraudsystem.validators.StolenCardValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StolenCardService {
    private final StolenCardMapper stolenCardMapper;
    private final StolenCardRepo stolenCardRepo;
    private final StolenCardValidator stolenCardValidator;

    public StolenCard addCard(StolenCardNumberRequestDto stolenCard){
        String cardNumber = stolenCard.getCardNumber();
        if(stolenCardValidator.isStolenCard(cardNumber)){
            throw new StolenCardConflictException(stolenCard.getCardNumber() + " already exists in the stolen card list!");
        }
        StolenCard card = stolenCardMapper.toEntity(stolenCard);
        return stolenCardRepo.save(card);
    }

    public void deleteCard(String cardNumber){
        if(stolenCardValidator.isStolenCard(cardNumber)){
            throw new StolenCardNumberNotFoundException(cardNumber + " not found!");
        }
        stolenCardRepo.deleteStolenCardByCardNumber(cardNumber);
    }


    public StolenCardResponseDto convertToDto(StolenCard stolenCard){
        return stolenCardMapper.toDto(stolenCard);
    }

    public List<StolenCardResponseDto> getAllStolenCards(){
        return stolenCardRepo.findAll(Sort.by("id"))
                .stream()
                .map(stolenCardMapper::toDto)
                .toList();
    }

}
