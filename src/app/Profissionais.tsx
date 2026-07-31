import { useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { Button } from '@/components/Button';
import { Card } from '@/components/Card';
import { AppScreen } from '@/components/Header';
import { colors } from '@/constants/theme';

const professionals = [
  { name: 'Dra. Marina Costa', role: 'Fisioterapeuta', tag: 'Avaliacao postural' },
  { name: 'Renato Alves', role: 'Personal Trainer', tag: 'Hipertrofia' },
  { name: 'Camila Nunes', role: 'Nutricionista', tag: 'Performance' },
];

const filters = ['Especialidade', 'Horario', 'Unidade', 'Avaliacao'];

export default function Profissionais() {
  const [scheduled, setScheduled] = useState<Record<string, boolean>>({});
  const [activeFilter, setActiveFilter] = useState('Especialidade');

  return (
    <AppScreen title="CADASTRO PROFISSIONAL" subtitle="Agendar Avaliacao">
      {professionals.map((person) => (
        <Card key={person.name}>
          <View style={styles.row}>
            <View style={styles.avatar}>
              <Text style={styles.avatarText}>{initials(person.name)}</Text>
            </View>
            <View style={styles.info}>
              <Text style={styles.name}>{person.name}</Text>
              <Text style={styles.role}>{person.role}</Text>
              <Text style={styles.tag}>{scheduled[person.name] ? 'Agendado para sexta 14h' : person.tag}</Text>
            </View>
          </View>
          <Button
            title={scheduled[person.name] ? 'Cancelar agendamento' : 'Agendar avaliacao'}
            variant="dark"
            style={styles.button}
            onPress={() => setScheduled((current) => ({ ...current, [person.name]: !current[person.name] }))}
          />
        </Card>
      ))}

      <Card>
        <Text style={styles.filterTitle}>Gerenciador de Filtros</Text>
        <View style={styles.filters}>
          {filters.map((item) => (
            <Pressable
              key={item}
              onPress={() => setActiveFilter(item)}
              style={[styles.filter, activeFilter === item && styles.filterActive]}
            >
              <Text style={[styles.filterText, activeFilter === item && styles.filterTextActive]}>{item}</Text>
            </Pressable>
          ))}
        </View>
      </Card>
    </AppScreen>
  );
}

function initials(name: string) {
  return name
    .split(' ')
    .slice(0, 2)
    .map((part) => part[0])
    .join('')
    .toUpperCase();
}

const styles = StyleSheet.create({
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 14,
  },
  avatar: {
    width: 52,
    height: 52,
    borderRadius: 26,
    backgroundColor: colors.accent,
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatarText: {
    color: colors.white,
    fontSize: 14,
    fontWeight: '900',
  },
  info: {
    flex: 1,
  },
  name: {
    color: colors.title,
    fontSize: 16,
    fontWeight: '800',
  },
  role: {
    color: colors.text2,
    fontSize: 13,
    marginTop: 3,
  },
  tag: {
    color: colors.text3,
    fontSize: 11,
    marginTop: 6,
    textTransform: 'uppercase',
    letterSpacing: 0.8,
  },
  button: {
    marginTop: 16,
  },
  filterTitle: {
    color: colors.title,
    fontSize: 18,
    fontWeight: '800',
    marginBottom: 14,
  },
  filters: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
  },
  filter: {
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: 3,
    paddingHorizontal: 12,
    paddingVertical: 8,
    backgroundColor: colors.card2,
  },
  filterActive: {
    borderColor: 'rgba(212,247,0,0.45)',
    backgroundColor: 'rgba(212,247,0,0.08)',
  },
  filterText: {
    color: colors.text2,
    fontSize: 12,
    fontWeight: '700',
  },
  filterTextActive: {
    color: colors.yellow,
  },
});
