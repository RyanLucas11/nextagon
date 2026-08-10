import { StyleSheet, Text, TextInput, TextInputProps, View } from 'react-native';

import { radius } from '@/constants/theme';
import { useTheme } from '@/contexts/ThemeContext';

type InputProps = TextInputProps & {
  icon?: string;
};

export function Input({ icon, style, placeholderTextColor, ...props }: InputProps) {
  const { colors } = useTheme();
  const styles = createStyles(colors);
  return (
    <View style={styles.wrap}>
      {icon ? <Text style={styles.icon}>{icon}</Text> : null}
      <TextInput
        {...props}
        placeholderTextColor={placeholderTextColor ?? colors.text3}
        style={[styles.input, icon ? styles.withIcon : null, style]}
      />
    </View>
  );
}

const createStyles = (colors: ReturnType<typeof useTheme>['colors']) => StyleSheet.create({
  wrap: {
    position: 'relative',
    justifyContent: 'center',
  },
  icon: {
    position: 'absolute',
    left: 14,
    zIndex: 1,
    color: colors.text3,
    fontSize: 14,
  },
  input: {
    minHeight: 46,
    width: '100%',
    borderRadius: radius.sm,
    borderWidth: 1,
    borderColor: colors.borderSoft,
    backgroundColor: colors.bgDeep,
    color: colors.text,
    paddingHorizontal: 14,
    fontSize: 13,
  },
  withIcon: {
    paddingLeft: 40,
  },
});

export default Input;
