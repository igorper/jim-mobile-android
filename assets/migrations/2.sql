DELETE FROM TrainingPlans;
DELETE FROM CompletedTrainings;
ALTER TABLE TrainingPlans ADD COLUMN user_id INTEGER;
ALTER TABLE CompletedTrainings ADD COLUMN user_id INTEGER;