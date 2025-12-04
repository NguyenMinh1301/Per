CREATE TABLE user_roles (
	user_id uuid NOT NULL,
	role_id int8 NOT NULL,
	CONSTRAINT user_roles_pkey PRIMARY KEY (user_id, role_id),
	CONSTRAINT user_roles_role_id_fkey FOREIGN KEY (role_id) REFERENCES roles(id),
	CONSTRAINT user_roles_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
