import type { ReactNode } from 'react';
import { StyleSheet, View, ViewStyle } from 'react-native';

import { radius } from '@/constants/theme';
import { useTheme } from '@/contexts/ThemeContext';

type CardProps = {
  children: ReactNode;
  style?: ViewStyle;
  login?: boolean;
};

export function Card({ children, style, login = false }: CardProps) {
  const { colors } = useTheme();
  const styles = createStyles(colors);
  return <View style={[styles.card, login && styles.loginCard, style]}>{children}</View>;
}

const createStyles = (colors: ReturnType<typeof useTheme>['colors']) => StyleSheet.create({
  card: {
    backgroundColor: colors.card,
    borderColor: colors.border,
    borderWidth: 1,
    borderRadius: radius.lg,
    padding: 20,
  },
  loginCard: {
    width: '100%',
    maxWidth: 400,
    borderRadius: 4,
    paddingHorizontal: 28,
    paddingTop: 38,
    paddingBottom: 30,
    backgroundColor: colors.card,
    borderColor: colors.border,
    elevation: 12,
  },
});

export default Card;
