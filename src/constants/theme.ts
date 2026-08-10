export type ThemeName = 'light' | 'dark';

const darkColors = {
  accent: '#1a6bde',
  accent2: '#1558c0',
  accentDim: 'rgba(26, 107, 222, 0.14)',
  yellow: '#d4f700',
  yellow2: '#c8e800',
  title: '#e8ff00',
  title2: '#c8e000',
  bgDeep: '#0d0d0d',
  bg: '#0a1120',
  card: '#111927',
  card2: '#19253a',
  panel: '#1c1c1c',
  border: '#1a2c42',
  borderSoft: 'rgba(255,255,255,0.08)',
  text: '#f1f5f9',
  text2: '#7a90aa',
  text3: '#3d5068',
  green: '#34d399',
  blue: '#60a5fa',
  red: '#f87171',
  black: '#000000',
  white: '#ffffff',
};

const lightColors = {
  ...darkColors,
  accent: '#1558c0',
  accent2: '#0f469f',
  accentDim: 'rgba(21, 88, 192, 0.12)',
  bgDeep: '#f5f7fb',
  bg: '#f5f7fb',
  card: '#ffffff',
  card2: '#eaf0f8',
  panel: '#ffffff',
  border: '#d7e0ec',
  borderSoft: 'rgba(15, 38, 68, 0.16)',
  text: '#152235',
  text2: '#52657d',
  text3: '#71839a',
};

// The exported object keeps legacy StyleSheet consumers compatible. New screens
// read the active palette through useTheme so changes apply immediately.
export const colors = { ...darkColors };
export const palettes = { light: lightColors, dark: darkColors };

export const spacing = {
  xs: 4,
  sm: 8,
  md: 12,
  lg: 16,
  xl: 24,
  xxl: 32,
};

export const radius = {
  sm: 3,
  md: 10,
  lg: 16,
  pill: 999,
};
