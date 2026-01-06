BEGIN;

-- Seed constraints (user preferences / rules)
INSERT INTO constraints (id, constraint_key, weight, enabled, params)
VALUES
  ('11111111-1111-1111-1111-111111111111', 'NO_MORNING_CLASSES', 8, true, '{"morningEnd":"10:00"}'::jsonb),
  ('22222222-2222-2222-2222-222222222222', 'NO_FRIDAY', 5, true, '{"bannedDayOfWeek":5}'::jsonb),
  ('33333333-3333-3333-3333-333333333333', 'PREFER_COMPACT_DAYS', 6, true, '{"maxGapMinutes":60}'::jsonb),
  ('44444444-4444-4444-4444-444444444444', 'AVOID_LONG_CONTINUOUS', 4, true, '{"maxContinuousMinutes":180}'::jsonb),
  ('55555555-5555-5555-5555-555555555555', 'PREFER_FREE_LUNCH', 3, true, '{"windowStart":"11:30","windowEnd":"13:00","minFreeMinutes":40}'::jsonb);

-- Seed courses
-- Notes for test coverage:
-- - Deliberate time conflicts: courses 0001 and 0002 overlap (Mon 08:00-09:40), also 0003 and 0004 overlap (Wed 14:00-15:40)
-- - Mix of FIXED and OPTIONAL time slots
INSERT INTO courses (id, name, teacher, location, time_slots)
VALUES
  (
    '00000000-0000-0000-0000-000000000001',
    'Calculus I [4cr] (Required)',
    'Dr. Li',
    'Science Bldg 201',
    '[{"dayOfWeek":1,"start":"08:00","end":"09:40","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16],"type":"FIXED"}]'::jsonb
  ),
  (
    '00000000-0000-0000-0000-000000000002',
    'Physics I [4cr] (Required)',
    'Prof. Wang',
    'Science Bldg 105',
    '[{"dayOfWeek":1,"start":"08:00","end":"09:40","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16],"type":"FIXED"}]'::jsonb
  ),
  (
    '00000000-0000-0000-0000-000000000003',
    'Modern History [3cr]',
    'Dr. Chen',
    'Humanities 303',
    '[{"dayOfWeek":3,"start":"14:00","end":"15:40","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16],"type":"FIXED"}]'::jsonb
  ),
  (
    '00000000-0000-0000-0000-000000000004',
    'Data Structures [4cr] (Required)',
    'Prof. Zhao',
    'CS Lab 2-01',
    '[{"dayOfWeek":3,"start":"14:00","end":"15:40","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16],"type":"FIXED"}]'::jsonb
  ),
  (
    '00000000-0000-0000-0000-000000000005',
    'English Writing [2cr]',
    'Ms. Brown',
    'Language Center 102',
    '[{"dayOfWeek":2,"start":"10:00","end":"11:40","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16],"type":"FIXED"}]'::jsonb
  ),
  (
    '00000000-0000-0000-0000-000000000006',
    'Discrete Mathematics [3cr]',
    'Dr. Liu',
    'Science Bldg 210',
    '[{"dayOfWeek":4,"start":"10:00","end":"11:40","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16],"type":"FIXED"}]'::jsonb
  ),
  (
    '00000000-0000-0000-0000-000000000007',
    'Linear Algebra [3cr]',
    'Dr. Li',
    'Science Bldg 202',
    '[{"dayOfWeek":2,"start":"14:00","end":"15:40","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16],"type":"FIXED"}]'::jsonb
  ),
  (
    '00000000-0000-0000-0000-000000000008',
    'Database Systems [3cr]',
    'Prof. Sun',
    'CS 1-12',
    '[
      {"dayOfWeek":1,"start":"14:00","end":"15:40","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16],"type":"OPTIONAL"},
      {"dayOfWeek":4,"start":"16:00","end":"17:40","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16],"type":"OPTIONAL"}
    ]'::jsonb
  ),
  (
    '00000000-0000-0000-0000-000000000009',
    'Operating Systems [4cr]',
    'Prof. Zhao',
    'CS 2-08',
    '[{"dayOfWeek":5,"start":"10:00","end":"11:40","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16],"type":"FIXED"}]'::jsonb
  ),
  (
    '00000000-0000-0000-0000-00000000000a',
    'Computer Networks [3cr]',
    'Dr. Yang',
    'CS 2-10',
    '[{"dayOfWeek":1,"start":"16:00","end":"17:40","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16],"type":"FIXED"}]'::jsonb
  ),
  (
    '00000000-0000-0000-0000-00000000000b',
    'Software Engineering [3cr]',
    'Dr. Zhang',
    'CS 1-05',
    '[
      {"dayOfWeek":3,"start":"10:00","end":"11:40","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16],"type":"OPTIONAL"},
      {"dayOfWeek":5,"start":"14:00","end":"15:40","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16],"type":"OPTIONAL"}
    ]'::jsonb
  ),
  (
    '00000000-0000-0000-0000-00000000000c',
    'AI Fundamentals [3cr]',
    'Prof. Gu',
    'CS 3-01',
    '[{"dayOfWeek":4,"start":"14:00","end":"15:40","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16],"type":"FIXED"}]'::jsonb
  ),
  (
    '00000000-0000-0000-0000-00000000000d',
    'Machine Learning [3cr]',
    'Prof. Gu',
    'CS 3-01',
    '[{"dayOfWeek":2,"start":"16:00","end":"17:40","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16],"type":"FIXED"}]'::jsonb
  ),
  (
    '00000000-0000-0000-0000-00000000000e',
    'Statistics [3cr]',
    'Dr. He',
    'Science Bldg 110',
    '[{"dayOfWeek":3,"start":"08:00","end":"09:40","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16],"type":"FIXED"}]'::jsonb
  ),
  (
    '00000000-0000-0000-0000-00000000000f',
    'Economics [2cr]',
    'Dr. Xu',
    'Business 204',
    '[{"dayOfWeek":5,"start":"08:00","end":"09:40","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16],"type":"FIXED"}]'::jsonb
  ),
  (
    '00000000-0000-0000-0000-000000000010',
    'Chemistry Lab [1cr]',
    'Ms. Lin',
    'Chem Lab L1',
    '[{"dayOfWeek":4,"start":"18:00","end":"20:40","weeks":[2,4,6,8,10,12,14,16],"type":"FIXED"}]'::jsonb
  ),
  (
    '00000000-0000-0000-0000-000000000011',
    'Art Appreciation [2cr]',
    'Mr. Wu',
    'Arts 101',
    '[{"dayOfWeek":1,"start":"10:00","end":"11:40","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16],"type":"FIXED"}]'::jsonb
  ),
  (
    '00000000-0000-0000-0000-000000000012',
    'Philosophy [2cr]',
    'Dr. Gao',
    'Humanities 210',
    '[{"dayOfWeek":2,"start":"08:00","end":"09:40","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16],"type":"FIXED"}]'::jsonb
  ),
  (
    '00000000-0000-0000-0000-000000000013',
    'PE - Basketball [1cr]',
    'Coach Zhang',
    'Gym Court A',
    '[
      {"dayOfWeek":3,"start":"16:00","end":"17:40","weeks":[1,3,5,7,9,11,13,15],"type":"OPTIONAL"},
      {"dayOfWeek":4,"start":"08:00","end":"09:40","weeks":[2,4,6,8,10,12,14,16],"type":"OPTIONAL"}
    ]'::jsonb
  ),
  (
    '00000000-0000-0000-0000-000000000014',
    'Mobile Development [3cr]',
    'Dr. Yang',
    'CS 2-06',
    '[{"dayOfWeek":4,"start":"14:00","end":"15:40","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16],"type":"FIXED"}]'::jsonb
  ),
  (
    '00000000-0000-0000-0000-000000000015',
    'Cybersecurity [3cr]',
    'Prof. Sun',
    'CS 1-12',
    '[{"dayOfWeek":5,"start":"16:00","end":"17:40","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16],"type":"FIXED"}]'::jsonb
  ),
  (
    '00000000-0000-0000-0000-000000000016',
    'Project Management [2cr]',
    'Ms. Brown',
    'Business 110',
    '[{"dayOfWeek":3,"start":"18:00","end":"19:40","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16],"type":"FIXED"}]'::jsonb
  ),
  (
    '00000000-0000-0000-0000-000000000017',
    'Japanese I [2cr]',
    'Ms. Sato',
    'Language Center 205',
    '[
      {"dayOfWeek":1,"start":"18:00","end":"19:40","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16],"type":"OPTIONAL"},
      {"dayOfWeek":2,"start":"18:00","end":"19:40","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16],"type":"OPTIONAL"}
    ]'::jsonb
  ),
  (
    '00000000-0000-0000-0000-000000000018',
    'Innovation & Entrepreneurship [2cr]',
    'Dr. Xu',
    'Business 204',
    '[{"dayOfWeek":2,"start":"14:00","end":"15:40","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16],"type":"FIXED"}]'::jsonb
  ),
  (
    '00000000-0000-0000-0000-000000000019',
    'Capstone Seminar [1cr] (Required)',
    'Prof. Wang',
    'Main Hall 401',
    '[{"dayOfWeek":5,"start":"14:00","end":"15:40","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16],"type":"FIXED"}]'::jsonb
  ),
  (
    '00000000-0000-0000-0000-00000000001a',
    'Advanced Algorithms [3cr]',
    'Dr. Liu',
    'CS 3-02',
    '[
      {"dayOfWeek":1,"start":"08:00","end":"09:40","weeks":[1,2,3,4,5,6,7,8],"type":"OPTIONAL"},
      {"dayOfWeek":4,"start":"10:00","end":"11:40","weeks":[9,10,11,12,13,14,15,16],"type":"OPTIONAL"}
    ]'::jsonb
  ),
  (
    '00000000-0000-0000-0000-00000000001b',
    'UI/UX Design [2cr]',
    'Mr. Wu',
    'Arts 203',
    '[{"dayOfWeek":3,"start":"10:00","end":"11:40","weeks":[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16],"type":"FIXED"}]'::jsonb
  );

COMMIT;
