import { describe, expect, it } from "vitest";
import { MESSAGES } from "@/constants/messages";
import { validateLoginForm } from "@/features/auth/validateLoginForm";

describe("validateLoginForm", () => {
  it("returns no errors for valid input", () => {
    expect(validateLoginForm({ email: "user@example.com", password: "anything" })).toEqual({});
  });

  it("flags an invalid email format", () => {
    const errors = validateLoginForm({ email: "not-an-email", password: "anything" });

    expect(errors.email).toBe(MESSAGES.VALIDATION.EMAIL_INVALID);
  });

  it("flags empty fields without checking password length", () => {
    const errors = validateLoginForm({ email: "", password: "" });

    expect(errors.email).toBe(MESSAGES.VALIDATION.EMAIL_REQUIRED);
    expect(errors.password).toBe(MESSAGES.VALIDATION.PASSWORD_REQUIRED);
  });
});
