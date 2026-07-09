package com.tanhung.antifraudsystem.controller;

import com.tanhung.antifraudsystem.dto.request.StolenCardNumberRequestDto;
import com.tanhung.antifraudsystem.dto.response.StatusResponseDto;
import com.tanhung.antifraudsystem.dto.response.StolenCardResponseDto;
import com.tanhung.antifraudsystem.exception.InvalidCardNumberException;
import com.tanhung.antifraudsystem.service.StolenCardAdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/antifraud/stolencard")
@Validated
public class StolenCardController {
    private final StolenCardAdminService stolenCardAdminService;

    @Autowired
    public StolenCardController(StolenCardAdminService service){
        this.stolenCardAdminService = service;
    }

    @PostMapping
    public ResponseEntity<StolenCardResponseDto> addStolenCardNumber(@RequestBody @Valid StolenCardNumberRequestDto card){
        StolenCardResponseDto result = stolenCardAdminService.addStolenCardNumber(card);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @DeleteMapping("/{number}")
    public ResponseEntity<StatusResponseDto> deleteStolenCardNumber(
            @PathVariable
            @Pattern(regexp = "^\\d{16}$", message = "Card number must be exactly 16 digits")
            @NotBlank(message = "Card number must not be null nor blank") String number){

        StatusResponseDto response = stolenCardAdminService.deleteStolenCardNumber(number);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<StolenCardResponseDto>> getStolenCardList(){
        List<StolenCardResponseDto> cards = stolenCardAdminService.getStolenCardList();
        return ResponseEntity.ok(cards);
    }
}
