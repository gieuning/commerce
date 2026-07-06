import { afterEach, describe, expect, it, vi } from "vitest";
import { generateId } from "@/utils/generateId";

const UUID_V4 = /^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/;

afterEach(() => {
  vi.unstubAllGlobals();
});

describe("generateId", () => {
  it("returns unique UUID-shaped values", () => {
    const first = generateId();
    const second = generateId();

    expect(first).toMatch(UUID_V4);
    expect(first).not.toBe(second);
  });

  it("falls back to getRandomValues when randomUUID is unavailable (HTTP context)", () => {
    // 비보안 컨텍스트 재현: randomUUID 없이 getRandomValues만 제공
    vi.stubGlobal("crypto", {
      getRandomValues: (bytes: Uint8Array) => {
        for (let index = 0; index < bytes.length; index += 1) {
          bytes[index] = index * 7 + 1;
        }
        return bytes;
      },
    });

    expect(generateId()).toMatch(UUID_V4);
  });
});
