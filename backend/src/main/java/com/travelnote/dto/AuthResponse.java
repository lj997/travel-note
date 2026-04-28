package com.travelnote.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String token;
    private String type = "Bearer";
    private Long userId;
    private String username;
    private String email;
    private String nickname;
    private String avatar;
    
    public AuthResponse(String token, Long userId, String username, String email, String nickname, String avatar) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.nickname = nickname;
        this.avatar = avatar;
    }
}
