package com.tanhung.antifraudsystem.service;

import com.tanhung.antifraudsystem.dto.request.StolenCardNumberRequestDto;
import com.tanhung.antifraudsystem.dto.request.SuspiciousIpRequestDto;
import com.tanhung.antifraudsystem.dto.request.TransactionRequestDto;
import com.tanhung.antifraudsystem.dto.response.*;
import com.tanhung.antifraudsystem.exception.*;
import com.tanhung.antifraudsystem.mapper.StolenCardMapper;
import com.tanhung.antifraudsystem.mapper.SuspiciousIpAddressMapper;
import com.tanhung.antifraudsystem.model.CardNumberValidator;
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
    public ActionResponseDto checkRequest(TransactionRequestDto request) throws InvalidAmountException{

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
        boolean isSuspiciousIp = suspiciousIPRepo.existsByIpAddress(request.getIpAddress());
        if(isStolenCard){
            result = "PROHIBITED";
            infos.add("card-number");
        }
        if(isSuspiciousIp){
            result = "PROHIBITED";
            infos.add("ip");
        }
        String infosString = infos.toString();
        infosString = infosString.substring(1, infosString.length() - 1);
        return new ActionResponseDto(result, infosString);
    }

    @Transactional
    public IPResponseDto addIP(SuspiciousIpRequestDto ipAddress) throws IPAddressNullException{
        if(ipAddress == null){
            throw new IPAddressNullException("IP object cannot be null!", HttpStatus.BAD_REQUEST);
        }
        validateIpDuplicate(ipAddress.getIpAddress());
        SuspiciousIPAddress ip = addIpToList(ipAddress);
        return buildIpResponse(ip);
    }

    private void validateIpDuplicate(String ipAddress) throws IPAddressConflictException{
        if(isIpAddressExist(ipAddress)){
            throw new IPAddressConflictException(ipAddress + " is existed in the list!", HttpStatus.CONFLICT);
        }
    }

    private boolean isIpAddressExist(String ipAddress){
        return suspiciousIPRepo.existsByIpAddress(ipAddress);
    }

    private SuspiciousIPAddress addIpToList(SuspiciousIpRequestDto ipAddress){
        SuspiciousIPAddress ipEntity = suspiciousIpAddressMapper.toEntity(ipAddress);
        return suspiciousIPRepo.save(ipEntity);
    }

    private IPResponseDto buildIpResponse(SuspiciousIPAddress ipAddress){
        return suspiciousIpAddressMapper.toDto(ipAddress);
    }

    @Transactional
    public StatusResponseDto deleteIP(String ip) throws IPAddressNullException{
        if(ip == null){
            throw new IPAddressNullException("IP address must not be null!", HttpStatus.BAD_REQUEST);
        }
        validateIpExistence(ip);
        return delete(ip);
    }

    private void validateIpExistence(String ipAddress) throws IPAddressNotFoundException{
        if(!isIpAddressExist(ipAddress)){
            throw new IPAddressNotFoundException("IP address not found!", HttpStatus.NOT_FOUND);
        }
    }

    private StatusResponseDto delete(String ip){
        suspiciousIPRepo.deleteByIpAddress(ip);
        return new StatusResponseDto("IP " + ip + " successfully removed!");
    }

    public List<IPResponseDto> getAllSuspiciousIPs(){
        return suspiciousIPRepo.findAll(Sort.by("id"))
                .stream()
                .map(suspiciousIpAddressMapper::toDto)
                .toList();
    }

    @Transactional
    public StolenCardResponseDto addStolenCardNumber(StolenCardNumberRequestDto stolenCard) throws  StolenCardNullException{
        if(stolenCard == null){
            throw new StolenCardNullException("Your card object must not be null!", HttpStatus.BAD_REQUEST);
        }
        validateCardNumber(stolenCard.getCardNumber());
        validateCardNumberDuplicate(stolenCard.getCardNumber());
        StolenCard cardEntity = addStolenCard(stolenCard);
        return buildStolenCardResponse(cardEntity);
    }

    private void validateCardNumber(String cardNumber) throws InvalidCardNumberException{
        if(!CardNumberValidator.isValidCardNumber(cardNumber)){
            throw new InvalidCardNumberException("Card number is invalid!", HttpStatus.BAD_REQUEST);
        }
    }

    private void validateCardNumberDuplicate(String cardNumber) throws StolenCardConflictException{
        if(isCardNumberExist(cardNumber)){
            throw new StolenCardConflictException(cardNumber + " is existed in the list!", HttpStatus.CONFLICT);
        }
    }

    private boolean isCardNumberExist(String cardNumber){
        return stolenCardRepo.existsStolenCardByCardNumber(cardNumber);
    }

    private StolenCard addStolenCard(StolenCardNumberRequestDto stolenCard){
        StolenCard cardEntity = stolenCardMapper.toEntity(stolenCard);
        return stolenCardRepo.save(cardEntity);
    }

    private StolenCardResponseDto buildStolenCardResponse(StolenCard stolenCard){
        return stolenCardMapper.toDto(stolenCard);
    }

    @Transactional
    public StatusResponseDto deleteStolenCardNumber(String cardNumber){
        if(cardNumber == null) throw new StolenCardNullException("Your card number must not be null!", HttpStatus.BAD_REQUEST);
        if(!stolenCardRepo.existsStolenCardByCardNumber(cardNumber)) {
            throw new StolenCardNumberNotFoundException(cardNumber + " not found!", HttpStatus.NOT_FOUND);
        }

        stolenCardRepo.deleteStolenCardByCardNumber(cardNumber);
        return new StatusResponseDto("Card " + cardNumber + " successfully removed!");
    }

    public List<StolenCardResponseDto> getAllStolenCards(){
        return stolenCardRepo.findAll(Sort.by("id"))
                .stream()
                .map(stolenCardMapper::toDto)
                .toList();
    }
}
