package com.tanhung.antifraudsystem.controller;

import com.tanhung.antifraudsystem.dto.request.AmountRequest;
import com.tanhung.antifraudsystem.dto.response.ActionResponse;
import com.tanhung.antifraudsystem.dto.response.StatusResponse;
import com.tanhung.antifraudsystem.model.SuspiciousIPAddress;
import com.tanhung.antifraudsystem.service.AntiFraudService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/antifraud")
public class AntiFraudController {

    private final AntiFraudService antiFraudService;

    @Autowired
    public AntiFraudController(AntiFraudService service){
        antiFraudService = service;
    }

    @PostMapping("/transaction")
    public ResponseEntity<ActionResponse> requestTransaction(@RequestBody @Valid AmountRequest amountRequest){
        ActionResponse result = antiFraudService.checkAmount(amountRequest.amount());

        return ResponseEntity.ok(result);
    }


    //      =================================suspicious-ip=================================
    @PostMapping("/suspicous-ip")
    public ResponseEntity<SuspiciousIPAddress> addSuspiciousIPAddress(@RequestBody SuspiciousIPAddress ipAddress){
        SuspiciousIPAddress response = antiFraudService.addIP(ipAddress);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/suspicious-ip/{ip}")
    public ResponseEntity<StatusResponse> deleteSuspiciousIP(@PathVariable String ip){
        SuspiciousIPAddress ipAddress = new SuspiciousIPAddress(null, ip);
        StatusResponse response = antiFraudService.deleteIP(ipAddress);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/suspicious-ip")
    public ResponseEntity<List<SuspiciousIPAddress>> getAllSuspiciousIP(){
        List<SuspiciousIPAddress> ipList = antiFraudService.getAllSuspiciousIp();

        return ResponseEntity.ok(ipList);
    }

    //      =================================stolencard=================================
}
