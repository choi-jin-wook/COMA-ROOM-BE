# COMA Room

중앙동아리 **COMA**의 부원 관리 백엔드 서버입니다.

부원 출석, 이벤트 참여, 스터디 운영, 투표, 공지사항 등을 통합 관리하며 XP(경험치) 기반의 활동 이력을 제공합니다.

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 25 |
| Framework | Spring Boot 4.0.1 |
| ORM | Spring Data JPA (Hibernate) |
| Database | MySQL 8 |
| Cache / Session | Redis |
| Auth | Spring Security + JWT (JJWT 0.12.5) |
| Build | Gradle |

---

## 주요 기능

### 회원 (Member)
- 학번 기반 회원가입 / 로그인
- JWT 액세스 토큰 + 리프레시 토큰 (Redis 저장)
- 역할 구분: `ADMIN` / `USER`
- XP 이력 조회 및 리더보드

### 이벤트 (Event)
- 이벤트 생성·수정·삭제 (관리자)
- 이벤트 카테고리별 기본 XP 자동 부여
  - 정기회의 3 XP / 행사·스터디·랩실 5 XP / 스태프 2 XP
- 출석 체크 및 참여 이력 관리
- 이벤트 게시글(EventPost) 작성·승인 흐름

### 스터디 (Study)
- 스터디 개설 및 팀원 관리
- 스터디 활동 기록

### 투표 (Vote)
- 투표 생성·수정·종료 (관리자)
- 투표 참여 / 취소, XP 연동

### 공지사항 (Notice)
- 공지 우선순위 설정
- 관리자 작성, 전체 부원 조회

---

## 프로젝트 구조

```
src/main/java/com/coma/comaroom/
├── auth/          # JWT 필터, Spring Security 설정
├── config/        # Redis 설정
├── event/         # 이벤트, 출석, 이벤트 게시글, XP 승인
├── member/        # 회원 가입/로그인, 프로필, XP 관리
├── notice/        # 공지사항
├── study/         # 스터디 관리
├── vote/          # 투표
└── utils/         # 공통 응답, 예외 처리
```

---

## 시작하기

### 요구 사항

- Java 25+
- MySQL 8+
- Redis

### 환경 변수 설정

`src/main/resources/application.yaml`에서 아래 항목을 환경에 맞게 수정합니다.

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/comaroom_db
    username: {DB_USERNAME}
    password: {DB_PASSWORD}
  data:
    redis:
      host: localhost
      port: 6379

jwt:
  secret: {32자 이상의 시크릿 키}
```

### 빌드 및 실행

```bash
# 빌드
./gradlew build

# 실행
./gradlew bootRun
```

---

## API 엔드포인트 개요

| 경로 | 설명 | 권한 |
|------|------|------|
| `POST /api/auth/register` | 회원가입 | 공개 |
| `POST /api/auth/login` | 로그인 | 공개 |
| `POST /api/auth/refresh` | 토큰 재발급 | 공개 |
| `GET /api/member/**` | 내 정보 / XP 이력 | 인증 필요 |
| `GET /api/event/**` | 이벤트 목록·상세 | 인증 필요 |
| `POST /api/vote/**` | 투표 참여 | 인증 필요 |
| `GET /api/notice/**` | 공지사항 조회 | 인증 필요 |
| `GET /api/study/**` | 스터디 조회 | 인증 필요 |
| `/api/admin/**` | 관리자 전용 기능 | ADMIN |

CORS 허용 도메인: `https://www.comaroom.site`

---

## 테스트 실행

```bash
./gradlew test
```
