package com.per.user.mapper;

import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.per.auth.entity.Role;
import com.per.user.dto.response.UserResponse;
import com.per.user.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

	@Mapping(target = "roles", source = "roles", qualifiedByName = "mapRoles")
	UserResponse toResponse(User user);

	@Named("mapRoles")
	default Set<String> mapRoles(Set<Role> roles) {
		if (roles == null || roles.isEmpty()) {
			return Set.of();
		}
		return roles.stream().map(role -> role.getName().name()).collect(Collectors.toSet());
	}
}
