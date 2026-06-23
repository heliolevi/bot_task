package com.br.task.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleCalendarService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleCalendarService.class);
    private Calendar calendarService;

    @Value("${google.calendar.id}")
    private String calendarId;

    @Value("${google.google-calendar.json.path}")
    private String credentialsPath;

    private final ResourceLoader resourceLoader;

    public GoogleCalendarService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void initGoogleCalendar() {
        try {
            // Carrega dinamicamente a partir do caminho configurado (seja arquivo local ou classpath)
            Resource resource = resourceLoader.getResource(credentialsPath);
            InputStream in = resource.getInputStream();

            GoogleCredentials credentials = GoogleCredentials.fromStream(in)
                    .createScoped(Collections.singleton(CalendarScopes.CALENDAR_READONLY));

            this.calendarService = new Calendar.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName("Task Scheduler Watcher")
                    .build();

            logger.info("📅 Google Calendar API inicializada com sucesso usando a Service Account!");
        } catch (Exception e) {
            logger.error("❌ Erro ao inicializar a integração com Google Calendar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Event> buscarProximosEventos() {
        if (calendarService == null) {
            logger.warn("⚠️ Serviço do Google Calendar não está ativo.");
            return Collections.emptyList();
        }
        try {
            com.google.api.client.util.DateTime agora = new com.google.api.client.util.DateTime(Instant.now().toEpochMilli());

            Events events = calendarService.events().list(calendarId)
                    .setMaxResults(5)
                    .setTimeMin(agora)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();

            return events.getItems();
        } catch (IOException e) {
            logger.error("❌ Erro ao buscar os eventos da agenda: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}