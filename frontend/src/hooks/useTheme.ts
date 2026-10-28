import { KEY } from '@/constants/components';
import type { Theme } from '@/types/components';
import { useEffect, useState } from 'react';

function apply(theme: Theme) {
  if (theme === 'light') {
    document.documentElement.dataset.theme = 'light';
  } else {
    delete document.documentElement.dataset.theme;
  }
  localStorage.setItem(KEY, theme);
}

export function useTheme() {
  const [theme, setThemeState] = useState<Theme>(
    () => (localStorage.getItem(KEY) as Theme | null) ?? 'dark',
  );

  useEffect(() => { apply(theme); }, [theme]);

  return { theme, setTheme: (t: Theme) => setThemeState(t) };
}
