import { router } from 'expo-router';
import { useMemo, useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { Button } from '@/components/Button';
import { Card } from '@/components/Card';
import { Input } from '@/components/Input';
import { colors } from '@/constants/theme';

type Panel = 'login' | 'criar' | 'senha';
type Role = 'atleta' | 'profissional' | 'admin';

export default function LoginScreen() {
  const [panel, setPanel] = useState<Panel>('login');
  const [role, setRole] = useState<Role>('profissional');
  const [email, setEmail] = useState('profissional@nextagon.com');
  const [password, setPassword] = useState('123456');
  const [newPassword, setNewPassword] = useState('');
  const [message, setMessage] = useState('');
  const score = usePasswordScore(panel === 'senha' ? newPassword : password);

  function enter() {
    if (!email.trim() || password.length < 6) {
      setMessage('Informe e-mail e senha com no minimo 6 caracteres.');
      return;
    }

    setMessage('');
    if (role === 'admin') {
      router.replace('/Admin');
      return;
    }
    router.replace(role === 'profissional' ? '/Chat?perfil=profissional' : '/Dashboard');
  }

  function createAccount() {
    if (!email.trim() || password.length < 6) {
      setMessage('Preencha os dados para criar a conta.');
      return;
    }

    setMessage('Conta criada. Entre para continuar.');
    setPanel('login');
  }

  function changePassword() {
    if (newPassword.length < 6) {
      setMessage('A nova senha precisa ter no minimo 6 caracteres.');
      return;
    }

    setPassword(newPassword);
    setNewPassword('');
    setMessage('Senha atualizada. Entre com a nova senha.');
    setPanel('login');
  }

  return (
    <SafeAreaView style={styles.screen}>
      <View style={styles.lightTop} />
      <View style={styles.lightBottom} />
      <Card login>
        <View style={styles.logo}>
          <Text style={styles.brand}>NEXT AGON</Text>
          <Text style={styles.tagline}>ATHLETICS</Text>
        </View>
        <View style={styles.divider} />

        <View style={styles.tabs}>
          <Tab label="Login" active={panel === 'login'} onPress={() => setPanel('login')} />
          <Tab label="Criar Conta" active={panel === 'criar'} onPress={() => setPanel('criar')} />
          <Tab label="Trocar Senha" active={panel === 'senha'} onPress={() => setPanel('senha')} />
        </View>

        {panel === 'login' ? (
          <View style={styles.panel}>
            <View style={styles.roles}>
              <RoleButton label="Atleta" icon="A" active={role === 'atleta'} onPress={() => setRole('atleta')} />
              <RoleButton label="Profissional" icon="P" active={role === 'profissional'} onPress={() => setRole('profissional')} />
              <RoleButton label="Admin" icon="S" active={role === 'admin'} onPress={() => setRole('admin')} />
            </View>
            <Input icon="@" value={email} onChangeText={setEmail} placeholder="E-mail" keyboardType="email-address" autoCapitalize="none" />
            <Input icon="*" value={password} onChangeText={setPassword} placeholder="Senha" secureTextEntry />
            <Button title="Entrar no treino" onPress={enter} style={styles.mainButton} />
            <Text style={styles.linkText}>
              Nao tem conta? <Text style={styles.link} onPress={() => setPanel('criar')}>Criar agora</Text>  ·  <Text style={styles.link} onPress={() => setPanel('senha')}>Esqueceu a senha?</Text>
            </Text>
          </View>
        ) : null}

        {panel === 'criar' ? (
          <View style={styles.panel}>
            <Input icon="o" placeholder="Nome completo" />
            <Input icon="@" value={email} onChangeText={setEmail} placeholder="E-mail" keyboardType="email-address" autoCapitalize="none" />
            <View style={styles.selectRow}>
              <RoleButton label="Atleta" icon="A" active={role === 'atleta'} onPress={() => setRole('atleta')} />
              <RoleButton label="Profissional" icon="P" active={role === 'profissional'} onPress={() => setRole('profissional')} />
            </View>
            <Input icon="*" value={password} onChangeText={setPassword} placeholder="Senha (min. 6 caracteres)" secureTextEntry />
            <Strength score={score} />
            <Input icon="*" placeholder="Confirmar senha" secureTextEntry />
            <Button title="Criar conta" onPress={createAccount} style={styles.mainButton} />
            <Text style={styles.linkText}>Ja tem conta? <Text style={styles.link} onPress={() => setPanel('login')}>Entrar</Text></Text>
          </View>
        ) : null}

        {panel === 'senha' ? (
          <View style={styles.panel}>
            <Input icon="@" value={email} onChangeText={setEmail} placeholder="E-mail da conta" keyboardType="email-address" autoCapitalize="none" />
            <Input icon="*" placeholder="Senha atual" secureTextEntry />
            <Input icon="#" value={newPassword} onChangeText={setNewPassword} placeholder="Nova senha" secureTextEntry />
            <Strength score={score} />
            <Input icon="#" placeholder="Confirmar nova senha" secureTextEntry />
            <Button title="Salvar nova senha" onPress={changePassword} style={styles.mainButton} />
            <Text style={styles.linkText}>Lembrou a senha? <Text style={styles.link} onPress={() => setPanel('login')}>Voltar ao login</Text></Text>
          </View>
        ) : null}

        {message ? <Text style={styles.message}>{message}</Text> : null}
        <Text style={styles.footer}>Next Agon v1.0 · Saude & Performance</Text>
      </Card>
    </SafeAreaView>
  );
}

function usePasswordScore(value: string) {
  return useMemo(() => {
    return [
      value.length >= 6,
      /\d/.test(value),
      /[A-Z]/.test(value),
      /[^A-Za-z0-9]/.test(value),
    ].filter(Boolean).length;
  }, [value]);
}

function Tab({ label, active, onPress }: { label: string; active: boolean; onPress: () => void }) {
  return (
    <Pressable onPress={onPress} style={[styles.tab, active && styles.tabActive]}>
      <Text style={[styles.tabText, active && styles.tabTextActive]}>{label}</Text>
    </Pressable>
  );
}

function RoleButton({ label, icon, active, onPress }: { label: string; icon: string; active: boolean; onPress: () => void }) {
  return (
    <Pressable onPress={onPress} style={[styles.role, active && styles.roleActive]}>
      <Text style={[styles.roleIcon, active && styles.roleTextActive]}>{icon}</Text>
      <Text style={[styles.roleText, active && styles.roleTextActive]}>{label}</Text>
    </Pressable>
  );
}

function Strength({ score }: { score: number }) {
  const labels = ['-', 'Fraca', 'Ok', 'Boa', 'Forte'];
  return (
    <View>
      <View style={styles.strengthBar}>
        <View style={[styles.strengthFill, { width: `${score * 25}%` }]} />
      </View>
      <Text style={styles.strengthLabel}>{labels[score]}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  screen: {
    flex: 1,
    backgroundColor: colors.bgDeep,
    alignItems: 'center',
    justifyContent: 'center',
    padding: 20,
  },
  lightTop: {
    position: 'absolute',
    top: -80,
    width: 520,
    height: 260,
    borderRadius: 260,
    backgroundColor: 'rgba(212,247,0,0.04)',
  },
  lightBottom: {
    position: 'absolute',
    bottom: -80,
    width: 380,
    height: 210,
    borderRadius: 210,
    backgroundColor: 'rgba(26,107,222,0.08)',
  },
  logo: {
    textAlign: 'center',
    alignItems: 'center',
    marginBottom: 20,
  },
  brand: {
    color: colors.yellow,
    fontSize: 40,
    fontWeight: '900',
    letterSpacing: 5,
    lineHeight: 44,
  },
  tagline: {
    color: 'rgba(255,255,255,0.3)',
    fontSize: 10,
    fontWeight: '600',
    letterSpacing: 4,
    textTransform: 'uppercase',
  },
  divider: {
    height: 1,
    backgroundColor: 'rgba(212,247,0,0.25)',
    marginBottom: 24,
  },
  tabs: {
    flexDirection: 'row',
    borderBottomWidth: 1,
    borderBottomColor: 'rgba(255,255,255,0.07)',
    marginBottom: 24,
    gap: 18,
  },
  tab: {
    paddingBottom: 9,
    borderBottomWidth: 2,
    borderBottomColor: 'transparent',
  },
  tabActive: {
    borderBottomColor: colors.yellow,
  },
  tabText: {
    color: 'rgba(255,255,255,0.24)',
    fontSize: 10,
    fontWeight: '800',
    letterSpacing: 1.1,
    textTransform: 'uppercase',
  },
  tabTextActive: {
    color: colors.yellow,
  },
  panel: {
    gap: 12,
  },
  roles: {
    flexDirection: 'row',
    gap: 6,
    marginBottom: 8,
  },
  selectRow: {
    flexDirection: 'row',
    gap: 6,
  },
  role: {
    flex: 1,
    minHeight: 64,
    borderRadius: 3,
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.07)',
    backgroundColor: 'rgba(255,255,255,0.03)',
    alignItems: 'center',
    justifyContent: 'center',
    padding: 8,
  },
  roleActive: {
    borderColor: 'rgba(212,247,0,0.5)',
    backgroundColor: 'rgba(212,247,0,0.08)',
  },
  roleIcon: {
    color: 'rgba(255,255,255,0.3)',
    fontSize: 18,
    marginBottom: 5,
    fontWeight: '900',
  },
  roleText: {
    color: 'rgba(255,255,255,0.3)',
    fontSize: 9,
    fontWeight: '800',
    letterSpacing: 0.8,
    textTransform: 'uppercase',
  },
  roleTextActive: {
    color: colors.yellow,
  },
  mainButton: {
    marginTop: 4,
  },
  strengthBar: {
    height: 2,
    borderRadius: 2,
    backgroundColor: 'rgba(255,255,255,0.07)',
    overflow: 'hidden',
  },
  strengthFill: {
    height: '100%',
    backgroundColor: colors.yellow,
  },
  strengthLabel: {
    color: 'rgba(255,255,255,0.28)',
    fontSize: 10,
    textAlign: 'right',
    marginTop: 4,
  },
  linkText: {
    color: 'rgba(255,255,255,0.2)',
    fontSize: 11,
    textAlign: 'center',
    marginTop: 6,
  },
  link: {
    color: 'rgba(212,247,0,0.75)',
    fontWeight: '700',
  },
  message: {
    color: colors.yellow,
    fontSize: 12,
    textAlign: 'center',
    marginTop: 14,
    lineHeight: 18,
  },
  footer: {
    color: 'rgba(255,255,255,0.12)',
    fontSize: 10,
    letterSpacing: 0.8,
    textAlign: 'center',
    textTransform: 'uppercase',
    marginTop: 28,
  },
});
