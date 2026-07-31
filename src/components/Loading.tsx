import { ActivityIndicator, StyleSheet, Text, View } from 'react-native';

import { colors } from '@/constants/theme';

type LoadingProps = {
  label?: string;
};

export function Loading({ label = 'Carregando' }: LoadingProps) {
  return (
    <View style={styles.container}>
      <ActivityIndicator color={colors.yellow} />
      <Text style={styles.label}>{label}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
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
