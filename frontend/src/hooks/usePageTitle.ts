import { useEffect } from 'react';

export function usePageTitle(title: string) {
  useEffect(() => {
    document.title = title ? `${title} · Fendly` : 'Fendly';
    return () => { document.title = 'Fendly'; };
  }, [title]);
}
