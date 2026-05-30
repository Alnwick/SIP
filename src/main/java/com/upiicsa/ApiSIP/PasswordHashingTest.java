package com.upiicsa.ApiSIP;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordHashingTest {
    public static void main(String[] args) {
        String password = "Yatg0809";

        // Crea el codificador de contraseñas usando BCrypt
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        // Hashea la contraseña
        for(int i = 1; i<=40; i++) {
            String hashedPassword = passwordEncoder.encode(password);

            // Imprime el hash generado
            //System.out.println("Contraseña original: " + password);
            System.out.println("Contraseña hasheada " + i + " : " + hashedPassword);
        }
        // Verifica si la contraseña coincide con el hash generado (esto es solo para demostración)
        //boolean matches = passwordEncoder.matches(password, hashedPassword);
        //System.out.println("¿Contraseña coincide con el hash? " + matches);
    }
}
