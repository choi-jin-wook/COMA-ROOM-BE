# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

COMA ROOM 백엔드 — 스터디룸/동아리 관리 플랫폼. Spring Boot 4.0.1 + Java 25 기반 REST API 서버.

**인프라**: MySQL (포트 3306, DB명 `comaroom_db`), Redis (포트 6379), 서버 포트 8080

## Environment

**JDK**: Amazon Corretto 25 (`C:\Users\tempadmin\.jdks\corretto-25.0.2`)

Gradle 실행 전 JAVA_HOME이 설정되지 않은 경우 아래처럼 명시한다:

```bash
JAVA_HOME="C:/Users/tempadmin/.jdks/corretto-25.0.2" ./gradlew <task>
```

## Commands

```bash
# 빌드
./gradlew build

# 실행
./gradlew bootRun

# 전체 테스트
./gradlew test

# 단일 테스트 클래스 실행
./gradlew test --tests "com.coma.comaroom.member.service.AuthServiceTest"

# 단일 테스트 메서드 실행
./gradlew test --tests "com.coma.comaroom.member.service.AuthServiceTest.methodName"
```

**환경 변수** (없으면 기본값 사용):
- `DB_URL`: MySQL 접속 URL
- `JWT_SECRET`: JWT 서명 키 (최소 32바이트)
- `CORS_ALLOWED_ORIGINS`: CORS 허용 출처

## Architecture

### 도메인 구조

각 도메인은 `controller / dto(request+response) / entity / mapper / repository / service` 레이어로 구성된다.

- `member` — 회원 가입·로그인·프로필·XP 이력·리더보드
- `auth` — Spring Security 설정, JWT 필터, 로그인 성공/실패 핸들러
- `event` — 이벤트 생성·참여·출석 체크, XP 지급 승인 플로우
- `vote` — 투표 생성·참여·취소, 관리자 투표 관리
- `study` — 스터디 그룹 생성·멤버 관리·활동 기록
- `notice` — 공지사항 CRUD
- `utils` — 공통 응답·예외 처리 인프라

### 인증 흐름

1. `POST /api/auth/login` → `LoginSuccessHandler`가 JWT 액세스(30분)·리프레시(14일) 토큰 발급
2. 이후 모든 `/api/**` 요청은 `JwtTokenFilter`가 `Authorization: Bearer <token>` 헤더를 검증
3. 리프레시: `POST /api/auth/refresh` (Redis에 리프레시 토큰 저장·검증)
4. 현재 인증된 사용자는 `SecurityUtils.getCurrentMember()`로 꺼냄

### 공통 응답 규격

```json
{ "code": "GEN-000", "status": 200, "message": "Success", "data": { ... } }
```

- 성공: `Response.ok(data, HttpStatus.OK)` 또는 `Response.ok(HttpStatus.OK)`
- 실패: `BusinessException(ErrorCode)` throw → `GlobalExceptionHandler`가 `Response.errorResponse(errorCode)` 반환

### 에러 코드 패턴

도메인별 `*Error` enum이 `ErrorCode` 인터페이스를 구현. `code` / `httpStatus` / `message` 세 필드를 정의. 예:

```java
@Getter
public enum AuthError implements ErrorCode {
    MEMBER_NOT_FOUND("AUTH-001", HttpStatus.BAD_REQUEST, "로그인 후 이용해주세요");
    // ...
}
```

새 도메인 에러 추가 시 동일 패턴으로 enum 생성 후 `throw new BusinessException(SomeError.XYZ)`.

### XP 시스템

`Member.xp` 필드에 누적. 이벤트 참여·출석·투표 참여 등에서 직접 가산/차감. 관리자가 수동 XP 지급 요청 시 `EventApproval` 엔티티를 통해 승인 플로우(`ApprovalStatus.PENDING → APPROVED/REJECTED`) 진행.

### 소프트 삭제

`Member` 엔티티에 `@SQLRestriction("status = 'ACTIVE'")` 적용 — 탈퇴 회원은 `status = WITHDRAWN`으로 변경하며 쿼리에서 자동 제외.

### Admin vs 일반 API

컨트롤러가 도메인별로 `Admin*Controller` / `*Controller` 두 개로 분리. 현재 URL 레벨 권한 분리는 `SecurityConfig`에서 `.authenticated()` 수준이며, 관리자 여부는 서비스 레이어에서 `Role.ADMIN` 검사로 처리.
