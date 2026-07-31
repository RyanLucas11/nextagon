import { router, useLocalSearchParams } from 'expo-router';
import { useEffect, useMemo, useState } from 'react';
import { Platform, Pressable, StyleSheet, Text, View } from 'react-native';

import { Button } from '@/components/Button';
import { Card } from '@/components/Card';
import { AppScreen } from '@/components/Header';
import { Input } from '@/components/Input';
import { colors } from '@/constants/theme';
import {
  ChatProfile,
  clearChatMessages,
  createMessage,
  getChatMessages,
  saveChatMessages,
} from '@/services/chat';

export default function Chat() {
  const params = useLocalSearchParams<{ perfil?: string }>();
  const profile: ChatProfile = params.perfil === 'profissional' ? 'profissional' : 'atleta';
  const [messages, setMessages] = useState(getChatMessages);
  const [draft, setDraft] = useState('');

  const contact = profile === 'atleta' ? 'Marina Costa' : 'Joao Silva';
  const contactRole = profile === 'atleta' ? 'Profissional' : 'Atleta';
  const profileLabel = profile === 'atleta' ? 'Atleta' : 'Profissional';

  const orderedMessages = useMemo(
    () => [...messages].sort((a, b) => a.createdAt.localeCompare(b.createdAt)),
    [messages],
  );

  useEffect(() => {
    if (Platform.OS !== 'web' || typeof window === 'undefined') {
      return;
    }

    const sync = () => setMessages(getChatMessages());
    window.addEventListener('storage', sync);
    window.addEventListener('focus', sync);

    return () => {
      window.removeEventListener('storage', sync);
      window.removeEventListener('focus', sync);
    };
  }, []);

  function chooseProfile(nextProfile: ChatProfile) {
    router.setParams({ perfil: nextProfile });
  }

  function sendMessage() {
    if (!draft.trim()) {
      return;
    }

    const nextMessages = [...getChatMessages(), createMessage(profile, draft)];
    saveChatMessages(nextMessages);
    setMessages(nextMessages);
    setDraft('');
  }

  function resetConversation() {
    const reset = clearChatMessages();
    setMessages(reset);
  }

  return (
    <AppScreen title="Chat" subtitle={`Perfil ativo: ${profileLabel}`}>
      <Card style={styles.profileCard}>
        <Text style={styles.label}>Entrar como</Text>
        <View style={styles.profileTabs}>
          <ProfileButton label="Atleta" active={profile === 'atleta'} onPress={() => chooseProfile('atleta')} />
          <ProfileButton
            label="Profissional"
            active={profile === 'profissional'}
            onPress={() => chooseProfile('profissional')}
          />
        </View>
        <Text style={styles.hint}>
          No web, abra duas abas: uma em /Chat?perfil=atleta e outra em /Chat?perfil=profissional.
        </Text>
      </Card>

      <Card style={styles.contact}>
        <View style={styles.avatar}>
          <Text style={styles.avatarText}>{contactInitials(contact)}</Text>
        </View>
        <View style={styles.contactInfo}>
          <Text style={styles.name}>{contact}</Text>
          <Text style={styles.status}>{contactRole} online agora</Text>
        </View>
      </Card>

      <Card style={styles.chat}>
        {orderedMessages.map((message) => {
          const mine = message.from === profile;
          return (
            <View key={message.id} style={[styles.message, mine && styles.mine]}>
              <Text style={[styles.author, mine && styles.mineAuthor]}>
                {message.from === 'atleta' ? 'Atleta' : 'Profissional'}
              </Text>
              <Text style={[styles.messageText, mine && styles.mineText]}>{message.text}</Text>
              <Text style={[styles.time, mine && styles.mineTime]}>{formatTime(message.createdAt)}</Text>
            </View>
          );
        })}
      </Card>

      <View style={styles.composer}>
        <Input
          icon="+"
          placeholder={`Mensagem como ${profileLabel}`}
          value={draft}
          onChangeText={setDraft}
          returnKeyType="send"
          onSubmitEditing={sendMessage}
        />
        <Button title="Enviar" onPress={sendMessage} disabled={!draft.trim()} />
        <Button title="Limpar conversa" variant="dark" onPress={resetConversation} />
      </View>
    </AppScreen>
  );
}

function ProfileButton({ label, active, onPress }: { label: string; active: boolean; onPress: () => void }) {
  return (
    <Pressable onPress={onPress} style={[styles.profileButton, active && styles.profileButtonActive]}>
      <Text style={[styles.profileButtonText, active && styles.profileButtonTextActive]}>{label}</Text>
    </Pressable>
  );
}

function contactInitials(name: string) {
  return name
    .split(' ')
    .slice(0, 2)
    .map((part) => part[0])
    .join('')
    .toUpperCase();
}

function formatTime(value: string) {
  const date = new Date(value);
  return `${date.getHours().toString().padStart(2, '0')}:${date
    .getMinutes()
    .toString()
    .padStart(2, '0')}`;
}

const styles = StyleSheet.create({
  profileCard: {
    gap: 10,
  },
  label: {
    color: colors.text3,
    fontSize: 10,
    fontWeight: '800',
    letterSpacing: 1,
    textTransform: 'uppercase',
  },
  profileTabs: {
    flexDirection: 'row',
    gap: 8,
  },
  profileButton: {
    flex: 1,
    borderWidth: 1,
    borderColor: colors.border,
    borderRadius: 3,
    backgroundColor: colors.card2,
    paddingVertical: 12,
    alignItems: 'center',
  },
  profileButtonActive: {
    borderColor: 'rgba(212,247,0,0.5)',
    backgroundColor: 'rgba(212,247,0,0.08)',
  },
  profileButtonText: {
    color: colors.text2,
    fontSize: 12,
    fontWeight: '800',
    letterSpacing: 0.8,
    textTransform: 'uppercase',
  },
  profileButtonTextActive: {
    color: colors.yellow,
  },
  hint: {
    color: colors.text3,
    fontSize: 11,
    lineHeight: 16,
  },
  contact: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  avatar: {
    width: 44,
    height: 44,
    borderRadius: 22,
    backgroundColor: colors.accent,
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatarText: {
    color: colors.white,
    fontSize: 13,
    fontWeight: '900',
  },
  contactInfo: {
    flex: 1,
  },
  name: {
    color: colors.title,
    fontSize: 16,
    fontWeight: '800',
  },
  status: {
    color: colors.green,
    fontSize: 12,
    marginTop: 3,
  },
  chat: {
    gap: 10,
    minHeight: 330,
  },
  message: {
    maxWidth: '82%',
    alignSelf: 'flex-start',
    borderRadius: 12,
    borderTopLeftRadius: 3,
    backgroundColor: colors.card2,
    paddingHorizontal: 14,
    paddingVertical: 10,
  },
  mine: {
    alignSelf: 'flex-end',
    borderTopLeftRadius: 12,
    borderTopRightRadius: 3,
    backgroundColor: colors.accent,
  },
  author: {
    color: colors.text3,
    fontSize: 10,
    fontWeight: '800',
    marginBottom: 4,
    textTransform: 'uppercase',
  },
  mineAuthor: {
    color: 'rgba(255,255,255,0.68)',
  },
  messageText: {
    color: colors.text,
    fontSize: 13,
    lineHeight: 19,
  },
  mineText: {
    color: colors.white,
  },
  time: {
    color: colors.text3,
    fontSize: 10,
    marginTop: 6,
    textAlign: 'right',
  },
  mineTime: {
    color: 'rgba(255,255,255,0.65)',
  },
  composer: {
    gap: 10,
  },
});
