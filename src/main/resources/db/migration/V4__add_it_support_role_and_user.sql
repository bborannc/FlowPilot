-- 1. IT_SUPPORT rolünün eklenmesi
INSERT INTO roles (name)
VALUES ('IT_SUPPORT')
    ON CONFLICT (name) DO NOTHING;

-- 2. IT_SUPPORT rolüne atanacak örnek çalışan (Can Destek)
-- department_id için 'IT' veya 'Yazılım' isimli departmanı, yoksa ilk bulunan departmanı seçer
INSERT INTO employees (name, email, role_id, department_id, manager_id)
VALUES (
           'Can Destek',
           'can.destek@flowpilot.com',
           (SELECT id FROM roles WHERE name = 'IT_SUPPORT'),
           COALESCE(
                   (SELECT id FROM departments WHERE name ILIKE '%IT%' OR name ILIKE '%Bilgi%' LIMIT 1),
        (SELECT id FROM departments ORDER BY id ASC LIMIT 1)
    ),
           NULL
       )
    ON CONFLICT (email) DO NOTHING;