package com.example.userbehaviorservice;

import com.example.userbehaviorservice.models.Carts;
import com.example.userbehaviorservice.modules.Carts.dto.CreateCartDto;
import com.example.userbehaviorservice.modules.Carts.dto.UpdateCartDto;
import com.example.userbehaviorservice.modules.Carts.repository.CartRepository;
import com.example.userbehaviorservice.modules.Carts.service.CartService;
import com.example.userbehaviorservice.modules.feign.ProductFeign.ProductClient;
import com.example.userbehaviorservice.modules.feign.ProductFeign.ProductResponse;
import com.example.userbehaviorservice.modules.feign.UserFeign.UserClient;
import com.example.userbehaviorservice.modules.feign.UserFeign.UserResponse;
import com.example.userbehaviorservice.utils.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private ProductClient productClient;
    @Mock
    private UserClient userClient;
    @InjectMocks
    private CartService cartService;

    // Helper method tạo UserResponse mẫu
    private static UserResponse createUserResponse(Long userId) {
        return new UserResponse(userId, "User " + userId, "male", "avatar.png", "0123456789",
                "user" + userId + "@example.com", "user" + userId, "USER");
    }

    // Helper method tạo ProductResponse mẫu
    private static ProductResponse createProductResponse(Long productId) {
        return new ProductResponse("Product " + productId, "Brand", "Description", 100,
                "img.png", "video.mp4", "available", 10, 1L);
    }

    // Helper method tạo Carts mẫu
    private static Carts createCart(Long id, Long userId, Long productId, Integer quantity) {
        Carts cart = new Carts();
        cart.setId(id);
        cart.setUserId(userId);
        cart.setProductId(productId);
        cart.setQuantity(quantity);
        return cart;
    }

    // ==========================================================
    // TEST METHOD: createCart
    // ==========================================================
    @Nested
    @DisplayName("createCart")
    class createCart {
        // Precondition: Record Cart(id = 2, productId = 2, userId = 2, quantity=4)
        // existed in database
        // Server works stably

        // Test case 01: UTCC01
        // Input: quantity=5, productId=3, userId=3
        // Expected: Create new cart successfully
        // ==========================================================
        @Test
        @DisplayName("UTCC01: quantity=5, productId=3, userId=3 ⇒ success")
        void createCart_UTCC01_success() {
            // Arrange
            CreateCartDto createDto = new CreateCartDto(5, 3L, 3L);
            UserResponse user = createUserResponse(3L);
            ProductResponse product = createProductResponse(3L);

            when(userClient.getUserById(3L)).thenReturn(ResponseEntity.ok(user));
            when(productClient.getProductById(3L)).thenReturn(ResponseEntity.ok(product));
            when(cartRepository.findByUserIdAndProductId(3L, 3L)).thenReturn(Optional.empty());
            when(cartRepository.save(any(Carts.class))).thenAnswer(inv -> {
                Carts c = inv.getArgument(0);
                c.setId(3L);
                return c;
            });

            // Act
            Carts result = cartService.createCart(createDto);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(3L);
            assertThat(result.getQuantity()).isEqualTo(5);
            assertThat(result.getProductId()).isEqualTo(3L);
            assertThat(result.getUserId()).isEqualTo(3L);

            verify(userClient).getUserById(3L);
            verify(productClient).getProductById(3L);
            verify(cartRepository).findByUserIdAndProductId(3L, 3L);
            verify(cartRepository).save(any(Carts.class));
        }

        // Test case 02: UTCC02
        // Input: quantity=5, productId=2, userId=3
        // Expected: Create new cart successfully
        // ==========================================================
        @Test
        @DisplayName("UTCC02: quantity=5, productId=2, userId=3 ⇒ success")
        void createCart_UTCC02_success() {
            // Arrange
            CreateCartDto createDto = new CreateCartDto(5, 2L, 3L);
            UserResponse user = createUserResponse(3L);
            ProductResponse product = createProductResponse(2L);

            when(userClient.getUserById(3L)).thenReturn(ResponseEntity.ok(user));
            when(productClient.getProductById(2L)).thenReturn(ResponseEntity.ok(product));
            when(cartRepository.findByUserIdAndProductId(3L, 2L)).thenReturn(Optional.empty());
            when(cartRepository.save(any(Carts.class))).thenAnswer(inv -> {
                Carts c = inv.getArgument(0);
                c.setId(3L);
                return c;
            });

            // Act
            Carts result = cartService.createCart(createDto);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getQuantity()).isEqualTo(5);
            assertThat(result.getProductId()).isEqualTo(2L);
            assertThat(result.getUserId()).isEqualTo(3L);

            verify(userClient).getUserById(3L);
            verify(productClient).getProductById(2L);
            verify(cartRepository).findByUserIdAndProductId(3L, 2L);
            verify(cartRepository).save(any(Carts.class));
        }

        // Test case 03: UTCC03
        // Input: quantity=5, productId=2, userId=2
        // Expected: Update existing cart (quantity becomes 4+5=9)
        // ==========================================================
        @Test
        @DisplayName("UTCC03: quantity=5, productId=2, userId=2 ⇒ update existing cart")
        void createCart_UTCC03_updateExistingCart() {
            // Arrange
            CreateCartDto createDto = new CreateCartDto(5, 2L, 2L);
            UserResponse user = createUserResponse(2L);
            ProductResponse product = createProductResponse(2L);
            Carts existingCart = createCart(2L, 2L, 2L, 4);

            when(userClient.getUserById(2L)).thenReturn(ResponseEntity.ok(user));
            when(productClient.getProductById(2L)).thenReturn(ResponseEntity.ok(product));
            when(cartRepository.findByUserIdAndProductId(2L, 2L)).thenReturn(Optional.of(existingCart));
            when(cartRepository.save(any(Carts.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Carts result = cartService.createCart(createDto);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(2L);
            assertThat(result.getQuantity()).isEqualTo(9); // 4 + 5
            assertThat(result.getProductId()).isEqualTo(2L);
            assertThat(result.getUserId()).isEqualTo(2L);

            verify(userClient).getUserById(2L);
            verify(productClient).getProductById(2L);
            verify(cartRepository).findByUserIdAndProductId(2L, 2L);
            verify(cartRepository).save(existingCart);
        }

        // Test case 04: UTCC04
        // Input: quantity=5, productId=100, userId=2
        // Expected: NotFoundException("Product not found!")
        // ==========================================================
        @Test
        @DisplayName("UTCC04: productId=100 (not exists) ⇒ NotFoundException")
        void createCart_UTCC04_productNotFound() {
            // Arrange
            CreateCartDto createDto = new CreateCartDto(5, 100L, 2L);
            UserResponse user = createUserResponse(2L);

            when(userClient.getUserById(2L)).thenReturn(ResponseEntity.ok(user));
            when(productClient.getProductById(100L)).thenReturn(ResponseEntity.ok(null));

            // Act & Assert
            assertThatThrownBy(() -> cartService.createCart(createDto))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Product not found!");

            verify(userClient).getUserById(2L);
            verify(productClient).getProductById(100L);
            verify(cartRepository, never()).findByUserIdAndProductId(any(), any());
            verify(cartRepository, never()).save(any(Carts.class));
        }

        // Test case 05: UTCC05
        // Input: quantity=5, productId=2, userId=100
        // Expected: NotFoundException("User not found!")
        // ==========================================================
        @Test
        @DisplayName("UTCC05: userId=100 (not exists) ⇒ NotFoundException")
        void createCart_UTCC05_userNotFound() {
            // Arrange
            CreateCartDto createDto = new CreateCartDto(5, 2L, 100L);

            when(userClient.getUserById(100L)).thenReturn(ResponseEntity.ok(null));

            // Act & Assert
            assertThatThrownBy(() -> cartService.createCart(createDto))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("User not found!");

            verify(userClient).getUserById(100L);
            verify(productClient, never()).getProductById(any());
            verify(cartRepository, never()).findByUserIdAndProductId(any(), any());
            verify(cartRepository, never()).save(any(Carts.class));
        }
    }

    // ==========================================================
    // TEST METHOD: updateCart
    // ==========================================================
    @Nested
    @DisplayName("updateCart")
    class updateCart {
        // Precondition: Server works stably
        // Record Cart(id = 2, productId = 2, userId = 2, quantity=4) existed in
        // database

        // Test case 01: UTUC01
        // Input: id=2, quantity=5, productId=3, userId=3
        // Expected: Update successfully
        // ==========================================================
        @Test
        @DisplayName("UTUC01: id=2, quantity=5, productId=3, userId=3 ⇒ success")
        void updateCart_UTUC01_success() {
            // Arrange
            Long id = 2L;
            UpdateCartDto updateDto = new UpdateCartDto(5, 3L, 3L);
            Carts existingCart = createCart(2L, 2L, 2L, 4);
            UserResponse user = createUserResponse(3L);
            ProductResponse product = createProductResponse(3L);

            when(cartRepository.findOneById(id)).thenReturn(Optional.of(existingCart));
            when(userClient.getUserById(3L)).thenReturn(ResponseEntity.ok(user));
            when(productClient.getProductById(3L)).thenReturn(ResponseEntity.ok(product));
            when(cartRepository.save(any(Carts.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Carts result = cartService.updateCart(id, updateDto);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(2L);

            verify(cartRepository).findOneById(id);
            verify(userClient).getUserById(3L);
            verify(productClient).getProductById(3L);
            verify(cartRepository).save(any(Carts.class));
        }

        // Test case 02: UTUC02
        // Input: id=2, quantity=5, productId=3, userId=3
        // Expected: Update successfully
        // ==========================================================
        @Test
        @DisplayName("UTUC02: id=2, quantity=5, productId=3, userId=3 ⇒ success")
        void updateCart_UTUC02_success() {
            // Arrange
            Long id = 2L;
            UpdateCartDto updateDto = new UpdateCartDto(5, 3L, 3L);
            Carts existingCart = createCart(2L, 2L, 2L, 4);
            UserResponse user = createUserResponse(3L);
            ProductResponse product = createProductResponse(3L);

            when(cartRepository.findOneById(id)).thenReturn(Optional.of(existingCart));
            when(userClient.getUserById(3L)).thenReturn(ResponseEntity.ok(user));
            when(productClient.getProductById(3L)).thenReturn(ResponseEntity.ok(product));
            when(cartRepository.save(any(Carts.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Carts result = cartService.updateCart(id, updateDto);

            // Assert
            assertThat(result).isNotNull();

            verify(cartRepository).findOneById(id);
            verify(userClient).getUserById(3L);
            verify(productClient).getProductById(3L);
            verify(cartRepository).save(any(Carts.class));
        }

        // Test case 03: UTUC03
        // Input: id=2, quantity=5, productId=100, userId=3
        // Expected: NotFoundException("Product not found!")
        // ==========================================================
        @Test
        @DisplayName("UTUC03: id=2, productId=100 (not exists) ⇒ NotFoundException")
        void updateCart_UTUC03_productNotFound() {
            // Arrange
            Long id = 2L;
            UpdateCartDto updateDto = new UpdateCartDto(5, 100L, 3L);
            Carts existingCart = createCart(2L, 2L, 2L, 4);
            UserResponse user = createUserResponse(3L);

            when(cartRepository.findOneById(id)).thenReturn(Optional.of(existingCart));
            when(userClient.getUserById(3L)).thenReturn(ResponseEntity.ok(user));
            when(productClient.getProductById(100L)).thenReturn(ResponseEntity.ok(null));

            // Act & Assert
            assertThatThrownBy(() -> cartService.updateCart(id, updateDto))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Product not found!");

            verify(cartRepository).findOneById(id);
            verify(userClient).getUserById(3L);
            verify(productClient).getProductById(100L);
            verify(cartRepository, never()).save(any(Carts.class));
        }

        // Test case 04: UTUC04
        // Input: id=2, quantity=5, productId=null, userId=3
        // Expected: Update successfully (productId not changed)
        // ==========================================================
        @Test
        @DisplayName("UTUC04: id=2, quantity=5, productId=null, userId=3 ⇒ success")
        void updateCart_UTUC04_nullProductId() {
            // Arrange
            Long id = 2L;
            UpdateCartDto updateDto = new UpdateCartDto(5, null, 3L);
            Carts existingCart = createCart(2L, 2L, 2L, 4);
            UserResponse user = createUserResponse(3L);

            when(cartRepository.findOneById(id)).thenReturn(Optional.of(existingCart));
            when(userClient.getUserById(3L)).thenReturn(ResponseEntity.ok(user));
            when(cartRepository.save(any(Carts.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Carts result = cartService.updateCart(id, updateDto);

            // Assert
            assertThat(result).isNotNull();

            verify(cartRepository).findOneById(id);
            verify(userClient).getUserById(3L);
            verify(productClient, never()).getProductById(any());
            verify(cartRepository).save(any(Carts.class));
        }

        // Test case 05: UTUC05
        // Input: id=2, quantity=5, productId=3, userId=100
        // Expected: NotFoundException("User not found!")
        // ==========================================================
        @Test
        @DisplayName("UTUC05: id=2, userId=100 (not exists) ⇒ NotFoundException")
        void updateCart_UTUC05_userNotFound() {
            // Arrange
            Long id = 2L;
            UpdateCartDto updateDto = new UpdateCartDto(5, 3L, 100L);
            Carts existingCart = createCart(2L, 2L, 2L, 4);

            when(cartRepository.findOneById(id)).thenReturn(Optional.of(existingCart));
            when(userClient.getUserById(100L)).thenReturn(ResponseEntity.ok(null));

            // Act & Assert
            assertThatThrownBy(() -> cartService.updateCart(id, updateDto))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("User not found!");

            verify(cartRepository).findOneById(id);
            verify(userClient).getUserById(100L);
            verify(productClient, never()).getProductById(any());
            verify(cartRepository, never()).save(any(Carts.class));
        }

        // Test case 06: UTUC06
        // Input: id=100
        // Expected: NotFoundException("Cart not found")
        // ==========================================================
        @Test
        @DisplayName("UTUC06: id=100 (not exists) ⇒ NotFoundException")
        void updateCart_UTUC06_cartNotFound() {
            // Arrange
            Long id = 100L;
            UpdateCartDto updateDto = new UpdateCartDto(5, 3L, 3L);

            when(cartRepository.findOneById(id)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> cartService.updateCart(id, updateDto))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Cart not found");

            verify(cartRepository).findOneById(id);
            verify(userClient, never()).getUserById(any());
            verify(productClient, never()).getProductById(any());
            verify(cartRepository, never()).save(any(Carts.class));
        }
    }
}
