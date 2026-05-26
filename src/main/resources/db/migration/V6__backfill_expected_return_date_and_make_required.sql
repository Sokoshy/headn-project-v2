-- Backfill existing NULL date_retour_prevue with date_emprunt + 30 days,
-- then enforce NOT NULL constraint.

UPDATE emprunts
SET date_retour_prevue = date_emprunt + INTERVAL '30 days'
WHERE date_retour_prevue IS NULL;

ALTER TABLE emprunts ALTER COLUMN date_retour_prevue SET NOT NULL;
