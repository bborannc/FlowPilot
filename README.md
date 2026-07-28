# 🚀 FlowPilot - Dynamic Approval Engine Backend

FlowPilot, kurum içi dinamik onay süreçlerini (Satın Alma, İzin, Harcama vb.) esnek, ölçeklenebilir ve izlenebilir bir şekilde yönetmek üzere geliştirilmiş Spring Boot tabanlı bir onay motoru backend sistemidir.

---

## 🛠️ Teknoloji Yığını (Tech Stack)

* **Java Version:** 25
* **Framework:** Spring Boot 4.1.0
* **Database:** PostgreSQL 18.4
* **Migration Tool:** Flyway
* **Testing:** Testcontainers (PostgreSQL 16), JUnit 5, AssertJ
* **ORM:** Spring Data JPA / Hibernate 7
* **Build Tool:** Maven
* **Lombok:** Boilerplate kod azaltımı için kullanıldı.

---

## ⚙️ Uygulama Konfigürasyonu ve Ortam Değişkenleri (Environment Variables)

Güvenlik standartları gereği (12-Factor App) veritabanı bağlantı bilgileri kod içerisine hardcode yazılmamış, ortam değişkenleri (Environment Variables) üzerinden okunacak şekilde yapılandırılmıştır.

| Değişken Adı | Açıklama | Lokal Varsayılan Değer |
| :--- | :--- | :--- |
| `DB_URL` | PostgreSQL JDBC Bağlantı Adresi
| `DB_USERNAME` | Veritabanı Kullanıcı Adı 
| `DB_PASSWORD` | Veritabanı Şifresi 
| `SPRING_PROFILES_ACTIVE` | Aktif Spring Profili 

---

## 🏗️ Veritabanı Mimarısı ve Flyway Migration Stratejisi

Veritabanı şema yönetimi ve versiyon kontrolü **Flyway** ile otomatize edilmiştir. Hibernate'in otomatik DDL üretimi kapatılmış, şema doğrulaması için `spring.jpa.hibernate.ddl-auto=validate` kuralı benimsenmiştir.

### Migration Dosya Yapısı

* **`V1__init_schema.sql`**: Veritabanı tablolarının (`departments`, `roles`, `employees`, `requests`, `request_details`, `approval_steps`, `approval_histories`), `FOREIGN KEY` ilişkilerinin, performans indekslerinin ve kısıtlamaların (Constraints) tanımlandığı ana DDL script'idir.
* **`V2__insert_lookup_data.sql`**: Üretim (Production) ortamı için zorunlu olan temel statik tanım verilerini (`departments` ve `roles`) içerir.
* **`V3__insert_dev_seed_data.sql`**: Yalnızca `local` ve `test` ortamlarında kullanılacak örnek test kullanıcılarını (Yönetici, İK, Finans, Çalışan) içerir. Hardcoded ID kullanımı yerine ilişkiler `email` ve `name` alanları üzerinden **alt sorgular (subqueries)** ile dinamik olarak bağlanmıştır.

### Performans ve Veri Bütünlüğü Kısıtlamaları (Indexes & Constraints)

* **Indeksler:** Sorgu performansını optimize etmek amacıyla tüm `FOREIGN KEY` alanlarına (`department_id`, `role_id`, `manager_id`, `employee_id`, `request_id`, `assigned_employee_id`) ve sık sorgulanan durumlara (`status`) B-Tree indeks eklenmiştir.
* **Unique Constraints:**
  * `approval_steps`: Aynı talepte iki onay adımının aynı sıra numarasına sahip olmasını engellemek için `(request_id, step_order)` UNIQUE tanımlanmıştır.
  * `request_details`: Her talepte aynı detay anahtarının yalnızca bir kez bulunabilmesi için `(request_id, detail_key)` UNIQUE tanımlanmıştır.

---

## 🏛️ Mimari Kararlar (Architecture Decision Records - ADR)

### 1. Employee - Role İlişki Modeli
* **Karar:** Projenin mevcut MVP gereksinimleri doğrultusunda her çalışan organizasyonel şemada tek bir birincil role (`EMPLOYEE`, `MANAGER`, `HR`, `FINANCE`) sahip olacak şekilde `@ManyToOne` ilişkisiyle modellenmiştir.
* **Gelecek Esnekliği:** İlerleyen aşamalarda bir kullanıcının birden fazla role sahip olması (Çoklu Yetkilendirme/RBAC) gerekirse, veritabanı mimarisi `employee_roles` ara tablosu ve `@ManyToMany` ilişkisine kolayca refactor edilecek esneklikte kurgulanmıştır.

### 2. Enum Kullanımı ve Tip Güvenliği
* Veritabanı ve Java katmanı arasındaki uyumsuzlukları ve hatalı veri girişlerini önlemek için String alanlar yerine `@Enumerated(EnumType.STRING)` anotasyonuyla Enum sınıfları tercih edilmiştir:
  * `Request.requestType` $\rightarrow$ `RequestType` (`PURCHASE`, `LEAVE`, `EXPENSE`)
  * `Request.status` $\rightarrow$ `RequestStatus` (`PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`)
  * `Request.priority` $\rightarrow$ `RequestPriority` (`LOW`, `MEDIUM`, `HIGH`, `URGENT`)
  * `ApprovalStep.status` $\rightarrow$ `StepStatus` (`PENDING`, `APPROVED`, `REJECTED`, `SKIPPED`)
  * `ApprovalHistory.action` $\rightarrow$ `ApprovalAction` (`CREATED`, `APPROVED`, `REJECTED`, `DELEGATED`)

### 3. Profil Bazlı Konfigürasyon Yönetimi
* Konsol loglarını kirleten `show-sql` ve `format_sql` gibi geliştirme odaklı JPA konfigürasyonları ortak `application.properties` dosyasından çıkarılarak `application-local.properties` profiline taşınmıştır.

---

## 🧪 Entegrasyon Testleri (Testcontainers)

Projede veritabanı migration'larının doğruluğunu test etmek amacıyla `FlywayMigrationIntegrationTest` yazılmıştır. Bu test:
* Arka planda geçici bir **PostgreSQL 16 Docker konteyneri** başlatır.
* `V1`, `V2` ve `V3` migration script'lerini sıfır bir veritabanı üzerinde sırasıyla koşturur.
* Tüm migration'ların başarıyla uygulandığını ve veritabanının `v3` sürümüne pürüzsüz ulaştığını doğrular.

---

## 🚀 Yerel Ortamda Çalıştırma (Quick Start)

1. **PostgreSQL Veritabanını Hazırlayın:**
   ```sql
   CREATE DATABASE dynamic_approval_db;

