# 스터디 API 구현

기준 명세: 작업 폴더의 `STUDY_API_SPEC.md`. 기준 브랜치: 최신 `dev`의 `a7494bf`.
명세 원본은 수정하지 않았다.

## 구현 범위

명세에 나열된 11개 API를 구현했다. 기존 성공 응답 래퍼의 `data`에 명세 필드를 제공한다.
기존 생성 응답의 `managerName`은 유지하고 `managerId`를 추가했다.
관리자 권한은 기존 `/api/admin/**` 보안 설정을 이용하고, 스터디 소속과 스터디장 권한은 서비스에서 검사한다.

- 관리자 후보 검색: 활성 회원의 이름·학번 검색, 실제 memberId 제공.
- 생성: 이름 공백 제거, 빈 이름 거부, 최대 255자, 이름 중복 허용. 활성 회원을 스터디장으로 지정하고 소속을 함께 저장한다.
- 내 목록: ACTIVE/COMPLETED 필터. 미등록 메타데이터·진행률·출석 통계는 null.
- 내 통계: 실제 소속과 스터디별 저장 XP 집계. 기존 회원의 전체 XP나 이벤트 출석을 스터디 값으로 사용하지 않는다.
- 모집 목록: 진행 중이며 스터디장이 존재하고 정원이 남은 미소속 스터디.
- 참여 요청: PENDING 저장. 소속 회원·중복 대기·정원 마감·완료 스터디는 409. 이전 요청이 REJECTED/APPROVED이고 현재 미소속인 경우 기존 요청을 PENDING으로 갱신한다.
- 주차 목록: 1~16주차를 항상 오름차순 제공. 계획이 없으면 planId/title은 null.
- 주차 상세: 소속 검사 후 계획 조회. 계획이 없으면 404.
- 주차 생성: 스터디장만 multipart JSON request와 선택 파일 1개로 생성. 파일은 OCI Object Storage에, 계획과 자료 메타데이터는 DB에 저장. DB 롤백 시 업로드 객체 삭제를 시도한다.
- 자료 다운로드 URL: 해당 스터디·주차의 자료와 현재 소속을 확인한 뒤 5분 유효 URL 발급.
- 출석 생성: 스터디장·계획 존재 검사. 동일 주차의 유효 세션은 409. expirationTime은 1 이상의 Integer 분 단위.

계획·참여 요청·출석 생성은 스터디 행 잠금을 사용한다. 주차와 참여 요청에는 DB 유일 제약도 둔다.
기존 관리자 회원 추가/삭제 및 활동 API는 유지한다.

## 자료 저장과 다운로드 기본 정책

자료 본문은 Oracle Cloud Object Storage의 비공개 버킷에 저장한다. DB에는 objectKey, 파일명, Content-Type, 크기만 저장한다. 파일당 최대 1MiB이며 빈 파일은 400이다.
객체 키는 `studies/{studyId}/weeks/{weekNumber}/{UUID}`로 생성한다.
업로드 실패 시 DB 저장을 완료하지 않는다. DB 롤백 시 해당 객체 삭제를 시도하며, 삭제 실패는 객체 키와 함께 오류 로그를 남긴다.
DB와 OCI는 단일 트랜잭션이 아니므로 프로세스 중단이나 삭제 실패로 남은 객체는 운영 확인이 필요하다.
허용 Content-Type은 PDF, TXT, CSV, PNG, JPEG, ZIP, DOCX, PPTX, XLSX이다.
형식 검사는 업로드 Content-Type을 기준으로 하며 파일 내용 분석은 수행하지 않는다.
자료명에서 경로를 제거하고 제어문자·빈 이름·255자 초과 이름을 거부한다.
multipart 파싱 단계의 크기 초과도 스터디 경로에서는 message가 있는 413 응답으로 반환한다.

반환된 URL은 다음 실제 다운로드 엔드포인트를 가리킨다.

`GET /api/studies/{studyId}/weeks/{weekNumber}/materials/{materialId}/content?expires=...&signature=...`

이 요청에도 Bearer accessToken을 전송해야 한다. 프론트는 인증된 fetch로 Blob을 받아 저장하면 된다.
URL 서명은 스터디·주차·자료·요청 회원·만료 시각에 묶이고, 다운로드 시 현재 소속을 다시 검사한다.
URL 발급 전에 OCI HEAD로 객체 존재를 확인하며, 실제 다운로드는 서버가 OCI GET으로 읽어 반환한다.
OCI 객체가 삭제되었으면 404, 조회 장애는 503을 반환한다. 버킷 공개나 PAR 발급은 사용하지 않는다.
기존 jwt.secret을 HMAC 키로 사용하되 다운로드 전용 메시지 접두사로 구분한다.
파일은 attachment, application/octet-stream, nosniff, no-store 헤더로 반환한다.
프록시 배포에서는 기존 서버의 외부 URL/forwarded-header 설정이 올바르게 적용되어야 한다.

## 미확정 계약과 데이터

명세의 제안 API까지 구현한 상태이며, 참여 승인 API·QR 인증 API·XP 지급 기능은 포함하지 않는다.
스터디 출석 세션 생성은 기존 이벤트 출석과 연결되지 않는다.
파일 저장 방식은 Oracle Cloud Object Storage이며, 허용 형식·크기·URL 유효시간은 위 기본 정책을 적용했다.
출석 유효시간 상한은 아직 추가하지 않았다.

소개·정원·난이도·태그·일정·완료 상태는 영속 필드를 제공하지만 이를 편집하는 API는 이번 명세에 없어 추가하지 않았다.
earnedXp는 스터디 소속별 저장값을 조회한다. 현재 API에서는 XP를 지급하지 않으므로 신규 소속은 0으로 시작한다.
전체 세션 수·출석 수·진행률은 산정 계약이 없어 null이다. 주차 계획 등록 수를 출석 진행률로 환산하지 않는다.
스터디장이 탈퇴한 기존 스터디에서도 남은 소속 회원은 내 목록·주차 조회를 사용할 수 있다.

## DB 반영

`docs/sql/study-api.sql`은 기존 MySQL dev 스키마에 적용할 수 있는 수동 마이그레이션이다.
자동 실행이나 실제 DB 변경은 수행하지 않았다. 스키마가 이미 Hibernate update로 변경되었다면 중복 적용하지 않는다.
기존 스터디는 ACTIVE, 새 XP 집계 필드는 0으로 초기화하고 누락된 활성 스터디장 소속을 등록한다.
추가 테이블은 study_tag, study_week, study_material, study_join_request, study_attendance_session이다.
DB 마이그레이션은 코드 배포 전에 반영해야 한다.
이 SQL은 아직 적용하지 않은 초기 마이그레이션 기준으로 `content MEDIUMBLOB` 대신 `object_key VARCHAR(512)`를 생성한다.
이전 BLOB 스키마를 별도로 적용한 환경은 파일을 OCI로 옮기고 object_key를 채우는 별도 데이터 이관이 필요하다. 기존 BLOB 데이터를 자동 삭제하지 않는다.

## OCI 연결 설정

Oracle Object Storage Standard 비공개 버킷을 준비하고 아래 환경변수를 지정한다. 버킷 생성이나 실제 업로드는 이 작업에서 실행하지 않았다.

| 환경변수 | 값 |
| --- | --- |
| STUDY_STORAGE_OCI_REGION | 버킷 리전 식별자 |
| STUDY_STORAGE_OCI_NAMESPACE | Object Storage namespace |
| STUDY_STORAGE_OCI_BUCKET | 비공개 버킷 이름 |
| STUDY_STORAGE_OCI_AUTH | INSTANCE_PRINCIPAL(기본값) 또는 CONFIG_FILE |
| STUDY_STORAGE_OCI_CONFIGFILE | CONFIG_FILE 모드의 설정 파일 경로, 기본 ~/.oci/config |
| STUDY_STORAGE_OCI_PROFILE | 설정 프로필, 기본 DEFAULT |

OCI Compute에서는 Instance Principal을 사용한다. 해당 인스턴스의 Dynamic Group에 대상 버킷 객체의 생성·조회·삭제 권한을 부여해야 한다.
로컬에서는 CONFIG_FILE 모드를 선택하고 OCI SDK 표준 설정 파일의 API 키 인증을 사용한다. 비밀키를 저장소에 넣지 않는다.
Spring 설정 키는 `study.storage.oci.region`, `namespace`, `bucket`, `auth`, `config-file`, `profile`이다.
OCI 클라이언트는 자료 작업 시 초기화하므로 OCI 설정이 없어도 파일 없는 계획·목록 API는 사용할 수 있다. 미설정 시 자료 저장/조회는 실패하며 DB 저장 방식으로 대체하지 않는다.
SDK 연결/읽기 타임아웃은 5초/30초다. 객체 스토리지 사용량 제한은 버킷 운영 설정에서 관리한다.

인증과 SDK 구성은 [Oracle Java SDK 문서](https://docs.oracle.com/en-us/iaas/Content/API/SDKDocs/javasdkgettingstarted.htm) 및 [인증 방식 문서](https://docs.oracle.com/en-us/iaas/Content/API/Concepts/sdk_authentication_methods.htm)를 따른다.

## 검증

스터디 서비스 테스트, MockMvc HTTP 계약 테스트, 다운로드 서명/권한 테스트,
실제 DB 접속 없이 Hibernate 매핑과 Spring Data 저장소 쿼리 생성 검증을 제공한다.
기존 서비스·컨트롤러·JWT 테스트도 함께 실행한다.

```bash
./gradlew test --tests '*service.*' --tests '*controller.*' --tests '*auth.jwt.*' --tests '*study.repository.*' --tests '*study.storage.*'
```

OCI SDK 요청 생성·오류 변환·스트림 종료 및 DB 트랜잭션 완료 콜백에 따른 객체 정리를 mock 기반으로 검증한다.
실제 OCI 버킷 통신, MySQL 마이그레이션·병렬 요청 잠금 및 실제 multipart 컨테이너 통합 검증은 별도다.
