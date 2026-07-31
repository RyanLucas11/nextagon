import type { ReactNode } from 'react';
import { StyleSheet, View, ViewStyle } from 'react-native';

import { colors, radius } from '@/constants/theme';

type CardProps = {
  children: ReactNode;
  style?: ViewStyle;
  login?: boolean;
};

export function Card({ children, style, login = false }: CardProps) {
  return <View style={[styles.card, login && styles.loginCard, style]}>{children}</View>;
}

const styles = StyleSheet.create({
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
    backgroundColor: 'rgba(14,14,14,0.96)',
    borderColor: 'rgba(212,247,0,0.14)',
    elevation: 12,
  },
});

export default Card;
