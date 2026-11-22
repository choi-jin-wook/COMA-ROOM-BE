-- 기존 study-api.sql이 적용된 DB에서 스터디 필드를 단순화한다.
-- 여러 태그가 있으면 입력 순서상 첫 번째 태그만 주제로 이전한다.
ALTER TABLE study ADD COLUMN topic VARCHAR(255) NULL;

UPDATE study s
SET topic = (
    SELECT st.tag
    FROM study_tag st
    WHERE st.study_id = s.id
    ORDER BY st.tag_order
    LIMIT 1
);

DROP TABLE study_tag;

ALTER TABLE study
    DROP COLUMN max_members,
    DROP COLUMN next_session_at,
    DROP COLUMN completed_at,
    DROP COLUMN level;
