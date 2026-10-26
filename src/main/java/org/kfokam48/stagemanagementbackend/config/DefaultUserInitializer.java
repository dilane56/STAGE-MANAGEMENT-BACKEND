package org.kfokam48.stagemanagementbackend.config;
import org.kfokam48.stagemanagementbackend.enums.Roles;
import org.kfokam48.stagemanagementbackend.model.Administrateur;
import org.kfokam48.stagemanagementbackend.repository.AdministrateurRepository;
import org.kfokam48.stagemanagementbackend.repository.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Crée un administrateur au premier démarrage si aucun n'existe.
 * Identifiants configurables via ADMIN_EMAIL / ADMIN_PASSWORD (voir application.properties).
 */
@Component
public class DefaultUserInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DefaultUserInitializer.class);
    private static final String INSECURE_DEFAULT_PASSWORD = "password";

    private final AdministrateurRepository administrateurRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public DefaultUserInitializer(AdministrateurRepository administrateurRepository,
                                  UtilisateurRepository utilisateurRepository,
                                  PasswordEncoder passwordEncoder,
                                  @Value("${app.admin.email:admin@gmail.com}") String adminEmail,
                                  @Value("${app.admin.password:" + INSECURE_DEFAULT_PASSWORD + "}") String adminPassword) {
        this.administrateurRepository = administrateurRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }


    @Override
    public void run(String... args) {
        if (administrateurRepository.count() > 0 || utilisateurRepository.existsByEmail(adminEmail)) {
            logger.debug("Un administrateur existe déjà, aucun compte par défaut créé");
            return;
        }

        Administrateur user = new Administrateur();
        user.setFullName("Administrateur");
        user.setEmail(adminEmail);
        user.setPassword(passwordEncoder.encode(adminPassword));
        user.setRole(Roles.ADMIN);
        user.setCreateAt(LocalDate.now());
        administrateurRepository.save(user);

        logger.info("Administrateur par défaut créé : {}", adminEmail);
        if (INSECURE_DEFAULT_PASSWORD.equals(adminPassword)) {
            logger.warn("Le compte administrateur utilise le mot de passe par défaut : définissez ADMIN_PASSWORD et changez-le immédiatement.");
        }
    }
}
