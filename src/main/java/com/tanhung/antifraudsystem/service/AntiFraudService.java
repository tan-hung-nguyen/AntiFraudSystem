package com.tanhung.antifraudsystem.service;

import com.tanhung.antifraudsystem.dto.request.TransactionRequest;
import com.tanhung.antifraudsystem.dto.response.ActionResponse;
import com.tanhung.antifraudsystem.dto.response.StatusResponse;
import com.tanhung.antifraudsystem.exception.IPAddressException;
import com.tanhung.antifraudsystem.exception.InvalidAmountException;
import com.tanhung.antifraudsystem.exception.StolenCardException;
import com.tanhung.antifraudsystem.model.CardValidator;
import com.tanhung.antifraudsystem.model.StolenCard;
import com.tanhung.antifraudsystem.model.SuspiciousIPAddress;
import com.tanhung.antifraudsystem.repo.StolenCardRepo;
import com.tanhung.antifraudsystem.repo.SuspiciousIPRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Service
public class AntiFraudService {

    private final SuspiciousIPRepo suspiciousIPRepo;
    private final StolenCardRepo stolenCardRepo;

    @Autowired
    public AntiFraudService(SuspiciousIPRepo suspiciousIPRepo, StolenCardRepo stolenCardRepo){
        this.suspiciousIPRepo = suspiciousIPRepo;
        this.stolenCardRepo = stolenCardRepo;
    }

    public ActionResponse checkRequest(TransactionRequest request) throws InvalidAmountException{

        String result;
        Set<String> infos = new TreeSet<>();
        if(request.getAmount().compareTo(new BigDecimal("1")) < 0)
            throw new InvalidAmountException("Your amount must be at least 1 dollar!");

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

        boolean isStolenCard = stolenCardRepo.existsStolenCardByCardNumber(request.getNumber());
        boolean isSuspiciousIp = suspiciousIPRepo.existsByIp(request.getIp());
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
    public SuspiciousIPAddress addIP(SuspiciousIPAddress ipAddress) throws IPAddressException{
        if(ipAddress == null){
            throw new IPAddressException("IP object cannot be null!", HttpStatus.BAD_REQUEST);
        }
        if(suspiciousIPRepo.existsByIp(ipAddress.getIp())){
            throw new IPAddressException(ipAddress.getIp() + " is existed in the list!", HttpStatus.CONFLICT);
        }
        ipAddress.setId(null);
        return suspiciousIPRepo.save(ipAddress);
    }

    @Transactional
    public StatusResponse deleteIP(String ip) throws IPAddressException{
        if(ip == null) throw new IPAddressException("IP address must not be null!", HttpStatus.BAD_REQUEST);

        if(!suspiciousIPRepo.existsByIp(ip)){
            throw new IPAddressException("IP address not found!", HttpStatus.NOT_FOUND);
        }

        suspiciousIPRepo.deleteByIp(ip);
        return new StatusResponse("IP " + ip + " successfully removed!");
    }

    public List<SuspiciousIPAddress> getAllSuspiciousIp(){
        return suspiciousIPRepo.findAll(Sort.by("id"));
    }

    @Transactional
    public StolenCard addStolenCardNumber(StolenCard card) throws  StolenCardException{
        if(card == null) throw new StolenCardException("Your card must not be null!", HttpStatus.BAD_REQUEST);
        if(!CardValidator.isValidCardNumber(card.getCardNumber())){
            throw new StolenCardException("Card number not valid!", HttpStatus.BAD_REQUEST);
        }

        if(stolenCardRepo.existsStolenCardByCardNumber(card.getCardNumber())){
            throw new StolenCardException(card.getCardNumber() + " is existed in the list!", HttpStatus.CONFLICT);
        }

        card.setId(null);
        return stolenCardRepo.save(card);
    }

    @Transactional
    public StatusResponse deleteStolenCardNumber(String cardNumber){
        if(cardNumber == null) throw new StolenCardException("Your card number must not be null!", HttpStatus.BAD_REQUEST);
        if(!stolenCardRepo.existsStolenCardByCardNumber(cardNumber)) {
            throw new StolenCardException(cardNumber + " not found!", HttpStatus.NOT_FOUND);
        }

        stolenCardRepo.deleteStolenCardByCardNumber(cardNumber);
        return new StatusResponse("Card " + cardNumber + " successfully removed!");
    }

    public List<StolenCard> getAllStolenCards(){
        return stolenCardRepo.findAll(Sort.by("id"));
    }
}
