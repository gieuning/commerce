import { beforeEach, describe, expect, it, vi } from "vitest";
import { STORAGE_KEYS } from "@/constants/storageKeys";
import { apiClient } from "@/services/apiClient";

const createJsonResponse = (body: unknown, status = 200): Response =>
  new Response(JSON.stringify(body), {
    status,
    headers: { "Content-Type": "application/json" },
  });

describe("apiClient", () => {
  beforeEach(() => {
    localStorage.clear();
    vi.unstubAllGlobals();
  });

  it("unwraps backend data responses", async () => {
    const requestMock = vi.fn((_input: RequestInfo | URL, _init?: RequestInit) =>
      Promise.resolve(createJsonResponse({ data: { name: "Keyboard" } })),
    );
    vi.stubGlobal("fetch", requestMock);

    const product = await apiClient.get<{ name: string }>("/products/1");

    expect(product.name).toBe("Keyboard");
  });

  it("throws typed api errors from backend error responses", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn((_input: RequestInfo | URL, _init?: RequestInit) =>
        Promise.resolve(
          createJsonResponse(
            {
              error: {
                errorCode: "NOT_FOUND_PRODUCT",
                errorMessage: "상품을 찾을 수 없습니다.",
              },
            },
            404,
          ),
        ),
      ),
    );

    await expect(apiClient.get("/products/404")).rejects.toMatchObject({
      errorCode: "NOT_FOUND_PRODUCT",
      message: "상품을 찾을 수 없습니다.",
      status: 404,
    });
  });

  it("sends the auth cookie by using credentials include", async () => {
    const requestMock = vi.fn((_input: RequestInfo | URL, _init?: RequestInit) =>
      Promise.resolve(createJsonResponse({ data: { id: 1 } })),
    );
    vi.stubGlobal("fetch", requestMock);

    await apiClient.get("/users/me");

    const requestInit = requestMock.mock.calls[0]?.[1];
    const requestHeaders = new Headers(requestInit?.headers);

    expect(requestInit?.credentials).toBe("include");
    // 토큰은 쿠키로만 전송된다 — Authorization 헤더는 더 이상 붙지 않는다.
    expect(requestHeaders.has("Authorization")).toBe(false);
  });

  it("stores the guest token issued in the response header", async () => {
    const guestResponse = new Response(JSON.stringify({ data: { items: [] } }), {
      status: 201,
      headers: { "Content-Type": "application/json", "X-Guest-Token": "guest-token-abc" },
    });
    const requestMock = vi.fn((_input: RequestInfo | URL, _init?: RequestInit) =>
      Promise.resolve(guestResponse),
    );
    vi.stubGlobal("fetch", requestMock);

    await apiClient.post("/cart/items", { productId: 1, quantity: 1 });

    expect(localStorage.getItem(STORAGE_KEYS.GUEST_TOKEN)).toBe("guest-token-abc");
    // 상태 변경(POST) 요청에도 인증 쿠키가 실려야 한다.
    expect(requestMock.mock.calls[0]?.[1]?.credentials).toBe("include");
  });

  it("attaches guest token header when guest token exists", async () => {
    localStorage.setItem(STORAGE_KEYS.GUEST_TOKEN, "guest-token-abc");
    const requestMock = vi.fn((_input: RequestInfo | URL, _init?: RequestInit) =>
      Promise.resolve(createJsonResponse({ data: { items: [] } })),
    );
    vi.stubGlobal("fetch", requestMock);

    await apiClient.get("/cart");

    const requestInit = requestMock.mock.calls[0]?.[1];
    const requestHeaders = new Headers(requestInit?.headers);

    expect(requestHeaders.get("X-Guest-Token")).toBe("guest-token-abc");
  });
});
