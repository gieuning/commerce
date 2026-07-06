import { PASSWORD_MAX_LENGTH, PASSWORD_MIN_LENGTH } from "@/constants/auth";
import { MESSAGES } from "@/constants/messages";

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
// 이름: 한글/영문/공백만 허용(숫자·특수문자 불가).
const NAME_PATTERN = /^[가-힣a-zA-Z\s]+$/;
// 휴대폰: '-' 없이 숫자 10~11자리.
const PHONE_PATTERN = /^\d{10,11}$/;

export const validateEmailField = (value: string): string | undefined => {
  if (!value.trim()) {
    return MESSAGES.VALIDATION.EMAIL_REQUIRED;
  }

  if (!EMAIL_PATTERN.test(value)) {
    return MESSAGES.VALIDATION.EMAIL_INVALID;
  }

  return undefined;
};

export const validateNameField = (value: string): string | undefined => {
  if (!value.trim()) {
    return MESSAGES.VALIDATION.NAME_REQUIRED;
  }

  if (!NAME_PATTERN.test(value)) {
    return MESSAGES.VALIDATION.NAME_INVALID;
  }

  return undefined;
};

// 휴대폰 번호는 선택 입력. 입력됐다면 '-' 없는 숫자 형식이어야 한다.
export const validatePhoneField = (value: string): string | undefined => {
  if (!value.trim()) {
    return undefined;
  }

  return PHONE_PATTERN.test(value) ? undefined : MESSAGES.VALIDATION.PHONE_INVALID;
};

// 입력 단계에서 허용되지 않는 문자를 제거해 애초에 들어가지 못하도록 막는다.
export const sanitizeNameInput = (value: string): string =>
  value.replace(/[^가-힣ㄱ-ㅎㅏ-ㅣa-zA-Z\s]/g, "");

export const sanitizePhoneInput = (value: string): string => value.replace(/\D/g, "");

// 로그인: 존재 여부만 확인(길이/형식은 서버 자격증명 검증에 맡긴다).
export const validateRequiredPassword = (value: string): string | undefined =>
  value ? undefined : MESSAGES.VALIDATION.PASSWORD_REQUIRED;

// 회원가입: 서버 @Size(min=8, max=72)와 동일한 길이 규칙을 미리 검증한다.
export const validateNewPassword = (value: string): string | undefined => {
  if (!value) {
    return MESSAGES.VALIDATION.PASSWORD_REQUIRED;
  }

  if (value.length < PASSWORD_MIN_LENGTH || value.length > PASSWORD_MAX_LENGTH) {
    return MESSAGES.VALIDATION.PASSWORD_LENGTH(PASSWORD_MIN_LENGTH, PASSWORD_MAX_LENGTH);
  }

  return undefined;
};
