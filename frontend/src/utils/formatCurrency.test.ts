import { describe, expect, it } from "vitest";
import { formatCurrency } from "@/utils/formatCurrency";

describe("formatCurrency", () => {
  it("formats numeric won amounts without decimal places", () => {
    expect(formatCurrency(12500)).toBe("12,500원");
  });

  it("formats string decimal amounts from backend responses", () => {
    expect(formatCurrency("18900.00")).toBe("18,900원");
  });
});
