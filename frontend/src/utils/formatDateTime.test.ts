import { describe, expect, it } from "vitest";
import { formatDateTime } from "@/utils/formatDateTime";

describe("formatDateTime", () => {
  it("formats ISO date time values in Korean numeric form", () => {
    expect(formatDateTime("2026-06-29T14:05:30")).toBe("2026. 06. 29. 14:05");
  });

  it("returns fallback text for missing values", () => {
    expect(formatDateTime(null)).toBe("-");
  });
});
