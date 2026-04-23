package com.tanhung.antifraudsystem.mapper;

import com.tanhung.antifraudsystem.dto.request.SuspiciousIpRequest;
import com.tanhung.antifraudsystem.dto.response.IPResponse;
import com.tanhung.antifraudsystem.model.SuspiciousIPAddress;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SuspiciousIpAddressMapper {
    SuspiciousIPAddress toEntity(SuspiciousIpRequest request);

    IPResponse toDto(SuspiciousIPAddress ipAddress);
}
