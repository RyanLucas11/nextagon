import type { ReactNode } from 'react';
import { Pressable, StyleSheet, Text, ViewStyle } from 'react-native';

import { colors, radius } from '@/constants/theme';

type ButtonProps = {
  title: string;
  onPress?: () => void;
  variant?: 'primary' | 'dark';
  icon?: ReactNode;
  style?: ViewStyle;
  disabled?: boolean;
};

export function Button({ title, onPress, variant = 'primary', icon, style, disabled = false }: ButtonProps) {
  return (
    <Pressable
      disabled={disabled}
      onPress={disabled ? undefined : onPress}
      style={({ pressed }) => [
        styles.button,
        variant === 'dark' && styles.dark,
        pressed && styles.pressed,
        disabled && styles.disabled,
        style,
      ]}
    >
      {icon}
      <Text style={[styles.text, variant === 'dark' && styles.darkText]}>{title}</Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  button: {
    width: '100%',
    minHeight: 48,
    borderRadius: radius.sm,
    backgroundColor: colors.yellow,
    alignItems: 'center',
    justifyContent: 'center',
    flexDirection: 'row',
    gap: 8,
    paddingHorizontal: 14,
    paddingVertical: 14,
    elevation: 6,
  },
  dark: {
    backgroundColor: colors.card2,
    borderWidth: 1,
    borderColor: colors.border,
    elevation: 0,
  },
  pressed: {
    transform: [{ scale: 0.98 }],
    opacity: 0.9,
  },
  text: {
    color: colors.black,
    fontSize: 12,
    fontWeight: '800',
    letterSpacing: 1.6,
    textTransform: 'uppercase',
  },
  darkText: {
    color: colors.text,
    letterSpacing: 0.4,
  },
  disabled: {
    opacity: 0.45,
  },
});

export default Button;
