package com.per.auth.service;

import com.per.auth.dto.response.user.MeResponse;

public interface MeService {
    MeResponse getCurrentUser();
}
