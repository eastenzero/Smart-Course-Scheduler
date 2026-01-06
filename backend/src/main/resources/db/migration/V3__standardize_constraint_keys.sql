UPDATE constraints
SET
  constraint_key = 'NO_MORNING_CLASSES',
  params = CASE
    WHEN params ? 'morningEnd' THEN params - 'earliestStart'
    WHEN params ? 'earliestStart' THEN jsonb_set(params - 'earliestStart', '{morningEnd}', to_jsonb(params->>'earliestStart'), true)
    ELSE params
  END,
  updated_at = now()
WHERE constraint_key = 'NO_EARLY_CLASS';

UPDATE constraints
SET
  constraint_key = 'MAX_DAYS_OFF',
  updated_at = now()
WHERE constraint_key = 'MAXIMIZE_DAYS_OFF';
