package com.tanhung.antifraudsystem.controller;

import com.tanhung.antifraudsystem.dto.request.TransactionRequest;
import com.tanhung.antifraudsystem.dto.response.ActionResponse;
import com.tanhung.antifraudsystem.dto.response.StatusResponse;
import com.tanhung.antifraudsystem.model.StolenCard;
import com.tanhung.antifraudsystem.model.SuspiciousIPAddress;
import com.tanhung.antifraudsystem.service.AntiFraudService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/antifraud")
@Validated
public class AntiFraudController {

    private final AntiFraudService antiFraudService;

    @Autowired
    public AntiFraudController(AntiFraudService service){
        antiFraudService = service;
    }

    @PostMapping("/transaction")
    public ResponseEntity<ActionResponse> requestTransaction(@RequestBody @Valid TransactionRequest transactionRequest){
        ActionResponse result = antiFraudService.checkRequest(transactionRequest);

        return ResponseEntity.ok(result);
    }


    //      =================================suspicious-ip=================================
    @PostMapping("/suspicous-ip")
    public ResponseEntity<SuspiciousIPAddress> addSuspiciousIPAddress(@RequestBody @Valid SuspiciousIPAddress ipAddress){
        SuspiciousIPAddress response = antiFraudService.addIP(ipAddress);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/suspicious-ip/{ip}")
    public ResponseEntity<StatusResponse> deleteSuspiciousIP(
      @PathVariable
      @Pattern(regexp = "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$",
              message = "Your ip in wrong format!")
      @NotNull(message = "IP address must not be null!") String ip) {

        StatusResponse response = antiFraudService.deleteIP(ip);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/suspicious-ip")
    public ResponseEntity<List<SuspiciousIPAddress>> getAllSuspiciousIP(){
        List<SuspiciousIPAddress> ipList = antiFraudService.getAllSuspiciousIp();

        return ResponseEntity.ok(ipList);
    }

    //      =================================stolencard=================================
    @PostMapping("/stolencard")
    public ResponseEntity<StolenCard> addStolenCardNumber(@RequestBody @Valid StolenCard card){
        StolenCard result = antiFraudService.addStolenCardNumber(card);

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @DeleteMapping("/stolencard/{number}")
    public ResponseEntity<StatusResponse> deleteStolenCardNumber(
            @PathVariable
            @NotNull(message = "Card number must not be null!")
            @Pattern(regexp = "^\\d{16}$", message = "Card number must be exactly 16 digits") String number){

        StatusResponse response = antiFraudService.deleteStolenCardNumber(number);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stolencard")
    public ResponseEntity<List<StolenCard>> getAllStolenCards(){
        List<StolenCard> cards = antiFraudService.getAllStolenCards();

        return ResponseEntity.ok(cards);
    }
}
