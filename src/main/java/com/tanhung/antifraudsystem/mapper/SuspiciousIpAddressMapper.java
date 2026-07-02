package com.tanhung.antifraudsystem.mapper;

import com.tanhung.antifraudsystem.dto.request.SuspiciousIpRequestDto;
import com.tanhung.antifraudsystem.dto.response.IPResponseDto;
import com.tanhung.antifraudsystem.model.SuspiciousIPAddress;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SuspiciousIpAddressMapper {
    SuspiciousIPAddress toEntity(SuspiciousIpRequestDto request);

    IPResponseDto toDto(SuspiciousIPAddress ipAddress);
}
