-- 기존 MySQL DB에 별도 적용한다. 신규 DB는 study-api.sql에 이 컬럼이 없다.
-- 적용 전 study_material.content_type을 읽는 구버전 애플리케이션을 종료한다.
ALTER TABLE study_material DROP COLUMN content_type;
