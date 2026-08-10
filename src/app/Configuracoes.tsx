import { router } from 'expo-router';
import { useState } from 'react';
import { Alert, Pressable, StyleSheet, Text, View } from 'react-native';
import { AppScreen } from '@/components/Header';
import { Button } from '@/components/Button';
import { Card } from '@/components/Card';
import { Input } from '@/components/Input';
import { useTheme } from '@/contexts/ThemeContext';
import { AuthService } from '@/services/chat';

type Form = 'password' | 'email' | null;
const otherItems = ['Notificações', 'Privacidade', 'Segurança', 'Ajuda', 'Sobre o aplicativo'];

export default function Settings() {
  const { theme, setTheme, colors } = useTheme();
  const styles = createStyles(colors);
  const [form, setForm] = useState<Form>(null);
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [email, setEmail] = useState(AuthService.active()?.email ?? '');
  const user = AuthService.active();
  if (!user) { router.replace('/'); return null; }
  const closeForm = () => { setForm(null); setCurrentPassword(''); setNewPassword(''); };
  const savePassword = () => {
    try { AuthService.updatePassword(user.id, currentPassword, newPassword); closeForm(); Alert.alert('Senha alterada', 'Sua nova senha já está ativa.'); }
    catch (error) { Alert.alert('Não foi possível alterar a senha', error instanceof Error ? error.message : 'Tente novamente.'); }
  };
  const saveEmail = () => {
    try { AuthService.updateEmail(user.id, email, currentPassword); closeForm(); Alert.alert('E-mail alterado', 'Seu novo e-mail foi salvo.'); }
    catch (error) { Alert.alert('Não foi possível alterar o e-mail', error instanceof Error ? error.message : 'Tente novamente.'); }
  };
  const showInfo = (item: string) => Alert.alert(item, item === 'Notificações' ? 'As notificações da comunidade estão ativadas.' : 'Esta preferência está disponível e usa as configurações padrão do aplicativo.');
  return <AppScreen title="Configurações" subtitle="Conta e preferências"><Card><Text style={styles.label}>APARÊNCIA</Text><Text style={styles.title}>Tema</Text><View style={styles.options}>{(['light', 'dark'] as const).map((item) => <Pressable key={item} accessibilityRole="button" accessibilityState={{ selected: theme === item }} onPress={() => setTheme(item)} style={[styles.option, theme === item && styles.active]}><Text style={[styles.optionText, theme === item && styles.activeText]}>{item === 'light' ? 'Claro' : 'Escuro'}</Text></Pressable>)}</View><Text style={styles.hint}>O tema escolhido é aplicado a todo o aplicativo e salvo para os próximos acessos.</Text></Card><Card><SettingRow label="Alterar senha" onPress={() => setForm('password')} styles={styles}/><SettingRow label="Alterar e-mail" onPress={() => setForm('email')} styles={styles}/>{otherItems.map((item) => <SettingRow key={item} label={item} onPress={() => showInfo(item)} styles={styles}/>)}</Card>{form ? <Card><Text style={styles.formTitle}>{form === 'password' ? 'Alterar senha' : 'Alterar e-mail'}</Text>{form === 'email' ? <Input value={email} onChangeText={setEmail} placeholder="Novo e-mail" keyboardType="email-address" autoCapitalize="none" /> : <Input value={newPassword} onChangeText={setNewPassword} placeholder="Nova senha (mínimo 6 caracteres)" secureTextEntry /> }<Input value={currentPassword} onChangeText={setCurrentPassword} placeholder="Senha atual" secureTextEntry /><View style={styles.formActions}><Button title="Cancelar" variant="dark" onPress={closeForm} style={styles.actionButton}/><Button title="Salvar" onPress={form === 'password' ? savePassword : saveEmail} style={styles.actionButton}/></View></Card> : null}<Pressable accessibilityRole="button" style={styles.logout} onPress={() => { AuthService.logout(); router.replace('/'); }}><Text style={styles.logoutText}>Sair da conta</Text></Pressable></AppScreen>;
}

function SettingRow({ label, onPress, styles }: { label: string; onPress: () => void; styles: ReturnType<typeof createStyles> }) { return <Pressable accessibilityRole="button" style={styles.row} onPress={onPress}><Text style={styles.rowText}>{label}</Text><Text style={styles.chevron}>›</Text></Pressable>; }
const createStyles = (colors: ReturnType<typeof useTheme>['colors']) => StyleSheet.create({label:{color:colors.text3,fontWeight:'800',fontSize:10,letterSpacing:1},title:{color:colors.text,fontSize:17,fontWeight:'800',marginTop:8},options:{flexDirection:'row',gap:7,marginTop:14},option:{flex:1,paddingVertical:10,borderWidth:1,borderColor:colors.border,borderRadius:9,alignItems:'center'},active:{borderColor:colors.accent,backgroundColor:colors.accentDim},optionText:{color:colors.text2,fontSize:12,fontWeight:'700'},activeText:{color:colors.accent},hint:{color:colors.text3,fontSize:11,lineHeight:16,marginTop:13},row:{minHeight:49,borderBottomWidth:1,borderColor:colors.border,flexDirection:'row',alignItems:'center',justifyContent:'space-between'},rowText:{color:colors.text,fontWeight:'600'},chevron:{color:colors.text3,fontSize:24},formTitle:{color:colors.text,fontSize:17,fontWeight:'800',marginBottom:14},formActions:{flexDirection:'row',gap:10,marginTop:12},actionButton:{flex:1},logout:{borderWidth:1,borderColor:colors.red,borderRadius:11,padding:14,alignItems:'center'},logoutText:{color:colors.red,fontWeight:'800'}});
