package com.br.task.repository;

import com.br.task.model.Task;
import com.br.task.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByStatusAndDataHoraEventoBefore(TaskStatus status, LocalDateTime horarioLimite);

    boolean existsByGoogleEventId(String googleEventId);
}