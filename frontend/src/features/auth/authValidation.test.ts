import { describe, expect, it } from "vitest";
import { sanitizeNameInput, sanitizePhoneInput } from "@/features/auth/authValidation";

describe("sanitizeNameInput", () => {
  it("removes digits and special characters, keeping Korean/English/spaces", () => {
    expect(sanitizeNameInput("홍길동123")).toBe("홍길동");
    expect(sanitizeNameInput("John 2 Doe!")).toBe("John  Doe");
  });

  it("preserves Korean jamo mid-composition", () => {
    expect(sanitizeNameInput("ㅎㅗㅇ")).toBe("ㅎㅗㅇ");
  });
});

describe("sanitizePhoneInput", () => {
  it("strips hyphens and any non-digit characters", () => {
    expect(sanitizePhoneInput("010-1234-5678")).toBe("01012345678");
    expect(sanitizePhoneInput("010 1234 5678")).toBe("01012345678");
    expect(sanitizePhoneInput("abc010")).toBe("010");
  });
});
