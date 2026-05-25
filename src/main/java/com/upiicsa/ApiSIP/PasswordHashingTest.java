package com.upiicsa.ApiSIP;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordHashingTest {
    public static void main(String[] args) {
        String password = "Yatg0809!";

        // Crea el codificador de contraseñas usando BCrypt
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        // Hashea la contraseña
        String hashedPassword = "$2a$10$bt76BAfGr7pKJe9OO3DIyOxlkUdwWH03JHBCa728BFN/4OTfzRDi6";

        // Imprime el hash generado
        System.out.println("Contraseña original: " + password);
        System.out.println("Contraseña hasheada: " + hashedPassword);

        // Verifica si la contraseña coincide con el hash generado (esto es solo para demostración)
        boolean matches = passwordEncoder.matches(password, hashedPassword);
        System.out.println("¿Contraseña coincide con el hash? " + matches);
    }
}
