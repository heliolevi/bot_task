package com.br.task.repository;

import com.br.task.model.Task;
import com.br.task.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStatusAndDataHoraEventoBefore(TaskStatus status, LocalDateTime dataHora);

    Optional<Task> findByGoogleEventId(String googleEventId);
    boolean existsByGoogleEventId(String googleEventId);
}