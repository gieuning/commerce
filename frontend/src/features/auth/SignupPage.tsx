import { type FormEvent, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { Button } from "@/components/Button";
import { ErrorState } from "@/components/ErrorState";
import { Input } from "@/components/Input";
import { PageHeader } from "@/components/PageHeader";
import { ROUTES } from "@/constants/routes";
import { useAsyncAction } from "@/hooks/useAsyncAction";
import { useAuth } from "@/hooks/useAuth";
import {
  sanitizeNameInput,
  sanitizePhoneInput,
} from "@/features/auth/authValidation";
import {
  type SignupFieldErrors,
  validateSignupForm,
} from "@/features/auth/validateSignupForm";

export const SignupPage = () => {
  const navigate = useNavigate();
  const { signup } = useAuth();
  const { errorMessage, isLoading, runAsyncAction } = useAsyncAction();
  const [email, setEmail] = useState("");
  const [name, setName] = useState("");
  const [password, setPassword] = useState("");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [fieldErrors, setFieldErrors] = useState<SignupFieldErrors>({});

  const clearFieldError = (field: keyof SignupFieldErrors) =>
    setFieldErrors((currentErrors) => ({ ...currentErrors, [field]: undefined }));

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    const validationErrors = validateSignupForm({ email, name, password, phoneNumber });
    setFieldErrors(validationErrors);

    if (Object.values(validationErrors).some(Boolean)) {
      return;
    }

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
      <form className="mt-6 grid gap-4" noValidate onSubmit={handleSubmit}>
        <Input
          autoComplete="email"
          errorMessage={fieldErrors.email}
          label="이메일"
          name="email"
          onChange={(event) => {
            setEmail(event.target.value);
            clearFieldError("email");
          }}
          required
          type="email"
          value={email}
        />
        <Input
          autoComplete="name"
          errorMessage={fieldErrors.name}
          label="이름"
          name="name"
          onChange={(event) => {
            setName(sanitizeNameInput(event.target.value));
            clearFieldError("name");
          }}
          required
          value={name}
        />
        <Input
          autoComplete="new-password"
          errorMessage={fieldErrors.password}
          label="비밀번호"
          name="password"
          onChange={(event) => {
            setPassword(event.target.value);
            clearFieldError("password");
          }}
          required
          type="password"
          value={password}
        />
        <Input
          autoComplete="tel"
          errorMessage={fieldErrors.phoneNumber}
          inputMode="numeric"
          label="휴대폰 번호"
          name="phoneNumber"
          onChange={(event) => {
            setPhoneNumber(sanitizePhoneInput(event.target.value));
            clearFieldError("phoneNumber");
          }}
          placeholder="'-' 없이 숫자만 입력"
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
