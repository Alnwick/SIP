package com.upiicsa.ApiSIP.Service.Infrastructure;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {


    @Value("${SPRING_EMAIL_USERNAME}")
    private String email;

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async("emailExecutor")
    public void sendResetEmail(String toEmail, String resetUrl) {
        log.info("Iniciando envío asíncrono de restablecimiento de contraseña hacia: {}", toEmail);

        SimpleMailMessage message = getMailMessage(toEmail, resetUrl);

        try {
            mailSender.send(message);
            log.info("Correo de restablecimiento enviado exitosamente a {}", toEmail);
        } catch (Exception e) {
            log.error("FALLO CRÍTICO al enviar correo de restablecimiento a {}. Motivo: {}", toEmail, e.getMessage(), e);
        }
    }

    private @NonNull SimpleMailMessage getMailMessage(String toEmail, String resetUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(email);
        message.setTo(toEmail);
        message.setSubject("Solicitud de Restablecimiento de Contraseña");

        String emailContent = "Estimado usuario,\n\n"
                + "Hemos recibido una solicitud para restablecer la contraseña de tu cuenta.\n"
                + "Por favor, haz clic en el siguiente enlace para continuar. Este enlace expira en 60 minutos.\n\n"
                + "Enlace de Restablecimiento: " + resetUrl + "\n\n"
                + "Si no solicitaste este cambio, puedes ignorar este correo.\n\n"
                + "Atentamente,\n"
                + "Tu equipo de Soporte.";
        message.setText(emailContent);
        return message;
    }

    @Async("emailExecutor")
    public void sendConfirmationCode(String toEmail, String code) {
        log.info("Iniciando envío asíncrono de código de verificación hacia: {}", toEmail);

        SimpleMailMessage message = getSimpleMailMessage(toEmail, code);

        try {
            mailSender.send(message);
            log.info("Correo de verificación enviado exitosamente a {}", toEmail);
        } catch (Exception e) {
            log.error("FALLO CRÍTICO al enviar código de verificación a {}. Motivo: {}", toEmail, e.getMessage(), e);
        }
    }

    private @NonNull SimpleMailMessage getSimpleMailMessage(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(email);
        message.setTo(toEmail);
        message.setSubject("Verificación de Correo Electrónico");

        String emailContent = "¡Bienvenido a nuestra plataforma!\n\n"
                + "Tu registro está casi completo. Por favor, utiliza el siguiente código para verificar tu dirección de correo electrónico:\n\n"
                + "Código de Verificación: " + code + "\n\n"
                + "Este código expira en 15 minutos.\n\n"
                + "Atentamente,\n"
                + "Tu equipo de Soporte.";

        message.setText(emailContent);
        return message;
    }
}
