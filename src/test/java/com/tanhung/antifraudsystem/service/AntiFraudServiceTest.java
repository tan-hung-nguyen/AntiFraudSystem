package com.tanhung.antifraudsystem.service;

import com.tanhung.antifraudsystem.dto.response.ActionResponse;
import com.tanhung.antifraudsystem.exception.InvalidAmountException;
import org.junit.jupiter.api.Test;


import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
class AntiFraudServiceTest {

    AntiFraudService antiFraudService = new AntiFraudService();

    @Test
    void shouldReturnAllowed_whenAmountBetween_1_And_200(){
        ActionResponse actual = antiFraudService.checkAmount(new BigDecimal("170.00"));

        assertEquals("ALLOWED", actual.result());

    }

    @Test
    void shouldReturnAllowed_whenAmountIsExact1(){
        ActionResponse actual = antiFraudService.checkAmount(new BigDecimal("1.00"));

        assertEquals("ALLOWED", actual.result());

    }

    @Test
    void shouldReturnAllowed_whenAmountIsExact200(){
        ActionResponse actual = antiFraudService.checkAmount(new BigDecimal("200.00"));
        assertEquals("ALLOWED", actual.result());
    }

    @Test
    void shouldReturnManualProcessing_whenAmountIs200point01(){
        ActionResponse actual = antiFraudService.checkAmount(new BigDecimal("200.01"));

        assertEquals("MANUAL_PROCESSING", actual.result());

    }
    @Test
    void shouldReturnManualProcessing_whenAmountFrom_200_Exclusive_To_1500_Inclusive(){
        ActionResponse actual = antiFraudService.checkAmount(new BigDecimal("1200.50"));

        assertEquals("MANUAL_PROCESSING", actual.result());
    }

    @Test
    void shouldReturnManualProcessing_whenAmountIsExact1500(){
        ActionResponse actual = antiFraudService.checkAmount(new BigDecimal("1500.00"));

        assertEquals("MANUAL_PROCESSING", actual.result());

    }
    @Test
    void shouldReturnProhibited_whenAmountOver1500(){
        ActionResponse actual = antiFraudService.checkAmount(new BigDecimal("1700.00"));

        assertEquals("PROHIBITED",actual.result());
    }

    @Test
    void shouldReturnProhibited_whenAmountIs1500Point001(){
        ActionResponse actual = antiFraudService.checkAmount(new BigDecimal("1500.001"));

        assertEquals("PROHIBITED",actual.result());
    }

    @Test
    void shouldThrowInvalidAmountException_whenAmountIsLessThanOneDollar(){

        assertThrows(InvalidAmountException.class,
                () -> antiFraudService.checkAmount(new BigDecimal("0.9999999")));
    }

    @Test
    void shouldThrowInvalidAmountException_whenAmountIsNull(){
        assertThrows(InvalidAmountException.class, () -> antiFraudService.checkAmount(null));
    }


}