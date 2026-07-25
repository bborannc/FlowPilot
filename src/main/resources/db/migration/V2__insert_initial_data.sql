-- Departmanlar
INSERT INTO departments (name) VALUES ('Yazılım Geliştirme'), ('İnsan Kaynakları'), ('Finans');

-- Roller
INSERT INTO roles (name) VALUES ('EMPLOYEE'), ('MANAGER'), ('HR'), ('FINANCE');

-- Yöneticiler ve Özel Rolleri Ekleme
INSERT INTO employees (name, email, department_id, role_id, manager_id)
VALUES ('Gökhan Yönetici', 'gokhan.manager@company.com', 1, 2, NULL); -- MANAGER (id: 1)

INSERT INTO employees (name, email, department_id, role_id, manager_id)
VALUES ('Ayşe İK', 'ayse.hr@company.com', 2, 3, NULL); -- HR (id: 2)

INSERT INTO employees (name, email, department_id, role_id, manager_id)
VALUES ('Mehmet Finans', 'mehmet.finance@company.com', 3, 4, NULL); -- FINANCE (id: 3)

-- Standart Çalışan -> Yöneticisi olarak Gökhan'ı bağlıyoruz
INSERT INTO employees (name, email, department_id, role_id, manager_id)
VALUES ('Boran Çalışan', 'boran.employee@company.com', 1, 1, 1); -- EMPLOYEE (id: 4)