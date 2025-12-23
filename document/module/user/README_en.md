User Module Overview
====================

The user module provides admin-only management of application users. It extends the authentication core by exposing CRUD APIs, role assignment, and pagination/search functionality for administrators.

Responsibilities
----------------

* Administer user accounts (create, update, delete).
* Provide pageable search with optional query across username, email, first/last name.
* Enforce uniqueness on usernames and emails.
* Manage role assignments and password hashing.
* Expose REST endpoints protected by `ROLE_ADMIN`.

Key Packages
------------

| Package | Description |
| --- | --- |
| `controller` | `UserController` (annotated with `@PreAuthorize("hasRole('ADMIN')")`). |
| `dto.request` | `UserCreateRequest`, `UserUpdateRequest`. |
| `dto.response` | `UserResponse`. |
| `entity` | `User` JPA entity (defined in migration `V1__init_schema.sql`). |
| `mapper` | `UserMapper` (static mapper methods to convert entities to DTOs). |
| `repository` | `UserAdminRepository` (extends JPA for admin ops) and `RoleAdminRepository` for role lookup. |
| `service` | `UserService` contract and `UserServiceImpl` implementation. |

Request Flow Summary
--------------------

1. Requests hit `UserController` under `/api/v1/users`. All endpoints require admin authentication.
2. Controller delegates to `UserService`, wrapping results with `ApiResponse` and success codes (`USER_CREATE_SUCCESS`, etc.).
3. Service layer operations:
   * **Create** – validate unique username/email, resolve requested roles (default to `USER`), hash password via `PasswordEncoder`, persist the new user.
   * **Update** – selective field updates, enforce unique username/email when changed, re-hash password when provided, reassign roles.
   * **Search/List** – page through users with optional `q` parameter that feeds repository search.
   * **Delete** – remove user by ID.
4. Repository methods:
   * `UserAdminRepository.search` – JPQL query across username/email/first/last name.
   * `RoleAdminRepository.findByName` – ensures roles exist before assignment.

API Contracts
-------------

| Endpoint | Description | Success Code |
| --- | --- | --- |
| `GET /api/v1/users/search` | Paginated search (optional `q`). | `USER_SEARCH_SUCCESS` |
| `GET /api/v1/users/{id}` | Fetch single user. | `USER_FETCH_SUCCESS` |
| `POST /api/v1/users` | Create new user. | `USER_CREATE_SUCCESS` |
| `PUT /api/v1/users/{id}` | Update user. | `USER_UPDATE_SUCCESS` |
| `DELETE /api/v1/users/{id}` | Remove user. | `USER_DELETE_SUCCESS` |

Validation & Error Codes
------------------------

* `USER_USERNAME_CONFLICT` – username already exists.
* `USER_EMAIL_CONFLICT` – email already exists.
* `NOT_FOUND` – user not found for the given ID.
* `BAD_REQUEST` – thrown when requested roles do not exist.
* Bean validation annotations on DTOs ensure mandatory fields and formats.

Security Considerations
-----------------------

* Controller-level `@PreAuthorize` restricts module access to administrators.
* Passwords are hashed via the shared `PasswordEncoder` before persistence.
* Role assignment uses `RoleType` enum to avoid arbitrary role strings.

Testing
-------

Unit tests should cover `UserServiceImpl`:
* Create/update flows (including hashing and role resolution).
* Duplicate detection for username/email.
* Search and pagination using mocked repository responses.

Run targeted tests with:
```
mvn -Dtest=UserServiceImplTest test
```

Extending the Module
--------------------

* **Audit history** – add entity listeners or a separate audit table to track user changes.
* **Bulk operations** – introduce batch endpoints to disable/activate multiple users at once.
* **Advanced filters** – extend repository queries for role-based or status-based filtering.
* **Soft delete** – leverage the existing `isActive` flag to deactivate accounts instead of hard deletion.
