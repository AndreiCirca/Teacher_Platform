package com.example.TeacherPlatform.service.functinalitiestests;

import com.example.TeacherPlatform.dataTransferObject.NotificationResponse;
import com.example.TeacherPlatform.exception.ResourceNotFoundException;
import com.example.TeacherPlatform.model.Notification;
import com.example.TeacherPlatform.model.User;
import com.example.TeacherPlatform.model.enums.NotificationType;
import com.example.TeacherPlatform.model.enums.UserRole;
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
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService - Functionality Tests (non-CRUD)")
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private NotificationService notificationService;

    private User userFilip;
    private User userAndreea;
    private Notification notif1;
    private Notification notif2Unread;

    @BeforeEach
    void setUp() {
        userFilip   = buildUser(1L, "Filip",   "Mureșan", "filip@edu.ro",   UserRole.PROFESOR);
        userAndreea = buildUser(2L, "Andreea", "Ionescu", "andreea@tech.ro", UserRole.FORMATOR);

        notif1        = buildNotification(10L, userFilip, "Curs nou disponibil",  "Cursul X a fost publicat.", NotificationType.INFO,    false, "/courses/1");
        notif2Unread  = buildNotification(11L, userFilip, "Înscriere confirmată", "Înscrierea la cursul Y a fost confirmată.", NotificationType.SUCCESS, false, "/enrollments/2");
    }

    // -------------------------------------------------------------------------
    // findMyNotifications
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findMyNotifications - returns all notifications for authenticated user, ordered by createdAt desc")
    void findMyNotifications_returnsAllForUser() {
        when(authentication.getName()).thenReturn("filip@edu.ro");
        when(userRepository.findByEmail("filip@edu.ro")).thenReturn(Optional.of(userFilip));
        when(notificationRepository.findByUserIdOrdered(1L)).thenReturn(List.of(notif2Unread, notif1));

        List<NotificationResponse> result = notificationService.findMyNotifications(authentication);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Înscriere confirmată");
        assertThat(result.get(1).getTitle()).isEqualTo("Curs nou disponibil");
        verify(notificationRepository).findByUserIdOrdered(1L);
    }

    @Test
    @DisplayName("findMyNotifications - returns empty list when user has no notifications")
    void findMyNotifications_returnsEmptyList_whenNone() {
        when(authentication.getName()).thenReturn("filip@edu.ro");
        when(userRepository.findByEmail("filip@edu.ro")).thenReturn(Optional.of(userFilip));
        when(notificationRepository.findByUserIdOrdered(1L)).thenReturn(List.of());

        List<NotificationResponse> result = notificationService.findMyNotifications(authentication);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findMyNotifications - throws ResourceNotFoundException when user email not found")
    void findMyNotifications_throwsException_whenUserNotFound() {
        when(authentication.getName()).thenReturn("ghost@edu.ro");
        when(userRepository.findByEmail("ghost@edu.ro")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.findMyNotifications(authentication))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ghost@edu.ro");
    }

    // -------------------------------------------------------------------------
    // findUnreadMyNotifications
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findUnreadMyNotifications - returns only unread notifications for user")
    void findUnreadMyNotifications_returnsUnreadOnly() {
        Notification readNotif = buildNotification(12L, userFilip, "Veche", "Mesaj vechi.", NotificationType.INFO, true, null);

        when(authentication.getName()).thenReturn("filip@edu.ro");
        when(userRepository.findByEmail("filip@edu.ro")).thenReturn(Optional.of(userFilip));
        when(notificationRepository.findUnreadNotificationsByUser(1L)).thenReturn(List.of(notif1, notif2Unread));

        List<NotificationResponse> result = notificationService.findUnreadMyNotifications(authentication);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(NotificationResponse::getRead).containsOnly(false);
    }

    @Test
    @DisplayName("findUnreadMyNotifications - returns empty list when all notifications are read")
    void findUnreadMyNotifications_returnsEmpty_whenAllRead() {
        when(authentication.getName()).thenReturn("filip@edu.ro");
        when(userRepository.findByEmail("filip@edu.ro")).thenReturn(Optional.of(userFilip));
        when(notificationRepository.findUnreadNotificationsByUser(1L)).thenReturn(List.of());

        List<NotificationResponse> result = notificationService.findUnreadMyNotifications(authentication);

        assertThat(result).isEmpty();
    }

    // -------------------------------------------------------------------------
    // findRecentMyNotifications
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("findRecentMyNotifications - returns up to 10 recent notifications")
    void findRecentMyNotifications_returnsRecentNotifications() {
        List<Notification> recent = List.of(notif2Unread, notif1);

        when(authentication.getName()).thenReturn("filip@edu.ro");
        when(userRepository.findByEmail("filip@edu.ro")).thenReturn(Optional.of(userFilip));
        when(notificationRepository.findRecentNotificationsByUser(1L)).thenReturn(recent);

        List<NotificationResponse> result = notificationService.findRecentMyNotifications(authentication);

        assertThat(result).hasSize(2);
        verify(notificationRepository).findRecentNotificationsByUser(1L);
    }

    // -------------------------------------------------------------------------
    // countUnread
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("countUnread - returns correct count of unread notifications")
    void countUnread_returnsCorrectCount() {
        when(authentication.getName()).thenReturn("filip@edu.ro");
        when(userRepository.findByEmail("filip@edu.ro")).thenReturn(Optional.of(userFilip));
        when(notificationRepository.countUnreadNotifications(1L)).thenReturn(5L);

        long count = notificationService.countUnread(authentication);

        assertThat(count).isEqualTo(5L);
    }

    @Test
    @DisplayName("countUnread - returns 0 when no unread notifications")
    void countUnread_returnsZero_whenNoneUnread() {
        when(authentication.getName()).thenReturn("filip@edu.ro");
        when(userRepository.findByEmail("filip@edu.ro")).thenReturn(Optional.of(userFilip));
        when(notificationRepository.countUnreadNotifications(1L)).thenReturn(0L);

        long count = notificationService.countUnread(authentication);

        assertThat(count).isEqualTo(0L);
    }

    // -------------------------------------------------------------------------
    // markAsRead
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("markAsRead - marks notification as read for the owning user")
    void markAsRead_marksAsRead_forOwner() {
        when(authentication.getName()).thenReturn("filip@edu.ro");
        when(notificationRepository.findById(10L)).thenReturn(Optional.of(notif1));
        when(userRepository.findByEmail("filip@edu.ro")).thenReturn(Optional.of(userFilip));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        NotificationResponse result = notificationService.markAsRead(10L, authentication);

        assertThat(result.getRead()).isTrue();
        verify(notificationRepository).save(notif1);
    }

    @Test
    @DisplayName("markAsRead - throws exception when notification does not belong to authenticated user")
    void markAsRead_throwsException_whenNotificationBelongsToOtherUser() {
        Notification otherUserNotif = buildNotification(20L, userAndreea, "Altă notificare", "Mesaj.", NotificationType.INFO, false, null);

        when(authentication.getName()).thenReturn("filip@edu.ro");
        when(notificationRepository.findById(20L)).thenReturn(Optional.of(otherUserNotif));
        when(userRepository.findByEmail("filip@edu.ro")).thenReturn(Optional.of(userFilip));

        assertThatThrownBy(() -> notificationService.markAsRead(20L, authentication))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Access denied");

        verify(notificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("markAsRead - throws ResourceNotFoundException when notification not found")
    void markAsRead_throwsException_whenNotificationNotFound() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(99L, authentication))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // -------------------------------------------------------------------------
    // markAllAsRead
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("markAllAsRead - marks all unread notifications as read for authenticated user")
    void markAllAsRead_marksAllUnreadAsRead() {
        when(authentication.getName()).thenReturn("filip@edu.ro");
        when(userRepository.findByEmail("filip@edu.ro")).thenReturn(Optional.of(userFilip));
        when(notificationRepository.findUnreadNotificationsByUser(1L)).thenReturn(List.of(notif1, notif2Unread));
        when(notificationRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        notificationService.markAllAsRead(authentication);

        assertThat(notif1.getRead()).isTrue();
        assertThat(notif2Unread.getRead()).isTrue();
        verify(notificationRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("markAllAsRead - does nothing when there are no unread notifications")
    void markAllAsRead_doesNothing_whenNoneUnread() {
        when(authentication.getName()).thenReturn("filip@edu.ro");
        when(userRepository.findByEmail("filip@edu.ro")).thenReturn(Optional.of(userFilip));
        when(notificationRepository.findUnreadNotificationsByUser(1L)).thenReturn(List.of());

        notificationService.markAllAsRead(authentication);

        verify(notificationRepository).saveAll(List.of());
    }

    // -------------------------------------------------------------------------
    // sendNotification
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("sendNotification - creates and saves a new notification for target user")
    void sendNotification_savesNewNotification() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userFilip));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationService.sendNotification(1L, "Titlu Test", "Mesaj test", NotificationType.INFO, "/test");

        verify(notificationRepository).save(argThat(n ->
                n.getUser().equals(userFilip) &&
                        n.getTitle().equals("Titlu Test") &&
                        n.getMessage().equals("Mesaj test") &&
                        n.getType() == NotificationType.INFO &&
                        n.getActionUrl().equals("/test") &&
                        Boolean.FALSE.equals(n.getRead())
        ));
    }

    @Test
    @DisplayName("sendNotification - throws ResourceNotFoundException when target user not found")
    void sendNotification_throwsException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                notificationService.sendNotification(99L, "T", "M", NotificationType.INFO, null))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");

        verify(notificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("sendNotification - sets read to false by default")
    void sendNotification_setsReadFalseByDefault() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(userAndreea));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        notificationService.sendNotification(2L, "T", "M", NotificationType.WARNING, null);

        verify(notificationRepository).save(argThat(n -> Boolean.FALSE.equals(n.getRead())));
    }

    @Test
    @DisplayName("sendNotification - actionUrl can be null")
    void sendNotification_allowsNullActionUrl() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userFilip));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatCode(() ->
                notificationService.sendNotification(1L, "Titlu", "Mesaj", NotificationType.INFO, null))
                .doesNotThrowAnyException();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private User buildUser(Long id, String firstName, String lastName, String email, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword("encoded_pass");
        user.setRole(role);
        user.setActive(true);
        user.setEmailVerified(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    private Notification buildNotification(Long id, User user, String title, String message,
                                           NotificationType type, boolean read, String actionUrl) {
        Notification n = new Notification();
        n.setId(id);
        n.setUser(user);
        n.setTitle(title);
        n.setMessage(message);
        n.setType(type);
        n.setRead(read);
        n.setActionUrl(actionUrl);
        n.setCreatedAt(LocalDateTime.now());
        n.setUpdatedAt(LocalDateTime.now());
        return n;
    }
}