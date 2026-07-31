import { useMemo, useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { Card } from '@/components/Card';
import { AppScreen } from '@/components/Header';
import { colors } from '@/constants/theme';

const sections = ['TODOS OS EXERCICIOS', 'MEU TREINO', 'TREINO RECOMENDADO', 'TREINOS SALVOS'] as const;

const exercises = [
  { name: 'Agachamento livre', group: 'Pernas', series: '4 x 8', section: 'TODOS OS EXERCICIOS' },
  { name: 'Supino reto', group: 'Peitoral', series: '4 x 10', section: 'MEU TREINO' },
  { name: 'Remada curvada', group: 'Costas', series: '3 x 12', section: 'TREINO RECOMENDADO' },
  { name: 'Prancha', group: 'Core', series: '3 x 45s', section: 'TREINOS SALVOS' },
];

export default function Academia() {
  const [selected, setSelected] = useState<(typeof sections)[number]>('TODOS OS EXERCICIOS');
  const visibleExercises = useMemo(() => {
    if (selected === 'TODOS OS EXERCICIOS') {
      return exercises;
    }
    return exercises.filter((exercise) => exercise.section === selected);
  }, [selected]);

  return (
    <AppScreen title="Academia" subtitle="Biblioteca e treino recomendado">
      <View style={styles.chips}>
        {sections.map((section) => (
          <Pressable
            key={section}
            onPress={() => setSelected(section)}
            style={[styles.chip, selected === section && styles.chipActive]}
          >
            <Text style={[styles.chipText, selected === section && styles.chipTextActive]}>{section}</Text>
          </Pressable>
        ))}
      </View>

      <Card>
        <Text style={styles.sectionTitle}>{selected}</Text>
        {visibleExercises.map((exercise) => (
          <View key={exercise.name} style={styles.exercise}>
            <View>
              <Text style={styles.name}>{exercise.name}</Text>
              <Text style={styles.group}>{exercise.group}</Text>
            </View>
            <Text style={styles.series}>{exercise.series}</Text>
          </View>
        ))}
      </Card>
    </AppScreen>
  );
}

const styles = StyleSheet.create({
  chips: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
  },
  chip: {
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.card,
    borderRadius: 3,
    paddingHorizontal: 12,
    paddingVertical: 9,
  },
  chipActive: {
    borderColor: 'rgba(212,247,0,0.45)',
    backgroundColor: 'rgba(212,247,0,0.08)',
  },
  chipText: {
    color: colors.text2,
    fontSize: 10,
    fontWeight: '800',
    letterSpacing: 0.8,
  },
  chipTextActive: {
    color: colors.yellow,
  },
  sectionTitle: {
    color: colors.title,
    fontSize: 18,
    fontWeight: '900',
    marginBottom: 12,
  },
  exercise: {
    minHeight: 58,
    borderTopWidth: 1,
    borderTopColor: colors.border,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  name: {
    color: colors.text,
    fontSize: 14,
    fontWeight: '700',
  },
  group: {
    color: colors.text3,
    fontSize: 12,
    marginTop: 2,
  },
  series: {
    color: colors.accent,
    fontSize: 13,
    fontWeight: '800',
  },
});
