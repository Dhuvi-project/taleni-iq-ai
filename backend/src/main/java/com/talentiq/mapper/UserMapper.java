package com.talentiq.mapper;

import com.talentiq.dto.response.UserResponse;
import com.talentiq.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toUserResponse(User user);
}
