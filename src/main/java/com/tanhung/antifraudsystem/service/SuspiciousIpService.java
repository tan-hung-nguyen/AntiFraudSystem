package com.tanhung.antifraudsystem.service;

import com.tanhung.antifraudsystem.dto.request.SuspiciousIpRequestDto;
import com.tanhung.antifraudsystem.dto.response.IPResponseDto;
import com.tanhung.antifraudsystem.exception.IPAddressConflictException;
import com.tanhung.antifraudsystem.exception.IPAddressNotFoundException;
import com.tanhung.antifraudsystem.mapper.SuspiciousIpAddressMapper;
import com.tanhung.antifraudsystem.model.SuspiciousIPAddress;
import com.tanhung.antifraudsystem.repo.SuspiciousIPRepo;
import com.tanhung.antifraudsystem.validators.SuspiciousIpValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SuspiciousIpService {
    private final SuspiciousIPRepo suspiciousIPRepo;
    private final SuspiciousIpAddressMapper suspiciousIpAddressMapper;
    private final SuspiciousIpValidator suspiciousIpValidator;

    public SuspiciousIPAddress addIp(SuspiciousIpRequestDto requestIp){
        String ipAddress = requestIp.getIpAddress();
        if(suspiciousIpValidator.isSuspiciousIp(ipAddress)){
            throw new IPAddressConflictException(ipAddress + " already exists in the suspicious ip list!");
        }
        SuspiciousIPAddress ipEntity = suspiciousIpAddressMapper.toEntity(requestIp);
        return suspiciousIPRepo.save(ipEntity);
    }

    public void deleteIp(String ip){
        if(!suspiciousIpValidator.isSuspiciousIp(ip)){
            throw new IPAddressNotFoundException(ip + " not found!");
        }
        suspiciousIPRepo.deleteByIpAddress(ip);
    }

    public IPResponseDto convertToDto(SuspiciousIPAddress ipAddress){
        return suspiciousIpAddressMapper.toDto(ipAddress);
    }

    public List<IPResponseDto> getAllSuspiciousIps(){
        return suspiciousIPRepo.findAll(Sort.by("id"))
                .stream()
                .map(suspiciousIpAddressMapper::toDto)
                .toList();
    }
}
