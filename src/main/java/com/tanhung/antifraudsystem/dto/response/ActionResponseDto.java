package com.tanhung.antifraudsystem.dto.response;

import com.tanhung.antifraudsystem.enums.TransactionResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ActionResponseDto {
    private TransactionResult result;
    private String info;

}
