# 스터디 API 명세 초안

작성일: 2026-09-07

## 1. 명세의 근거와 상태

- 대상: 스터디 목록, 관리자 스터디 생성, 스터디장/스터디원 LMS, 주차 계획, 강의자료, 주차 출석.
- Figma `831:2`에 `POST /api/admin/study`, `CreateStudyRequest: studyName, managerId`가 기록돼 있다. 백엔드 코드나 실제 응답으로 검증한 계약은 아니다.
- 현재 프론트에는 스터디 API 호출이 없다. 아래에서 위 생성 경로와 두 필드를 제외한 경로·응답·제약은 **백엔드 협의를 위한 제안**이다.
- 더미데이터는 사용하지 않는다. 이 문서는 예시 회원/스터디 데이터 대신 필드와 타입을 정의한다.
- 기존 이벤트 출석 생성 API는 스터디 주차 출석 API로 간주하지 않는다.

## 2. 공통 규칙

| 항목 | 내용 |
| --- | --- |
| 인증 | `Authorization: Bearer <accessToken>` — 기존 프론트 방식 |
| JSON 요청 | `Content-Type: application/json` |
| 성공 응답 | `{ "data": T }` — 기존 `apiFetch`가 `data`를 추출 |
| 오류 응답 | `{ "message": string }` 필수. `code: string` 추가 제안 |
| 날짜/시간 | 시간대가 포함된 ISO 8601 문자열 제안 |
| 식별자 | `studyId`, `memberId`, `planId`, `materialId`, `attendanceSessionId` |
| 빈 목록 | `data.items: []` 반환. 조회 실패를 빈 목록으로 대체하지 않음 |
| 페이지 | `page`는 0부터, `size` 기본 20·최대 100 제안 |

목록 응답 공통 구조: `items: T[]`, `page: integer`, `size: integer`, `totalElements: integer`, `totalPages: integer`.

스터디 권한은 서버가 로그인 사용자와 실제 소속을 기준으로 검증한다. 프론트의 버튼 숨김이나 URL의 ID로 권한을 결정하지 않는다.

## 3. API 목록

| 기능 | 메서드 | 경로 | 권한 | 상태 |
| --- | --- | --- | --- | --- |
| 스터디장 후보 검색 | GET | `/api/admin/study/managers` | 관리자 | 제안 |
| 스터디 생성 | POST | `/api/admin/study` | 관리자 | 경로·요청 필드는 Figma 기록 |
| 내 스터디 통계 | GET | `/api/member/studies/summary` | 로그인 회원 | 제안 |
| 내 스터디 목록 | GET | `/api/member/studies` | 로그인 회원 | 제안 |
| 참여 가능한 스터디 목록 | GET | `/api/studies/recruiting` | 로그인 회원 | 제안 |
| 참여 요청 | POST | `/api/studies/{studyId}/join-requests` | 미참여 회원 | 제안 |
| 주차 목록·스터디 정보 | GET | `/api/studies/{studyId}/weeks` | 해당 스터디원·스터디장 | 제안 |
| 주차 상세 | GET | `/api/studies/{studyId}/weeks/{weekNumber}` | 해당 스터디원·스터디장 | 제안 |
| 주차 계획 생성 | POST | `/api/studies/{studyId}/weeks` | 해당 스터디장 | 제안 |
| 강의자료 다운로드 URL | GET | `/api/studies/{studyId}/weeks/{weekNumber}/materials/{materialId}/download` | 해당 스터디원·스터디장 | 제안 |
| 주차 출석 생성 | POST | `/api/studies/{studyId}/weeks/{weekNumber}/attendances` | 해당 스터디장 | 미확정 제안 |

## 4. 관리자 스터디 생성

### GET /api/admin/study/managers

쿼리: `keyword?: string`, `page?: integer`, `size?: integer`.

응답: 페이지 구조. `items`의 각 항목은 다음 필드를 가진다.

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| memberId | integer | 실제 회원 식별자. 선택 후 `managerId`로 전달 |
| name | string | 부원 이름 |
| studentId | string | 동명이인 구분용 학번 |
| major | string | 학과 |

기존 회원 관리 화면에서 배열 인덱스로 만든 `id`는 실제 회원 식별자가 아니므로 `managerId`로 전달하면 안 된다. 기존 회원 조회 API가 실제 `memberId`를 제공한다면 별도 후보 API 대신 재사용할 수 있다.

### POST /api/admin/study

요청: JSON.

| 필드 | 타입 | 필수 | 검증 |
| --- | --- | --- | --- |
| studyName | string | O | 앞뒤 공백 제거 후 비어 있지 않아야 함 |
| managerId | integer | O | 실제 존재하며 스터디장 지정이 가능한 회원 |

성공: `201 Created`, `data: { studyId: integer, studyName: string, managerId: integer }` 제안.

스터디 생성과 스터디장 소속 등록은 함께 성공하거나 함께 실패해야 한다. 이름 길이·중복 허용 정책은 협의가 필요하다.

## 5. 스터디 목록과 참여

### GET /api/member/studies/summary

`data`: `participatingCount: integer`, `totalEarnedXp: integer`, `completedCount: integer`.

현재 로그인한 회원의 스터디 참여·획득 XP·완료 집계이며, 프론트에서 임의 수치를 설정하지 않는다.

### GET /api/member/studies

쿼리: `status: ACTIVE | COMPLETED`, `page?: integer`, `size?: integer`.

응답: 페이지 구조. 각 스터디의 공통 필드:

| 필드 | 타입 | UI 용도 |
| --- | --- | --- |
| studyId | integer | 상세 화면 이동 |
| studyName | string | 제목 |
| description | string 또는 null | 소개 |
| myRole | LEADER 또는 MEMBER | 스터디장·스터디원 구분 |
| status | ACTIVE 또는 COMPLETED | 내 스터디·완료 탭 |
| progressPercent | number 또는 null | 진행률. 산정 기준 협의 필요 |
| currentMembers | integer | 현재 인원 |
| maxMembers | integer 또는 null | 정원. 제한 없으면 null |
| nextSessionAt | string 또는 null | 다음 일정 |
| scheduleDescription | string 또는 null | 정기 진행 일정 설명 |
| completedAt | string 또는 null | 완료 날짜 |
| totalSessions | integer 또는 null | 완료 스터디 전체 세션 수 |
| attendedSessions | integer 또는 null | 해당 회원 참석 수 |
| earnedXp | integer 또는 null | 해당 회원 획득 XP |

제공되지 않는 선택 필드는 숨기거나 `미등록`으로 표시한다. 누락값을 실제 집계값 0으로 간주하지 않는다.

### GET /api/studies/recruiting

쿼리: `page?: integer`, `size?: integer`.

응답: 페이지 구조. 항목 필드: `studyId`, `studyName`, `description`, `currentMembers`, `maxMembers`, `scheduleDescription`은 위와 동일.

추가 필드: `level: BEGINNER | INTERMEDIATE | ADVANCED | null`, `tags: string[]`, `manager: { memberId: integer, name: string }`, `myJoinRequestStatus: NONE | PENDING | APPROVED | REJECTED`.

소개·난이도·태그·정원·일정은 현재 스터디 생성 폼에 입력란이 없다. 서버에서 관리하는 값인지, 추후 입력 화면이 필요한지 확정해야 한다.

### POST /api/studies/{studyId}/join-requests

본문 없음. 신청자는 인증 토큰에서 판별한다.

성공: `201 Created`, `data: { joinRequestId: integer, studyId: integer, status: PENDING }`.

이미 소속된 회원·중복 대기 신청·정원 마감은 `409 Conflict` 제안. 현재 UI는 ‘참여 요청’이므로 즉시 가입 완료로 표시하지 않는다. 승인 처리 주체와 승인 API는 별도 확정이 필요하다.

## 6. LMS 주차 조회

### GET /api/studies/{studyId}/weeks

`data`:

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| studyId | integer | 스터디 식별자 |
| studyName | string | 스터디명 |
| myRole | LEADER 또는 MEMBER | 서버가 판별한 현재 회원의 역할 |
| weeks | WeekSummary[] | 1~16주차를 오름차순으로 반환 |

`WeekSummary`: `weekNumber: integer`, `planId: integer | null`, `title: string | null`.

계획이 없는 주차는 `planId`와 `title`을 null로 반환한다. 프론트는 ‘계획 미등록’을 표시하고, 스터디장에게만 ‘계획 추가’를 보여준다.

직접 URL 진입·새로고침 시에도 이 API로 스터디 정보와 권한을 다시 조회한다. 라우터의 이동 상태에만 의존하지 않는다.

### GET /api/studies/{studyId}/weeks/{weekNumber}

`weekNumber`: 1~16 정수.

`data`: `studyId: integer`, `studyName: string`, `myRole: LEADER | MEMBER`, `planId: integer`, `weekNumber: integer`, `title: string`, `topic: string | null`, `description: string | null`, `materials: Material[]`.

`Material`: `materialId: integer`, `fileName: string`, `contentType: string`, `sizeBytes: integer`.

계획이 없으면 `404`. 자료가 없으면 `materials: []`. 주차 상세의 제목·설명·자료명은 모두 서버 응답으로 렌더링한다.

## 7. 주차 계획과 자료

### POST /api/studies/{studyId}/weeks

파일 첨부를 포함하므로 `multipart/form-data` 방식 제안.

| 파트 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| request | application/json | O | `{ weekNumber: integer, title: string }` |
| file | binary | X | 현재 파일 선택 UI에서 선택한 자료 1개 |

검증: 주차는 1~16, 제목은 공백만 입력할 수 없음, 동일 스터디·주차의 계획 중복 생성은 `409`.

성공: `201 Created`, `data: { studyId: integer, planId: integer, weekNumber: integer, title: string, materials: Material[] }`.

현재 생성 폼에는 `topic`, `description` 입력란이 없다. 상세 응답에서는 null 허용을 제안하며, 해당 필드 입력이 필요하면 UI와 요청 계약을 함께 확정한다.

허용 파일 형식·크기 제한은 백엔드와 확정해야 한다. 업로드 실패 시 성공 응답을 반환하지 않는다.

현재 `apiFetch`는 기본적으로 JSON Content-Type을 넣는다. 실제 연동 시 multipart 요청은 브라우저가 boundary를 생성하도록 Content-Type을 지정하지 않는 처리가 필요하다. 토큰 인증·갱신 동작은 유지해야 한다.

### GET /api/studies/{studyId}/weeks/{weekNumber}/materials/{materialId}/download

성공: `200 OK`, `data: { fileName: string, downloadUrl: string, expiresAt: string }`.

서버는 자료가 해당 스터디·주차에 속하는지와 요청자의 소속을 확인한 후 유효기간이 있는 다운로드 URL을 반환한다. 접근 불가·삭제된 자료는 다운로드 링크를 반환하지 않는다.

## 8. 주차 출석 생성 — 별도 협의 필요

Figma와 현재 UI는 출석 생성 준비 상태만 제공하며 요청 DTO는 확정되지 않았다.

제안 경로: `POST /api/studies/{studyId}/weeks/{weekNumber}/attendances`.

제안 요청: `{ expirationTime: integer }` — QR 유효시간(분), 1 이상의 정수. 상한은 협의 필요.

제안 성공 응답: `201 Created`, `data: { attendanceSessionId: integer, qrCodeId: string, expiresAt: string }`.

필수 조건: 해당 스터디장만 생성 가능, 주차 계획 존재 확인, 이미 유효한 세션이 있으면 중복 생성 정책 적용(`409` 제안).

추가 확정 사항:

- 출석 인정 대상과 지각·중복 출석 기준
- 스터디 출석 XP와 지급 시점
- QR 인증 API의 기존 이벤트 QR 지원 범위
- 세션 종료·명단 조회가 필요한지 여부

확정 전에는 기존 `/api/admin/event/attendances`에 스터디 ID나 주차를 임의로 전달하지 않는다.

## 9. 오류 처리 제안

| HTTP | 상황 |
| --- | --- |
| 400 | 필수값 누락, 빈 제목, 주차 범위·유효시간 오류 |
| 401 | 로그인 필요 또는 토큰 만료 |
| 403 | 관리자·스터디장 권한 부족 또는 해당 스터디 미소속 |
| 404 | 스터디·주차 계획·자료를 찾을 수 없음 |
| 409 | 중복 참여 요청, 중복 주차 계획, 유효 출석 세션 충돌 |
| 413 | 업로드 크기 초과 |
| 415 | 허용하지 않는 파일 형식 |
| 500 | 서버 처리 실패 |

입력/저장 실패 시 입력값을 유지한다. 조회 실패 시 오류와 재시도를 제공하고 더미데이터로 대체하지 않는다.

## 10. 연동 순서

1. 스터디 생성 경로·DTO 및 실제 `memberId`를 제공하는 후보 조회 확정
2. 내 스터디·참여 가능 목록과 통계 조회
3. 주차 목록·상세 조회 및 서버 기준 역할 판별
4. 주차 계획 생성·자료 업로드·다운로드
5. 참여 요청 정책과 승인 흐름 확정
6. 스터디 출석 DTO와 QR 인증·XP 지급 계약 확정 후 연결
