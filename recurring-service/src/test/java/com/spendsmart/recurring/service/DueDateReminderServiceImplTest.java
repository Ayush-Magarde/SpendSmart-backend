package com.spendsmart.recurring.service;

import com.spendsmart.recurring.dto.DueDateReminderRequest;
import com.spendsmart.recurring.entity.DueDateReminder;
import com.spendsmart.recurring.exception.RecurringException;
import com.spendsmart.recurring.repository.DueDateReminderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DueDateReminderServiceImplTest {

    @Mock
    private DueDateReminderRepository repository;

    @InjectMocks
    private DueDateReminderServiceImpl service;

    @Test
    void addDueDateReminder() {
        DueDateReminderRequest req = new DueDateReminderRequest();
        req.setTitle("Bill");

        DueDateReminder reminder = new DueDateReminder();
        reminder.setId(1L);

        when(repository.save(any())).thenReturn(reminder);

        DueDateReminder res = service.addDueDateReminder("test@test.com", req);

        assertThat(res.getId()).isEqualTo(1L);
    }

    @Test
    void getDueDateReminders() {
        DueDateReminder reminder = new DueDateReminder();
        when(repository.findByUserEmail("test@test.com")).thenReturn(List.of(reminder));

        List<DueDateReminder> res = service.getDueDateReminders("test@test.com");

        assertThat(res).hasSize(1);
    }

    @Test
    void getActiveDueDateReminders() {
        DueDateReminder reminder = new DueDateReminder();
        when(repository.findByUserEmailAndIsActive("test@test.com", true)).thenReturn(List.of(reminder));

        List<DueDateReminder> res = service.getActiveDueDateReminders("test@test.com");

        assertThat(res).hasSize(1);
    }

    @Test
    void deleteDueDateReminder() {
        DueDateReminder reminder = new DueDateReminder();
        reminder.setUserEmail("test@test.com");

        when(repository.findById(1L)).thenReturn(Optional.of(reminder));

        service.deleteDueDateReminder(1L, "test@test.com");

        verify(repository).delete(reminder);
    }

    @Test
    void deleteDueDateReminder_NotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteDueDateReminder(1L, "test@test.com"))
                .isInstanceOf(RecurringException.class);
    }

    @Test
    void deleteDueDateReminder_Unauthorized() {
        DueDateReminder reminder = new DueDateReminder();
        reminder.setUserEmail("other@test.com");
        when(repository.findById(1L)).thenReturn(Optional.of(reminder));

        assertThatThrownBy(() -> service.deleteDueDateReminder(1L, "test@test.com"))
                .isInstanceOf(RecurringException.class)
                .hasMessageContaining("Access denied");
    }

    @Test
    void toggleDueDateReminder() {
        DueDateReminder reminder = new DueDateReminder();
        reminder.setUserEmail("test@test.com");
        reminder.setIsActive(true);

        when(repository.findById(1L)).thenReturn(Optional.of(reminder));
        when(repository.save(any())).thenReturn(reminder);

        DueDateReminder res = service.toggleDueDateReminder(1L, "test@test.com");

        assertThat(res.getIsActive()).isFalse();
    }

    @Test
    void toggleDueDateReminder_NotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.toggleDueDateReminder(1L, "test@test.com"))
                .isInstanceOf(RecurringException.class);
    }

    @Test
    void toggleDueDateReminder_Unauthorized() {
        DueDateReminder reminder = new DueDateReminder();
        reminder.setUserEmail("other@test.com");
        when(repository.findById(1L)).thenReturn(Optional.of(reminder));

        assertThatThrownBy(() -> service.toggleDueDateReminder(1L, "test@test.com"))
                .isInstanceOf(RecurringException.class)
                .hasMessageContaining("Access denied");
    }

    @Test
    void getAllDueDateReminders() {
        DueDateReminder reminder = new DueDateReminder();
        when(repository.findAll()).thenReturn(List.of(reminder));

        List<DueDateReminder> res = service.getAllDueDateReminders();

        assertThat(res).hasSize(1);
    }

    @Test
    void getDueDateReminderCount() {
        when(repository.count()).thenReturn(5L);

        long count = service.getDueDateReminderCount();

        assertThat(count).isEqualTo(5L);
    }

    @Test
    void getDueDateRemindersByType() {
        DueDateReminder reminder = new DueDateReminder();
        when(repository.findByUserEmailAndTargetType("test@test.com", "BILL")).thenReturn(List.of(reminder));

        List<DueDateReminder> res = service.getDueDateRemindersByType("test@test.com", "BILL");

        assertThat(res).hasSize(1);
    }

    @Test
    void getPendingReminders() {
        DueDateReminder reminder = new DueDateReminder();
        when(repository.findByStatus("PENDING")).thenReturn(List.of(reminder));

        List<DueDateReminder> res = service.getPendingReminders();

        assertThat(res).hasSize(1);
    }

    @Test
    void markReminderAsSent() {
        DueDateReminder reminder = new DueDateReminder();
        reminder.setSentCount(0);

        when(repository.findById(1L)).thenReturn(Optional.of(reminder));
        when(repository.save(any())).thenReturn(reminder);

        DueDateReminder res = service.markReminderAsSent(1L);

        assertThat(res.getStatus()).isEqualTo("SENT");
        assertThat(res.getSentCount()).isEqualTo(1);
    }

    @Test
    void markReminderAsFailed() {
        DueDateReminder reminder = new DueDateReminder();

        when(repository.findById(1L)).thenReturn(Optional.of(reminder));
        when(repository.save(any())).thenReturn(reminder);

        service.markReminderAsFailed(1L);

        assertThat(reminder.getStatus()).isEqualTo("FAILED");
    }

    @Test
    void getExpiredReminders() {
        DueDateReminder reminder = new DueDateReminder();
        when(repository.findByReminderDateBeforeAndStatusNot("2023-01-01", "EXPIRED")).thenReturn(List.of(reminder));

        List<DueDateReminder> res = service.getExpiredReminders("2023-01-01");

        assertThat(res).hasSize(1);
    }

    @Test
    void markReminderAsExpired() {
        DueDateReminder reminder = new DueDateReminder();

        when(repository.findById(1L)).thenReturn(Optional.of(reminder));
        when(repository.save(any())).thenReturn(reminder);

        service.markReminderAsExpired(1L);

        assertThat(reminder.getStatus()).isEqualTo("EXPIRED");
    }

    @Test
    void updateReminderSchedule() {
        DueDateReminder reminder = new DueDateReminder();
        when(repository.save(any())).thenReturn(reminder);

        service.updateReminderSchedule(reminder);

        verify(repository).save(reminder);
    }
}
