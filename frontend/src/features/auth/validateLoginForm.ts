import {
  validateEmailField,
  validateRequiredPassword,
} from "@/features/auth/authValidation";

export interface LoginFormValues {
  email: string;
  password: string;
}

export interface LoginFieldErrors {
  email?: string;
  password?: string;
}

export const validateLoginForm = (values: LoginFormValues): LoginFieldErrors => {
  const errors: LoginFieldErrors = {};

  const emailError = validateEmailField(values.email);
  if (emailError) {
    errors.email = emailError;
  }

  const passwordError = validateRequiredPassword(values.password);
  if (passwordError) {
    errors.password = passwordError;
  }

  return errors;
};
