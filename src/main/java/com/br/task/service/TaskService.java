package com.br.task.service;

import com.br.task.model.Task;
import com.br.task.model.TaskStatus;
import com.br.task.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.api.services.calendar.model.Event;
import com.br.task.model.TaskStatus;
import java.time.LocalDateTime;
import java.time.ZoneId;

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

    public void sincronizarEventoDoGoogle(Event googleEvent) {
        // Verifica se o evento já foi sincronizado antes para não duplicar
        if (taskRepository.findByGoogleEventId(googleEvent.getId()).isPresent()) {
            return; // Já existe, não faz nada
        }

        // Cria uma nova Task a partir do evento do Google
        Task novaTarefa = new Task();
        novaTarefa.setTitulo(googleEvent.getSummary());
        novaTarefa.setDescricao(googleEvent.getDescription() != null ? googleEvent.getDescription() : "Sem descrição");
        novaTarefa.setGoogleEventId(googleEvent.getId());
        novaTarefa.setStatus(TaskStatus.AGUARDANDO_AVISO); // Deixa pronto para o WhatsApp vigiar!

        // Converte a data de início do Google (DateTime) para o LocalDateTime do Java
        if (googleEvent.getStart().getDateTime() != null) {
            long epochMilli = googleEvent.getStart().getDateTime().getValue();
            LocalDateTime dataHora = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault());
            novaTarefa.setDataHoraEvento(dataHora);
        } else {
            // Trata caso seja um evento de "Dia Inteiro" (sem horário específico)
            long epochMilli = googleEvent.getStart().getDate().getValue();
            LocalDateTime dataHora = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(epochMilli), ZoneId.systemDefault());
            novaTarefa.setDataHoraEvento(dataHora);
        }

        // Salva no banco PostgreSQL
        taskRepository.save(novaTarefa);
        System.out.println("📥 Nova tarefa sincronizada do Google Agenda: " + novaTarefa.getTitulo());
    }
}