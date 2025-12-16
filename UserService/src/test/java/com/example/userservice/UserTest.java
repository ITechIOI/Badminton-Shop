package com.example.userservice;

import com.example.userservice.models.Users;
import com.example.userservice.modules.Users.dto.CreateUserDto;
import com.example.userservice.modules.Users.repository.UserRepository;
import com.example.userservice.modules.Users.service.UserService;
import com.example.userservice.utils.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserTest {

    private static final String REALM = "myrealm";
    private static final String ROLE_USER = "user";
    private static final String ROLE_ADMIN = "admin";
    private static final String ROLE_CLIENT = "client";
    private static final String KC_USER_URL_PREFIX = "http://kc/realms/" + REALM + "/users/";

    @Mock private UserRepository userRepository;
    @Mock private Keycloak keycloak;
    @Mock private RealmResource realmResource;
    @Mock private UsersResource usersResource;
    @Mock private UserResource userResource;
    @Mock private RoleMappingResource roleMappingResource;
    @Mock private RoleScopeResource realmLevelRoles;
    @Mock private RolesResource rolesResource;
    @Mock private RoleResource roleResource;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setup() throws Exception {
        setPrivateField(UserService.class, userService, "realm", REALM);
        when(keycloak.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(realmResource.roles()).thenReturn(rolesResource);
        when(usersResource.get(anyString())).thenReturn(userResource);
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(realmLevelRoles);
        when(realmLevelRoles.listAll()).thenReturn(List.of());
        when(realmLevelRoles.listEffective()).thenReturn(List.of());
    }

    private static void setPrivateField(Class<?> type, Object target, String field, Object value) throws Exception {
        Field f = type.getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static Response created201(String keycloakId) {
        return Response.created(URI.create(KC_USER_URL_PREFIX + keycloakId)).status(201).build();
    }

    private void stubCreate201(String keycloakId) {
        when(usersResource.create(any())).thenReturn(created201(keycloakId));
        when(usersResource.get(keycloakId)).thenReturn(userResource);
    }

    private void stubCreateStatus(int status) {
        when(usersResource.create(any())).thenReturn(Response.status(status).build());
    }

    private void stubRealmRole(String roleName) {
        RoleRepresentation rr = new RoleRepresentation();
        rr.setName(roleName);
        when(rolesResource.get(roleName)).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(rr);
    }

    private static RoleRepresentation role(String name) {
        RoleRepresentation r = new RoleRepresentation();
        r.setName(name);
        return r;
    }

    private static CreateUserDto baseDto() {
        return CreateUserDto.builder()
                .firstName("John")
                .lastName("Doe")
                .gender("Male")
                .avatar("https://res.cloudinary.com/demo/image/upload/sample.jpg")
                .phone("343485633")
                .email("bob@gmail.com")
                .username("nguyenloan123")
                .roles(ROLE_USER)
                .password("nguyenloan123")
                .build();
    }

    private static CreateUserDto dtoRolesNull() {
        return CreateUserDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("bob@gmail.com")
                .username("nguyenloan123")
                .password("pw")
                .roles(null)
                .build();
    }

    private static Users mysqlUser(long id, String kcId) {
        Users u = new Users();
        u.setId(id);
        u.setKeycloakId(kcId);
        return u;
    }

    private void stubKcUser(String keycloakId, UserRepresentation rep, List<RoleRepresentation> roles) {
        when(usersResource.get(keycloakId)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(rep);
        when(realmLevelRoles.listAll()).thenReturn(roles);
        when(realmLevelRoles.listEffective()).thenReturn(roles);
    }

    private void stubFullRoleChain(String keycloakId, List<RoleRepresentation> roles) {
        when(usersResource.get(keycloakId)).thenReturn(userResource);
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(realmLevelRoles);
        when(realmLevelRoles.listAll()).thenReturn(roles);
        when(realmLevelRoles.listEffective()).thenReturn(roles);
    }

    @Nested
    @DisplayName("createUser")
    class CreateUserTests {

        @Test
        @DisplayName("UTCD001: success full input")
        void createUser_success_fullInput() {
            String kcId = "abc-123";
            stubCreate201(kcId);
            stubRealmRole(ROLE_USER);
            when(userRepository.save(any(Users.class))).thenAnswer(inv -> {
                Users u = inv.getArgument(0);
                u.setId(10L);
                return u;
            });

            CreateUserDto dto = baseDto();
            Users saved = userService.createUser(dto);

            ArgumentCaptor<UserRepresentation> repCap = ArgumentCaptor.forClass(UserRepresentation.class);
            verify(usersResource).create(repCap.capture());
            UserRepresentation rep = repCap.getValue();

            assertThat(rep.getUsername()).isEqualTo(dto.getUsername());
            assertThat(rep.getEmail()).isEqualTo(dto.getEmail());
            assertThat(rep.getAttributes()).isNotNull();
            assertThat(rep.getAttributes().get("gender")).contains(dto.getGender());

            ArgumentCaptor<CredentialRepresentation> pwdCap = ArgumentCaptor.forClass(CredentialRepresentation.class);
            verify(userResource).resetPassword(pwdCap.capture());
            assertThat(pwdCap.getValue().getValue()).isEqualTo(dto.getPassword());

            ArgumentCaptor<List<RoleRepresentation>> rolesCap = ArgumentCaptor.forClass(List.class);
            verify(realmLevelRoles).add(rolesCap.capture());
            assertThat(rolesCap.getValue()).extracting("name").containsExactly(ROLE_USER);

            ArgumentCaptor<Users> entityCap = ArgumentCaptor.forClass(Users.class);
            verify(userRepository).save(entityCap.capture());
            assertThat(entityCap.getValue().getKeycloakId()).isEqualTo(kcId);

            assertThat(saved.getId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("UTCD002: roles null defaults to user")
        void createUser_rolesNull() {
            String kcId = "kc-999";
            stubCreate201(kcId);
            stubRealmRole(ROLE_USER);
            when(userRepository.save(any())).thenAnswer(inv -> {
                Users u = inv.getArgument(0);
                u.setId(11L);
                return u;
            });

            CreateUserDto dto = dtoRolesNull();
            userService.createUser(dto);

            verify(realmLevelRoles).add(argThat(list ->
                    list != null && list.size() == 1 && ROLE_USER.equals(list.get(0).getName())
            ));
            verify(userRepository).save(any(Users.class));
        }

        @Test
        @DisplayName("UTCD003: optional fields null")
        void createUser_optionalFieldsNull() {
            String kcId = "kc-opt";
            stubCreate201(kcId);
            stubRealmRole(ROLE_USER);
            when(userRepository.save(any())).thenAnswer(inv -> {
                Users u = inv.getArgument(0);
                u.setId(12L);
                return u;
            });

            CreateUserDto dto = baseDto().toBuilder()
                    .gender(null).avatar(null).phone(null).build();

            Users saved = userService.createUser(dto);

            ArgumentCaptor<UserRepresentation> repCap = ArgumentCaptor.forClass(UserRepresentation.class);
            verify(usersResource).create(repCap.capture());
            UserRepresentation rep = repCap.getValue();

            assertThat(saved.getId()).isEqualTo(12L);
        }

        @Test
        @DisplayName("UTCD004: conflict email throws")
        void createUser_conflictEmail() {
            stubCreateStatus(409);

            assertThatThrownBy(() -> userService.createUser(baseDto()))
                    .isInstanceOf(RuntimeException.class);

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getUserByKeycloakId")
    class GetUserByKeycloakIdTests {

        @Test
        @DisplayName("UTKG001: success admin role")
        void getUserByKeycloakId_success() {
            String kcId = "abc-123";
            when(userRepository.findOneByKeycloakId(kcId)).thenReturn(mysqlUser(1L, kcId));

            UserRepresentation rep = new UserRepresentation();
            rep.setFirstName("John");
            rep.setLastName("Doe");
            rep.setEmail("bob@gmail.com");
            rep.setUsername("johndoe");
            rep.setAttributes(Map.of(
                    "gender", List.of("Male"),
                    "avatar", List.of("img"),
                    "phone", List.of("0909")
            ));

            List<RoleRepresentation> roles = List.of(role("admin"), role("client"));

            stubFullRoleChain(kcId, roles);
            when(userResource.toRepresentation()).thenReturn(rep);

            var res = userService.getUserByKeycloakId(kcId);
            assertThat(res.roles()).isEqualTo("admin");
        }

        @Test
        @DisplayName("UTKG002: not found in MySQL")
        void getUserByKeycloakId_notFoundInMySQL() {
            when(userRepository.findOneByKeycloakId("123")).thenReturn(null);

            assertThatThrownBy(() -> userService.getUserByKeycloakId("123"))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("UTKG003: not found in Keycloak")
        void getUserByKeycloakId_notFoundInKeycloak() {
            String kcId = "000";
            when(userRepository.findOneByKeycloakId(kcId)).thenReturn(mysqlUser(7L, kcId));
            when(usersResource.get(kcId)).thenReturn(userResource);
            when(userResource.toRepresentation()).thenThrow(new RuntimeException("404"));

            assertThatThrownBy(() -> userService.getUserByKeycloakId(kcId))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("UTKG004: empty names, null attributes")
        void getUserByKeycloakId_emptyAttributes() {
            String kcId = "4ae046f7-ef...";
            when(userRepository.findOneByKeycloakId(kcId)).thenReturn(mysqlUser(2L, kcId));

            UserRepresentation rep = new UserRepresentation();
            rep.setFirstName("");
            rep.setLastName(null);
            rep.setEmail("client@gmail.com");
            rep.setUsername("client123");
            rep.setAttributes(null);

            List<RoleRepresentation> roles = List.of(new RoleRepresentation(ROLE_CLIENT, null, false));
            stubKcUser(kcId, rep, roles);

            var res = userService.getUserByKeycloakId(kcId);

            assertThat(res.id()).isEqualTo(2L);
            assertThat(res.name()).isNull();
            assertThat(res.roles()).isEqualTo("client");
        }
    }

    @Nested
    @DisplayName("getUserById")
    class GetUserByIdTests {

        private void stubKeycloakByKcId(String kcId, UserRepresentation rep, List<RoleRepresentation> roles) {
            when(usersResource.get(kcId)).thenReturn(userResource);
            when(userResource.toRepresentation()).thenReturn(rep);
            when(userResource.roles()).thenReturn(roleMappingResource);
            when(roleMappingResource.realmLevel()).thenReturn(realmLevelRoles);
            when(realmLevelRoles.listAll()).thenReturn(roles);
        }

        @Test
        @DisplayName("UTID001: success admin role")
        void getUserById_success() {
            Long id = 3L;
            String kcId = "kc-3";
            when(userRepository.findOneById(id)).thenReturn(mysqlUser(id, kcId));

            UserRepresentation rep = new UserRepresentation();
            rep.setFirstName("John");
            rep.setLastName("Doe");
            rep.setEmail("john.doe@mail.com");
            rep.setUsername("johnny");
            rep.setAttributes(Map.of(
                    "gender", List.of("Male"),
                    "avatar", List.of("https://img"),
                    "phone", List.of("0909")
            ));

            List<RoleRepresentation> roles = List.of(new RoleRepresentation(ROLE_ADMIN, null, false));
            stubKeycloakByKcId(kcId, rep, roles);

            var res = userService.getUserById(id);
            assertThat(res.roles()).isEqualTo("admin");
        }

        @Test
        @DisplayName("UTID002: not found in MySQL")
        void getUserById_notFoundInMySQL() {
            when(userRepository.findOneById(20L)).thenReturn(null);

            assertThatThrownBy(() -> userService.getUserById(20L))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("UTID003: not found in Keycloak")
        void getUserById_notFoundInKeycloak() {
            Long id = 3L;
            String kcId = "kc-missing";

            when(userRepository.findOneById(id)).thenReturn(mysqlUser(id, kcId));
            when(usersResource.get(kcId)).thenReturn(userResource);
            when(userResource.toRepresentation()).thenThrow(new RuntimeException("404"));

            assertThatThrownBy(() -> userService.getUserById(id))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getAllUsers")
    class GetAllUsersTests {

        private UserRepresentation kcUser(String id, String first, String last,
                                          String email, String username,
                                          String gender, String avatar, String phone) {
            UserRepresentation u = new UserRepresentation();
            u.setId(id);
            u.setFirstName(first);
            u.setLastName(last);
            u.setEmail(email);
            u.setUsername(username);
            u.setAttributes(Map.of(
                    "gender", gender == null ? null : List.of(gender),
                    "avatar", avatar == null ? null : List.of(avatar),
                    "phone", phone == null ? null : List.of(phone)
            ));
            return u;
        }

        private void stubEffectiveRoleUserForAnyId() {
            when(usersResource.get(anyString())).thenReturn(userResource);
            when(userResource.roles()).thenReturn(roleMappingResource);
            when(roleMappingResource.realmLevel()).thenReturn(realmLevelRoles);
            when(realmLevelRoles.listEffective()).thenReturn(List.of(new RoleRepresentation(ROLE_USER, null, false)));
        }

        private void prepareTwoUsersInKcAndDb() {
            var rep1 = kcUser("kc1", "John", "Doe", "john@mail.com", "johnny", "Male", "https://img1", "0901");
            var rep2 = kcUser("kc2", "Jane", "Smith", "jane@mail.com", "jane", "Female", "https://img2", "0902");

            when(realmResource.users()).thenReturn(usersResource);
            when(usersResource.search("", 0, Integer.MAX_VALUE)).thenReturn(List.of(rep1, rep2));

            Users u1 = mysqlUser(1L, "kc1");
            Users u2 = mysqlUser(2L, "kc2");

            when(userRepository.findOneByKeycloakId("kc1")).thenReturn(u1);
            when(userRepository.findOneByKeycloakId("kc2")).thenReturn(u2);

            stubEffectiveRoleUserForAnyId();
        }

        @Test
        @DisplayName("UTCG001: page0 limit2 returns 2 users")
        void getAllUsers_page0_limit2() {
            prepareTwoUsersInKcAndDb();
            var resp = userService.getAllUsers(0, 2);

            assertThat(resp.getContent()).hasSize(2);
            assertThat(resp.getTotalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("UTCG002: page1 limit2 returns empty")
        void getAllUsers_page1_limit2() {
            prepareTwoUsersInKcAndDb();
            var resp = userService.getAllUsers(1, 2);

            assertThat(resp.getContent()).isEmpty();
            assertThat(resp.getTotalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("UTCG003: negative page throws")
        void getAllUsers_negativePage() {
            prepareTwoUsersInKcAndDb();

            assertThatThrownBy(() -> userService.getAllUsers(-1, 2))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("updateUser")
    class UpdateUserTests {

        private UserRepresentation kcRep(String first, String last, String email, String username,
                                         String gender, String avatar, String phone) {
            UserRepresentation rep = new UserRepresentation();
            rep.setFirstName(first);
            rep.setLastName(last);
            rep.setEmail(email);
            rep.setUsername(username);
            if (gender != null || avatar != null || phone != null) {
                rep.setAttributes(Map.of(
                        "gender", gender == null ? null : List.of(gender),
                        "avatar", avatar == null ? null : List.of(avatar),
                        "phone", phone == null ? null : List.of(phone)
                ));
            }
            return rep;
        }

        private void stubUpdateChain(String kcId, UserRepresentation current) {
            when(realmResource.users()).thenReturn(usersResource);
            when(usersResource.get(kcId)).thenReturn(userResource);
            when(userResource.toRepresentation()).thenReturn(current);
        }

        @Test
        @DisplayName("UTU001: success update")
        void updateUser_success() {
            String kcId = "4ae046f7-ef...";
            var current = kcRep("John", "Scale", "old@example.com", "johnjohn",
                    "Male", "http://old.png", "0333333333");
            stubUpdateChain(kcId, current);

            var dto = com.example.userservice.modules.Users.dto.UpdateUserDto.builder()
                    .firstName("Jack").lastName("Son").email("johnscale@example.com")
                    .gender("Female").avatar("http://avatar_copy.png").phone("0344444444")
                    .password("johnjohn")
                    .build();

            userService.updateUser(kcId, dto);

            ArgumentCaptor<UserRepresentation> cap = ArgumentCaptor.forClass(UserRepresentation.class);
            verify(userResource).update(cap.capture());
            UserRepresentation updated = cap.getValue();

            assertThat(updated.getFirstName()).isEqualTo("Jack");
            assertThat(updated.getLastName()).isEqualTo("Son");
        }

        @Test
        @DisplayName("UTU002: partial update no password")
        void updateUser_partialUpdate() {
            String kcId = "4ae046f7-ef...";
            var current = kcRep("John", "Scale", "old@example.com", "johnjohn",
                    "Male", "http://old.png", "0333333333");
            stubUpdateChain(kcId, current);

            var dto = com.example.userservice.modules.Users.dto.UpdateUserDto.builder()
                    .lastName("Jackson")
                    .build();

            userService.updateUser(kcId, dto);

            ArgumentCaptor<UserRepresentation> cap = ArgumentCaptor.forClass(UserRepresentation.class);
            verify(userResource).update(cap.capture());
            UserRepresentation updated = cap.getValue();

            assertThat(updated.getLastName()).isEqualTo("Jackson");
            verify(userResource, never()).resetPassword(any());
        }
    }

    @Nested
    @DisplayName("deleteUser")
    class DeleteUserTests {

        private Users dbUser(long id, String kcId) {
            return mysqlUser(id, kcId);
        }

        private void stubKeycloakUserExists(String kcId) {
            when(usersResource.get(kcId)).thenReturn(userResource);
            when(userResource.toRepresentation()).thenReturn(new UserRepresentation());
        }

        private void stubKeycloakUserMissing(String kcId) {
            when(usersResource.get(kcId)).thenReturn(userResource);
            when(userResource.toRepresentation()).thenReturn(null);
        }

        @Test
        @DisplayName("DEL001: success delete")
        void deleteUser_success() {
            String kcId = "4ae046f7-efd9-481b-b507-0fcbc330ac7b";
            stubKeycloakUserExists(kcId);
            when(userRepository.findOneByKeycloakId(kcId)).thenReturn(dbUser(101L, kcId));

            userService.deleteUser(kcId);

            verify(userResource).remove();
            verify(userRepository).softDeleteById(101L);
        }

        @Test
        @DisplayName("DEL002: keycloak not found")
        void deleteUser_keycloakNotFound() {
            String kcId = "123";
            stubKeycloakUserMissing(kcId);

            assertThatThrownBy(() -> userService.deleteUser(kcId))
                    .isInstanceOf(NotFoundException.class);

            verify(userRepository, never()).softDeleteById(any());
        }

        @Test
        @DisplayName("DEL003: keycloak remove fails")
        void deleteUser_removeFails() {
            String kcId = "4ae046f7-efd9-481b-b507-0fcbc335hh5";
            stubKeycloakUserExists(kcId);
            when(userRepository.findOneByKeycloakId(kcId)).thenReturn(dbUser(202L, kcId));
            doThrow(new RuntimeException("500")).when(userResource).remove();

            assertThatThrownBy(() -> userService.deleteUser(kcId))
                    .isInstanceOf(RuntimeException.class);

            verify(userRepository, never()).softDeleteById(any());
        }

        @Test
        @DisplayName("DEL004: null id throws")
        void deleteUser_nullId() {
            assertThatThrownBy(() -> userService.deleteUser(null))
                    .isInstanceOf(IllegalArgumentException.class);

            verifyNoInteractions(usersResource, userResource, userRepository);
        }
    }
}
