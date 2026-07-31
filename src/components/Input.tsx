import { StyleSheet, Text, TextInput, TextInputProps, View } from 'react-native';

import { colors, radius } from '@/constants/theme';

type InputProps = TextInputProps & {
  icon?: string;
};

export function Input({ icon, style, placeholderTextColor, ...props }: InputProps) {
  return (
    <View style={styles.wrap}>
      {icon ? <Text style={styles.icon}>{icon}</Text> : null}
      <TextInput
        {...props}
        placeholderTextColor={placeholderTextColor ?? 'rgba(255,255,255,0.2)'}
        style={[styles.input, icon ? styles.withIcon : null, style]}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  wrap: {
    position: 'relative',
    justifyContent: 'center',
  },
  icon: {
    position: 'absolute',
    left: 14,
    zIndex: 1,
    color: 'rgba(255,255,255,0.35)',
    fontSize: 14,
  },
  input: {
    minHeight: 46,
    width: '100%',
    borderRadius: radius.sm,
    borderWidth: 1,
    borderColor: colors.borderSoft,
    backgroundColor: 'rgba(255,255,255,0.04)',
    color: 'rgba(255,255,255,0.88)',
    paddingHorizontal: 14,
    fontSize: 13,
  },
  withIcon: {
    paddingLeft: 40,
  },
});

export default Input;
