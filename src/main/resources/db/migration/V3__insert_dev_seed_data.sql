-- 1. Yönetici
INSERT INTO employees (name, email, department_id, role_id, manager_id)
VALUES (
           'Gökhan Yönetici',
           'gokhan.manager@company.com',
           (SELECT id FROM departments WHERE name = 'Yazılım Geliştirme'),
           (SELECT id FROM roles WHERE name = 'MANAGER'),
           NULL
       );

-- 2. İK Yetkilisi
INSERT INTO employees (name, email, department_id, role_id, manager_id)
VALUES (
           'Ayşe İK',
           'ayse.hr@company.com',
           (SELECT id FROM departments WHERE name = 'İnsan Kaynakları'),
           (SELECT id FROM roles WHERE name = 'HR'),
           NULL
       );

-- 3. Finans Yetkilisi
INSERT INTO employees (name, email, department_id, role_id, manager_id)
VALUES (
           'Mehmet Finans',
           'mehmet.finance@company.com',
           (SELECT id FROM departments WHERE name = 'Finans'),
           (SELECT id FROM roles WHERE name = 'FINANCE'),
           NULL
       );

-- 4. Çalışan (Manager dinamik email sorgusuyla bağlanır)
INSERT INTO employees (name, email, department_id, role_id, manager_id)
VALUES (
           'Boran Çalışan',
           'boran.employee@company.com',
           (SELECT id FROM departments WHERE name = 'Yazılım Geliştirme'),
           (SELECT id FROM roles WHERE name = 'EMPLOYEE'),
           (SELECT id FROM employees WHERE email = 'gokhan.manager@company.com')
       );