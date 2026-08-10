import { router, usePathname } from 'expo-router';
import type { ReactNode } from 'react';
import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { radius } from '@/constants/theme';
import { useTheme } from '@/contexts/ThemeContext';

const tabs = [
  { label: 'Feed', path: '/Dashboard', icon: '⌂' },
  { label: 'Comunidade', path: '/Profissionais', icon: '♧' },
  { label: 'Chat', path: '/Chat', icon: '◌' },
  { label: 'Perfil', path: '/Perfil', icon: '◉' },
] as const;
export function AppScreen({ title, subtitle, children }: { title?: string; subtitle?: string; children: ReactNode; admin?: boolean }) {
  const { colors } = useTheme();
  const styles = createStyles(colors);
  const pathname = usePathname();
  return <SafeAreaView style={styles.safe} edges={['top']}><View style={styles.header}>
    <Pressable style={styles.logo} onPress={() => router.replace('/Dashboard')}><Text style={styles.logoText}>NEXTAGON</Text><Text style={styles.logoSub}>COMUNIDADE</Text></Pressable>
    <Pressable style={styles.avatar} onPress={() => router.push('/Perfil')}><Text style={styles.avatarText}>EU</Text></Pressable>
  </View><ScrollView contentContainerStyle={styles.content} showsVerticalScrollIndicator={false}>{title ? <View><Text style={styles.title}>{title}</Text>{subtitle ? <Text style={styles.subtitle}>{subtitle}</Text> : null}</View> : null}{children}</ScrollView>
  <SafeAreaView edges={['bottom']} style={styles.bottomSafe}><View style={styles.bottomNav}>{tabs.map((tab) => <Pressable key={tab.path} onPress={() => router.replace(tab.path)} style={styles.navItem}><Text style={[styles.navIcon, pathname === tab.path && styles.navActive]}>{tab.icon}</Text><Text style={[styles.navLabel, pathname === tab.path && styles.navActive]}>{tab.label}</Text></Pressable>)}</View></SafeAreaView></SafeAreaView>;
}
const createStyles = (colors: ReturnType<typeof useTheme>['colors']) => StyleSheet.create({ safe:{flex:1,backgroundColor:colors.bg},header:{height:62,paddingHorizontal:18,borderBottomWidth:1,borderColor:colors.border,backgroundColor:colors.bg,flexDirection:'row',alignItems:'center',justifyContent:'space-between'},logo:{gap:1},logoText:{color:colors.text,fontSize:19,fontWeight:'900',letterSpacing:1},logoSub:{color:colors.accent,fontSize:9,fontWeight:'700',letterSpacing:2},avatar:{width:36,height:36,borderRadius:radius.pill,backgroundColor:colors.accent,alignItems:'center',justifyContent:'center'},avatarText:{color:colors.white,fontSize:10,fontWeight:'800'},content:{padding:18,paddingBottom:100,gap:16},title:{color:colors.text,fontSize:26,fontWeight:'800'},subtitle:{color:colors.text2,fontSize:14,marginTop:3},bottomSafe:{position:'absolute',left:0,right:0,bottom:0,backgroundColor:colors.bg},bottomNav:{minHeight:62,borderTopWidth:1,borderColor:colors.border,flexDirection:'row'},navItem:{flex:1,alignItems:'center',justifyContent:'center',gap:3},navIcon:{color:colors.text3,fontSize:19},navLabel:{color:colors.text3,fontSize:10,fontWeight:'700'},navActive:{color:colors.accent} });
export default AppScreen;
