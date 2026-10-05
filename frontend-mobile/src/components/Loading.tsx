import { ActivityIndicator, StyleSheet, Text, View } from 'react-native';

import { useTheme } from '@/contexts/ThemeContext';

type LoadingProps = {
  label?: string;
};

export function Loading({ label = 'Carregando' }: LoadingProps) {
  const { colors } = useTheme();
  const styles = createStyles(colors);
  return (
    <View style={styles.container}>
      <ActivityIndicator color={colors.accent} />
      <Text style={styles.label}>{label}</Text>
    </View>
  );
}

const createStyles = (colors: ReturnType<typeof useTheme>['colors']) => StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    gap: 12,
    backgroundColor: colors.bg,
  },
  label: {
    color: colors.text2,
    fontSize: 12,
    fontWeight: '700',
    letterSpacing: 1,
    textTransform: 'uppercase',
  },
});

export default Loading;
