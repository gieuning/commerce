import { Button } from "@/components/Button";
import type { PageResult } from "@/types/api";

interface PaginationProps<TItem> {
  page: PageResult<TItem>;
  onPageChange: (pageNumber: number) => void;
}

export const Pagination = <TItem,>({ onPageChange, page }: PaginationProps<TItem>) => {
  if (page.totalPages <= 1) {
    return null;
  }

  return (
    <nav
      aria-label="페이지"
      className="flex flex-wrap items-center justify-center gap-3"
    >
      <Button
        disabled={page.first}
        onClick={() => onPageChange(page.pageNumber - 1)}
        size="sm"
        variant="secondary"
      >
        이전
      </Button>
      <span className="text-sm font-medium text-ink-soft">
        {page.pageNumber + 1} / {page.totalPages}
      </span>
      <Button
        disabled={page.last}
        onClick={() => onPageChange(page.pageNumber + 1)}
        size="sm"
        variant="secondary"
      >
        다음
      </Button>
    </nav>
  );
};
