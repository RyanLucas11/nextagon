import { router, usePathname } from 'expo-router';
import type { ReactNode } from 'react';
import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { colors, radius } from '@/constants/theme';

const tabs = [
  { label: 'Dashboard', path: '/Dashboard', icon: 'H' },
  { label: 'Treinos', path: '/Treinos', icon: 'T' },
  { label: 'Academia', path: '/Academia', icon: 'A' },
  { label: 'Profissionais', path: '/Profissionais', icon: 'P' },
  { label: 'Chat', path: '/Chat', icon: 'C' },
] as const;

type HeaderProps = {
  title?: string;
  subtitle?: string;
  children: ReactNode;
  admin?: boolean;
};

export function AppScreen({ title, subtitle, children, admin = false }: HeaderProps) {
  const pathname = usePathname();

  return (
    <SafeAreaView style={styles.safe} edges={['top']}>
      <View style={styles.header}>
        <Pressable style={styles.logo} onPress={() => router.replace('/Dashboard')}>
          <Text style={styles.logoIcon}>◆</Text>
          <Text style={styles.logoText}>Next Agon{admin ? ' ADMIN' : ''}</Text>
        </Pressable>
        <Pressable style={styles.avatar} onPress={() => router.push('/')}>
          <Text style={styles.avatarText}>JS</Text>
        </Pressable>
      </View>

      <ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>
        {title ? (
          <View style={styles.titleBlock}>
            <Text style={styles.title}>{title}</Text>
            {subtitle ? <Text style={styles.subtitle}>{subtitle}</Text> : null}
          </View>
        ) : null}
        {children}
      </ScrollView>

      <SafeAreaView edges={['bottom']} style={styles.bottomSafe}>
        <View style={styles.bottomNav}>
          {tabs.map((tab) => {
            const active = pathname === tab.path;
            return (
              <Pressable key={tab.path} onPress={() => router.push(tab.path)} style={styles.navItem}>
                <Text style={[styles.navIcon, active && styles.navActive]}>{tab.icon}</Text>
                <Text style={[styles.navLabel, active && styles.navActive]} numberOfLines={1}>
                  {tab.label}
                </Text>
                {active ? <View style={styles.navDot} /> : null}
              </Pressable>
            );
          })}
        </View>
      </SafeAreaView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safe: {
    flex: 1,
    backgroundColor: colors.bg,
  },
  header: {
    height: 58,
    paddingHorizontal: 16,
    borderBottomWidth: 1,
    borderBottomColor: 'rgba(26,44,66,0.9)',
    backgroundColor: 'rgba(10,17,32,0.96)',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  logo: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  logoIcon: {
    color: colors.accent,
    fontSize: 14,
  },
  logoText: {
    color: colors.title,
    fontSize: 17,
    fontWeight: '800',
  },
  avatar: {
    width: 36,
    height: 36,
    borderRadius: radius.pill,
    backgroundColor: colors.accent,
    borderWidth: 2,
    borderColor: 'rgba(26,107,222,0.4)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatarText: {
    color: colors.white,
    fontSize: 12,
    fontWeight: '800',
  },
  content: {
    padding: 16,
    paddingBottom: 92,
    gap: 16,
  },
  titleBlock: {
    gap: 4,
  },
  title: {
    color: colors.title,
    fontSize: 28,
    fontWeight: '800',
  },
  subtitle: {
    color: colors.title2,
    fontSize: 14,
  },
  bottomSafe: {
    position: 'absolute',
    left: 0,
    right: 0,
    bottom: 0,
    backgroundColor: 'rgba(10,17,32,0.97)',
  },
  bottomNav: {
    minHeight: 64,
    borderTopWidth: 1,
    borderTopColor: 'rgba(26,44,66,0.9)',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-around',
  },
  navItem: {
    flex: 1,
    height: 64,
    alignItems: 'center',
    justifyContent: 'center',
    gap: 3,
  },
  navIcon: {
    color: colors.text3,
    fontSize: 18,
    fontWeight: '800',
  },
  navLabel: {
    color: colors.text3,
    fontSize: 10,
    fontWeight: '600',
  },
  navActive: {
    color: colors.accent,
  },
  navDot: {
    width: 20,
    height: 3,
    borderRadius: 2,
    backgroundColor: colors.accent,
    position: 'absolute',
    bottom: 6,
  },
});

export default AppScreen;
