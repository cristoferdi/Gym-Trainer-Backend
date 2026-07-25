INSERT INTO trainers (id, full_name, email, password, phone_number, created_at, updated_at)
VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Trainer A', 'trainerA@test.com',
        '$2a$10$N9qo8uLOickgxQZ7Y7R8HOH8V3P7pVq1q8e5p6q7r8s9t0u1v2w3x4y5z',
        '111-1111', NOW(), NOW()),
       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Trainer B', 'trainerB@test.com',
        '$2a$10$N9qo8uLOickgxQZ7Y7R8HOH8V3P7pVq1q8e5p6q7r8s9t0u1v2w3x4y5z',
        '222-2222', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

INSERT INTO students (id, trainer_id, full_name, age, created_at, updated_at)
VALUES ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb',
        'Student of B', 20, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;
