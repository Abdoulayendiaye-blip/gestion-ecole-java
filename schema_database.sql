-- ============================================
-- Base de données: gestion_cahier_texte
-- ============================================

-- Créer la base de données
CREATE DATABASE IF NOT EXISTS gestion_cahier_texte;
USE gestion_cahier_texte;

-- ============================================
-- Table 1: UTILISATEURS (Administrateurs, Enseignants, Responsables)
-- ============================================
CREATE TABLE IF NOT EXISTS utilisateurs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    login VARCHAR(100) UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,  -- 'CHEF', 'ENSEIGNANT', 'RESPONSABLE'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- Table 2: CLASSES (Groupes d'étudiants)
-- ============================================
CREATE TABLE IF NOT EXISTS classes (
    id_classe INT PRIMARY KEY AUTO_INCREMENT,
    nom_classe VARCHAR(100) NOT NULL UNIQUE
);

-- ============================================
-- Table 3: ETUDIANTS (Étudiants inscrits)
-- ============================================
CREATE TABLE IF NOT EXISTS etudiants (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    id_classe INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_classe) REFERENCES classes(id_classe) ON DELETE CASCADE
);

-- ============================================
-- Table 4: MATIERES (Matières/Cours)
-- ============================================
CREATE TABLE IF NOT EXISTS matieres (
    nom_matiere VARCHAR(100) PRIMARY KEY,
    total_heures_prevues INT DEFAULT 0
);

-- ============================================
-- Table 5: AFFECTATIONS (Enseignant → Matière → Classe)
-- ============================================
CREATE TABLE IF NOT EXISTS affectations (
    enseignant_id INT NOT NULL,
    nom_matiere VARCHAR(100) NOT NULL,
    id_classe INT NOT NULL,
    PRIMARY KEY (enseignant_id, nom_matiere, id_classe),
    FOREIGN KEY (enseignant_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    FOREIGN KEY (nom_matiere) REFERENCES matieres(nom_matiere) ON DELETE CASCADE,
    FOREIGN KEY (id_classe) REFERENCES classes(id_classe) ON DELETE CASCADE
);

-- ============================================
-- Table 6: SEANCES (Sessions de cours - Cahier Texte)
-- ============================================
CREATE TABLE IF NOT EXISTS seances (
    id INT PRIMARY KEY AUTO_INCREMENT,
    enseignant_id INT NOT NULL,
    id_classe INT NOT NULL,
    nom_matiere VARCHAR(100) NOT NULL,
    contenu LONGTEXT,
    date_seance DATE NOT NULL,
    heure_debut TIME,
    duree INT,  -- en minutes
    statut VARCHAR(50) DEFAULT 'Terminé',  -- 'Terminé', 'VALIDÉ', 'REFUSÉ'
    commentaire TEXT,
    valide_par_responsable BOOLEAN DEFAULT FALSE,
    date_validation TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (enseignant_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    FOREIGN KEY (id_classe) REFERENCES classes(id_classe) ON DELETE CASCADE,
    FOREIGN KEY (nom_matiere) REFERENCES matieres(nom_matiere) ON DELETE CASCADE
);

-- ============================================
-- Table 7: ABSENCES (Gestion des absences)
-- ============================================
CREATE TABLE IF NOT EXISTS absences (
    id INT PRIMARY KEY AUTO_INCREMENT,
    etudiant_id INT NOT NULL,
    enseignant_id INT NOT NULL,
    id_classe INT NOT NULL,
    date_absence DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (etudiant_id) REFERENCES etudiants(id) ON DELETE CASCADE,
    FOREIGN KEY (enseignant_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    FOREIGN KEY (id_classe) REFERENCES classes(id_classe) ON DELETE CASCADE
);

-- ============================================
-- Table 8: PAIEMENTS (Gestion des paiements)
-- ============================================
CREATE TABLE IF NOT EXISTS paiements (
    id_paiement INT PRIMARY KEY AUTO_INCREMENT,
    id_etudiant INT NOT NULL,
    montant_paye DECIMAL(10, 2) NOT NULL,
    date_paiement DATE NOT NULL,
    type_paiement VARCHAR(50),  -- 'Scolarité', 'Inscription', 'Examen'
    statut VARCHAR(50) DEFAULT 'Effectué',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_etudiant) REFERENCES etudiants(id) ON DELETE CASCADE
);

-- ============================================
-- Données d'initialisation (par défaut)
-- ============================================

-- Insérer des classes par défaut
INSERT INTO classes (nom_classe) VALUES 
('1ère Année - A'),
('1ère Année - B'),
('2ème Année - A'),
('3ème Année');

-- Insérer des utilisateurs de test pour chaque rôle
-- ========================================
-- 🔐 CHEF D'ÉTABLISSEMENT
INSERT INTO utilisateurs (nom, prenom, email, login, password, role) VALUES 
('Dupont', 'Jean', 'chef@esitec.com', 'chef', 'chef123', 'CHEF');

-- 👨‍🏫 ENSEIGNANTS (3 professeurs)
INSERT INTO utilisateurs (nom, prenom, email, login, password, role) VALUES 
('Martin', 'Marie', 'marie.martin@esitec.com', 'prof1', 'prof123', 'ENSEIGNANT'),
('Bernard', 'Pierre', 'pierre.bernard@esitec.com', 'prof2', 'prof123', 'ENSEIGNANT'),
('Leclerc', 'Sophie', 'sophie.leclerc@esitec.com', 'prof3', 'prof123', 'ENSEIGNANT');

-- 👤 RESPONSABLES DE CLASSE (2 responsables)
INSERT INTO utilisateurs (nom, prenom, email, login, password, role) VALUES 
('Rousseau', 'Jacques', 'jacques.rousseau@esitec.com', 'resp1', 'resp123', 'RESPONSABLE'),
('Girard', 'Claudette', 'claudette.girard@esitec.com', 'resp2', 'resp123', 'RESPONSABLE');

-- Insérer des matières par défaut
INSERT INTO matieres (nom_matiere, total_heures_prevues) VALUES 
('Mathématiques', 60),
('Physique', 60),
('Chimie', 45),
('Français', 40),
('Anglais', 40),
('Histoire-Géographie', 50),
('Informatique', 60),
('Sciences Naturelles', 45);

-- ============================================
-- Données d'exemple pour tester l'application
-- ============================================

-- Ajouter 15 étudiants de test répartis dans les classes
INSERT INTO etudiants (nom, prenom, email, id_classe) VALUES 
-- Classe 1ère Année - A
('Durand', 'Alice', 'alice.durand@student.com', 1),
('Petit', 'Benjamin', 'benjamin.petit@student.com', 1),
('Renard', 'Céline', 'celine.renard@student.com', 1),
('Fontaine', 'David', 'david.fontaine@student.com', 1),
('Lefevre', 'Emma', 'emma.lefevre@student.com', 1),

-- Classe 1ère Année - B
('Gerard', 'Fabrice', 'fabrice.gerard@student.com', 2),
('Picard', 'Gabrielle', 'gabrielle.picard@student.com', 2),
('Caron', 'Henri', 'henri.caron@student.com', 2),
('Marchand', 'Isabelle', 'isabelle.marchand@student.com', 2),
('Brun', 'Jules', 'jules.brun@student.com', 2),

-- Classe 2ème Année - A
('Moreau', 'Karine', 'karine.moreau@student.com', 3),
('Lemoine', 'Luc', 'luc.lemoine@student.com', 3),
('Simon', 'Marie', 'marie.simon@student.com', 3),
('Sauvage', 'Nicolas', 'nicolas.sauvage@student.com', 3),
('Leblanc', 'Olivier', 'olivier.leblanc@student.com', 3);

-- Assigner des matières aux enseignants et classes
-- Prof 1 (Marie) - Mathématiques et Physique
INSERT INTO affectations (enseignant_id, nom_matiere, id_classe) VALUES 
(2, 'Mathématiques', 1),
(2, 'Mathématiques', 2),
(2, 'Physique', 3);

-- Prof 2 (Pierre) - Français et Anglais
INSERT INTO affectations (enseignant_id, nom_matiere, id_classe) VALUES 
(3, 'Français', 1),
(3, 'Anglais', 2),
(3, 'Français', 3);

-- Prof 3 (Sophie) - Sciences Naturelles et Informatique
INSERT INTO affectations (enseignant_id, nom_matiere, id_classe) VALUES 
(4, 'Sciences Naturelles', 1),
(4, 'Informatique', 2),
(4, 'Informatique', 3);

-- Ajouter des séances d'exemple (cahier de texte)
INSERT INTO seances (enseignant_id, id_classe, nom_matiere, contenu, date_seance, heure_debut, duree, statut, valide_par_responsable) VALUES 
(2, 1, 'Mathématiques', 'Chapitre 1 : Algèbre linéaire - Matrices et déterminants', '2026-05-08', '08:00', 60, 'VALIDÉ', true),
(2, 2, 'Mathématiques', 'Exercices pratiques sur les équations du second degré', '2026-05-08', '10:00', 60, 'Terminé', false),
(3, 1, 'Français', 'Analyse du poème « Le Cimetière marin » de Valéry', '2026-05-08', '14:00', 50, 'Terminé', false),
(3, 2, 'Anglais', 'Conversation : Shopping et vocabulaire commercial', '2026-05-07', '13:00', 45, 'VALIDÉ', true),
(4, 2, 'Informatique', 'Introduction à Python : Variables et boucles', '2026-05-06', '09:00', 75, 'VALIDÉ', true);

-- Ajouter quelques absences d'exemple
INSERT INTO absences (etudiant_id, enseignant_id, id_classe, date_absence) VALUES 
(1, 2, 1, '2026-05-05'),
(2, 2, 1, '2026-05-05'),
(3, 3, 1, '2026-05-04'),
(6, 3, 2, '2026-05-03'),
(11, 4, 3, '2026-05-02');

-- Ajouter des paiements d'exemple
INSERT INTO paiements (id_etudiant, montant_paye, date_paiement, type_paiement, statut) VALUES 
(1, 1000.00, '2026-04-01', 'Scolarité', 'Effectué'),
(2, 500.00, '2026-04-15', 'Inscription', 'Effectué'),
(3, 1000.00, '2026-04-01', 'Scolarité', 'Effectué'),
(4, 100.00, '2026-05-01', 'Examen', 'Effectué'),
(5, 1000.00, '2026-04-01', 'Scolarité', 'Effectué'),
(6, 750.00, '2026-04-20', 'Scolarité', 'En attente'),
(7, 1000.00, '2026-04-01', 'Scolarité', 'Effectué'),
(8, 500.00, '2026-04-15', 'Inscription', 'Effectué');

-- ============================================
-- Index pour améliorer les performances
-- ============================================
CREATE INDEX idx_utilisateurs_role ON utilisateurs(role);
CREATE INDEX idx_utilisateurs_login ON utilisateurs(login);
CREATE INDEX idx_etudiants_classe ON etudiants(id_classe);
CREATE INDEX idx_seances_enseignant ON seances(enseignant_id);
CREATE INDEX idx_seances_classe ON seances(id_classe);
CREATE INDEX idx_seances_date ON seances(date_seance);
CREATE INDEX idx_seances_statut ON seances(statut);
CREATE INDEX idx_absences_etudiant ON absences(etudiant_id);
CREATE INDEX idx_absences_date ON absences(date_absence);
CREATE INDEX idx_paiements_etudiant ON paiements(id_etudiant);
CREATE INDEX idx_paiements_date ON paiements(date_paiement);

-- ============================================
-- Affichage de confirmation
-- ============================================
SELECT 'Base de données créée avec succès!' AS message;
SHOW TABLES;
