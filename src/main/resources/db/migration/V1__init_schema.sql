-- 1. Departments Tablosu
CREATE TABLE departments (
                             id BIGSERIAL PRIMARY KEY,
                             name VARCHAR(255) NOT NULL UNIQUE
);

-- 2. Roles Tablosu
CREATE TABLE roles (
                       id BIGSERIAL PRIMARY KEY,
                       name VARCHAR(255) NOT NULL UNIQUE
);

-- 3. Employees Tablosu (User yerine Employee ve manager hiyerarşisi)
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

-- 4. Requests Tablosu
CREATE TABLE requests (
                          id BIGSERIAL PRIMARY KEY,
                          employee_id BIGINT NOT NULL,
                          request_type VARCHAR(50) NOT NULL,
                          status VARCHAR(50) NOT NULL,
                          priority VARCHAR(50) NOT NULL,
                          created_at TIMESTAMP NOT NULL,
                          CONSTRAINT fk_requests_employee FOREIGN KEY (employee_id) REFERENCES employees(id)
);

-- 5. Request Details Tablosu
CREATE TABLE request_details (
                                 id BIGSERIAL PRIMARY KEY,
                                 request_id BIGINT NOT NULL,
                                 detail_key VARCHAR(255) NOT NULL,
                                 detail_value TEXT NOT NULL,
                                 CONSTRAINT fk_details_request FOREIGN KEY (request_id) REFERENCES requests(id) ON DELETE CASCADE
);

-- 6. Approval Steps Tablosu (assigned_employee_id eklendi)
CREATE TABLE approval_steps (
                                id BIGSERIAL PRIMARY KEY,
                                request_id BIGINT NOT NULL,
                                assigned_role_id BIGINT NOT NULL,
                                assigned_employee_id BIGINT,
                                step_order INT NOT NULL,
                                status VARCHAR(50) NOT NULL,
                                CONSTRAINT fk_steps_request FOREIGN KEY (request_id) REFERENCES requests(id) ON DELETE CASCADE,
                                CONSTRAINT fk_steps_role FOREIGN KEY (assigned_role_id) REFERENCES roles(id),
                                CONSTRAINT fk_steps_employee FOREIGN KEY (assigned_employee_id) REFERENCES employees(id)
);

-- 7. Approval Histories Tablosu
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