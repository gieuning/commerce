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

  it("attaches authorization header when token exists", async () => {
    localStorage.setItem(STORAGE_KEYS.ACCESS_TOKEN, "access-token-value");
    const requestMock = vi.fn((_input: RequestInfo | URL, _init?: RequestInit) =>
      Promise.resolve(createJsonResponse({ data: { id: 1 } })),
    );
    vi.stubGlobal("fetch", requestMock);

    await apiClient.get("/users/me");

    const requestInit = requestMock.mock.calls[0]?.[1];
    const requestHeaders = new Headers(requestInit?.headers);

    expect(requestHeaders.get("Authorization")).toBe("Bearer access-token-value");
  });
});
