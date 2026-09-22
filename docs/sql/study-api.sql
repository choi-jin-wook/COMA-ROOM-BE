-- MySQL 8: 기존 dev 스키마에 한 번 적용하는 스터디 API 마이그레이션.
-- 자동 실행되지 않는다. Hibernate ddl-auto=update로 이미 반영한 DB에는 중복 적용하지 않는다.
ALTER TABLE study
    ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN description VARCHAR(2000) NULL,
    ADD COLUMN max_members INT NULL,
    ADD COLUMN schedule_description VARCHAR(255) NULL,
    ADD COLUMN next_session_at DATETIME(6) NULL,
    ADD COLUMN completed_at DATETIME(6) NULL,
    ADD COLUMN level ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED') NULL;

ALTER TABLE study_member
    ADD COLUMN earned_xp BIGINT NOT NULL DEFAULT 0;

-- 과거 생성 코드에서 빠졌던 스터디장 소속을 실제 활성 회원에 한해 등록한다.
INSERT INTO study_member (study_id, member_id, earned_xp, created_at, updated_at)
SELECT s.id, m.member_id, 0, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM study s JOIN member m ON m.member_id = s.manager_id AND m.status = 'ACTIVE'
WHERE NOT EXISTS (
    SELECT 1 FROM study_member sm WHERE sm.study_id = s.id AND sm.member_id = m.member_id
);

CREATE TABLE study_tag (
    study_id BIGINT NOT NULL,
    tag_order INT NOT NULL,
    tag VARCHAR(255),
    PRIMARY KEY (study_id, tag_order),
    CONSTRAINT fk_study_tag_study FOREIGN KEY (study_id) REFERENCES study(id)
);

CREATE TABLE study_week (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    study_id BIGINT NOT NULL,
    week_number INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    topic VARCHAR(255),
    description VARCHAR(2000),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    CONSTRAINT uk_study_week UNIQUE (study_id, week_number),
    CONSTRAINT fk_study_week_study FOREIGN KEY (study_id) REFERENCES study(id),
    CONSTRAINT ck_study_week_number CHECK (week_number BETWEEN 1 AND 16)
);

CREATE TABLE study_material (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    size_bytes BIGINT NOT NULL,
    object_key VARCHAR(512) NOT NULL UNIQUE,
    CONSTRAINT fk_study_material_plan FOREIGN KEY (plan_id) REFERENCES study_week(id)
);

CREATE TABLE study_join_request (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    study_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    CONSTRAINT uk_study_join_member UNIQUE (study_id, member_id),
    CONSTRAINT fk_study_join_study FOREIGN KEY (study_id) REFERENCES study(id),
    CONSTRAINT fk_study_join_member FOREIGN KEY (member_id) REFERENCES member(member_id)
);

CREATE TABLE study_attendance_session (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    qr_code_id VARCHAR(36) NOT NULL UNIQUE,
    expires_at DATETIME(6) NOT NULL,
    CONSTRAINT fk_study_attendance_plan FOREIGN KEY (plan_id) REFERENCES study_week(id),
    INDEX idx_study_attendance_expiry (plan_id, expires_at)
);
