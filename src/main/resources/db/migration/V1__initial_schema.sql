-- Initial schema creation for the library management system

-- Table des livres
CREATE TABLE IF NOT EXISTS livres (
    id BIGSERIAL PRIMARY KEY,
    titre VARCHAR(255) NOT NULL,
    auteur VARCHAR(255) NOT NULL,
    disponible BOOLEAN DEFAULT TRUE,
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table des utilisateurs
CREATE TABLE IF NOT EXISTS utilisateurs (
    id BIGSERIAL PRIMARY KEY,
    nom VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    date_inscription TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table des emprunts
CREATE TABLE IF NOT EXISTS emprunts (
    id BIGSERIAL PRIMARY KEY,
    utilisateur_id BIGINT REFERENCES utilisateurs(id) ON DELETE CASCADE,
    livre_id BIGINT REFERENCES livres(id) ON DELETE CASCADE,
    date_emprunt DATE NOT NULL DEFAULT CURRENT_DATE,
    date_retour DATE,
    UNIQUE(livre_id, date_retour)
);

-- Index pour améliorer les performances
CREATE INDEX idx_emprunts_utilisateur_id ON emprunts(utilisateur_id);
CREATE INDEX idx_emprunts_livre_id ON emprunts(livre_id);
CREATE INDEX idx_emprunts_date_retour ON emprunts(date_retour);
CREATE INDEX idx_livres_disponible ON livres(disponible);
CREATE INDEX idx_utilisateurs_email ON utilisateurs(email);
