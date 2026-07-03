package com.tanhung.antifraudsystem.controller;

import com.tanhung.antifraudsystem.dto.request.SuspiciousIpRequestDto;
import com.tanhung.antifraudsystem.dto.response.IPResponseDto;
import com.tanhung.antifraudsystem.dto.response.StatusResponseDto;
import com.tanhung.antifraudsystem.service.SuspiciousIpAdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/antifraud/suspicious-ip")
public class SuspiciousIpController {

    private final SuspiciousIpAdminService suspiciousIpAdminService;

    @Autowired
    public SuspiciousIpController(SuspiciousIpAdminService service){
        this.suspiciousIpAdminService = service;
    }

    @PostMapping
    public ResponseEntity<IPResponseDto> addSuspiciousIPAddress(@RequestBody @Valid SuspiciousIpRequestDto ipAddress){
        IPResponseDto response = suspiciousIpAdminService.addIP(ipAddress);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{ip:.+}")
    public ResponseEntity<StatusResponseDto> deleteSuspiciousIP(
            @PathVariable
            @Pattern(regexp = "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$",
                    message = "Your ip address must be IPV4 format!")
            @NotNull(message = "IP address must not be null!") String ip) {
        StatusResponseDto response = suspiciousIpAdminService.deleteIP(ip);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<IPResponseDto>> getSuspiciousIpList(){
        List<IPResponseDto> ipList = suspiciousIpAdminService.getSuspiciousIpList();
        return ResponseEntity.ok(ipList);
    }
}
