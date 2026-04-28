package com.tanhung.antifraudsystem.service;

import com.tanhung.antifraudsystem.dto.request.StolenCardRequest;
import com.tanhung.antifraudsystem.dto.request.SuspiciousIpRequest;
import com.tanhung.antifraudsystem.dto.request.TransactionRequest;
import com.tanhung.antifraudsystem.dto.response.ActionResponse;
import com.tanhung.antifraudsystem.dto.response.IPResponse;
import com.tanhung.antifraudsystem.dto.response.StatusResponse;
import com.tanhung.antifraudsystem.dto.response.StolenCardResponse;
import com.tanhung.antifraudsystem.exception.*;
import com.tanhung.antifraudsystem.mapper.StolenCardMapper;
import com.tanhung.antifraudsystem.mapper.SuspiciousIpAddressMapper;
import com.tanhung.antifraudsystem.model.CardValidator;
import com.tanhung.antifraudsystem.model.StolenCard;
import com.tanhung.antifraudsystem.model.SuspiciousIPAddress;
import com.tanhung.antifraudsystem.repo.StolenCardRepo;
import com.tanhung.antifraudsystem.repo.SuspiciousIPRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Service
@RequiredArgsConstructor
public class AntiFraudService {

    private final SuspiciousIPRepo suspiciousIPRepo;
    private final StolenCardRepo stolenCardRepo;
    private final SuspiciousIpAddressMapper suspiciousIpAddressMapper;
    private final StolenCardMapper stolenCardMapper;
    public ActionResponse checkRequest(TransactionRequest request) throws InvalidAmountException{

        String result;
        Set<String> infos = new TreeSet<>();
        if(request.getAmount().compareTo(new BigDecimal("1")) < 0)
            throw new InvalidAmountException("Your amount must be at least 1 dollar!", HttpStatus.BAD_REQUEST);

        if(request.getAmount().compareTo(new BigDecimal(200)) <= 0){
            result = "ALLOWED";
            infos.add("none");
        } else if(request.getAmount().compareTo(new BigDecimal(1500)) <= 0) {
            result = "MANUAL_PROCESSING";
            infos.add("amount");
        } else {
            result = "PROHIBITED";
            infos.add("amount");
        }

        boolean isStolenCard = stolenCardRepo.existsStolenCardByCardNumber(request.getCardNumber());
        boolean isSuspiciousIp = suspiciousIPRepo.existsByIp(request.getIpAddress());
        if(isStolenCard){
            result = "PROHIBITED";
            infos.add("card-number");
        }
        if(isSuspiciousIp){
            result = "PROHIBITED";
            infos.add("ip");
        }
        if(result.equalsIgnoreCase("ALLOWED")) infos.remove("none");

        return new ActionResponse(result, infos.toString());
    }

    @Transactional
    public IPResponse addIP(SuspiciousIpRequest ipAddress) throws IPAddressException{
        if(ipAddress == null){
            throw new IPAddressNullException("IP object cannot be null!", HttpStatus.BAD_REQUEST);
        }
        if(suspiciousIPRepo.existsByIp(ipAddress.getIpAddress())){
            throw new IPAddressConflictException(ipAddress.getIpAddress() + " is existed in the list!", HttpStatus.CONFLICT);
        }
        SuspiciousIPAddress entityIpAddress = suspiciousIPRepo.save(suspiciousIpAddressMapper.toEntity(ipAddress));

        return suspiciousIpAddressMapper.toDto(entityIpAddress);
    }

    @Transactional
    public StatusResponse deleteIP(String ip) throws IPAddressException{
        if(ip == null) throw new IPAddressNullException("IP address must not be null!", HttpStatus.BAD_REQUEST);

        if(!suspiciousIPRepo.existsByIp(ip)){
            throw new IPAddressNotFoundException("IP address not found!", HttpStatus.NOT_FOUND);
        }

        suspiciousIPRepo.deleteByIp(ip);
        return new StatusResponse("IP " + ip + " successfully removed!");
    }

    public List<IPResponse> getAllSuspiciousIPs(){
        return suspiciousIPRepo.findAll(Sort.by("id"))
                .stream()
                .map(suspiciousIpAddressMapper::toDto)
                .toList();
    }

    @Transactional
    public StolenCardResponse addStolenCardNumber(StolenCardRequest card) throws  StolenCardException{
        if(card == null) throw new StolenCardNullException("Your card number must not be null!", HttpStatus.BAD_REQUEST);
        if(!CardValidator.isValidCardNumber(card.getCardNumber())){
            throw new InvalidCardNumberException("Card number is invalid!", HttpStatus.BAD_REQUEST);
        }

        if(stolenCardRepo.existsStolenCardByCardNumber(card.getCardNumber())){
            throw new StolenCardConflictException(card.getCardNumber() + " is existed in the list!", HttpStatus.CONFLICT);
        }


        StolenCard cardEntity = stolenCardRepo.save(stolenCardMapper.toEntity(card));

        return stolenCardMapper.toDto(cardEntity);
    }

    @Transactional
    public StatusResponse deleteStolenCardNumber(String cardNumber){
        if(cardNumber == null) throw new StolenCardNullException("Your card number must not be null!", HttpStatus.BAD_REQUEST);
        if(!stolenCardRepo.existsStolenCardByCardNumber(cardNumber)) {
            throw new StolenCardNumberNotFoundException(cardNumber + " not found!", HttpStatus.NOT_FOUND);
        }

        stolenCardRepo.deleteStolenCardByCardNumber(cardNumber);
        return new StatusResponse("Card " + cardNumber + " successfully removed!");
    }

    public List<StolenCardResponse> getAllStolenCards(){
        return stolenCardRepo.findAll(Sort.by("id"))
                .stream()
                .map(stolenCardMapper::toDto)
                .toList();
    }
}
