import { router } from 'expo-router';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { AppScreen } from '@/components/Header';
import { Card } from '@/components/Card';
import { Button } from '@/components/Button';
import { colors } from '@/constants/theme';

const stats = [
  { label: 'Treinos', value: '12', note: '3 nesta semana', route: '/Treinos' },
  { label: 'Carga media', value: '74%', note: 'evolucao constante', route: '/Academia' },
  { label: 'Avaliacoes', value: '4', note: 'uma pendente', route: '/Profissionais' },
  { label: 'Mensagens', value: '8', note: '2 novas', route: '/Chat' },
] as const;

export default function Dashboard() {
  return (
    <AppScreen title="Bem-vindo, Joao" subtitle="Saude & Performance">
      <View style={styles.statsGrid}>
        {stats.map((item) => (
          <Pressable key={item.label} style={styles.statPressable} onPress={() => router.push(item.route)}>
            <Card style={styles.stat}>
              <Text style={styles.statLabel}>{item.label}</Text>
              <Text style={styles.statValue}>{item.value}</Text>
              <Text style={styles.statNote}>{item.note}</Text>
            </Card>
          </Pressable>
        ))}
      </View>

      <Card>
        <Text style={styles.sectionTitle}>Alteracoes do Sistema</Text>
        <View style={styles.timeline}>
          <Timeline title="Novo treino recomendado" body="Plano de forca ajustado para membros inferiores." />
          <Timeline title="Avaliacao atualizada" body="Profissional adicionou observacoes ao seu perfil." />
          <Timeline title="Agenda aberta" body="Horarios disponiveis para consulta nesta semana." />
        </View>
      </Card>

      <Card>
        <Text style={styles.sectionTitle}>Meu Perfil</Text>
        <Text style={styles.body}>Atleta em acompanhamento, foco em hipertrofia, mobilidade e recuperacao.</Text>
        <Button title="Ver detalhes" variant="dark" onPress={() => router.push('/Profissionais')} />
      </Card>
    </AppScreen>
  );
}

function Timeline({ title, body }: { title: string; body: string }) {
  return (
    <View style={styles.timelineItem}>
      <View style={styles.dot} />
      <View style={styles.timelineText}>
        <Text style={styles.itemTitle}>{title}</Text>
        <Text style={styles.body}>{body}</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  statsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 12,
  },
  statPressable: {
    width: '47.8%',
  },
  stat: {
    minHeight: 128,
    padding: 18,
  },
  statLabel: {
    color: colors.text3,
    fontSize: 10,
    fontWeight: '800',
    letterSpacing: 1,
    textTransform: 'uppercase',
  },
  statValue: {
    color: colors.text,
    fontSize: 38,
    fontWeight: '800',
    marginTop: 12,
  },
  statNote: {
    color: colors.text2,
    fontSize: 12,
  },
  sectionTitle: {
    color: colors.title,
    fontSize: 18,
    fontWeight: '800',
    marginBottom: 14,
  },
  timeline: {
    gap: 16,
  },
  timelineItem: {
    flexDirection: 'row',
    gap: 12,
  },
  dot: {
    width: 10,
    height: 10,
    borderRadius: 5,
    backgroundColor: colors.accent,
    marginTop: 5,
  },
  timelineText: {
    flex: 1,
  },
  itemTitle: {
    color: colors.text,
    fontSize: 14,
    fontWeight: '700',
  },
  body: {
    color: colors.text2,
    fontSize: 13,
    lineHeight: 19,
    marginBottom: 14,
  },
});
