import { createContext, type ReactNode, useContext, useState } from 'react';
import { Platform } from 'react-native';
import { palettes, type ThemeName } from '@/constants/theme';

const STORAGE_KEY = 'nextagon-theme';
type ThemeContextValue = { theme: ThemeName; setTheme: (theme: ThemeName) => void; colors: typeof palettes.dark };
const ThemeContext = createContext<ThemeContextValue | null>(null);

function savedTheme(): ThemeName {
  if (Platform.OS !== 'web' || typeof localStorage === 'undefined') return 'dark';
  return localStorage.getItem(STORAGE_KEY) === 'light' ? 'light' : 'dark';
}

export function ThemeProvider({ children }: { children: ReactNode }) {
  const [theme, updateTheme] = useState<ThemeName>(savedTheme);
  const setTheme = (nextTheme: ThemeName) => {
    updateTheme(nextTheme);
    if (Platform.OS === 'web' && typeof localStorage !== 'undefined') localStorage.setItem(STORAGE_KEY, nextTheme);
  };
  return <ThemeContext.Provider value={{ theme, setTheme, colors: palettes[theme] }}>{children}</ThemeContext.Provider>;
}

export function useTheme() {
  const value = useContext(ThemeContext);
  if (!value) throw new Error('useTheme deve ser usado dentro de ThemeProvider.');
  return value;
}
