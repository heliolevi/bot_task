package com.br.task.service;

import com.br.task.model.Task;
import com.br.task.model.TaskStatus;
import com.br.task.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.api.services.calendar.model.Event;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    public List<Task> listarTodas() {
        return taskRepository.findAll();
    }

    public List<Task> buscarTarefasParaAvisar(int minutosDeAntecedencia) {
        LocalDateTime horarioLimite = LocalDateTime.now().plusMinutes(minutosDeAntecedencia);
        return taskRepository.findByStatusAndDataHoraEventoBefore(TaskStatus.AGUARDANDO_AVISO, horarioLimite);
    }

    public void atualizarStatus(Long id, TaskStatus novoStatus) {
        taskRepository.findById(id).ifPresent(task -> {
            task.setStatus(novoStatus);
            taskRepository.save(task);
        });
    }

    public void sincronizarEventoDoGoogle(Event googleEvent) {
        if (taskRepository.existsByGoogleEventId(googleEvent.getId())) {
            return;
        }

        Task novaTarefa = new Task();

        String tituloEvento = googleEvent.getSummary();
        if (tituloEvento == null || tituloEvento.trim().isEmpty()) {
            tituloEvento = "Tarefa Sem Título";
        }
        novaTarefa.setTitulo(tituloEvento);

        novaTarefa.setDescricao(googleEvent.getDescription() != null ? googleEvent.getDescription() : "Sem descrição");
        novaTarefa.setGoogleEventId(googleEvent.getId());
        novaTarefa.setStatus(TaskStatus.AGUARDANDO_AVISO);

        if (googleEvent.getStart().getDateTime() != null) {
            long epochMilli = googleEvent.getStart().getDateTime().getValue();
            LocalDateTime dataHora = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault());
            novaTarefa.setDataHoraEvento(dataHora);
        } else {
            long epochMilli = googleEvent.getStart().getDate().getValue();
            LocalDateTime dataHora = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault());
            novaTarefa.setDataHoraEvento(dataHora);
        }

        taskRepository.save(novaTarefa);
        System.out.println("📥 Nova tarefa sincronizada do Google Agenda: " + novaTarefa.getTitulo());
    }
}