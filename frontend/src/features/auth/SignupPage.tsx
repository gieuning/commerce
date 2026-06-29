import { type FormEvent, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Button } from "@/components/Button";
import { ErrorState } from "@/components/ErrorState";
import { Input } from "@/components/Input";
import { PageHeader } from "@/components/PageHeader";
import { ROUTES } from "@/constants/routes";
import { useAsyncAction } from "@/hooks/useAsyncAction";
import { useAuth } from "@/hooks/useAuth";

export const SignupPage = () => {
  const navigate = useNavigate();
  const { signup } = useAuth();
  const { errorMessage, isLoading, runAsyncAction } = useAsyncAction();
  const [email, setEmail] = useState("");
  const [name, setName] = useState("");
  const [password, setPassword] = useState("");
  const [phoneNumber, setPhoneNumber] = useState("");

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const signupResult = await runAsyncAction(() =>
      signup({
        email,
        name,
        password,
        phoneNumber: phoneNumber || undefined,
      }),
    );

    if (signupResult !== null) {
      void navigate(ROUTES.LOGIN);
    }
  };

  return (
    <section className="mx-auto max-w-md">
      <PageHeader title="회원가입" description="커머스 서비스를 사용할 계정을 만듭니다." />
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
          autoComplete="name"
          label="이름"
          name="name"
          onChange={(event) => setName(event.target.value)}
          required
          value={name}
        />
        <Input
          autoComplete="new-password"
          label="비밀번호"
          name="password"
          onChange={(event) => setPassword(event.target.value)}
          required
          type="password"
          value={password}
        />
        <Input
          autoComplete="tel"
          label="휴대폰 번호"
          name="phoneNumber"
          onChange={(event) => setPhoneNumber(event.target.value)}
          value={phoneNumber}
        />
        {errorMessage ? <ErrorState message={errorMessage} /> : null}
        <Button disabled={isLoading} type="submit">
          {isLoading ? "가입 중" : "회원가입"}
        </Button>
      </form>
      <p className="mt-5 text-center text-sm text-ink-soft">
        이미 계정이 있나요?{" "}
        <Link className="font-semibold text-primary" to={ROUTES.LOGIN}>
          로그인
        </Link>
      </p>
    </section>
  );
};
