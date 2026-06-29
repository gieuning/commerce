import type { Config } from "tailwindcss";

export default {
  content: ["./index.html", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        primary: "#6366F1",
        "primary-hover": "#4F46E5",
        secondary: "#20970B",
        neutral: "#9C9C9C",
        background: "#FAFAFA",
        surface: "#FFFFFF",
        ink: "#0A0A0A",
        "ink-soft": "#6B6B6B",
        line: "#E8E8EC",
        success: "#10B981",
        warning: "#F59E0B",
        error: "#EF4444",
      },
      borderRadius: {
        card: "8px",
        btn: "6px",
      },
      fontFamily: {
        sans: ["Inter", "Pretendard", "system-ui", "sans-serif"],
        display: ["Inter", "Pretendard", "system-ui", "sans-serif"],
      },
    },
  },
  plugins: [],
} satisfies Config;
