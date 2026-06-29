import type { ReactNode } from "react";

interface PageHeaderProps {
  title: string;
  description?: string;
  action?: ReactNode;
}

export const PageHeader = ({ action, description, title }: PageHeaderProps) => (
  <header className="flex flex-col gap-4 border-b border-line pb-6 md:flex-row md:items-end md:justify-between">
    <div>
      <h1 className="text-2xl font-bold text-ink md:text-3xl">{title}</h1>
      {description ? <p className="mt-2 text-sm leading-6 text-ink-soft">{description}</p> : null}
    </div>
    {action ? <div className="shrink-0">{action}</div> : null}
  </header>
);
