package com.smartDine.dto.auth;
import com.smartDine.entity.Role;
import com.smartDine.entity.User;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public  class LoginResponse {
        private String token;
        private long expiresIn;
        private Long userId;
        private String name;
        private String email;
        private Role role ;

      public static LoginResponse fromEntity( User user) {
          LoginResponse dto = new LoginResponse();
          dto.setUserId(user.getId());
            dto.setName( user.getName());
            dto.setEmail(user.getEmail());
          dto.setRole(user.getRole());
          return dto;
}
}
