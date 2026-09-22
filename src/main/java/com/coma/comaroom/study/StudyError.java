package com.coma.comaroom.study;

import com.coma.comaroom.utils.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum StudyError implements ErrorCode {
    STUDY_NOT_FOUND("STUDY-001", HttpStatus.NOT_FOUND, "존재하지 않는 스터디입니다."),
    MEMBER_NOT_FOUND("STUDY-002", HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    ALREADY_STUDY_MEMBER("STUDY-003", HttpStatus.BAD_REQUEST, "이미 스터디 멤버입니다."),
    NOT_STUDY_MEMBER("STUDY-004", HttpStatus.BAD_REQUEST, "해당 스터디의 멤버가 아닙니다."),
    ACTIVITY_NOT_FOUND("STUDY-005", HttpStatus.NOT_FOUND, "존재하지 않는 스터디 일정입니다."),
    ACTIVITY_NOT_IN_STUDY("STUDY-006", HttpStatus.BAD_REQUEST, "해당 스터디의 일정이 아닙니다."),
    INVALID_INPUT("STUDY-007", HttpStatus.BAD_REQUEST, "스터디 요청값이 올바르지 않습니다."),
    ACCESS_DENIED("STUDY-008", HttpStatus.FORBIDDEN, "해당 스터디에 대한 권한이 없습니다."),
    WEEK_NOT_FOUND("STUDY-009", HttpStatus.NOT_FOUND, "등록된 주차 계획이 없습니다."),
    WEEK_CONFLICT("STUDY-010", HttpStatus.CONFLICT, "이미 등록된 주차 계획입니다."),
    MATERIAL_NOT_FOUND("STUDY-011", HttpStatus.NOT_FOUND, "해당 주차의 자료를 찾을 수 없습니다."),
    FILE_TOO_LARGE("STUDY-012", HttpStatus.PAYLOAD_TOO_LARGE, "자료는 1MB 이하여야 합니다."),
    UNSUPPORTED_FILE("STUDY-013", HttpStatus.UNSUPPORTED_MEDIA_TYPE, "허용하지 않는 파일 형식입니다."),
    UPLOAD_FAILED("STUDY-014", HttpStatus.INTERNAL_SERVER_ERROR, "자료 업로드에 실패했습니다."),
    JOIN_CONFLICT("STUDY-015", HttpStatus.CONFLICT, "이미 소속되었거나 대기 중인 참여 요청이 있습니다."),
    STUDY_CLOSED("STUDY-016", HttpStatus.CONFLICT, "현재 참여 가능한 스터디가 아닙니다."),
    ATTENDANCE_CONFLICT("STUDY-017", HttpStatus.CONFLICT, "아직 유효한 주차 출석 세션이 있습니다."),
    STORAGE_UNAVAILABLE("STUDY-018", HttpStatus.SERVICE_UNAVAILABLE, "자료 저장소에 연결할 수 없습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
