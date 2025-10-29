package com.per.auth.service.me.impl;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.per.auth.dto.response.user.MeResponse;
import com.per.auth.mapper.AuthUserMapper;
import com.per.auth.repository.UserRepository;
import com.per.auth.service.MeService;
import com.per.common.exception.ApiErrorCode;
import com.per.common.exception.ApiException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MeServiceImpl implements MeService {

	private final UserRepository userRepository;
	private final AuthUserMapper authUserMapper;

	@Override
	public MeResponse getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new ApiException(ApiErrorCode.UNAUTHORIZED, "Unauthorized");
		}
		String username = authentication.getName();
		return userRepository
				.findByUsername(username)
				.map(authUserMapper::toMeResponse)
				.orElseThrow(() -> new ApiException(ApiErrorCode.NOT_FOUND, "User not found"));
	}
}
