package com.upiicsa.ApiSIP.Model.Enum;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    INVALID_CONFIRMATION_CODE("Código de confirmación inválido o no corresponde al usuario.",
            HttpStatus.BAD_REQUEST),
    USER_ALREADY_VERIFIED("Este usuario ya ha sido verificado anteriormente.",
            HttpStatus.BAD_REQUEST),
    INVALID_RESET_TOKEN("El token de recuperación es inválido o ya fue utilizado.",
            HttpStatus.BAD_REQUEST),
    INVALID_CREDENTIALS("Correo o contraseña incorrectos.",
            HttpStatus.BAD_REQUEST),
    PASSWORD_MISMATCH("Las contraseñas no coinciden.",
            HttpStatus.BAD_REQUEST),
    DOCUMENT_ALREADY_APPROVED("Este documento ya fue aprobado y no puede modificarse.",
            HttpStatus.BAD_REQUEST),

    TOKEN_EXPIRED("La sesión ha expirado o el token es inválido.",
            HttpStatus.UNAUTHORIZED),

    EMAIL_UNVERIFIED("Debes verificar tu correo antes de iniciar sesión.",
            HttpStatus.FORBIDDEN),

    RESOURCE_NOT_FOUND("El recurso solicitado no existe.",
            HttpStatus.NOT_FOUND),
    PROCESS_NOT_FOUND("No se encontró un proceso activo para este alumno.",
            HttpStatus.NOT_FOUND),
    CATALOG_NOT_FOUND("El elemento solicitado no existe. Elemento: ",
            HttpStatus.NOT_FOUND),

    EMAIL_SEND_FAILED("No se pudo enviar el correo electrónico. Inténtalo más tarde.",
            HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_STORAGE_ERROR("Error al procesar el archivo físico en el servidor.",
            HttpStatus.INTERNAL_SERVER_ERROR),
    PDF_GENERATION_ERROR("Ocurrió un problema al generar o leer el documento PDF.",
            HttpStatus.INTERNAL_SERVER_ERROR),
    INTERNAL_ERROR("Ocurrió un error inesperado en el servidor.",
            HttpStatus.INTERNAL_SERVER_ERROR),

    ALREADY_UNDER_REVIEW("EL alumno ya esta siendo revisado por otro operador.",
            HttpStatus.BAD_REQUEST);

    private final String defaultMessage;
    private final HttpStatus httpStatus;

    ErrorCode(String defaultMessage, HttpStatus httpStatus) {
        this.defaultMessage = defaultMessage;
        this.httpStatus = httpStatus;
    }
}
