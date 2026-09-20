package com.coma.comaroom.vote.concurrency;

import com.coma.comaroom.member.entity.Major;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.member.entity.Role;
import com.coma.comaroom.member.repository.MemberRepository;
import com.coma.comaroom.utils.SecurityUtils;
import com.coma.comaroom.vote.dto.request.ParticipateVoteRequestDto;
import com.coma.comaroom.vote.entity.Vote;
import com.coma.comaroom.vote.entity.VoteOption;
import com.coma.comaroom.vote.entity.VoteStatus;
import com.coma.comaroom.vote.repository.VoteRepository;
import com.coma.comaroom.vote.repository.VoteResultRepository;
import com.coma.comaroom.vote.service.VoteService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * 투표 참여 로직의 동시성 결함을 실제 트랜잭션으로 재현하는 통합 테스트.
 *
 * 재현 대상 (현재 코드 기준 실패):
 *   1) 동일 회원의 동시 중복 투표 → check-then-act 경쟁으로 DB 유니크 제약 위반(500)이
 *      깔끔한 ALREADY_VOTED(409) 대신 그대로 노출된다.
 *   2) 서로 다른 활동으로 인한 XP 동시 증가 → read-modify-write 로 인한 lost update 로
 *      최종 XP 가 실제 획득량보다 작아진다.
 *
 * 각 워커 스레드는 서비스의 @Transactional 을 각각 별도 트랜잭션으로 실행하므로
 * 실서비스의 "요청 당 회원을 DB에서 새로 읽는" 흐름을 그대로 모사한다.
 */
@SpringBootTest
@ActiveProfiles("test")
class VoteConcurrencyTest {

    @Autowired private VoteService voteService;
    @Autowired private MemberRepository memberRepository;
    @Autowired private VoteRepository voteRepository;
    @Autowired private VoteResultRepository voteResultRepository;

    // Redis 는 이 테스트 경로에서 쓰이지 않으므로 목으로 대체 (컨텍스트 로딩용)
    @MockitoBean private StringRedisTemplate stringRedisTemplate;

    // getCurrentMember() 가 매 호출마다 DB 에서 회원을 새로 읽도록 하여 실요청 흐름을 모사
    @MockitoBean private SecurityUtils securityUtils;

    private Long memberId;

    @BeforeEach
    void setUp() {
        Member member = memberRepository.saveAndFlush(Member.builder()
                .studentId("concurrency-tester")
                .name("동시성테스터")
                .password("encoded")
                .xp(0L)
                .role(Role.USER)
                .major(Major.COMPUTER_INFO)
                .build());
        this.memberId = member.getMemberId();

        // 서비스가 호출될 때마다 현재 트랜잭션에서 회원을 새로 조회하도록 설정
        when(securityUtils.getCurrentMember())
                .thenAnswer(inv -> memberRepository.findById(memberId).orElseThrow());
    }

    @AfterEach
    void tearDown() {
        voteResultRepository.deleteAllInBatch();
        voteRepository.deleteAll();
        memberRepository.deleteAll();
    }

    // ─────────────────────────────────────────────────────────────
    // 1) 동일 회원 동시 중복 투표 → 정확히 1표만 반영 + 나머지는 정상 실패여야 한다
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("동일 회원이 같은 투표에 동시 참여해도 1표만 반영되고 500이 노출되지 않아야 한다")
    void concurrentDuplicateVote_shouldCountOnce_withoutServerError() throws InterruptedException {
        Vote vote = persistSingleChoiceVote();
        Long optionId = vote.getVoteOptions().get(0).getVoteOptionId();

        int threads = 30;
        AtomicInteger success = new AtomicInteger();
        AtomicInteger alreadyVoted = new AtomicInteger();   // 기대되는 정상 실패 (BusinessException)
        AtomicInteger unexpectedError = new AtomicInteger(); // 버그: 제약 위반이 그대로 터진 경우
        List<String> unexpectedTypes = new CopyOnWriteArrayList<>();

        runConcurrently(threads, () -> {
            try {
                voteService.participateVote(requestOf(optionId), vote.getVoteId());
                success.incrementAndGet();
            } catch (com.coma.comaroom.BusinessException e) {
                alreadyVoted.incrementAndGet();
            } catch (Exception e) {
                unexpectedError.incrementAndGet();
                unexpectedTypes.add(e.getClass().getSimpleName());
            }
        });

        long persistedVotes = voteResultRepository.count();
        long finalXp = memberRepository.findById(memberId).orElseThrow().getXp();

        System.out.printf("[중복투표] 성공=%d, ALREADY_VOTED=%d, 예상밖오류=%d %s, 저장된표=%d, XP=%d%n",
                success.get(), alreadyVoted.get(), unexpectedError.get(), unexpectedTypes, persistedVotes, finalXp);

        assertThat(persistedVotes).as("정확히 1표만 저장되어야 함").isEqualTo(1);
        assertThat(finalXp).as("XP 는 2 만 부여되어야 함").isEqualTo(2L);
        assertThat(unexpectedError.get())
                .as("경쟁 상황에서도 DB 제약 위반(500)이 사용자에게 노출되면 안 됨 - 발생 타입: %s", unexpectedTypes)
                .isZero();
    }

    // ─────────────────────────────────────────────────────────────
    // 2) 서로 다른 투표 N개 동시 참여 → XP 는 정확히 N*2 여야 한다 (lost update 없음)
    // ─────────────────────────────────────────────────────────────
    @Test
    @DisplayName("서로 다른 투표에 동시 참여 시 XP 가 lost update 없이 정확히 합산되어야 한다")
    void concurrentXpGain_shouldNotLoseUpdates() throws InterruptedException {
        int voteCount = 20;
        List<Long> optionIds = new ArrayList<>();
        List<Long> voteIds = new ArrayList<>();
        for (int i = 0; i < voteCount; i++) {
            Vote vote = persistSingleChoiceVote();
            voteIds.add(vote.getVoteId());
            optionIds.add(vote.getVoteOptions().get(0).getVoteOptionId());
        }

        runConcurrently(voteCount, i -> {
            voteService.participateVote(requestOf(optionIds.get(i)), voteIds.get(i));
        });

        long finalXp = memberRepository.findById(memberId).orElseThrow().getXp();
        long persistedVotes = voteResultRepository.count();

        System.out.printf("[XP경쟁] 참여수=%d, 저장된표=%d, 기대XP=%d, 실제XP=%d, 손실=%d%n",
                voteCount, persistedVotes, voteCount * 2L, finalXp, voteCount * 2L - finalXp);

        assertThat(persistedVotes).as("모든 투표가 저장되어야 함").isEqualTo(voteCount);
        assertThat(finalXp)
                .as("XP 는 lost update 없이 정확히 %d 여야 함", voteCount * 2)
                .isEqualTo(voteCount * 2L);
    }

    // ───────────────────────────── helpers ─────────────────────────────

    private Vote persistSingleChoiceVote() {
        Vote vote = Vote.builder()
                .title("동시성 테스트 투표")
                .isMultiVote(false)
                .voteStatus(VoteStatus.IN_PROGRESS)
                .deadline(LocalDateTime.now().plusDays(1))
                .voteOptions(new ArrayList<>())
                .build();
        vote.addOption(VoteOption.builder().content("옵션A").voteResults(new ArrayList<>()).build());
        vote.addOption(VoteOption.builder().content("옵션B").voteResults(new ArrayList<>()).build());
        return voteRepository.saveAndFlush(vote);
    }

    private ParticipateVoteRequestDto requestOf(Long optionId) {
        ParticipateVoteRequestDto dto = new ParticipateVoteRequestDto();
        dto.setVoteOptionId(List.of(optionId));
        return dto;
    }

    /** 모든 스레드를 동시에 출발시켜 경쟁 구간을 최대화한다. */
    private void runConcurrently(int n, Runnable task) throws InterruptedException {
        runConcurrently(n, i -> task.run());
    }

    private void runConcurrently(int n, java.util.function.IntConsumer task) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(n);
        CountDownLatch ready = new CountDownLatch(n);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(n);
        for (int i = 0; i < n; i++) {
            final int idx = i;
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    task.accept(idx);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        ready.await();          // 전 스레드 준비 대기
        start.countDown();      // 동시 출발
        done.await(30, TimeUnit.SECONDS);
        pool.shutdownNow();
    }
}
