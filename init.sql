-- =============================================================================
-- init.sql — Seed data for the Library Management System
-- Runs after Flyway migrations V1–V6 have been applied.
-- =============================================================================

-- Insertion des données de test pour les livres
INSERT INTO livres (titre, auteur) VALUES
('Le Petit Prince', 'Antoine de Saint-Exupéry'),
('1984', 'George Orwell'),
('L''Alchimiste', 'Paulo Coelho'),
('Les Misérables', 'Victor Hugo'),
('Harry Potter à l''école des sorciers', 'J.K. Rowling'),
('Le Seigneur des Anneaux', 'J.R.R. Tolkien'),
('Pride and Prejudice', 'Jane Austen'),
('To Kill a Mockingbird', 'Harper Lee');

-- Insertion des données de test pour les utilisateurs
INSERT INTO utilisateurs (nom, email) VALUES
('Alice Dupont', 'alice@example.com'),
('Bob Martin', 'bob@example.com'),
('Charlie Lambert', 'charlie@example.com'),
('David Morel', 'david@example.com'),
('Emma Leroy', 'emma@example.com');

-- Insertion des données de test pour les emprunts
-- date_retour_prevue is required (NOT NULL), set to 30 days after date_emprunt
INSERT INTO emprunts (utilisateur_id, livre_id, date_emprunt, date_retour, date_retour_prevue) VALUES
(1, 2, '2026-02-01', NULL,        '2026-03-02'),  -- Alice emprunte "1984" (en cours)
(2, 3, '2026-01-20', '2026-02-02', '2026-02-19'),  -- Bob a emprunté "L'Alchimiste" (rendu)
(3, 1, '2026-01-25', NULL,        '2026-02-24'),  -- Charlie emprunte "Le Petit Prince" (en cours)
(4, 5, '2026-01-30', NULL,        '2026-02-29'),  -- David emprunte "Harry Potter" (en cours)
(5, 4, '2026-02-03', '2026-02-15', '2026-03-04'); -- Emma a emprunté "Les Misérables" (rendu)
