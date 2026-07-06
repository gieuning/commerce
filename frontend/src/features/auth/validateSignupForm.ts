import {
  validateEmailField,
  validateNameField,
  validateNewPassword,
  validatePhoneField,
} from "@/features/auth/authValidation";

export interface SignupFormValues {
  email: string;
  name: string;
  password: string;
  phoneNumber: string;
}

export interface SignupFieldErrors {
  email?: string;
  name?: string;
  password?: string;
  phoneNumber?: string;
}

// 서버(SignupRequest) 제약과 동일한 규칙을 즉시 피드백용으로 프론트에서도 검증한다.
export const validateSignupForm = (values: SignupFormValues): SignupFieldErrors => {
  const errors: SignupFieldErrors = {};

  const emailError = validateEmailField(values.email);
  if (emailError) {
    errors.email = emailError;
  }

  const nameError = validateNameField(values.name);
  if (nameError) {
    errors.name = nameError;
  }

  const passwordError = validateNewPassword(values.password);
  if (passwordError) {
    errors.password = passwordError;
  }

  const phoneError = validatePhoneField(values.phoneNumber);
  if (phoneError) {
    errors.phoneNumber = phoneError;
  }

  return errors;
};
