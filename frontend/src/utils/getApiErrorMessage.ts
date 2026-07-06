import { MESSAGES } from "@/constants/messages";
import { ApiError } from "@/services/apiClient";

const HTTP_STATUS_MESSAGES: Record<number, string> = {
  400: MESSAGES.API.BAD_REQUEST,
  401: MESSAGES.AUTH.SESSION_EXPIRED,
  403: MESSAGES.AUTH.ACCESS_DENIED,
  404: MESSAGES.API.NOT_FOUND,
  409: MESSAGES.API.CONFLICT,
  500: MESSAGES.API.SERVER_ERROR,
};

const ERROR_CODE_MESSAGES: Record<string, string> = {
  EXPIRED_TOKEN: MESSAGES.AUTH.SESSION_EXPIRED,
  INVALID_CREDENTIALS: MESSAGES.AUTH.INVALID_CREDENTIALS,
  INVALID_TOKEN: MESSAGES.AUTH.SESSION_EXPIRED,
  INVALID_REQUEST_BODY: MESSAGES.API.BAD_REQUEST,
  MISSING_TOKEN: MESSAGES.AUTH.SESSION_EXPIRED,
  NOT_FOUND: MESSAGES.API.NOT_FOUND,
  OPTIMISTIC_LOCK_CONFLICT: MESSAGES.API.CONFLICT,
  SERVER_ERROR: MESSAGES.API.SERVER_ERROR,
  UNAUTHORIZED_ACCESS: MESSAGES.AUTH.ACCESS_DENIED,
};

const SESSION_EXPIRED_ERROR_CODES = new Set([
  "EXPIRED_TOKEN",
  "INVALID_TOKEN",
  "MISSING_TOKEN",
]);

export const getApiErrorMessage = (error: unknown): string => {
  if (!navigator.onLine) {
    return MESSAGES.API.NETWORK_ERROR;
  }

  if (!(error instanceof ApiError)) {
    if (error instanceof TypeError) {
      return MESSAGES.API.NETWORK_ERROR;
    }

    return error instanceof Error ? error.message : MESSAGES.COMMON.UNKNOWN_ERROR;
  }

  const codeMessage = ERROR_CODE_MESSAGES[error.errorCode];

  if (codeMessage) {
    return codeMessage;
  }

  // 백엔드 도메인 에러(구조화된 errorCode + 메시지)는 서버가 준 구체 메시지를 그대로 노출한다.
  // (중복 이메일, 검증 오류 등) "HTTP_ERROR"는 구조화되지 않은 응답이라 아래 일반 상태 문구로 폴백한다.
  if (error.errorCode !== "HTTP_ERROR" && error.message) {
    return error.message;
  }

  const statusMessage = HTTP_STATUS_MESSAGES[error.status];

  if (statusMessage) {
    return statusMessage;
  }

  return error.message || MESSAGES.COMMON.UNKNOWN_ERROR;
};

export const isSessionExpiredError = (error: unknown): boolean =>
  error instanceof ApiError &&
  (SESSION_EXPIRED_ERROR_CODES.has(error.errorCode) ||
    (error.errorCode === "HTTP_ERROR" && error.status === 401));
