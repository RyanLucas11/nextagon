import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { ThemeProvider, useTheme } from '@/contexts/ThemeContext';

function LayoutContent() {
  const { theme } = useTheme();
  return <><StatusBar style={theme === 'dark' ? 'light' : 'dark'} /><Stack screenOptions={{ headerShown: false }} /></>;
}

export default function RootLayout() {
  return <ThemeProvider><LayoutContent /></ThemeProvider>;
}
