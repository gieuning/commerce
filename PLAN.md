# 커머스/주문 플랫폼 — 4주 프로젝트 계획 (모놀리식)

> **아키텍처**: 모놀리식 (단일 Spring Boot 애플리케이션)
> **스택**: Spring Boot 3 · Gradle · JPA · Spring Security(JWT) · MySQL
> **기간**: 4주 · 주 15~20시간 (총 60~80시간)

---

## 1. 최종 목표 (MVP)

> **회원가입/로그인(JWT) → 상품 목록·상세 조회 → 주문 → 재고 차감·결제 생성 → 주문 내역 조회**

이 한 줄이 끝까지 동작하면 프로젝트의 핵심을 증명한 것.

---

## 2. 패키지 구조 (도메인별)

```
com.gieun.commerce
 ├── CommerceApplication.java
 ├── domain/                도메인별 패키지 (계층 구조)
 │    ├── user/
 │    │    ├── controller/
 │    │    ├── service/
 │    │    ├── repository/
 │    │    ├── entity/
 │    │    └── dto/
 │    │         ├── request/    (요청 DTO)
 │    │         └── response/   (응답 DTO)
 │    ├── product/          (user와 동일한 계층 구조)
 │    │    └── entity/       Product, ProductStatus(도메인 전용 enum)
 │    ├── order/            ──▶ 인-프로세스로 product · payment Service 직접 호출
 │    │    └── entity/       Order, OrderItem, OrderStatus(도메인 전용 enum)
 │    ├── payment/
 │    └── cart/             (Should-Have)
 └── global/                공통 인프라
      ├── config/           설정 (P6Spy, QueryDSL, Swagger ...)
      ├── security/         인증/인가 (JWT, CustomUserDetails ...)
      ├── exception/        공통 예외 (DomainException, DomainExceptionCode, GlobalExceptionHandler)
      ├── response/         공통 응답 포맷 (ApiResponse, PageResult)
      └── constants/        범용 enum/상수 (StatusType 등)
```

- 도메인은 `domain/` 하위에 두고, 각 도메인은 **controller · service · repository · entity · dto(request/response)** 계층으로 분리.
- product · order · payment · cart 는 **user와 동일한 계층 구조**로 추가.
- **배치 기준**: 도메인 모델(엔티티 상태 enum: `ProductStatus`/`OrderStatus`)은 해당 도메인 `entity/`에, 범용 enum(`StatusType`)·횡단 관심사(`DomainExceptionCode` = HTTP 상태 매핑)는 `global`에 둔다.
- 서비스 간 통신 = Service 메서드 직접 호출. 트랜잭션 = `@Transactional`.

---

## 3. 기능 우선순위 (MoSCoW)

| 분류 | 기능 |
|---|---|
| **Must** | 회원가입/로그인+JWT, 상품 CRUD/목록/상세, 주문 생성/조회, 주문 시 재고 차감, 권한(USER/ADMIN) |
| **Should** | 결제 처리, 장바구니, 상품 검색/페이징, 주문 취소(재고 복원), 예외/검증 표준화, 테스트 코드 |
| **Could** | Redis 캐싱, 쿠폰/할인, QueryDSL 동적검색, Swagger 문서, Docker Compose, 배송 상태 |

> 밀리면 기간이 아니라 **범위(Should→Could)를 줄인다.**

---

## 4. 주차별 계획

| 주차 | 목표 | 핵심 Task |
|---|---|---|
| **1주차** | 환경 + 인증 | 프로젝트 셋업, ERD/DB 설계, Security+JWT, 회원가입·로그인, 공통 예외/응답 포맷 |
| **2주차** | 상품 + 장바구니 | 상품 CRUD·목록·상세, 권한 분리(ADMIN 등록), 페이징/검색, 장바구니 |
| **3주차** | 주문 + 결제 (하이라이트) | 주문 생성/조회, 재고 차감 트랜잭션, 결제 처리, 주문 취소+재고 복원 |
| **4주차** | 마무리·버퍼 | 버그 수정, 테스트 코드, 리팩토링, README+ERD/API 문서, (남으면) Could 1개 |

**시간 배분 감각**: 주 15~20h ≈ 평일 2h×4 + 주말 4~6h. 각 주 마지막 날은 버퍼.

---

## 5. 1주차 상세 Task (시간 예측 포함)

> 보수적으로 예측. 합계 ≈ 16h → 주간 가용시간 내.

### 환경 셋업
- [ ] Gradle Spring Boot 3 프로젝트 생성 (Web, JPA, Security, Lombok, MySQL Driver, Validation) — `1h`
- [ ] `application.yml` MySQL 연결 + 프로파일(dev/prod) 분리 — `0.5h`
- [ ] 도메인별 패키지 구조 생성 (global/user/product/order/payment) — `0.5h`
- [ ] `.gitignore`, 로컬 MySQL DB/스키마 생성 — `0.5h`

### DB 설계
- [ ] ERD 설계 (user, product, order, order_item, payment) — `2h`
- [ ] User · Product · Order · OrderItem · Payment Entity 작성 — `2h`

### 공통(global) 기반
- [ ] 공통 응답 포맷(`ApiResponse`) + 공통 예외 핸들러(`@RestControllerAdvice`) — `1.5h`
- [ ] `BaseEntity`(createdAt/updatedAt, JPA Auditing) — `0.5h`

### 인증 (Must-Have)
- [ ] Spring Security 설정 (SecurityFilterChain, PasswordEncoder) — `1.5h`
- [ ] JWT 유틸 (토큰 생성·검증) + JWT 인증 필터 — `2.5h`
- [ ] 회원가입 API (`POST /users/signup`) — DTO·검증·중복체크·암호화 — `1.5h`
- [ ] 로그인 API (`POST /users/login`) — 인증·JWT 발급 — `1.5h`
- [ ] Postman으로 회원가입→로그인→토큰 발급 검증 — `0.5h`

**1주차 끝 데모**: Postman으로 회원가입 → 로그인 → JWT 발급 성공.

---
## 6. 진행 현황

- [ ] 1주차 — 환경 + 인증
- [ ] 2주차 — 상품 + 장바구니
- [ ] 3주차 — 주문 + 결제
- [ ] 4주차 — 마무리·버퍼
