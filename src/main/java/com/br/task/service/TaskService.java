package com.br.task.service;

import com.br.task.model.Task;
import com.br.task.model.TaskStatus;
import com.br.task.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    public Task criarTarefa(Task task) {
        if (task.getDataHoraEvento().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("A data da tarefa não pode estar no passado!");
        }
        return taskRepository.save(task);
    }

    // Busca todas as tarefas do banco
    public List<Task> listarTodas() {
        return taskRepository.findAll();
    }

    // Busca as tarefas que precisam ser notificadas nas próximas horas
    public List<Task> buscarTarefasParaAvisar(int minutosDeAntecedencia) {
        LocalDateTime horarioLimite = LocalDateTime.now().plusMinutes(minutosDeAntecedencia);
        return taskRepository.findByStatusAndDataHoraEventoBefore(TaskStatus.AGUARDANDO_AVISO, horarioLimite);
    }

    // Atualiza o status da tarefa após o envio da mensagem
    public void atualizarStatus(Long id, TaskStatus novoStatus) {
        taskRepository.findById(id).ifPresent(task -> {
            task.setStatus(novoStatus);
            taskRepository.save(task);
        });
    }
}