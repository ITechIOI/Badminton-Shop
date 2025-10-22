package com.example.userservice;

import com.example.userservice.models.Users;
import com.example.userservice.modules.Users.dto.CreateUserDto;
import com.example.userservice.modules.Users.repository.UserRepository;
import com.example.userservice.modules.Users.service.UserService;
import com.example.userservice.utils.NotFoundException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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

/**
 * Unit tests for UserService: createUser / getUserByKeycloakId / getUserById / getAllUsers / updateUser / deleteUser.
 * Focus: clarity, DRY helpers, stable assertions.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserTest {

    // =========================
    // Constants
    // =========================
    private static final String REALM = "myrealm";
    private static final String ROLE_USER = "user";
    private static final String ROLE_ADMIN = "admin";
    private static final String ROLE_CLIENT = "client";
    private static final String KC_USER_URL_PREFIX = "http://kc/realms/" + REALM + "/users/";

    // =========================
    // Mocks / SUT
    // =========================
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

    // =========================
    // Setup
    // =========================
    @BeforeEach
    void setup() throws Exception {
        setPrivateField(UserService.class, userService, "realm", REALM);
        when(keycloak.realm(REALM)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(realmResource.roles()).thenReturn(rolesResource);
        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.realmLevel()).thenReturn(realmLevelRoles);
    }

    // =========================
    // Helpers
    // =========================
    private static void setPrivateField(Class<?> type, Object target, String field, Object value) throws Exception {
        final Field f = type.getDeclaredField(field);
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
        final RoleRepresentation rr = new RoleRepresentation();
        rr.setName(roleName);
        when(rolesResource.get(roleName)).thenReturn(roleResource);
        when(roleResource.toRepresentation()).thenReturn(rr);
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
        final Users u = new Users();
        u.setId(id);
        u.setKeycloakId(kcId);
        return u;
    }

    private void stubKcUser(String keycloakId, UserRepresentation rep, List<RoleRepresentation> roles) {
        when(usersResource.get(keycloakId)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(rep);
        when(realmLevelRoles.listAll()).thenReturn(roles);
    }

    // =====================================================================
    // createUser
    // =====================================================================
    @Nested @DisplayName("createUser")
    class CreateUserTests {

        @Test
        @DisplayName("UTCD001: success — full input with explicit role 'user'")
        void createUser_success_fullInput_withExplicitRole() {
            // Arrange
            final String kcId = "abc-123";
            stubCreate201(kcId);
            stubRealmRole(ROLE_USER);
            when(userRepository.save(any(Users.class))).thenAnswer(inv -> {
                final Users u = inv.getArgument(0);
                u.setId(10L);
                return u;
            });
            final CreateUserDto dto = baseDto();

            // Act
            final Users saved = userService.createUser(dto);

            // Assert
            final ArgumentCaptor<UserRepresentation> repCap = ArgumentCaptor.forClass(UserRepresentation.class);
            verify(usersResource).create(repCap.capture());
            final UserRepresentation rep = repCap.getValue();

            assertThat(rep.getUsername()).isEqualTo(dto.getUsername());
            assertThat(rep.getEmail()).isEqualTo(dto.getEmail());
            assertThat(rep.getFirstName()).isEqualTo(dto.getFirstName());
            assertThat(rep.getLastName()).isEqualTo(dto.getLastName());
            assertThat(rep.isEnabled()).isTrue();
            assertThat(rep.getAttributes()).isNotNull();
            assertThat(rep.getAttributes().get("gender")).contains(dto.getGender());
            assertThat(rep.getAttributes().get("avatar")).contains(dto.getAvatar());
            assertThat(rep.getAttributes().get("phone")).contains(dto.getPhone());

            final ArgumentCaptor<CredentialRepresentation> pwdCap = ArgumentCaptor.forClass(CredentialRepresentation.class);
            verify(userResource).resetPassword(pwdCap.capture());
            assertThat(pwdCap.getValue().getType()).isEqualTo(CredentialRepresentation.PASSWORD);
            assertThat(pwdCap.getValue().isTemporary()).isFalse();
            assertThat(pwdCap.getValue().getValue()).isEqualTo(dto.getPassword());

            @SuppressWarnings("unchecked")
            final ArgumentCaptor<List<RoleRepresentation>> rolesCap = ArgumentCaptor.forClass((Class) List.class);
            verify(realmLevelRoles).add(rolesCap.capture());
            assertThat(rolesCap.getValue()).extracting("name").containsExactly(ROLE_USER);

            final ArgumentCaptor<Users> entityCap = ArgumentCaptor.forClass(Users.class);
            verify(userRepository).save(entityCap.capture());
            assertThat(entityCap.getValue().getKeycloakId()).isEqualTo(kcId);

            assertThat(saved.getId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("UTCD002: success — roles=null ⇒ default to 'user'")
        void createUser_success_rolesNull_defaultsToUser() {
            // Arrange
            final String kcId = "kc-999";
            stubCreate201(kcId);
            stubRealmRole(ROLE_USER);
            when(userRepository.save(any())).thenAnswer(inv -> {
                final Users u = inv.getArgument(0);
                u.setId(11L);
                return u;
            });
            final CreateUserDto dto = dtoRolesNull();

            // Act
            userService.createUser(dto);

            // Assert
            verify(realmLevelRoles).add(argThat(list ->
                    list != null && list.size() == 1 && ROLE_USER.equals(list.get(0).getName())
            ));
            verify(userResource).resetPassword(any(CredentialRepresentation.class));
            verify(usersResource).create(any());
            verify(userRepository, times(1)).save(any(Users.class));
        }

        @Test
        @DisplayName("UTCD003: success — optional gender/avatar/phone are null")
        void createUser_success_optionalFieldsNull() {
            // Arrange
            final String kcId = "kc-opt";
            stubCreate201(kcId);
            stubRealmRole(ROLE_USER);
            when(userRepository.save(any())).thenAnswer(inv -> {
                final Users u = inv.getArgument(0);
                u.setId(12L);
                return u;
            });
            final CreateUserDto dto = baseDto().toBuilder()
                    .gender(null).avatar(null).phone(null).build();

            // Act
            final Users saved = userService.createUser(dto);

            // Assert
            final ArgumentCaptor<UserRepresentation> repCap = ArgumentCaptor.forClass(UserRepresentation.class);
            verify(usersResource).create(repCap.capture());
            final UserRepresentation rep = repCap.getValue();
            final Map<String, List<String>> attrs = rep.getAttributes();
            if (attrs != null) {
                assertThat(attrs.get("gender")).isNull();
                assertThat(attrs.get("avatar")).isNull();
                assertThat(attrs.get("phone")).isNull();
            }
            assertThat(saved.getId()).isEqualTo(12L);
            verify(realmLevelRoles, times(1)).add(anyList());
        }

        @Test
        @DisplayName("UTCD004: conflict email ⇒ 409 ⇒ throws")
        void createUser_conflictEmail_throws() {
            // Arrange
            stubCreateStatus(409);
            final CreateUserDto dto = baseDto();

            // Act + Assert
            assertThatThrownBy(() -> userService.createUser(dto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to create user in Keycloak");

            verify(userResource, never()).resetPassword(any());
            verify(realmLevelRoles, never()).add(anyList());
            verify(userRepository, never()).save(any());
        }
    }

    // =====================================================================
    // getUserByKeycloakId
    // =====================================================================
    @Nested @DisplayName("getUserByKeycloakId")
    class GetUserByKeycloakIdTests {

        @Test
        @DisplayName("UTKG001: success — found in MySQL & Keycloak; roles contain 'admin' ⇒ mainRole='admin'")
        void getUserByKeycloakId_success_returnsUserResponseWithAdminRole() {
            // Arrange
            final String kcId = "4ae046f7-efd9-431b-b507-0fbc330ac7b";
            when(userRepository.findOneByKeycloakId(kcId)).thenReturn(mysqlUser(1L, kcId));

            final UserRepresentation rep = new UserRepresentation();
            rep.setFirstName("John");
            rep.setLastName("Doe");
            rep.setEmail("bob@gmail.com");
            rep.setUsername("nguyenloan123");
            rep.setAttributes(Map.of(
                    "gender", List.of("Male"),
                    "avatar", List.of("https://res.cloudinary.com/demo/image/upload/sample.jpg"),
                    "phone",  List.of("343485633")
            ));

            final List<RoleRepresentation> roles = List.of(
                    new RoleRepresentation(ROLE_ADMIN, null, false),
                    new RoleRepresentation(ROLE_CLIENT, null, false)
            );
            stubKcUser(kcId, rep, roles);

            // Act
            final var res = userService.getUserByKeycloakId(kcId);

            // Assert
            assertThat(res.id()).isEqualTo(1L);
            assertThat(res.name()).isEqualTo("John Doe");
            assertThat(res.gender()).isEqualTo("Male");
            assertThat(res.avatar()).isEqualTo("https://res.cloudinary.com/demo/image/upload/sample.jpg");
            assertThat(res.phone()).isEqualTo("343485633");
            assertThat(res.email()).isEqualTo("bob@gmail.com");
            assertThat(res.username()).isEqualTo("nguyenloan123");
            assertThat(res.roles()).isEqualTo(ROLE_ADMIN); // ưu tiên admin
        }

        @Test
        @DisplayName("UTKG002: not found in MySQL ⇒ NotFoundException('User not found in MySQL')")
        void getUserByKeycloakId_notFoundInMySQL_throws() {
            // Arrange
            final String kcId = "000";
            when(userRepository.findOneByKeycloakId(kcId)).thenReturn(null);

            // Act + Assert
            assertThatThrownBy(() -> userService.getUserByKeycloakId(kcId))
                    .isInstanceOf(com.example.userservice.utils.NotFoundException.class)
                    .hasMessage("User not found in MySQL");

            verify(usersResource, never()).get(anyString());
        }

        @Test
        @DisplayName("UTKG003: found in MySQL but Keycloak missing ⇒ NotFoundException('... in Keycloak with ID: <id>')")
        void getUserByKeycloakId_notFoundInKeycloak_throws() {
            // Arrange
            final String kcId = "123";
            when(userRepository.findOneByKeycloakId(kcId)).thenReturn(mysqlUser(7L, kcId));
            when(usersResource.get(kcId)).thenReturn(userResource);
            when(userResource.toRepresentation()).thenThrow(new RuntimeException("404"));

            // Act + Assert
            assertThatThrownBy(() -> userService.getUserByKeycloakId(kcId))
                    .isInstanceOf(com.example.userservice.utils.NotFoundException.class)
                    .hasMessageContaining("User not found in Keycloak with ID: " + kcId);
        }

        @Test
        @DisplayName("UTKG004: empty names & null attributes ⇒ name=null; gender/avatar/phone=null; role='client'")
        void getUserByKeycloakId_emptyNames_attributesNull_clientRole() {
            // Arrange
            final String kcId = "4ae046f7-ef...";
            when(userRepository.findOneByKeycloakId(kcId)).thenReturn(mysqlUser(2L, kcId));

            final UserRepresentation rep = new UserRepresentation();
            rep.setFirstName(""); // empty
            rep.setLastName(null); // null
            rep.setEmail("client@gmail.com");
            rep.setUsername("client123");
            rep.setAttributes(null);

            final List<RoleRepresentation> roles = List.of(new RoleRepresentation(ROLE_CLIENT, null, false));
            stubKcUser(kcId, rep, roles);

            // Act
            final var res = userService.getUserByKeycloakId(kcId);

            // Assert
            assertThat(res.id()).isEqualTo(2L);
            assertThat(res.name()).isNull(); // empty+null => null theo service
            assertThat(res.gender()).isNull();
            assertThat(res.avatar()).isNull();
            assertThat(res.phone()).isNull();
            assertThat(res.email()).isEqualTo("client@gmail.com");
            assertThat(res.username()).isEqualTo("client123");
            assertThat(res.roles()).isEqualTo(ROLE_CLIENT);
        }
    }

    // =====================================================================
    // getUserById
    // =====================================================================
    @Nested @DisplayName("getUserById")
    class GetUserByIdTests {

        private void stubKeycloakByKcId(String kcId, UserRepresentation rep, List<RoleRepresentation> roles) {
            when(usersResource.get(kcId)).thenReturn(userResource);
            when(userResource.toRepresentation()).thenReturn(rep);
            when(userResource.roles()).thenReturn(roleMappingResource);
            when(roleMappingResource.realmLevel()).thenReturn(realmLevelRoles);
            when(realmLevelRoles.listAll()).thenReturn(roles);
        }

        @Test
        @DisplayName("UTID001: success — MySQL & Keycloak exist; roles include 'admin' ⇒ mainRole='admin'")
        void getUserById_success_fullInfo_adminRole() {
            // Arrange
            final Long id = 3L;
            final String kcId = "kc-3";
            when(userRepository.findOneById(id)).thenReturn(mysqlUser(id, kcId));

            final UserRepresentation rep = new UserRepresentation();
            rep.setFirstName("John");
            rep.setLastName("Doe");
            rep.setEmail("john.doe@mail.com");
            rep.setUsername("johnny");
            rep.setAttributes(Map.of(
                    "gender", List.of("Male"),
                    "avatar", List.of("https://img"),
                    "phone",  List.of("0909")
            ));
            final var roles = List.of(new RoleRepresentation(ROLE_ADMIN, null, false));
            stubKeycloakByKcId(kcId, rep, roles);

            // Act
            final var res = userService.getUserById(id);

            // Assert
            assertThat(res.id()).isEqualTo(id);
            assertThat(res.name()).isEqualTo("John Doe");
            assertThat(res.gender()).isEqualTo("Male");
            assertThat(res.avatar()).isEqualTo("https://img");
            assertThat(res.phone()).isEqualTo("0909");
            assertThat(res.email()).isEqualTo("john.doe@mail.com");
            assertThat(res.username()).isEqualTo("johnny");
            assertThat(res.roles()).isEqualTo(ROLE_ADMIN);
        }

        @Test
        @DisplayName("UTID002: not found in MySQL ⇒ NotFoundException('User not found in MySQL')")
        void getUserById_notFoundInMySQL_throws() {
            final Long id = 20L;
            when(userRepository.findOneById(id)).thenReturn(null);

            assertThatThrownBy(() -> userService.getUserById(id))
                    .isInstanceOf(com.example.userservice.utils.NotFoundException.class)
                    .hasMessage("User not found in MySQL");

            verify(usersResource, never()).get(anyString());
        }

        @Test
        @DisplayName("UTID003: found in MySQL but Keycloak missing ⇒ NotFoundException('... in Keycloak with ID: <kcId>')")
        void getUserById_notFoundInKeycloak_throws() {
            final Long id = 3L;
            final String kcId = "kc-missing";
            when(userRepository.findOneById(id)).thenReturn(mysqlUser(id, kcId));
            when(usersResource.get(kcId)).thenReturn(userResource);
            when(userResource.toRepresentation()).thenThrow(new RuntimeException("404"));

            assertThatThrownBy(() -> userService.getUserById(id))
                    .isInstanceOf(com.example.userservice.utils.NotFoundException.class)
                    .hasMessageContaining("User not found in Keycloak with ID: " + kcId);
        }

        @Test
        @DisplayName("UTID004: empty names + null attributes ⇒ name=null; optional fields null; role='client'")
        void getUserById_emptyNames_andNullAttributes_clientRole() {
            final Long id = 3L;
            final String kcId = "kc-3";
            when(userRepository.findOneById(id)).thenReturn(mysqlUser(id, kcId));

            final UserRepresentation rep = new UserRepresentation();
            rep.setFirstName(""); rep.setLastName(null);
            rep.setEmail("c@mail.com"); rep.setUsername("clienty");
            rep.setAttributes(null);
            final var roles = List.of(new RoleRepresentation(ROLE_CLIENT, null, false));
            stubKeycloakByKcId(kcId, rep, roles);

            final var res = userService.getUserById(id);

            assertThat(res.name()).isNull();
            assertThat(res.gender()).isNull();
            assertThat(res.avatar()).isNull();
            assertThat(res.phone()).isNull();
            assertThat(res.roles()).isEqualTo(ROLE_CLIENT);
            assertThat(res.email()).isEqualTo("c@mail.com");
            assertThat(res.username()).isEqualTo("clienty");
        }

        @Test
        @DisplayName("UTID005: id=0 ⇒ behave as not found in MySQL")
        void getUserById_zeroId_behavesAsNotFoundInMySQL() {
            final Long id = 0L;
            when(userRepository.findOneById(id)).thenReturn(null);

            assertThatThrownBy(() -> userService.getUserById(id))
                    .isInstanceOf(com.example.userservice.utils.NotFoundException.class)
                    .hasMessage("User not found in MySQL");

            verify(usersResource, never()).get(anyString());
        }

        @Test
        @DisplayName("UTID006: id=null ⇒ behave as not found in MySQL")
        void getUserById_nullId_behavesAsNotFoundInMySQL() {
            when(userRepository.findOneById(null)).thenReturn(null);

            assertThatThrownBy(() -> userService.getUserById(null))
                    .isInstanceOf(com.example.userservice.utils.NotFoundException.class)
                    .hasMessage("User not found in MySQL");

            verify(usersResource, never()).get(anyString());
        }
    }

    // =====================================================================
    // getAllUsers
    // =====================================================================
    @Nested @DisplayName("getAllUsers")
    class GetAllUsersTests {

        private UserRepresentation kcUser(String id, String first, String last,
                                          String email, String username,
                                          String gender, String avatar, String phone) {
            final var u = new UserRepresentation();
            u.setId(id); u.setFirstName(first); u.setLastName(last);
            u.setEmail(email); u.setUsername(username);
            u.setAttributes(Map.of(
                    "gender", gender == null ? null : List.of(gender),
                    "avatar", avatar == null ? null : List.of(avatar),
                    "phone",  phone  == null ? null : List.of(phone)
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
            final var rep1 = kcUser("kc1", "John", "Doe", "john@mail.com", "johnny", "Male", "https://img1", "0901");
            final var rep2 = kcUser("kc2", "Jane", "Smith", "jane@mail.com", "jane", "Female", "https://img2", "0902");
            when(realmResource.users()).thenReturn(usersResource);
            when(usersResource.search("", 0, Integer.MAX_VALUE)).thenReturn(List.of(rep1, rep2));

            final Users u1 = mysqlUser(1L, "kc1");
            final Users u2 = mysqlUser(2L, "kc2");
            when(userRepository.findOneByKeycloakId("kc1")).thenReturn(u1);
            when(userRepository.findOneByKeycloakId("kc2")).thenReturn(u2);

            stubEffectiveRoleUserForAnyId();
        }

        @Test
        @DisplayName("UTCG001: page=0, limit=2 ⇒ 2 results, totalPages=1, totalElements=2")
        void getAllUsers_page0_limit2_returnsTwo_andPaginationIsOne() {
            prepareTwoUsersInKcAndDb();

            final var resp = userService.getAllUsers(0, 2);

            assertThat(resp.getContent()).hasSize(2);
            assertThat(resp.getTotalElements()).isEqualTo(2);
            assertThat(resp.getTotalPages()).isEqualTo(1);
            assertThat(resp.getContent().get(0).username()).isIn("johnny", "jane");
            assertThat(resp.getContent().get(1).username()).isIn("johnny", "jane");
        }

        @Test
        @DisplayName("UTCG002: page=1, limit=2 ⇒ empty page, totals unchanged")
        void getAllUsers_page1_limit2_returnsEmpty_butTotalsStay() {
            prepareTwoUsersInKcAndDb();

            final var resp = userService.getAllUsers(1, 2);

            assertThat(resp.getContent()).isEmpty();
            assertThat(resp.getTotalElements()).isEqualTo(2);
            assertThat(resp.getTotalPages()).isEqualTo(1);
        }

        @Test
        @DisplayName("UTCG003: page=0, limit=3 ⇒ 2 results, totalPages=1")
        void getAllUsers_page0_limit3_returnsTwo_totalPages1() {
            prepareTwoUsersInKcAndDb();

            final var resp = userService.getAllUsers(0, 3);

            assertThat(resp.getContent()).hasSize(2);
            assertThat(resp.getTotalElements()).isEqualTo(2);
            assertThat(resp.getTotalPages()).isEqualTo(1);
        }

        @Test
        @DisplayName("UTCG004: negative page ⇒ RuntimeException('Failed to get users from Keycloak')")
        void getAllUsers_negativePage_throwsRuntimeWrapped() {
            prepareTwoUsersInKcAndDb();

            assertThatThrownBy(() -> userService.getAllUsers(-1, 2))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to get users from Keycloak");
        }
    }

    // =====================================================================
    // updateUser
    // =====================================================================
    @Nested @DisplayName("updateUser")
    class UpdateUserTests {

        private UserRepresentation kcRep(String first, String last, String email, String username,
                                         String gender, String avatar, String phone) {
            final var rep = new UserRepresentation();
            rep.setFirstName(first);
            rep.setLastName(last);
            rep.setEmail(email);
            rep.setUsername(username);
            if (gender != null || avatar != null || phone != null) {
                rep.setAttributes(Map.of(
                        "gender", gender == null ? null : List.of(gender),
                        "avatar", avatar == null ? null : List.of(avatar),
                        "phone",  phone  == null ? null : List.of(phone)
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
        @DisplayName("UTU001: success — update core & attributes & password")
        void updateUser_success_updateCoreAndAttributes_andPassword() {
            final String kcId = "4ae046f7-ef...";
            final var current = kcRep("John", "Scale", "old@example.com", "johnjohn",
                    "Male", "http://old.png", "0333333333");
            stubUpdateChain(kcId, current);

            final var dto = com.example.userservice.modules.Users.dto.UpdateUserDto.builder()
                    .firstName("Jack").lastName("Son").email("johnscale@example.com")
                    .gender("Female").avatar("http://avatar_copy.png").phone("0344444444")
                    .password("johnjohn")
                    .build();

            userService.updateUser(kcId, dto);

            final ArgumentCaptor<UserRepresentation> cap = ArgumentCaptor.forClass(UserRepresentation.class);
            verify(userResource).update(cap.capture());
            final var updated = cap.getValue();

            assertThat(updated.getFirstName()).isEqualTo("Jack");
            assertThat(updated.getLastName()).isEqualTo("Son");
            assertThat(updated.getEmail()).isEqualTo("johnscale@example.com");
            assertThat(updated.getUsername()).isEqualTo("johnjohn"); // unchanged
            assertThat(updated.getAttributes().get("gender")).containsExactly("Female");
            assertThat(updated.getAttributes().get("avatar")).containsExactly("http://avatar_copy.png");
            assertThat(updated.getAttributes().get("phone")).containsExactly("0344444444");

            final ArgumentCaptor<CredentialRepresentation> pwdCap = ArgumentCaptor.forClass(CredentialRepresentation.class);
            verify(userResource).resetPassword(pwdCap.capture());
            assertThat(pwdCap.getValue().getType()).isEqualTo(CredentialRepresentation.PASSWORD);
            assertThat(pwdCap.getValue().isTemporary()).isFalse();
            assertThat(pwdCap.getValue().getValue()).isEqualTo("johnjohn");
        }

        @Test
        @DisplayName("UTU002: partial update — preserve unchanged; password=null ⇒ do not reset")
        void updateUser_partialUpdate_preserveUnchanged_doNotResetPasswordWhenNull() {
            final String kcId = "4ae046f7-ef...";
            final var current = kcRep("John", "Scale", "old@example.com", "johnjohn",
                    "Male", "http://old.png", "0333333333");
            stubUpdateChain(kcId, current);

            final var dto = com.example.userservice.modules.Users.dto.UpdateUserDto.builder()
                    .lastName("Jackson") // only this changes
                    .build();

            userService.updateUser(kcId, dto);

            final ArgumentCaptor<UserRepresentation> cap = ArgumentCaptor.forClass(UserRepresentation.class);
            verify(userResource).update(cap.capture());
            final var updated = cap.getValue();

            assertThat(updated.getFirstName()).isEqualTo("John");        // unchanged
            assertThat(updated.getLastName()).isEqualTo("Jackson");      // changed
            assertThat(updated.getEmail()).isEqualTo("old@example.com"); // unchanged
            assertThat(updated.getAttributes().get("gender")).containsExactly("Male");
            assertThat(updated.getAttributes().get("avatar")).containsExactly("http://old.png");
            assertThat(updated.getAttributes().get("phone")).containsExactly("0333333333");
            verify(userResource, never()).resetPassword(any());
        }

        @Test
        @DisplayName("UTU003: Keycloak missing ⇒ RuntimeException('Update failed')")
        void updateUser_keycloakUserMissing_throwRuntime() {
            final String kcId = "123";
            when(realmResource.users()).thenReturn(usersResource);
            when(usersResource.get(kcId)).thenReturn(userResource);
            when(userResource.toRepresentation()).thenThrow(new RuntimeException("404"));

            final var dto = com.example.userservice.modules.Users.dto.UpdateUserDto.builder()
                    .firstName("Jack").build();

            assertThatThrownBy(() -> userService.updateUser(kcId, dto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Update failed");
        }

        @Test
        @DisplayName("UTU004: update conflict ⇒ RuntimeException('Update failed')")
        void updateUser_conflictOnUpdate_throwRuntime() {
            final String kcId = "4ae046f7-ef...";
            final var current = kcRep("John", "Scale", "old@example.com", "johnjohn",
                    "Male", "http://old.png", "0333333333");
            stubUpdateChain(kcId, current);
            doThrow(new RuntimeException("409 Conflict")).when(userResource).update(any(UserRepresentation.class));

            final var dto = com.example.userservice.modules.Users.dto.UpdateUserDto.builder()
                    .email("johnscale@example.com").build();

            assertThatThrownBy(() -> userService.updateUser(kcId, dto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Update failed");
        }
    }

    // =====================================================================
    // deleteUser
    // =====================================================================
    @Nested @DisplayName("deleteUser")
    class DeleteUserTests {

        private Users dbUser(long id, String kcId) { return mysqlUser(id, kcId); }

        private void stubKeycloakUserExists(String kcId) {
            when(usersResource.get(kcId)).thenReturn(userResource);
            when(userResource.toRepresentation()).thenReturn(new UserRepresentation());
        }

        private void stubKeycloakUserMissing(String kcId) {
            when(usersResource.get(kcId)).thenReturn(userResource);
            when(userResource.toRepresentation()).thenReturn(null);
        }

        @Test
        @DisplayName("DEL001: success — remove in Keycloak & soft-delete in DB")
        void deleteUser_success_removeKeycloak_and_softDeleteDb() {
            final String kcId = "4ae046f7-efd9-481b-b507-0fcbc330ac7b";
            stubKeycloakUserExists(kcId);
            when(userRepository.findOneByKeycloakId(kcId)).thenReturn(dbUser(101L, kcId));

            userService.deleteUser(kcId);

            verify(userResource).remove();
            verify(userRepository).softDeleteById(101L);
        }

        @Test
        @DisplayName("DEL002: missing in Keycloak ⇒ NotFoundException")
        void deleteUser_keycloakNotFound_throwsNotFoundException() {
            final String kcId = "123";
            stubKeycloakUserMissing(kcId);

            assertThatThrownBy(() -> userService.deleteUser(kcId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("User not found in Keycloak with ID: " + kcId);

            verify(userResource, never()).remove();
            verify(userRepository, never()).softDeleteById(anyLong());
        }

        @Test
        @DisplayName("DEL003: Keycloak remove() fails ⇒ RuntimeException('Failed to delete...')")
        void deleteUser_keycloakRemoveFails_throwsRuntime() {
            final String kcId = "4ae046f7-efd9-481b-b507-0fcbc335hh5";
            stubKeycloakUserExists(kcId);
            when(userRepository.findOneByKeycloakId(kcId)).thenReturn(dbUser(202L, kcId));
            doThrow(new RuntimeException("500")).when(userResource).remove();

            assertThatThrownBy(() -> userService.deleteUser(kcId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Failed to delete user in Keycloak");

            verify(userRepository, never()).softDeleteById(anyLong());
        }

        // Lưu ý: Test này giả định service có guard null/blank; nếu chưa có, thêm:
        // if (keycloakId == null || keycloakId.isBlank()) throw new IllegalArgumentException("Keycloak ID must not be null or empty");
        @Test
        @DisplayName("DEL004: null id ⇒ IllegalArgumentException (guard expected in service)")
        void deleteUser_nullId_throwsIllegalArgument() {
            assertThatThrownBy(() -> userService.deleteUser(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid Keycloak ID");

            verifyNoInteractions(usersResource, userResource, userRepository);
        }
    }
}
