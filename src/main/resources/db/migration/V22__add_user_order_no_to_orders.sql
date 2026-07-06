-- 유저별 주문 순번: 전역 PK(id)가 아니라 회원마다 1, 2, 3... 으로 매겨지는 번호.
ALTER TABLE orders ADD COLUMN user_order_no BIGINT NULL;

-- 기존 주문 백필: 회원별 생성순(id 오름차순)으로 순번 부여.
UPDATE orders o
JOIN (
    SELECT id, ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY id) AS rn
    FROM orders
) ranked ON o.id = ranked.id
SET o.user_order_no = ranked.rn;

ALTER TABLE orders MODIFY COLUMN user_order_no BIGINT NOT NULL;

-- 회원당 순번 유일성 보강(동시 생성 시 중복 방지).
ALTER TABLE orders ADD CONSTRAINT uq_orders_user_order_no UNIQUE (user_id, user_order_no);
