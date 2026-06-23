package com.br.task.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppService {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppService.class);

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.whatsapp.from}")
    private String whatsappFrom;

    @Value("${twilio.whatsapp.to}")
    private String whatsappTo;


    @PostConstruct
    public void initTwilio() {
        if (!"SEU_ACCOUNT_SID_AQUI".equals(accountSid)) {
            Twilio.init(accountSid, authToken);
            logger.info("📱 Twilio inicializada com sucesso para envio de mensagens!");
        } else {
            logger.warn("⚠️ Chaves da Twilio não configuradas. O envio de WhatsApp está desativado.");
        }
    }

    public void enviarMensagem(String texto) {
        try {
            Message message = Message.creator(
                    new PhoneNumber(whatsappTo),
                    new PhoneNumber(whatsappFrom),
                    texto
            ).create();

            logger.info("... Mensagem enviada para o WhatsApp com sucesso! SID: " + message.getSid());
        } catch (Exception e) {
            logger.error("... Erro ao enviar mensagem pelo WhatsApp: " + e.getMessage());
            e.printStackTrace();
        }
    }
}