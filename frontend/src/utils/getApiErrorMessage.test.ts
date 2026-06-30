import { describe, expect, it } from "vitest";
import { MESSAGES } from "@/constants/messages";
import { ApiError } from "@/services/apiClient";
import { getApiErrorMessage, isSessionExpiredError } from "@/utils/getApiErrorMessage";

describe("getApiErrorMessage", () => {
  it("maps invalid credentials to a login failure message", () => {
    const error = new ApiError(
      {
        errorCode: "INVALID_CREDENTIALS",
        errorMessage: "이메일 또는 비밀번호가 올바르지 않습니다.",
      },
      401,
    );

    expect(getApiErrorMessage(error)).toBe(MESSAGES.AUTH.INVALID_CREDENTIALS);
    expect(isSessionExpiredError(error)).toBe(false);
  });

  it("maps default security 403 responses to an access denied message", () => {
    const error = new ApiError(
      {
        errorCode: "HTTP_ERROR",
        errorMessage: "Forbidden",
      },
      403,
    );

    expect(getApiErrorMessage(error)).toBe(MESSAGES.AUTH.ACCESS_DENIED);
    expect(isSessionExpiredError(error)).toBe(false);
  });

  it("maps unauthorized access domain errors without logging out", () => {
    const error = new ApiError(
      {
        errorCode: "UNAUTHORIZED_ACCESS",
        errorMessage: "인증되지 않은 접근입니다.",
      },
      401,
    );

    expect(getApiErrorMessage(error)).toBe(MESSAGES.AUTH.ACCESS_DENIED);
    expect(isSessionExpiredError(error)).toBe(false);
  });

  it("maps not found responses to a user-friendly message", () => {
    const error = new ApiError(
      {
        errorCode: "HTTP_ERROR",
        errorMessage: "Not Found",
      },
      404,
    );

    expect(getApiErrorMessage(error)).toBe(MESSAGES.API.NOT_FOUND);
  });
});
