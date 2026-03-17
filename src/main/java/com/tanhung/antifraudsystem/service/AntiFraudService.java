package com.tanhung.antifraudsystem.service;

import com.tanhung.antifraudsystem.dto.response.ActionResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AntiFraudService {

    public ActionResponse checkAmount(BigDecimal amount){
        String result;
        if(amount.compareTo(new BigDecimal(200)) <= 0){
            result = "ALLOWED";
        } else if(amount.compareTo(new BigDecimal(1500)) <= 0) {
            result = "MANUAL_PROCESSING";
        } else {
            result = "PROHIBITED";
        }

        return new ActionResponse(result);
    }

}
