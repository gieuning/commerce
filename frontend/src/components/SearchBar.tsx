import { Search } from "lucide-react";
import { type FormEvent } from "react";
import { MESSAGES } from "@/constants/messages";

interface SearchBarProps {
  keyword: string;
  onKeywordChange: (keyword: string) => void;
  onSearch: () => void;
}

export const SearchBar = ({ keyword, onKeywordChange, onSearch }: SearchBarProps) => {
  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    onSearch();
  };

  return (
    <form className="relative" noValidate onSubmit={handleSubmit} role="search">
      <Search
        aria-hidden
        className="pointer-events-none absolute left-4 top-1/2 -translate-y-1/2 text-neutral"
        size={18}
      />
      <input
        aria-label={MESSAGES.PRODUCT.SEARCH_LABEL}
        className="h-12 w-full rounded-full border border-line bg-surface pl-11 pr-28 text-sm text-ink outline-none transition placeholder:text-neutral focus:border-primary focus:ring-2 focus:ring-primary/20"
        name="keyword"
        onChange={(event) => onKeywordChange(event.target.value)}
        placeholder={MESSAGES.PRODUCT.SEARCH_PLACEHOLDER}
        type="search"
        value={keyword}
      />
      <button
        className="absolute right-1.5 top-1/2 h-9 -translate-y-1/2 rounded-full bg-primary px-5 text-sm font-semibold text-white transition hover:bg-primary-hover focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-2"
        type="submit"
      >
        {MESSAGES.PRODUCT.SEARCH_SUBMIT}
      </button>
    </form>
  );
};
