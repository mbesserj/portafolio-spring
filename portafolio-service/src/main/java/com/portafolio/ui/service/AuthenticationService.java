package com.portafolio.ui.service;

import com.portafolio.model.entities.UsuarioEntity;
import com.portafolio.persistence.repositorio.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.Optional;
import com.portafolio.ui.util.PasswordSecurityConfig;
import com.portafolio.ui.util.PasswordSecurityConfig.PasswordValidationResult;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    // Dependencias inyectadas por Spring gracias a @RequiredArgsConstructor
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // La lógica de control de intentos se mantiene, ya que es estado interno del servicio.
    private final ConcurrentMap<String, LoginAttemptInfo> loginAttempts = new ConcurrentHashMap<>();

    @Transactional(readOnly = true)
    public AuthenticationResult autenticar(String usuario, String password) {
        if (usuario == null || usuario.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            logger.warn("Intento de login con credenciales vacías.");
            return AuthenticationResult.failure(PasswordSecurityConfig.INVALID_CREDENTIALS_MSG);
        }

        String normalizedUser = usuario.trim().toLowerCase();

        if (isAccountLocked(normalizedUser)) {
            logger.warn("Intento de login en cuenta bloqueada: {}", normalizedUser);
            return AuthenticationResult.failure(PasswordSecurityConfig.ACCOUNT_LOCKED_MSG);
        }

        // Usamos el repositorio de Spring Data JPA
        Optional<UsuarioEntity> userOptional = usuarioRepository.findByUsuarioIgnoreCaseAndFechaInactivoIsNull(normalizedUser);

        if (userOptional.isEmpty()) {
            recordFailedAttempt(normalizedUser);
            logger.warn("Intento de login con usuario inexistente o inactivo: {}", normalizedUser);
            return AuthenticationResult.failure(PasswordSecurityConfig.INVALID_CREDENTIALS_MSG);
        }

        UsuarioEntity user = userOptional.get();

        // Usamos el PasswordEncoder inyectado por Spring
        if (passwordEncoder.matches(password, user.getPassword())) {
            clearLoginAttempts(normalizedUser);
            logger.info("Login exitoso para usuario: {}", normalizedUser);
            return AuthenticationResult.success(user);
        } else {
            recordFailedAttempt(normalizedUser);
            logger.warn("Password incorrecto para usuario: {}", normalizedUser);
            return AuthenticationResult.failure(PasswordSecurityConfig.INVALID_CREDENTIALS_MSG);
        }
    }

    /**
     * Valida y codifica una contraseña de forma segura.
     *
     * @param plainPassword Contraseña en texto plano
     * @return Resultado con la contraseña codificada o error
     */
    public PasswordEncodingResult encodePassword(String plainPassword) {

        PasswordValidationResult validation = PasswordSecurityConfig.validatePassword(plainPassword);

        if (!validation.isValid()) {
            logger.debug("Contraseña rechazada por política de seguridad");
            return PasswordEncodingResult.failure(validation.getErrorMessage());
        }

        try {
            // CODIFICAR CON BCRYPT SEGURO
            String encodedPassword = passwordEncoder.encode(plainPassword);
            logger.debug("Contraseña codificada exitosamente");
            return PasswordEncodingResult.success(encodedPassword);

        } catch (Exception e) {
            logger.error("Error al codificar contraseña", e);
            return PasswordEncodingResult.failure("Error al procesar la contraseña");
        }
    }

    /**
     * Verifica si una cuenta está bloqueada por intentos fallidos.
     */
    private boolean isAccountLocked(String username) {
        LoginAttemptInfo attemptInfo = loginAttempts.get(username);

        if (attemptInfo == null) {
            return false;
        }

        // VERIFICAR SI EL BLOQUEO HA EXPIRADO
        if (attemptInfo.isLockExpired()) {
            loginAttempts.remove(username);
            return false;
        }

        return attemptInfo.isLocked();
    }

    /**
     * Registra un intento de login fallido.
     */
    private void recordFailedAttempt(String username) {
        loginAttempts.compute(username, (key, attemptInfo) -> {
            if (attemptInfo == null) {
                return new LoginAttemptInfo();
            }
            attemptInfo.recordFailedAttempt();
            return attemptInfo;
        });
    }

    /**
     * Limpia los intentos de login fallidos para un usuario.
     */
    private void clearLoginAttempts(String username) {
        loginAttempts.remove(username);
    }

    /**
     * Información de intentos de login para un usuario.
     */
    private static class LoginAttemptInfo {

        private int attempts = 0;
        private LocalDateTime lastAttempt = LocalDateTime.now();
        private LocalDateTime lockTime = null;

        void recordFailedAttempt() {
            attempts++;
            lastAttempt = LocalDateTime.now();

            if (attempts >= PasswordSecurityConfig.MAX_LOGIN_ATTEMPTS) {
                lockTime = LocalDateTime.now().plusMinutes(PasswordSecurityConfig.LOCKOUT_DURATION_MINUTES);
            }
        }

        boolean isLocked() {
            return lockTime != null && LocalDateTime.now().isBefore(lockTime);
        }

        boolean isLockExpired() {
            return lockTime != null && LocalDateTime.now().isAfter(lockTime);
        }
    }

    /**
     * Resultado de autenticación.
     */
    public static class AuthenticationResult {

        private final boolean success;
        private final String errorMessage;
        private final UsuarioEntity user;

        private AuthenticationResult(boolean success, String errorMessage, UsuarioEntity user) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.user = user;
        }

        public static AuthenticationResult success(UsuarioEntity user) {
            return new AuthenticationResult(true, null, user);
        }

        public static AuthenticationResult failure(String errorMessage) {
            return new AuthenticationResult(false, errorMessage, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public UsuarioEntity getUser() {
            return user;
        }
    }

    /**
     * Resultado de codificación de contraseña.
     */
    public static class PasswordEncodingResult {

        private final boolean success;
        private final String encodedPassword;
        private final String errorMessage;

        private PasswordEncodingResult(boolean success, String encodedPassword, String errorMessage) {
            this.success = success;
            this.encodedPassword = encodedPassword;
            this.errorMessage = errorMessage;
        }

        public static PasswordEncodingResult success(String encodedPassword) {
            return new PasswordEncodingResult(true, encodedPassword, null);
        }

        public static PasswordEncodingResult failure(String errorMessage) {
            return new PasswordEncodingResult(false, null, errorMessage);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getEncodedPassword() {
            return encodedPassword;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
