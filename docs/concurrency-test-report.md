# 투표 참여 동시성 결함 진단 및 개선 리포트

> 배포 전 동시성 검증(concurrency testing)을 통해 실제 트랜잭션 경쟁 상황에서
> 발생하는 데이터 정합성 결함 2건을 재현하고, 원자적 갱신으로 수정한 기록.

## 1. 요약

| 항목 | 내용 |
|------|------|
| 대상 | `POST /api/vote/{voteId}/participate` (`VoteService.participateVote`) |
| 검증 방식 | 실제 MySQL(InnoDB) + `@SpringBootTest` 통합 테스트, `ExecutorService`로 동시 요청 재현 |
| 발견 결함 | ① XP lost update, ② 중복 투표 경쟁 시 500 노출 |
| 결과 | 두 결함 모두 재현(RED) → 원자적 갱신·제약 변환으로 수정(GREEN) |

테스트 코드: `src/test/java/com/coma/comaroom/vote/concurrency/VoteConcurrencyTest.java`
운영 데이터 오염 방지를 위해 별도 스키마(`comaroom_concurrency_test`)를 매 실행마다 재생성.

---

## 2. 결함 ① — XP Lost Update (심각)

### 원인
```java
// 수정 전: 애플리케이션 레벨 read-modify-write
member.setXp(member.getXp() + 2);   // 읽고(0) → 더하고(2) → 커밋
```
각 요청이 자기 트랜잭션에서 회원을 읽어 `xp`를 계산하므로, 동시에 들어온
요청들이 모두 같은 값(예: 0)을 읽고 각자 덮어써서 증가분이 사라진다.

### 재현 (동시에 서로 다른 투표 20개 참여, 기대 XP = 40)
```
[RED ] 참여수=20, 저장된표=20, 기대XP=40, 실제XP=6,  손실=34   ← 85% 유실
[GREEN] 참여수=20, 저장된표=20, 기대XP=40, 실제XP=40, 손실=0
```

### 수정
```java
// 수정 후: DB 원자적 증가
@Modifying
@Query("UPDATE Member m SET m.xp = m.xp + :delta WHERE m.memberId = :memberId")
void incrementXp(Long memberId, long delta);
```
`UPDATE ... SET xp = xp + n`은 행 수준 잠금 하에 원자적으로 수행되어
동시 증가분이 유실되지 않는다.

---

## 3. 결함 ② — 중복 투표 경쟁 시 500 노출 (중간)

### 원인
```java
if (voteResultRepository.existsByVoter...(member, voteId)) {   // ① 확인
    throw new BusinessException(ALREADY_VOTED);
}
...
vote.participate(voteOptionIds, member);                       // ② 저장
```
"확인 → 저장" 사이(check-then-act)에 락이 없어, 같은 사용자의 동시 요청이
모두 ①을 통과한다. 저장 시 `vote_result`의 유니크 제약(`uk_voter_option`)이
중복은 막아 주지만, 그 위반이 `DataIntegrityViolationException`(HTTP 500)으로
사용자에게 그대로 노출된다.

### 재현 (같은 투표에 30명 동시 참여)
```
[RED ] 성공=1, ALREADY_VOTED=20, 예상밖오류=9(DataIntegrityViolationException), 저장된표=1, XP=2
[GREEN] 성공=1, ALREADY_VOTED=29, 예상밖오류=0,                                  저장된표=1, XP=2
```
표 개수와 XP는 유니크 제약 덕에 항상 정확했지만, 수정 전에는 **9명이 500 에러**를
받았다. 수정 후에는 전원이 명확한 `ALREADY_VOTED`(409)로 응답받는다.

### 수정
```java
try {
    voteResultRepository.flush();          // 커밋 전에 제약 위반을 앞당겨 감지
} catch (DataIntegrityViolationException e) {
    throw new BusinessException(VoteError.ALREADY_VOTED);   // 500 → 409 변환
}
```
DB 유니크 제약을 "최종 방어선"으로 신뢰하되, 위반을 잡아 도메인 에러로 변환해
사용자 경험과 API 계약을 지킨다.

---

## 4. 재현 방법

```bash
# 로컬 MySQL 에 테스트 스키마 준비 (최초 1회)
mysql -uroot -p -e "CREATE DATABASE IF NOT EXISTS comaroom_concurrency_test;"

# 동시성 테스트 실행
./gradlew test --tests "com.coma.comaroom.vote.concurrency.VoteConcurrencyTest"
```
수정 전 커밋에서 실행하면 두 테스트가 실패(RED), 수정 후에는 통과(GREEN)한다.

---

## 5. 남은 개선 여지 (후속 권장)

- `VoteService.cancelVote`의 `member.setXp(getXp() - 2)`도 동일한 lost update
  패턴 → 원자적 감소로 전환 권장.
- `EventService`·`AdminEventService`의 XP 부여 경로도 동일 점검 필요.
- 다중 항목(멀티 투표) 취소/재참여의 XP 정합성 회귀 테스트 추가.
- 근본적으로는 XP 변경을 단일 도메인 서비스(원자적 연산)로 모으고,
  변경 이력을 별도 테이블에 적재해 감사 가능하게 하는 설계 고려.
