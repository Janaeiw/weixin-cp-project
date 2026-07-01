package com.ewcp.service;

import com.ewcp.entity.User;

public interface UserService {
    User getByUsername(String username);
}
