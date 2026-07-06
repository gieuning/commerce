import { describe, expect, it } from "vitest";
import { PASSWORD_MAX_LENGTH, PASSWORD_MIN_LENGTH } from "@/constants/auth";
import { MESSAGES } from "@/constants/messages";
import { validateSignupForm } from "@/features/auth/validateSignupForm";

const validValues = {
  email: "user@example.com",
  name: "홍길동",
  password: "password123",
  phoneNumber: "01012345678",
};

describe("validateSignupForm", () => {
  it("returns no errors for valid input", () => {
    expect(validateSignupForm(validValues)).toEqual({});
  });

  it("allows an empty phone number (optional field)", () => {
    expect(validateSignupForm({ ...validValues, phoneNumber: "" })).toEqual({});
  });

  it("flags a password shorter than the minimum length", () => {
    const errors = validateSignupForm({ ...validValues, password: "short" });

    expect(errors.password).toBe(
      MESSAGES.VALIDATION.PASSWORD_LENGTH(PASSWORD_MIN_LENGTH, PASSWORD_MAX_LENGTH),
    );
  });

  it("flags an invalid email format", () => {
    const errors = validateSignupForm({ ...validValues, email: "not-an-email" });

    expect(errors.email).toBe(MESSAGES.VALIDATION.EMAIL_INVALID);
  });

  it("flags a name containing digits", () => {
    const errors = validateSignupForm({ ...validValues, name: "홍길동2" });

    expect(errors.name).toBe(MESSAGES.VALIDATION.NAME_INVALID);
  });

  it("flags a phone number that contains hyphens", () => {
    const errors = validateSignupForm({ ...validValues, phoneNumber: "010-1234-5678" });

    expect(errors.phoneNumber).toBe(MESSAGES.VALIDATION.PHONE_INVALID);
  });

  it("flags empty required fields", () => {
    const errors = validateSignupForm({ email: "  ", name: "", password: "", phoneNumber: "" });

    expect(errors.email).toBe(MESSAGES.VALIDATION.EMAIL_REQUIRED);
    expect(errors.name).toBe(MESSAGES.VALIDATION.NAME_REQUIRED);
    expect(errors.password).toBe(MESSAGES.VALIDATION.PASSWORD_REQUIRED);
  });
});
