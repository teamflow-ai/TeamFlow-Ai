import { useCallback, useEffect, useState } from 'react';

export function useAsyncList(service, { size = 8, search = '', filters = {} } = {}) {
  const [page, setPage] = useState(0);
  const [data, setData] = useState({ content: [], totalPages: 1, totalElements: 0 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await service.list({ page, size, query: search, ...filters });
      setData(res);
    } catch (e) {
      setError(e);
    } finally {
      setLoading(false);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [service, page, size, search, JSON.stringify(filters)]);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => {
    setPage(0);
  }, [search, JSON.stringify(filters)]);

  return { ...data, page, setPage, loading, error, reload: load };
}
