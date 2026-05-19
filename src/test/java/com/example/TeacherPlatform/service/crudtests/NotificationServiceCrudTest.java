package com.example.TeacherPlatform.service.crudtests;

import com.example.TeacherPlatform.dataTransferObject.NotificationRequest;
import com.example.TeacherPlatform.dataTransferObject.NotificationResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.model.Notification;
import com.example.TeacherPlatform.model.User;
import com.example.TeacherPlatform.model.enums.NotificationType;
import com.example.TeacherPlatform.repository.NotificationRepository;
import com.example.TeacherPlatform.repository.UserRepository;
import com.example.TeacherPlatform.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService - Business Logic & Security Tests")
class NotificationServiceCrudTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private UserRepository userRepository;
    @Mock private Authentication authentication;

    @InjectMocks
    private NotificationService notificationService;

    private User user;
    private Notification notification;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@edu.ro");

        notification = new Notification();
        notification.setId(1L);
        notification.setUser(user);
        notification.setTitle("Alertă");
        notification.setMessage("Mesaj de test");
        notification.setType(NotificationType.INFO);
        notification.setRead(false);

        // Curățăm mereu SecurityContext-ul ca să nu intervină între teste
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("findMyNotifications - Aduce toate notificările userului")
    void findMyNotifications_returnsAll() {
        when(authentication.getName()).thenReturn("user@edu.ro");
        when(userRepository.findByEmail("user@edu.ro")).thenReturn(Optional.of(user));
        when(notificationRepository.findByUserIdOrdered(1L)).thenReturn(List.of(notification));

        List<NotificationResponse> result = notificationService.findMyNotifications(authentication);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Alertă");
    }

    @Test
    @DisplayName("markAsRead - Marchează notificarea ca citită dacă îi aparține utilizatorului")
    void markAsRead_marksAsRead_ifOwner() {
        when(authentication.getName()).thenReturn("user@edu.ro");
        when(userRepository.findByEmail("user@edu.ro")).thenReturn(Optional.of(user));
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        NotificationResponse result = notificationService.markAsRead(1L, authentication);

        assertThat(result.getRead()).isTrue();
        verify(notificationRepository).save(notification);
    }

    @Test
    @DisplayName("markAsRead - Aruncă excepție (Anti-IDOR) dacă notificarea este a altcuiva")
    void markAsRead_throwsException_ifNotOwner() {
        User altUser = new User();
        altUser.setId(99L);
        altUser.setEmail("alt_user@edu.ro");

        when(authentication.getName()).thenReturn("alt_user@edu.ro");
        when(userRepository.findByEmail("alt_user@edu.ro")).thenReturn(Optional.of(altUser));
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification)); // Notificarea e a userului 1

        assertThatThrownBy(() -> notificationService.markAsRead(1L, authentication))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access denied");
    }

    @Test
    @DisplayName("markAllAsRead - Trece toate notificările necitite pe read=true")
    void markAllAsRead_works() {
        when(authentication.getName()).thenReturn("user@edu.ro");
        when(userRepository.findByEmail("user@edu.ro")).thenReturn(Optional.of(user));
        when(notificationRepository.findUnreadNotificationsByUser(1L)).thenReturn(List.of(notification));

        notificationService.markAllAsRead(authentication);

        assertThat(notification.getRead()).isTrue();
        verify(notificationRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("sendNotification - Salvează o notificare automată corect")
    void sendNotification_savesSuccessfully() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        notificationService.sendNotification(1L, "Test", "Mesaj test", NotificationType.SUCCESS, "/link");

        verify(notificationRepository).save(argThat(n ->
                n.getUser().getId().equals(1L) &&
                        n.getTitle().equals("Test") &&
                        n.getType() == NotificationType.SUCCESS &&
                        !n.getRead()
        ));
    }

    @Test
    @DisplayName("findById - Controlează accesul folosind SecurityContextHolder")
    void findById_withSecurityContext_checksPermissions() {
        // Pentru această metodă, serviciul tău folosește SecurityContextHolder static
        var auth = new UsernamePasswordAuthenticationToken("user@edu.ro", null,
                List.of(new SimpleGrantedAuthority("PROFESOR")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(userRepository.findByEmail("user@edu.ro")).thenReturn(Optional.of(user));

        NotificationResponse result = notificationService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }
}