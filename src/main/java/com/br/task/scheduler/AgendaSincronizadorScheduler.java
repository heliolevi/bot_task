package com.br.task.scheduler;

import com.br.task.service.GoogleCalendarService;
import com.br.task.service.TaskService;
import com.google.api.services.calendar.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AgendaSincronizadorScheduler {

    private static final Logger logger = LoggerFactory.getLogger(AgendaSincronizadorScheduler.class);

    @Autowired
    private GoogleCalendarService googleCalendarService;

    @Autowired
    private TaskService taskService;

    // Roda a cada 5 minutos (300000 milissegundos)
    @Scheduled(fixedRate = 300000)
    public void buscarNovasTarefasDaAgenda() {
        logger.info("🔄 Sincronizador: Buscando próximos eventos no Google Agenda...");

        List<Event> eventos = googleCalendarService.buscarProximosEventos();

        if (eventos.isEmpty()) {
            logger.info("🔄 Sincronizador: Nenhum evento encontrado na agenda para os próximos dias.");
            return;
        }

        for (Event evento : eventos) {
            taskService.sincronizarEventoDoGoogle(evento);
        }

        logger.info("🔄 Sincronizador: Sincronização com o Google concluída!");
    }
}