package com.tanhung.antifraudsystem.mapper;

import com.tanhung.antifraudsystem.dto.request.StolenCardNumberRequestDto;
import com.tanhung.antifraudsystem.dto.response.StolenCardResponseDto;
import com.tanhung.antifraudsystem.model.StolenCard;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StolenCardMapper {

    StolenCard toEntity(StolenCardNumberRequestDto request);

    StolenCardResponseDto toDto(StolenCard card);
}
