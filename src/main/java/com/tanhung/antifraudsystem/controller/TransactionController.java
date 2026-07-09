package com.tanhung.antifraudsystem.controller;

import com.tanhung.antifraudsystem.dto.request.TransactionRequestDto;
import com.tanhung.antifraudsystem.dto.response.ActionResponseDto;
import com.tanhung.antifraudsystem.service.TransactionReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/antifraud")
public class TransactionController {

    private final TransactionReviewService transactionReviewService;

    @Autowired
    public TransactionController(TransactionReviewService service){
        this.transactionReviewService = service;
    }

    @PostMapping("/transaction")
    public ResponseEntity<ActionResponseDto> requestTransaction(@RequestBody @Valid TransactionRequestDto transactionRequestDto){
        ActionResponseDto result = transactionReviewService.reviewRequest(transactionRequestDto);
        return ResponseEntity.ok(result);
    }
}
