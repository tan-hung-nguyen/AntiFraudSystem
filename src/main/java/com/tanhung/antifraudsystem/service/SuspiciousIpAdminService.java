package com.tanhung.antifraudsystem.service;

import com.tanhung.antifraudsystem.dto.request.SuspiciousIpRequestDto;
import com.tanhung.antifraudsystem.dto.response.IPResponseDto;
import com.tanhung.antifraudsystem.dto.response.StatusResponseDto;
import com.tanhung.antifraudsystem.exception.IPAddressNullException;
import com.tanhung.antifraudsystem.model.SuspiciousIPAddress;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SuspiciousIpAdminService {
    private final SuspiciousIpService suspiciousIpService;

    @Transactional
    public IPResponseDto addIP(SuspiciousIpRequestDto ipAddress){
        if(ipAddress == null){
            throw new IPAddressNullException("IP object cannot be null!");
        }
        SuspiciousIPAddress ip = suspiciousIpService.addIp(ipAddress);
        return suspiciousIpService.convertToDto(ip);
    }

    @Transactional
    public StatusResponseDto deleteIP(String ip){
        if(ip == null){
            throw new IPAddressNullException("IP address must not be null!");
        }
        return proceedDeleteIpAddress(ip);
    }

    private StatusResponseDto proceedDeleteIpAddress(String ip){
        suspiciousIpService.deleteIp(ip);
        return new StatusResponseDto("IP " + ip + " successfully removed!");
    }

    public List<IPResponseDto> getSuspiciousIpList(){
        return suspiciousIpService.getAllSuspiciousIps();
    }
}
