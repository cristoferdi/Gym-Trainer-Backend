INSERT INTO trainers (id, full_name, email, password, phone_number, created_at, updated_at)
VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Trainer A', 'trainerA@test.com',
        '$2a$10$N9qo8uLOickgxQZ7Y7R8HOH8V3P7pVq1q8e5p6q7r8s9t0u1v2w3x4y5z',
        '111-1111', NOW(), NOW()),
       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Trainer B', 'trainerB@test.com',
        '$2a$10$N9qo8uLOickgxQZ7Y7R8HOH8V3P7pVq1q8e5p6q7r8s9t0u1v2w3x4y5z',
        '222-2222', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO students (id, trainer_id, full_name, age, objetivos, created_at, updated_at)
VALUES ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        'Student of A', 25, 'Ganar masa muscular', NOW(), NOW()),
       ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        'Student of B', 20, 'Perder grasa', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO routines (id, trainer_id, name, description, coach_name, created_at, updated_at)
VALUES ('ffffffff-ffff-ffff-ffff-ffffffffffff', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        'Template A', '{"content":"Descripcion de template A"}', 'Coach A', NOW(), NOW()),
       ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        'Template B', '{"content":"Descripcion de template B"}', 'Coach B', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO routine_days (id, routine_id, name, order_index, created_at)
VALUES ('11111111-1111-1111-1111-111111111111', 'ffffffff-ffff-ffff-ffff-ffffffffffff',
        'Dia 1', 0, NOW()),
       ('22222222-2222-2222-2222-222222222222', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
        'Dia 1', 0, NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO exercise_blocks (id, day_id, is_combined, series, reps, rest_time, indications, created_at)
VALUES ('33333333-3333-3333-3333-333333333333', '11111111-1111-1111-1111-111111111111',
        false, 3, 12, '60s', 'Calentar antes', NOW()),
       ('44444444-4444-4444-4444-444444444444', '22222222-2222-2222-2222-222222222222',
        false, 4, 10, '90s', 'Mantener postura', NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO sub_exercise_details (id, block_id, exercise_id, name, gif_url, video_url, instructions, order_index)
VALUES ('55555555-5555-5555-5555-555555555555', '33333333-3333-3333-3333-333333333333',
        'ex-001', 'Press Banca', 'http://test.com/press.gif', 'http://test.com/press.mp4',
        'Bajar lento|Subir explosivo', 0),
       ('66666666-6666-6666-6666-666666666666', '44444444-4444-4444-4444-444444444444',
        'ex-002', 'Sentadilla', 'http://test.com/squat.gif', 'http://test.com/squat.mp4',
        'Bajar profundo|Subir fuerte', 0)
ON CONFLICT (id) DO NOTHING;
