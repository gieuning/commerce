import { type FormEvent, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { Button } from "@/components/Button";
import { ErrorState } from "@/components/ErrorState";
import { Input } from "@/components/Input";
import { PageHeader } from "@/components/PageHeader";
import { ROUTES } from "@/constants/routes";
import { useAsyncAction } from "@/hooks/useAsyncAction";
import { useAuth } from "@/hooks/useAuth";

interface RedirectLocation {
  pathname: string;
  search: string;
  hash: string;
}

export const LoginPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();
  const { errorMessage, isLoading, runAsyncAction } = useAsyncAction();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const loginResult = await runAsyncAction(() => login({ email, password }));

    if (loginResult !== null) {
      const locationState = location.state as { from?: RedirectLocation | string } | null;
      const from = locationState?.from;
      const redirectTo =
        typeof from === "string"
          ? from
          : from
            ? `${from.pathname}${from.search}${from.hash}`
            : ROUTES.PRODUCTS;

      void navigate(redirectTo, { replace: true });
    }
  };

  return (
    <section className="mx-auto max-w-md">
      <PageHeader title="로그인" description="주문과 장바구니를 사용하려면 로그인해 주세요." />
      <form className="mt-6 grid gap-4" onSubmit={handleSubmit}>
        <Input
          autoComplete="email"
          label="이메일"
          name="email"
          onChange={(event) => setEmail(event.target.value)}
          required
          type="email"
          value={email}
        />
        <Input
          autoComplete="current-password"
          label="비밀번호"
          name="password"
          onChange={(event) => setPassword(event.target.value)}
          required
          type="password"
          value={password}
        />
        {errorMessage ? <ErrorState message={errorMessage} /> : null}
        <Button disabled={isLoading} type="submit">
          {isLoading ? "로그인 중" : "로그인"}
        </Button>
      </form>
      <p className="mt-5 text-center text-sm text-ink-soft">
        아직 계정이 없나요?{" "}
        <Link className="font-semibold text-primary" to={ROUTES.SIGNUP}>
          회원가입
        </Link>
      </p>
    </section>
  );
};
