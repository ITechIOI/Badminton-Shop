package com.example.userservice.modules.Users.service;

import com.example.userservice.models.Roles;
import com.example.userservice.models.Users;
import com.example.userservice.modules.Roles.service.RoleService;
import com.example.userservice.modules.Users.dto.CreateUserDto;
import com.example.userservice.modules.Users.dto.UpdateUserDto;
import com.example.userservice.modules.Users.dto.UserResponse;
import com.example.userservice.modules.Users.repository.UserRepository;
import com.example.userservice.utils.NotFoundException;
import com.example.userservice.utils.NullAwareBeanUtilsBean;
import com.example.userservice.utils.PagedResponse;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.keycloak.representations.idm.UserRepresentation;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final Keycloak keycloak;
    @Value("${keycloak.realm}") private String realm;

    private RealmResource realmResource() {
        return keycloak.realm(realm);
    }

    public Users findRawByKeycloakId(String keycloakId) {
        Users user = userRepository.findOneByKeycloakId(keycloakId);
        if (user == null) {
            throw new NotFoundException("User not found in MySQL");
        }
        return user;
    }

    public Users findRawById(Long id) {
        Users user = userRepository.findOneById(id);
        if (user == null) {
            throw new NotFoundException("User not found in MySQL");
        }
        return user;
    }

    public Users createUser(CreateUserDto dto) {
        // 1. Tạo UserRepresentation như bạn đã làm
        UserRepresentation userRep = new UserRepresentation();
        userRep.setUsername(dto.getUsername());
        userRep.setEmail(dto.getEmail());
        userRep.setEnabled(true);
        userRep.setFirstName(dto.getFirstName());
        userRep.setLastName(dto.getLastName());
        userRep.setEmailVerified(true);

// 2. Set custom attributes
        Map<String, List<String>> attributes = new HashMap<>();
        if (dto.getGender() != null) attributes.put("gender", List.of(dto.getGender()));
        if (dto.getAvatar() != null) attributes.put("avatar", List.of(dto.getAvatar()));
        if (dto.getPhone() != null) attributes.put("phone", List.of(dto.getPhone()));
        userRep.setAttributes(attributes);

// 3. Gửi request tạo user
        Response response = realmResource().users().create(userRep);
        if (response.getStatus() != 201) {
            throw new RuntimeException("Failed to create user in Keycloak: " + response.getStatusInfo());
        }
        String location = response.getHeaderString("Location");
        String keycloakId = location.substring(location.lastIndexOf('/') + 1);

// 4. Set mật khẩu
        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(dto.getPassword()); // đảm bảo dto.getPassword() != null

        realmResource()
                .users()
                .get(keycloakId)
                .resetPassword(passwordCred);

// 5. Gán role (nếu có)
        if (dto.getRoles() != null) {
            RoleRepresentation role = realmResource().roles().get(dto.getRoles()).toRepresentation();
            realmResource().users().get(keycloakId).roles().realmLevel().add(List.of(role));
        } else {
            // Gán role mặc định "client"
            RoleRepresentation defaultRole = realmResource().roles().get("user").toRepresentation();
            realmResource().users().get(keycloakId).roles().realmLevel().add(List.of(defaultRole));
        }

// 6. Lưu xuống MySQL
        Users user = new Users();
        user.setKeycloakId(keycloakId);
        return userRepository.save(user);

    }

    public UserResponse getUserByKeycloakId(String keycloakId) {
        // 1. Lấy user trong MySQL
        Users user = userRepository.findOneByKeycloakId(keycloakId);
        System.out.println("Keycloak ID: " + user.toString());
        if (user == null) {
            logger.error("Error fetching user from Keycloak: {}");
            throw new NotFoundException("User not found in MySQL");
        }



        // 2. Lấy thông tin user từ Keycloak
        UserRepresentation keycloakUser;
        try {
            keycloakUser = realmResource()
                    .users()
                    .get(keycloakId)
                    .toRepresentation();
        } catch (Exception e) {
            logger.error("Error fetching user from Keycloak: {}", e.getMessage());
            throw new NotFoundException("User not found in Keycloak with ID: " + keycloakId);
        }

        // 3. Lấy tên đầy đủ
        String fullName = String.format("%s %s",
                keycloakUser.getFirstName() != null ? keycloakUser.getFirstName() : "",
                keycloakUser.getLastName() != null ? keycloakUser.getLastName() : ""
        ).trim();
        if (fullName.isEmpty()) fullName = null;

        // 4. Lấy custom attributes từ Keycloak
        Map<String, List<String>> attributes = keycloakUser.getAttributes();
        String gender = attributes != null && attributes.containsKey("gender") ? attributes.get("gender").get(0) : null;
        String avatar = attributes != null && attributes.containsKey("avatar") ? attributes.get("avatar").get(0) : null;
        String phone = attributes != null && attributes.containsKey("phone") ? attributes.get("phone").get(0) : null;

        // 5. Lấy danh sách realm roles
        List<String> realmRoles;
        try {
            realmRoles = realmResource()
                    .users()
                    .get(keycloakId)
                    .roles()
                    .realmLevel()
                    .listAll()
                    .stream()
                    .map(RoleRepresentation::getName)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            realmRoles = new ArrayList<>();
        }

        String mainRole = null;
        if (realmRoles.contains("admin")) {
            mainRole = "admin";
        } else if (realmRoles.contains("client")) {
            mainRole = "client";
        }

        System.out.println("Main Role: " + mainRole);

        // 6. Trả về DTO
        return new UserResponse(
                user.getId(),
                fullName,
                gender,
                avatar,
                phone,
                keycloakUser.getEmail(),
                keycloakUser.getUsername(),
                mainRole  // Trả về List<String> role
        );
    }

    public PagedResponse<UserResponse> getAllUsers(int page, int limit) {
        try {
            // Lấy toàn bộ user từ Keycloak để lọc chính xác
            List<UserRepresentation> keycloakUsers = realmResource()
                    .users()
                    .search("", 0, Integer.MAX_VALUE);

            // Lọc và chuyển đổi sang danh sách hợp lệ
            List<UserResponse> allValidUsers = keycloakUsers.stream()
                    .map(user -> {
                        try {
                            Users userInMySQL = userRepository.findOneByKeycloakId(user.getId());
                            if (userInMySQL == null) return null;

                            String fullName = String.format("%s %s",
                                    Optional.ofNullable(user.getFirstName()).orElse(""),
                                    Optional.ofNullable(user.getLastName()).orElse("")
                            ).trim();

                            Map<String, List<String>> attributes = Optional.ofNullable(user.getAttributes()).orElse(new HashMap<>());
                            String gender = attributes.getOrDefault("gender", Collections.emptyList()).stream().findFirst().orElse(null);
                            String avatar = attributes.getOrDefault("avatar", Collections.emptyList()).stream().findFirst().orElse(null);
                            String phone = attributes.getOrDefault("phone", Collections.emptyList()).stream().findFirst().orElse(null);

                            // Lấy realm roles và tìm role "admin" hoặc "user"
                            List<RoleRepresentation> roles = realmResource()
                                    .users()
                                    .get(user.getId())
                                    .roles()
                                    .realmLevel()
                                    .listEffective();

                            String matchedRole = roles.stream()
                                    .map(RoleRepresentation::getName)
                                    .filter(role -> role.equals("admin") || role.equals("user"))
                                    .findFirst()
                                    .orElse(null); // Nếu không có thì set là null

                            return new UserResponse(
                                    userInMySQL.getId(),
                                    fullName.isEmpty() ? null : fullName,
                                    gender,
                                    avatar,
                                    phone,
                                    user.getEmail(),
                                    user.getUsername(),
                                    matchedRole // role là String
                            );
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // Tính toán phân trang
            int totalElements = allValidUsers.size();
            int totalPages = (int) Math.ceil((double) totalElements / limit);
            int fromIndex = Math.min(page * limit, totalElements);
            int toIndex = Math.min(fromIndex + limit, totalElements);
            List<UserResponse> pagedUsers = allValidUsers.subList(fromIndex, toIndex);

            return PagedResponse.<UserResponse>builder()
                    .content(pagedUsers)
                    .totalPages(totalPages)
                    .totalElements(totalElements)
                    .build();

        } catch (Exception e) {
            logger.error("Error fetching users from Keycloak: {}", e.getMessage());
            throw new RuntimeException("Failed to get users from Keycloak", e);
        }
    }

    public UserResponse getUserById(Long userId) {
        // 1. Lấy user trong MySQL
        Users user = userRepository.findOneById(userId);
        if (user == null) {
            throw new NotFoundException("User not found in MySQL");
        }

        String keycloakId = user.getKeycloakId();

        System.out.println("Keycloak ID: " + keycloakId);

        if (user == null) {
            throw new NotFoundException("User not found in MySQL");
        }

        // 2. Lấy thông tin user từ Keycloak
        UserRepresentation keycloakUser;
        try {
            keycloakUser = realmResource()
                    .users()
                    .get(keycloakId)
                    .toRepresentation();
        } catch (Exception e) {
            logger.error("Error fetching user from Keycloak: {}", e.getMessage());
            throw new NotFoundException("User not found in Keycloak with ID: " + keycloakId);
        }

        // 3. Lấy tên đầy đủ
        String fullName = String.format("%s %s",
                keycloakUser.getFirstName() != null ? keycloakUser.getFirstName() : "",
                keycloakUser.getLastName() != null ? keycloakUser.getLastName() : ""
        ).trim();
        if (fullName.isEmpty()) fullName = null;

        // 4. Lấy custom attributes từ Keycloak
        Map<String, List<String>> attributes = keycloakUser.getAttributes();
        String gender = attributes != null && attributes.containsKey("gender") ? attributes.get("gender").get(0) : null;
        String avatar = attributes != null && attributes.containsKey("avatar") ? attributes.get("avatar").get(0) : null;
        String phone = attributes != null && attributes.containsKey("phone") ? attributes.get("phone").get(0) : null;

        // 5. Lấy danh sách realm roles
        List<String> realmRoles;
        try {
            realmRoles = realmResource()
                    .users()
                    .get(keycloakId)
                    .roles()
                    .realmLevel()
                    .listAll()
                    .stream()
                    .map(RoleRepresentation::getName)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            realmRoles = new ArrayList<>();
        }

        String mainRole = null;
        if (realmRoles.contains("admin")) {
            mainRole = "admin";
        } else if (realmRoles.contains("client")) {
            mainRole = "client";
        }

        // 6. Trả về DTO
        return new UserResponse(
                user.getId(),
                fullName,
                gender,
                avatar,
                phone,
                keycloakUser.getEmail(),
                keycloakUser.getUsername(),
                mainRole
        );
    }

    public void updateUser(String keycloakId, UpdateUserDto dto) {
        try {
            UserResource userResource = realmResource().users().get(keycloakId);
            UserRepresentation userRep = userResource.toRepresentation();

            // Update core fields
            if (dto.getEmail() != null) userRep.setEmail(dto.getEmail());
            if (dto.getFirstName() != null) userRep.setFirstName(dto.getFirstName());
            if (dto.getLastName() != null) userRep.setLastName(dto.getLastName());

            // Update custom attributes safely
            Map<String, List<String>> attributes = new HashMap<>(userRep.getAttributes() != null ? userRep.getAttributes() : Map.of());
            if (dto.getGender() != null) attributes.put("gender", List.of(dto.getGender()));
            if (dto.getAvatar() != null) attributes.put("avatar", List.of(dto.getAvatar()));
            if (dto.getPhone() != null) attributes.put("phone", List.of(dto.getPhone()));
            userRep.setAttributes(attributes);

            userResource.update(userRep); // Commit changes

            // Password update
            if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
                CredentialRepresentation cred = new CredentialRepresentation();
                cred.setType(CredentialRepresentation.PASSWORD);
                cred.setTemporary(false);
                cred.setValue(dto.getPassword());
                userResource.resetPassword(cred);
            }
        } catch (Exception e) {
            logger.error("Error updating user in Keycloak: {}", e.getMessage());
            throw new RuntimeException("Update failed", e);
        }
    }


//    public void deleteUser(Long userId) {
//        System.out.println("User deleted" + userId);
//        Users user = userRepository.findUserById(userId);
//        if (user == null) {
//            throw new NotFoundException("User does not exist");
//        }
//        userRepository.softDeleteById(userId);
//    }

    public void deleteUser(String keycloakId) {

        if (keycloakId == null || keycloakId.isEmpty()) {
            throw new IllegalArgumentException("Invalid Keycloak ID");
        }

        try {
            // 1. Lấy user từ Keycloak theo keycloakId
            UserResource userResource = realmResource().users().get(keycloakId);

            // 2. Kiểm tra xem user có tồn tại không (tránh lỗi 404)
            UserRepresentation user = userResource.toRepresentation();
            if (user == null) {
                throw new NotFoundException("User not found in Keycloak with ID: " + keycloakId);
            }

            // 3. Gọi API xóa user
            userResource.remove();

            Users userMySQL = userRepository.findOneByKeycloakId(keycloakId);
            if (userMySQL != null) {
                userRepository.softDeleteById(userMySQL.getId());
            }

        } catch (NotFoundException e) {
            logger.error("User not found in Keycloak: {}", e.getMessage());
            throw new NotFoundException("User not found in Keycloak with ID: " + keycloakId);
        } catch (Exception e) {
            logger.error("Failed to delete user in Keycloak: {}", e.getMessage());
            throw new RuntimeException("Failed to delete user in Keycloak", e);
        }
    }

}
