package com.tanhung.antifraudsystem.controller;

import com.tanhung.antifraudsystem.dto.request.AmountRequest;
import com.tanhung.antifraudsystem.dto.response.ActionResponse;
import com.tanhung.antifraudsystem.service.AntiFraudService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AntiFraudController {

    private final AntiFraudService antiFraudService;

    @Autowired
    public AntiFraudController(AntiFraudService service){
        antiFraudService = service;
    }

    @PostMapping("/antifraud/transaction")
    public ResponseEntity<ActionResponse> requestTransaction(@RequestBody @Valid AmountRequest amountRequest){
        ActionResponse result = antiFraudService.checkAmount(amountRequest.amount());

        return ResponseEntity.ok(result);
    }

}
