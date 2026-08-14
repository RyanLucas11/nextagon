import { useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';

import { Button } from '@/components/Button';
import { Card } from '@/components/Card';
import { AppScreen } from '@/components/Header';
import { colors } from '@/constants/theme';

const adminStats = [
  ['Usuarios', '148'],
  ['Profissionais', '26'],
  ['Treinos', '312'],
  ['Alertas', '5'],
];

const initialTasks = [
  'Aprovar cadastros profissionais',
  'Auditar mensagens reportadas',
  'Atualizar catalogo de exercicios',
];

export default function Admin() {
  const [tasks, setTasks] = useState(initialTasks.map((title) => ({ title, done: false })));

  function toggleTask(title: string) {
    setTasks((current) =>
      current.map((task) => (task.title === title ? { ...task, done: !task.done } : task)),
    );
  }

  function processQueue() {
    setTasks((current) => current.map((task) => ({ ...task, done: true })));
  }

  return (
    <AppScreen title="Painel Admin" subtitle="Next Agon ADMIN" admin>
      <View style={styles.grid}>
        {adminStats.map(([label, value]) => (
          <Card key={label} style={styles.stat}>
            <Text style={styles.statLabel}>{label}</Text>
            <Text style={styles.statValue}>{value}</Text>
          </Card>
        ))}
      </View>

      <Card>
        <Text style={styles.title}>Controle do Sistema</Text>
        {tasks.map((item) => (
          <View key={item.title} style={styles.task}>
            <View style={styles.taskCopy}>
              <Text style={styles.taskText}>{item.title}</Text>
              <Text style={[styles.taskStatus, item.done && styles.taskDone]}>
                {item.done ? 'Resolvido' : 'Pendente'}
              </Text>
            </View>
            <Button
              title={item.done ? 'Reabrir' : 'Resolver'}
              variant="dark"
              onPress={() => toggleTask(item.title)}
              style={styles.smallButton}
            />
          </View>
        ))}
        <Button title="Abrir fila admin" variant="dark" style={styles.button} onPress={processQueue} />
      </Card>
    </AppScreen>
  );
}

const styles = StyleSheet.create({
  grid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 12,
  },
  stat: {
    width: '47.8%',
  },
  statLabel: {
    color: colors.text3,
    fontSize: 10,
    fontWeight: '800',
    letterSpacing: 1,
    textTransform: 'uppercase',
  },
  statValue: {
    color: colors.title,
    fontSize: 34,
    fontWeight: '900',
    marginTop: 10,
  },
  title: {
    color: colors.title,
    fontSize: 18,
    fontWeight: '800',
    marginBottom: 14,
  },
  task: {
    borderTopWidth: 1,
    borderTopColor: colors.border,
    paddingVertical: 14,
    gap: 10,
  },
  taskCopy: {
    gap: 4,
  },
  taskText: {
    color: colors.text,
    fontSize: 14,
    fontWeight: '700',
  },
  taskStatus: {
    color: colors.red,
    fontSize: 11,
    textTransform: 'uppercase',
    letterSpacing: 0.8,
    fontWeight: '800',
  },
  taskDone: {
    color: colors.green,
  },
  smallButton: {
    minHeight: 42,
  },
  button: {
    marginTop: 10,
  },
});
