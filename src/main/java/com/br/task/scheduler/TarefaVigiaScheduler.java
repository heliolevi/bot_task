package com.br.task.scheduler;

import com.br.task.model.Task;
import com.br.task.model.TaskStatus;
import com.br.task.service.TaskService;
import com.br.task.service.WhatsAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TarefaVigiaScheduler {

    private static final Logger logger = LoggerFactory.getLogger(TarefaVigiaScheduler.class);

    @Autowired
    private TaskService taskService;

    @Autowired
    private WhatsAppService whatsAppService;

    @Scheduled(fixedRate = 60000)
    public void verificarTarefasProximas() {
        logger.info(" ...Verificando se há tarefas pendentes para avisar... ");

        int minutosAntecedencia = 60;
        List<Task> tarefasParaAvisar = taskService.buscarTarefasParaAvisar(minutosAntecedencia);

        if (tarefasParaAvisar.isEmpty()) {
            logger.info("Nenhuma tarefa crítica encontrada para a próxima hora.");
            return;
        }

        for (Task task : tarefasParaAvisar) {
            logger.info("🚨 ALERTA DE TAREFA PRÓXIMA! 🚨");
            logger.info("Título: " + task.getTitulo());
            logger.info("Descrição: " + task.getDescricao());
            logger.info("Horário do Evento: " + task.getDataHoraEvento());

            String mensagemTexto = "Your appointment is coming up on " + task.getDataHoraEvento() + ". " + task.getTitulo() + ": " + task.getDescricao();
            whatsAppService.enviarMensagem(mensagemTexto);

            taskService.atualizarStatus(task.getId(), TaskStatus.AVISADO);
            logger.info("✅ Status da tarefa ID " + task.getId() + " atualizado para AVISADO.");
        }
    }
}