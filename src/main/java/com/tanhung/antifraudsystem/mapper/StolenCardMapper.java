package com.tanhung.antifraudsystem.mapper;

import com.tanhung.antifraudsystem.dto.request.StolenCardRequest;
import com.tanhung.antifraudsystem.dto.response.StolenCardResponse;
import com.tanhung.antifraudsystem.model.StolenCard;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StolenCardMapper {

    StolenCard toEntity(StolenCardRequest request);

    StolenCardResponse toDto(StolenCard card);
}
