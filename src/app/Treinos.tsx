import type { DimensionValue } from 'react-native';
import { useState } from 'react';
import { StyleSheet, Text, View } from 'react-native';

import { Button } from '@/components/Button';
import { Card } from '@/components/Card';
import { AppScreen } from '@/components/Header';
import { colors } from '@/constants/theme';

type Workout = {
  name: string;
  focus: string;
  level: string;
  progress: DimensionValue;
  progressLabel: string;
};

const initialWorkouts: Workout[] = [
  { name: 'Forca A', focus: 'Peito, triceps e ombro', level: 'Intermediario', progress: '72%', progressLabel: '72%' },
  { name: 'Forca B', focus: 'Costas e biceps', level: 'Intermediario', progress: '48%', progressLabel: '48%' },
  { name: 'Mobilidade', focus: 'Quadril, tornozelo e coluna', level: 'Leve', progress: '90%', progressLabel: '90%' },
];

export default function Treinos() {
  const [workouts, setWorkouts] = useState(initialWorkouts);
  const [completed, setCompleted] = useState<Record<string, boolean>>({});

  function addWorkout() {
    const nextIndex = workouts.length + 1;
    setWorkouts((current) => [
      ...current,
      {
        name: `Treino ${nextIndex}`,
        focus: 'Novo plano personalizado',
        level: 'Novo',
        progress: '0%',
        progressLabel: '0%',
      },
    ]);
  }

  function toggleComplete(name: string) {
    setCompleted((current) => ({ ...current, [name]: !current[name] }));
  }

  return (
    <AppScreen title="Treinos" subtitle="Planejamento e evolucao">
      {workouts.map((workout) => (
        <Card key={workout.name}>
          <View style={styles.row}>
            <View style={styles.badge}>
              <Text style={styles.badgeText}>{workout.name.slice(-1)}</Text>
            </View>
            <View style={styles.info}>
              <Text style={styles.title}>{workout.name}</Text>
              <Text style={styles.body}>{workout.focus}</Text>
              <Text style={styles.meta}>{completed[workout.name] ? 'Concluido hoje' : workout.level}</Text>
            </View>
            <Text style={styles.progress}>{completed[workout.name] ? '100%' : workout.progressLabel}</Text>
          </View>
          <View style={styles.bar}>
            <View style={[styles.fill, { width: completed[workout.name] ? '100%' : workout.progress }]} />
          </View>
          <Button
            title={completed[workout.name] ? 'Reabrir treino' : 'Marcar concluido'}
            variant="dark"
            onPress={() => toggleComplete(workout.name)}
            style={styles.action}
          />
        </Card>
      ))}
      <Button title="Criar novo treino" onPress={addWorkout} />
    </AppScreen>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 14,
  },
  badge: {
    width: 46,
    height: 46,
    borderRadius: 23,
    backgroundColor: colors.accentDim,
    alignItems: 'center',
    justifyContent: 'center',
  },
  badgeText: {
    color: colors.accent,
    fontSize: 18,
    fontWeight: '900',
  },
  info: {
    flex: 1,
  },
  title: {
    color: colors.title,
    fontSize: 17,
    fontWeight: '800',
  },
  body: {
    color: colors.text2,
    fontSize: 13,
    marginTop: 3,
  },
  meta: {
    color: colors.text3,
    fontSize: 11,
    marginTop: 6,
    textTransform: 'uppercase',
    letterSpacing: 0.8,
  },
  progress: {
    color: colors.text,
    fontSize: 18,
    fontWeight: '800',
  },
  bar: {
    height: 5,
    borderRadius: 5,
    backgroundColor: colors.card2,
    overflow: 'hidden',
    marginTop: 16,
  },
  fill: {
    height: '100%',
    backgroundColor: colors.accent,
  },
  action: {
    marginTop: 14,
  },
});
