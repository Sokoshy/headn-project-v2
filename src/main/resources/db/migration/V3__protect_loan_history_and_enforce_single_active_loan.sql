-- Protect loan history and guarantee a single active loan per book.

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM emprunts
        WHERE date_retour IS NULL
        GROUP BY livre_id
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Impossible d''appliquer V3 : plusieurs emprunts actifs existent pour un meme livre';
    END IF;
END $$;

ALTER TABLE emprunts DROP CONSTRAINT IF EXISTS emprunts_utilisateur_id_fkey;
ALTER TABLE emprunts DROP CONSTRAINT IF EXISTS emprunts_livre_id_fkey;
ALTER TABLE emprunts DROP CONSTRAINT IF EXISTS emprunts_livre_id_date_retour_key;

ALTER TABLE emprunts
    ADD CONSTRAINT emprunts_utilisateur_id_fkey
        FOREIGN KEY (utilisateur_id) REFERENCES utilisateurs(id) ON DELETE RESTRICT,
    ADD CONSTRAINT emprunts_livre_id_fkey
        FOREIGN KEY (livre_id) REFERENCES livres(id) ON DELETE RESTRICT;

CREATE UNIQUE INDEX IF NOT EXISTS idx_emprunts_livre_actif_unique
    ON emprunts (livre_id)
    WHERE date_retour IS NULL;

UPDATE livres l
SET disponible = NOT EXISTS (
    SELECT 1
    FROM emprunts e
    WHERE e.livre_id = l.id
      AND e.date_retour IS NULL
);
