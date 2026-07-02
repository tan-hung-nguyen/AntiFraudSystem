package com.tanhung.antifraudsystem.mapper;

import com.tanhung.antifraudsystem.dto.request.TransactionRequestDto;
import com.tanhung.antifraudsystem.exception.InvalidTransactionDateFormatException;
import com.tanhung.antifraudsystem.model.Region;
import com.tanhung.antifraudsystem.model.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

@Mapper(componentModel = "spring", imports = {Region.class})
public interface TransactionMapper {
    @Mappings({
            @Mapping(target = "region",
                    expression = "java(Region.builder().code(dto.getRegion().toUpperCase()).build())"),
            @Mapping(target = "date", source = "date", qualifiedByName = "parseDate")
    })
    Transaction toEntity(TransactionRequestDto dto);

    @Named("parseDate")
    default LocalDateTime parseDate(String date){
        try{
            return LocalDateTime.parse(date);
        } catch (DateTimeParseException e){
            throw new InvalidTransactionDateFormatException("Invalid date format: " + date, HttpStatus.BAD_REQUEST);
        }
    }
}
