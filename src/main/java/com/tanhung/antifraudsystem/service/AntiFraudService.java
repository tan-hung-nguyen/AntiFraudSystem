package com.tanhung.antifraudsystem.service;

import com.tanhung.antifraudsystem.dto.response.ActionResponse;
import com.tanhung.antifraudsystem.dto.response.IPResponse;
import com.tanhung.antifraudsystem.dto.response.StatusResponse;
import com.tanhung.antifraudsystem.exception.IPAddressException;
import com.tanhung.antifraudsystem.exception.InvalidAmountException;
import com.tanhung.antifraudsystem.model.SuspiciousIPAddress;
import com.tanhung.antifraudsystem.repo.SuspiciousIPRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AntiFraudService {

    private final SuspiciousIPRepo suspiciousIPRepo;

    @Autowired
    public AntiFraudService(SuspiciousIPRepo suspiciousIPRepo){
        this.suspiciousIPRepo = suspiciousIPRepo;
    }

    public ActionResponse checkAmount(BigDecimal amount) throws InvalidAmountException{
        String result;

        if(amount == null)
            throw new InvalidAmountException("Your amount cannot be null!");

        if(amount.compareTo(new BigDecimal("1")) < 0)
            throw new InvalidAmountException("Your amount must be at least 1 dollar!");

        if(amount.compareTo(new BigDecimal(200)) <= 0){
            result = "ALLOWED";
        } else if(amount.compareTo(new BigDecimal(1500)) <= 0) {
            result = "MANUAL_PROCESSING";
        } else {
            result = "PROHIBITED";
        }

        return new ActionResponse(result);
    }

    public SuspiciousIPAddress addIP(SuspiciousIPAddress ipAddress) throws IPAddressException{
        if(ipAddress.getIp() == null){
            throw new IPAddressException("Your IP cannot be null!", HttpStatus.BAD_REQUEST);
        }
        if(suspiciousIPRepo.existsByIp(ipAddress.getIp())){
            throw new IPAddressException(ipAddress.getIp() + " is already existed", HttpStatus.CONFLICT);
        }
        ipAddress.setId(null);
        return suspiciousIPRepo.save(ipAddress);
    }

    public StatusResponse deleteIP(SuspiciousIPAddress ip) throws IPAddressException{
        if(ip == null || ip.getIp() == null ) throw new IPAddressException("IP cannot be null!", HttpStatus.BAD_REQUEST);

        if(!suspiciousIPRepo.existsByIp(ip.getIp())){
            throw new IPAddressException("IP not found!", HttpStatus.NOT_FOUND);
        }

        suspiciousIPRepo.deleteByIp(ip.getIp());
        return new StatusResponse("IP " + ip.getIp() + " successfully removed!");
    }

    public List<SuspiciousIPAddress> getAllSuspiciousIp(){
        return suspiciousIPRepo.findAll(Sort.by("id"));
    }
}
