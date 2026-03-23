package com.tanhung.antifraudsystem.mapper;

import com.tanhung.antifraudsystem.dto.request.UserRegistrationRequest;
import com.tanhung.antifraudsystem.dto.response.UserRegistrationResponse;
import com.tanhung.antifraudsystem.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(UserRegistrationRequest dto);

    @Mapping(target = "name",
    expression = "java(user.getFirstName() + \" \" + user.getLastName())")
    UserRegistrationResponse toDto(User user);
}
