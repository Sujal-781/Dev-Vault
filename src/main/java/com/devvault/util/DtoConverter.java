package com.devvault.util;

import com.devvault.dto.UserResponseDTO;
import com.devvault.model.User;

public class DtoConverter {

    public static UserResponseDTO toUserResponse(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setRewardPoints(user.getRewardPoints());
        return dto;
    }
}
