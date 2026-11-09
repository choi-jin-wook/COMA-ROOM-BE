package com.coma.comaroom.study.service;

import com.coma.comaroom.BusinessException;
import com.coma.comaroom.member.entity.Member;
import com.coma.comaroom.member.repository.MemberRepository;
import com.coma.comaroom.study.StudyError;
import com.coma.comaroom.study.dto.request.AddStudyMemberRequest;
import com.coma.comaroom.study.dto.request.CreateStudyRequest;
import com.coma.comaroom.study.dto.response.StudyResponse;
import com.coma.comaroom.study.entity.Study;
import com.coma.comaroom.study.entity.StudyMember;
import com.coma.comaroom.study.repository.StudyMemberRepository;
import com.coma.comaroom.study.repository.StudyRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 관리자의 스터디 생성 및 멤버 등록·삭제를 처리한다. */
@Service
@Transactional
@AllArgsConstructor
public class StudyManagerService {

    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final MemberRepository memberRepository;

    /** 스터디와 스터디장의 멤버십을 한 트랜잭션으로 생성한다. */
    public StudyResponse createStudy(CreateStudyRequest request) {
        Member manager = memberRepository.findById(request.getManagerId())
                .orElseThrow(() -> new BusinessException(StudyError.MEMBER_NOT_FOUND));
        Study study = request.toEntity(manager);
        Study saved = studyRepository.save(study);
        studyMemberRepository.save(StudyMember.builder().study(saved).member(manager).build());
        return StudyResponse.from(saved);
    }

    /** 스터디와 회원의 존재 및 중복 등록 여부를 확인한다. */
    public void addStudyMember(Long studyId, AddStudyMemberRequest request) {
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new BusinessException(StudyError.STUDY_NOT_FOUND));
        Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new BusinessException(StudyError.MEMBER_NOT_FOUND));
        if (studyMemberRepository.existsByStudyIdAndMemberMemberId(studyId, request.getMemberId())) {
            throw new BusinessException(StudyError.ALREADY_STUDY_MEMBER);
        }
        studyMemberRepository.save(request.toEntity(study, member));
    }

    /** 등록된 멤버십만 삭제한다. */
    public void removeStudyMember(Long studyId, Long memberId) {
        studyRepository.findById(studyId)
                .orElseThrow(() -> new BusinessException(StudyError.STUDY_NOT_FOUND));
        StudyMember studyMember = studyMemberRepository.findByStudyIdAndMemberMemberId(studyId, memberId)
                .orElseThrow(() -> new BusinessException(StudyError.NOT_STUDY_MEMBER));
        studyMemberRepository.delete(studyMember);
    }
}
