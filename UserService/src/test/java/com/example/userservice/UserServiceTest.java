package com.example.userservice;

import com.example.userservice.models.Users;
import com.example.userservice.modules.Users.dto.CreateUserDto;
import com.example.userservice.modules.Users.dto.UserResponse;
import com.example.userservice.modules.Users.repository.UserRepository;
import com.example.userservice.modules.Users.service.UserService;
import com.example.userservice.utils.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private Keycloak keycloak;
    @Mock private RealmResource realmResource;
    @Mock private UsersResource usersResource;
    @Mock private UserResource userResource;
    @Mock private Response response;
    @Mock private RoleMappingResource roleMappingResource;
    @Mock private RoleScopeResource roleScopeResource; // ✅ dùng đúng interface

    @InjectMocks private UserService userService;

    @BeforeEach
    void setup() {
        // Cho chắc chắn luôn khớp với code thật
        lenient().when(keycloak.realm(any())).thenReturn(realmResource);
        lenient().when(realmResource.users()).thenReturn(usersResource);

        lenient().when(userResource.roles()).thenReturn(roleMappingResource);
        lenient().when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);
        lenient().when(roleScopeResource.listEffective())
                .thenReturn(List.of(new RoleRepresentation("admin", null, false)));
    }


    @Test
    void testFindRawById_found() {
        Users user = new Users();
        user.setId(1L);
        when(userRepository.findOneById(1L)).thenReturn(user);

        Users result = userService.findRawById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void testFindRawById_notFound() {
        when(userRepository.findOneById(99L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> userService.findRawById(99L));
    }

    @Test
    void testCreateUser_success() {
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(201);
        when(response.getHeaderString("Location")).thenReturn("http://kc/users/abc-123");

        when(usersResource.get("abc-123")).thenReturn(userResource);

        Users saved = new Users();
        saved.setId(1L);
        saved.setKeycloakId("abc-123");
        when(userRepository.save(any(Users.class))).thenReturn(saved);

        CreateUserDto dto = new CreateUserDto();
        dto.setUsername("huy");
        dto.setEmail("huy@example.com");
        dto.setPassword("123456");

        Users result = userService.createUser(dto);

        assertNotNull(result);
        assertEquals("abc-123", result.getKeycloakId());
        verify(userRepository, times(1)).save(any(Users.class));
        verify(userResource, times(1)).resetPassword(any(CredentialRepresentation.class));
    }

    @Test
    void testCreateUser_failStatus() {
        when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatus()).thenReturn(400);

        CreateUserDto dto = new CreateUserDto();
        dto.setUsername("huy");
        dto.setEmail("huy@example.com");
        dto.setPassword("123456");

        assertThrows(RuntimeException.class, () -> userService.createUser(dto));
    }

    @Test
    void testGetUserByKeycloakId_notInMySQL() {
        when(userRepository.findOneByKeycloakId("abc")).thenReturn(null);

        assertThrows(NotFoundException.class, () -> userService.getUserByKeycloakId("abc"));
    }

    @Test
    void testGetUserByKeycloakId_success() {
        Users user = new Users();
        user.setId(1L);
        user.setKeycloakId("abc");
        when(userRepository.findOneByKeycloakId("abc")).thenReturn(user);

        UserRepresentation kcUser = new UserRepresentation();
        kcUser.setFirstName("Gia");
        kcUser.setLastName("Huy");
        kcUser.setEmail("huy@example.com");
        kcUser.setUsername("huy");
        kcUser.setAttributes(Map.of("gender", List.of("male")));

        when(usersResource.get("abc")).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(kcUser);

        // Mock roles (dùng listEffective chứ không phải listAll)
        RoleRepresentation role = new RoleRepresentation("admin", null, false);
        when(roleScopeResource.listEffective()).thenReturn(List.of(role));

        UserResponse result = userService.getUserByKeycloakId("abc");

        assertEquals("Gia Huy", result.name());
        assertEquals("admin", result.roles());
    }
}
