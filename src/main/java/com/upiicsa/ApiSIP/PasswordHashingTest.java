package com.upiicsa.ApiSIP;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordHashingTest {
    public static void main(String[] args) {
        String password = "Yatg0809";

        // Crea el codificador de contraseñas usando BCrypt
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        // Hashea la contraseña
        String hashedPassword = "$2a$10$d29ALUjQUqL7Hk3iAGgkIuCYidJppcfcawmwqVsZH6YnCpNSunG92";

        // Imprime el hash generado
        System.out.println("Contraseña original: " + password);
        System.out.println("Contraseña hasheada: " + hashedPassword);

        // Verifica si la contraseña coincide con el hash generado (esto es solo para demostración)
        boolean matches = passwordEncoder.matches(password, hashedPassword);
        System.out.println("¿Contraseña coincide con el hash? " + matches);
    }
}
