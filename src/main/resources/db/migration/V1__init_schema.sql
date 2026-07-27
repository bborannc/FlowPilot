-- ==========================================
-- 1. TABLO OLUŞTURMA İŞLEMLERİ
-- ==========================================

-- Departmanlar Tablosu
CREATE TABLE departments (
                             id BIGSERIAL PRIMARY KEY,
                             name VARCHAR(255) NOT NULL UNIQUE
);

-- Roller Tablosu
CREATE TABLE roles (
                       id BIGSERIAL PRIMARY KEY,
                       name VARCHAR(255) NOT NULL UNIQUE
);

-- Çalışanlar Tablosu
CREATE TABLE employees (
                           id BIGSERIAL PRIMARY KEY,
                           name VARCHAR(255) NOT NULL,
                           email VARCHAR(255) NOT NULL UNIQUE,
                           department_id BIGINT NOT NULL,
                           role_id BIGINT NOT NULL,
                           manager_id BIGINT,
                           CONSTRAINT fk_employees_department FOREIGN KEY (department_id) REFERENCES departments(id),
                           CONSTRAINT fk_employees_role FOREIGN KEY (role_id) REFERENCES roles(id),
                           CONSTRAINT fk_employees_manager FOREIGN KEY (manager_id) REFERENCES employees(id)
);

-- Talepler Tablosu
CREATE TABLE requests (
                          id BIGSERIAL PRIMARY KEY,
                          employee_id BIGINT NOT NULL,
                          request_type VARCHAR(50) NOT NULL,
                          status VARCHAR(50) NOT NULL,
                          priority VARCHAR(50) NOT NULL,
                          created_at TIMESTAMP NOT NULL,
                          CONSTRAINT fk_requests_employee FOREIGN KEY (employee_id) REFERENCES employees(id)
);

-- Talep Detayları Tablosu
CREATE TABLE request_details (
                                 id BIGSERIAL PRIMARY KEY,
                                 request_id BIGINT NOT NULL,
                                 detail_key VARCHAR(255) NOT NULL,
                                 detail_value TEXT NOT NULL,
                                 CONSTRAINT fk_details_request FOREIGN KEY (request_id) REFERENCES requests(id) ON DELETE CASCADE,
    -- Kural: Her talepte aynı detail_key yalnızca bir kez bulunabilir.
                                 CONSTRAINT uq_request_detail_key UNIQUE (request_id, detail_key)
);

-- Onay Adımları Tablosu
CREATE TABLE approval_steps (
                                id BIGSERIAL PRIMARY KEY,
                                request_id BIGINT NOT NULL,
                                assigned_role_id BIGINT NOT NULL,
                                assigned_employee_id BIGINT,
                                step_order INT NOT NULL,
                                status VARCHAR(50) NOT NULL,
                                CONSTRAINT fk_steps_request FOREIGN KEY (request_id) REFERENCES requests(id) ON DELETE CASCADE,
                                CONSTRAINT fk_steps_role FOREIGN KEY (assigned_role_id) REFERENCES roles(id),
                                CONSTRAINT fk_steps_employee FOREIGN KEY (assigned_employee_id) REFERENCES employees(id),
    -- Kural: Aynı talepte iki onay adımı aynı sıra numarasına (step_order) sahip olamaz.
                                CONSTRAINT uq_approval_step_order UNIQUE (request_id, step_order)
);

-- Onay Geçmişi (Audit Trail) Tablosu
CREATE TABLE approval_histories (
                                    id BIGSERIAL PRIMARY KEY,
                                    request_id BIGINT NOT NULL,
                                    employee_id BIGINT NOT NULL,
                                    action VARCHAR(50) NOT NULL,
                                    description TEXT,
                                    action_date TIMESTAMP NOT NULL,
                                    CONSTRAINT fk_histories_request FOREIGN KEY (request_id) REFERENCES requests(id) ON DELETE CASCADE,
                                    CONSTRAINT fk_histories_employee FOREIGN KEY (employee_id) REFERENCES employees(id)
);

-- ==========================================
-- 2. İNDEKS TANIMLAMALARI (PERFORMANS İÇİN)
-- ==========================================

-- Employees indeksleri
CREATE INDEX idx_employees_department_id ON employees(department_id);
CREATE INDEX idx_employees_role_id ON employees(role_id);
CREATE INDEX idx_employees_manager_id ON employees(manager_id);

-- Requests indeksleri
CREATE INDEX idx_requests_employee_id ON requests(employee_id);
CREATE INDEX idx_requests_status ON requests(status);

-- Approval Steps indeksleri
CREATE INDEX idx_approval_steps_request_id ON approval_steps(request_id);
CREATE INDEX idx_approval_steps_assigned_employee_id ON approval_steps(assigned_employee_id);
CREATE INDEX idx_approval_steps_status ON approval_steps(status);

-- Approval Histories indeksleri
CREATE INDEX idx_approval_histories_request_id ON approval_histories(request_id);