import { useState } from 'react';

export function usePagination<T>(items: T[], pageSize: number) {
  const [page, setPage] = useState(0);
  const totalPages = Math.max(1, Math.ceil(items.length / pageSize));
  const safePage = Math.min(page, totalPages - 1);

  const paginated = items.slice(safePage * pageSize, (safePage + 1) * pageSize);

  return {
    page: safePage,
    setPage,
    paginated,
    totalPages,
    total: items.length,
    from: items.length === 0 ? 0 : safePage * pageSize + 1,
    to: Math.min((safePage + 1) * pageSize, items.length),
  };
}
