-- Align existing schema with JPA mappings introduced in phase 3

-- Drop foreign keys temporarily so column types can be adjusted safely.
ALTER TABLE emprunts DROP CONSTRAINT IF EXISTS emprunts_utilisateur_id_fkey;
ALTER TABLE emprunts DROP CONSTRAINT IF EXISTS emprunts_livre_id_fkey;

-- Drop legacy views that depend on the historical integer identifiers.
DROP VIEW IF EXISTS emprunts_actifs;
DROP VIEW IF EXISTS historique_emprunts;

-- Normalize primary and foreign key column types to BIGINT.
ALTER TABLE livres
    ALTER COLUMN id TYPE BIGINT;

ALTER TABLE utilisateurs
    ALTER COLUMN id TYPE BIGINT;

ALTER TABLE emprunts
    ALTER COLUMN id TYPE BIGINT,
    ALTER COLUMN utilisateur_id TYPE BIGINT USING utilisateur_id::bigint,
    ALTER COLUMN livre_id TYPE BIGINT USING livre_id::bigint;

-- Bring column nullability/defaults in line with entity mappings.
UPDATE livres
SET disponible = TRUE
WHERE disponible IS NULL;

ALTER TABLE livres
    ALTER COLUMN disponible SET DEFAULT TRUE,
    ALTER COLUMN disponible SET NOT NULL;

ALTER TABLE emprunts
    ALTER COLUMN utilisateur_id SET NOT NULL,
    ALTER COLUMN livre_id SET NOT NULL;

-- Restore referential integrity with explicit foreign keys.
ALTER TABLE emprunts
    ADD CONSTRAINT emprunts_utilisateur_id_fkey
        FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE CASCADE,
    ADD CONSTRAINT emprunts_livre_id_fkey
        FOREIGN KEY (livre_id) REFERENCES livres(id) ON DELETE CASCADE;

-- Recreate historical views on top of the aligned schema.
CREATE VIEW emprunts_actifs AS
SELECT e.id,
       u.nom AS nom_utilisateur,
       u.email,
       l.titre,
       l.auteur,
       e.date_emprunt
FROM emprunts e
JOIN utilisateurs u ON e.utilisateur_id = u.id
JOIN livres l ON e.livre_id = l.id
WHERE e.date_retour IS NULL;

CREATE VIEW historique_emprunts AS
SELECT e.id,
       u.nom AS nom_utilisateur,
       u.email,
       l.titre,
       l.auteur,
       e.date_emprunt,
       e.date_retour
FROM emprunts e
JOIN utilisateurs u ON e.utilisateur_id = u.id
JOIN livres l ON e.livre_id = l.id
ORDER BY e.date_emprunt DESC;
