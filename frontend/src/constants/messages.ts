export const MESSAGES = {
  COMMON: {
    LOADING: "불러오는 중입니다.",
    UNKNOWN_ERROR: "알 수 없는 오류가 발생했습니다.",
    EMPTY_RESULT: "표시할 내용이 없습니다.",
    RETRY: "다시 시도",
  },
  AUTH: {
    LOGIN_REQUIRED: "로그인이 필요한 화면입니다.",
    ADMIN_REQUIRED: "관리자 권한이 필요한 화면입니다.",
    LOGIN_SUCCESS: "로그인되었습니다.",
    SIGNUP_SUCCESS: "회원가입이 완료되었습니다.",
  },
  PRODUCT: {
    EMPTY: "등록된 상품이 없습니다.",
    SELECT_OPTION: "상품 옵션을 선택해 주세요.",
    ADDED_TO_CART: "장바구니에 담았습니다.",
  },
  CART: {
    EMPTY: "장바구니가 비어 있습니다.",
    ORDER_CREATED: "주문이 생성되었습니다.",
  },
  ORDER: {
    EMPTY: "주문 내역이 없습니다.",
    PAYMENT_PENDING: "결제 기능은 payment API가 main에 반영된 뒤 연결됩니다.",
  },
} as const;
