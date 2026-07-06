-- 결제 화면에서 회원별 주문번호를 보여주기 위해 주문의 user_order_no를 결제에 비정규화 저장.
-- (order_no는 주문 생성 시 확정되어 변하지 않으므로 안전하게 비정규화 가능. 재시도/취소로 여러 결제가
--  같은 주문을 참조할 수 있어 UNIQUE는 걸지 않는다.)
ALTER TABLE payments ADD COLUMN order_no BIGINT NULL;

UPDATE payments p
JOIN orders o ON p.order_id = o.id
SET p.order_no = o.user_order_no;

ALTER TABLE payments MODIFY COLUMN order_no BIGINT NOT NULL;
