Tổng Quan Module User
=====================

Module user cung cấp admin-only management cho application users. Nó extend authentication core bằng cách expose CRUD APIs, role assignment, và pagination/search functionality cho administrators.

Trách Nhiệm
-----------

* Administer user accounts (create, update, delete).
* Cung cấp pageable search với optional query across username, email, first/last name.
* Enforce uniqueness trên usernames và emails.
* Quản lý role assignments và password hashing.
* Expose REST endpoints được protect bởi `ROLE_ADMIN`.

Các Packages Chính
------------------

| Package | Mô tả |
| --- | --- |
| `controller` | `UserController` (annotated với `@PreAuthorize("hasRole('ADMIN')")`). |
| `dto.request` | `UserCreateRequest`, `UserUpdateRequest`. |
| `dto.response` | `UserResponse`. |
| `entity` | `User` JPA entity (defined trong migration `V1__init_schema.sql`). |
| `mapper` | `UserMapper` (static mapper methods để convert entities thành DTOs). |
| `repository` | `UserAdminRepository` (extends JPA cho admin ops) và `RoleAdminRepository` cho role lookup. |
| `service` | `UserService` contract và `UserServiceImpl` implementation. |

Tóm Tắt Request Flow
--------------------

1. Requests hit `UserController` dưới `/api/v1/users`. Tất cả endpoints require admin authentication.
2. Controller ủy quyền cho `UserService`, wrap results với `ApiResponse` và success codes (`USER_CREATE_SUCCESS`, v.v.).
3. Service layer operations:
   * **Create** – validate unique username/email, resolve requested roles (default thành `USER`), hash password qua `PasswordEncoder`, persist new user.
   * **Update** – selective field updates, enforce unique username/email khi changed, re-hash password khi provided, reassign roles.
   * **Search/List** – page through users với optional `q` parameter feeds repository search.
   * **Delete** – remove user theo ID.
4. Repository methods:
   * `UserAdminRepository.search` – JPQL query across username/email/first/last name.
   * `RoleAdminRepository.findByName` – đảm bảo roles exist trước assignment.

API Contracts
-------------

| Endpoint | Mô tả | Success Code |
| --- | --- | --- |
| `GET /api/v1/users/search` | Paginated search (optional `q`). | `USER_SEARCH_SUCCESS` |
| `GET /api/v1/users/{id}` | Fetch single user. | `USER_FETCH_SUCCESS` |
| `POST /api/v1/users` | Create new user. | `USER_CREATE_SUCCESS` |
| `PUT /api/v1/users/{id}` | Update user. | `USER_UPDATE_SUCCESS` |
| `DELETE /api/v1/users/{id}` | Remove user. | `USER_DELETE_SUCCESS` |

Validation & Error Codes
------------------------

* `USER_USERNAME_CONFLICT` – username đã exists.
* `USER_EMAIL_CONFLICT` – email đã exists.
* `NOT_FOUND` – user not found cho given ID.
* `BAD_REQUEST` – thrown khi requested roles không exist.
* Bean validation annotations trên DTOs đảm bảo mandatory fields và formats.

Cân Nhắc Security
-----------------

* Controller-level `@PreAuthorize` restrict module access cho administrators.
* Passwords được hash qua shared `PasswordEncoder` trước khi persist.
* Role assignment sử dụng `RoleType` enum để tránh arbitrary role strings.

Testing
-------

Unit tests nên cover `UserServiceImpl`:
* Create/update flows (including hashing và role resolution).
* Duplicate detection cho username/email.
* Search và pagination sử dụng mocked repository responses.

Chạy targeted tests với:
```
mvn -Dtest=UserServiceImplTest test
```

Mở Rộng Module
--------------

* **Audit history** – thêm entity listeners hoặc separate audit table để track user changes.
* **Bulk operations** – introduce batch endpoints để disable/activate multiple users cùng lúc.
* **Advanced filters** – extend repository queries cho role-based hoặc status-based filtering.
* **Soft delete** – leverage existing `isActive` flag để deactivate accounts thay vì hard deletion.
