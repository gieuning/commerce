import { LogOut, ShoppingCart, UserRound } from "lucide-react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { ROUTES } from "@/constants/routes";
import { useAuth } from "@/hooks/useAuth";
import { Button } from "@/components/Button";

const NAV_ITEMS = [
  { label: "상품", to: ROUTES.PRODUCTS },
  { label: "장바구니", to: ROUTES.CART },
  { label: "주문", to: ROUTES.ORDERS },
] as const;

export const AppLayout = () => {
  const { isAdmin, isAuthenticated, logout, user } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    void navigate(ROUTES.PRODUCTS);
  };

  return (
    <div className="min-h-screen bg-background text-ink">
      <header className="border-b border-line bg-surface">
        <div className="mx-auto flex max-w-6xl flex-col gap-4 px-4 py-4 md:flex-row md:items-center md:justify-between">
          <NavLink className="text-xl font-bold text-ink" to={ROUTES.PRODUCTS}>
            Commerce
          </NavLink>
          <nav aria-label="주요 메뉴" className="flex flex-wrap items-center gap-2">
            {NAV_ITEMS.map((item) => (
              <NavLink
                className={({ isActive }) =>
                  `rounded-btn px-3 py-2 text-sm font-semibold ${
                    isActive ? "bg-primary text-white" : "text-ink-soft hover:bg-line/70"
                  }`
                }
                key={item.to}
                to={item.to}
              >
                {item.label}
              </NavLink>
            ))}
            {isAdmin ? (
              <NavLink
                className={({ isActive }) =>
                  `rounded-btn px-3 py-2 text-sm font-semibold ${
                    isActive ? "bg-primary text-white" : "text-ink-soft hover:bg-line/70"
                  }`
                }
                to={ROUTES.ADMIN_PRODUCTS}
              >
                관리자
              </NavLink>
            ) : null}
          </nav>
          <div className="flex items-center gap-2">
            {isAuthenticated ? (
              <>
                <span className="hidden items-center gap-2 text-sm text-ink-soft sm:flex">
                  <UserRound size={16} />
                  {user?.name}
                </span>
                <Button icon={<LogOut size={16} />} onClick={handleLogout} variant="ghost">
                  로그아웃
                </Button>
              </>
            ) : (
              <>
                <Button onClick={() => void navigate(ROUTES.LOGIN)} variant="secondary">
                  로그인
                </Button>
                <Button onClick={() => void navigate(ROUTES.SIGNUP)}>회원가입</Button>
              </>
            )}
            <Button
              aria-label="장바구니로 이동"
              icon={<ShoppingCart size={18} />}
              onClick={() => void navigate(ROUTES.CART)}
              variant="ghost"
            />
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-4 py-8">
        <Outlet />
      </main>
    </div>
  );
};
