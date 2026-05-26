ALTER TABLE livres DROP COLUMN disponible;
DROP INDEX IF EXISTS idx_livres_disponible;
