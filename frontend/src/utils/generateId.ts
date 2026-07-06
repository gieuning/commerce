// crypto.randomUUID는 보안 컨텍스트(HTTPS·localhost)에서만 존재한다.
// HTTP·IP 접속 등 비보안 컨텍스트에서도 동작하도록 단계적 폴백을 둔다.
// 용도는 UI 로컬 식별용 id 생성이라 암호학적 강도가 필수는 아니다.
export const generateId = (): string => {
  const cryptoObj = globalThis.crypto;

  if (cryptoObj && typeof cryptoObj.randomUUID === "function") {
    return cryptoObj.randomUUID();
  }

  // getRandomValues는 비보안 컨텍스트에서도 제공된다 → UUID v4 직접 구성
  if (cryptoObj && typeof cryptoObj.getRandomValues === "function") {
    const bytes = cryptoObj.getRandomValues(new Uint8Array(16));
    bytes[6] = (bytes[6] & 0x0f) | 0x40; // version 4
    bytes[8] = (bytes[8] & 0x3f) | 0x80; // variant
    const hex = Array.from(bytes, (byte) => byte.toString(16).padStart(2, "0"));
    return (
      `${hex.slice(0, 4).join("")}-${hex.slice(4, 6).join("")}-` +
      `${hex.slice(6, 8).join("")}-${hex.slice(8, 10).join("")}-${hex.slice(10, 16).join("")}`
    );
  }

  return `id-${Date.now().toString(16)}-${Math.random().toString(16).slice(2, 10)}`;
};
