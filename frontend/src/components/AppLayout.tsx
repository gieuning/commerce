import { LogOut, ShoppingCart, UserRound } from "lucide-react";
import { useEffect, useState } from "react";
import { NavLink, Outlet, useNavigate, useSearchParams } from "react-router-dom";
import { Button } from "@/components/Button";
import { SearchBar } from "@/components/SearchBar";
import { ROUTES } from "@/constants/routes";
import { useAuth } from "@/hooks/useAuth";
import { cn } from "@/utils/cn";

const NAV_ITEMS = [
  { label: "상품", to: ROUTES.PRODUCTS },
  { label: "주문", to: ROUTES.ORDERS },
] as const;

const ICON_LINK_CLASS =
  "grid h-10 w-10 place-items-center rounded-full text-ink-soft transition hover:bg-line/60 hover:text-ink";

const navLinkClass = ({ isActive }: { isActive: boolean }) =>
  cn(
    "border-b-2 px-1 py-3 text-sm font-semibold transition",
    isActive ? "border-primary text-primary" : "border-transparent text-ink-soft hover:text-ink",
  );

const authLinkClass = "font-semibold text-ink-soft transition hover:text-ink";

export const AppLayout = () => {
  const { isAdmin, isAuthenticated, logout, user } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [keyword, setKeyword] = useState(searchParams.get("keyword") ?? "");

  useEffect(() => {
    setKeyword(searchParams.get("keyword") ?? "");
  }, [searchParams]);

  const handleSearch = () => {
    const trimmedKeyword = keyword.trim();
    void navigate(
      trimmedKeyword ? `${ROUTES.PRODUCTS}?keyword=${encodeURIComponent(trimmedKeyword)}` : ROUTES.PRODUCTS,
    );
  };

  const handleLogout = () => {
    logout();
    void navigate(ROUTES.PRODUCTS);
  };

  return (
    <div className="min-h-screen bg-background text-ink">
      <header className="border-b border-line bg-surface">
        <div className="mx-auto flex max-w-6xl flex-wrap items-center gap-4 px-4 py-3">
          <NavLink className="shrink-0 text-2xl font-extrabold text-primary" to={ROUTES.PRODUCTS}>
            Commerce
          </NavLink>
          <div className="order-last w-full md:order-none md:mx-auto md:w-auto md:max-w-xl md:flex-1">
            <SearchBar keyword={keyword} onKeywordChange={setKeyword} onSearch={handleSearch} />
          </div>
          <div className="ml-auto flex shrink-0 items-center gap-1 md:ml-0">
            <NavLink
              aria-label={isAuthenticated ? "내 주문" : "로그인"}
              className={ICON_LINK_CLASS}
              to={isAuthenticated ? ROUTES.ORDERS : ROUTES.LOGIN}
            >
              <UserRound size={20} />
            </NavLink>
            <NavLink aria-label="장바구니" className={ICON_LINK_CLASS} to={ROUTES.CART}>
              <ShoppingCart size={20} />
            </NavLink>
          </div>
        </div>
        <div className="border-t border-line">
          <div className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-4">
            <nav aria-label="주요 메뉴" className="flex items-center gap-5">
              {NAV_ITEMS.map((item) => (
                <NavLink className={navLinkClass} key={item.to} to={item.to}>
                  {item.label}
                </NavLink>
              ))}
              {isAdmin ? (
                <NavLink className={navLinkClass} to={ROUTES.ADMIN_PRODUCTS}>
                  관리자
                </NavLink>
              ) : null}
            </nav>
            <div className="flex items-center gap-3 text-sm">
              {isAuthenticated ? (
                <>
                  <span className="hidden items-center gap-1 text-ink-soft sm:flex">
                    <UserRound size={14} />
                    {user?.name}
                  </span>
                  <Button icon={<LogOut size={14} />} onClick={handleLogout} size="sm" variant="ghost">
                    로그아웃
                  </Button>
                </>
              ) : (
                <>
                  <NavLink className={authLinkClass} to={ROUTES.LOGIN}>
                    로그인
                  </NavLink>
                  <span className="text-line">|</span>
                  <NavLink className={authLinkClass} to={ROUTES.SIGNUP}>
                    회원가입
                  </NavLink>
                </>
              )}
            </div>
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-4 py-8">
        <Outlet />
      </main>
    </div>
  );
};
