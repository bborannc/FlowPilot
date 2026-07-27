-- Örnek test kullanıcıları (Dinamik Subquery kullanımı ile)

-- 1. Yönetici Ekleme
INSERT INTO employees (name, email, department_id, role_id, manager_id)
VALUES (
           'Gökhan Yönetici',
           'gokhan.manager@company.com',
           (SELECT id FROM departments WHERE name = 'Yazılım Geliştirme'),
           (SELECT id FROM roles WHERE name = 'MANAGER'),
           NULL
       );

-- 2. İK Yetkilisi Ekleme
INSERT INTO employees (name, email, department_id, role_id, manager_id)
VALUES (
           'Ayşe İK',
           'ayse.hr@company.com',
           (SELECT id FROM departments WHERE name = 'İnsan Kaynakları'),
           (SELECT id FROM roles WHERE name = 'HR'),
           NULL
       );

-- 3. Finans Yetkilisi Ekleme
INSERT INTO employees (name, email, department_id, role_id, manager_id)
VALUES (
           'Mehmet Finans',
           'mehmet.finance@company.com',
           (SELECT id FROM departments WHERE name = 'Finans'),
           (SELECT id FROM roles WHERE name = 'FINANCE'),
           NULL
       );

-- 4. Çalışan Ekleme (Manager_id bilgisi gokhan.manager@company.com email'i üzerinden dinamik çekilir)
INSERT INTO employees (name, email, department_id, role_id, manager_id)
VALUES (
           'Boran Çalışan',
           'boran.employee@company.com',
           (SELECT id FROM departments WHERE name = 'Yazılım Geliştirme'),
           (SELECT id FROM roles WHERE name = 'EMPLOYEE'),
           (SELECT id FROM employees WHERE email = 'gokhan.manager@company.com')
       );