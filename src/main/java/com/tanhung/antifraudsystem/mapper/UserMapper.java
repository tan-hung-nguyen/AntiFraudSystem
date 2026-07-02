package com.tanhung.antifraudsystem.mapper;

import com.tanhung.antifraudsystem.dto.request.UserRegistrationRequestDto;
import com.tanhung.antifraudsystem.dto.response.UserResponseDto;
import com.tanhung.antifraudsystem.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(UserRegistrationRequestDto dto);

    @Mappings({
            @Mapping(target = "name",
                    expression = "java(user.getFirstName() + \" \" + user.getLastName())"),
            @Mapping(target = "role",
                    expression = "java(user.getRole().getRoleValue())")
    })
    UserResponseDto toDto(User user);
}
