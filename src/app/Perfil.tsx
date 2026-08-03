import { router } from 'expo-router';
import { useState } from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { AppScreen } from '@/components/Header';
import { Card } from '@/components/Card';
import { Button } from '@/components/Button';
import { Input } from '@/components/Input';
import { colors } from '@/constants/theme';
import { AuthService } from '@/services/chat';

export default function Perfil() {
  const user = AuthService.active();
  const [edit, setEdit] = useState(false);
  const [name, setName] = useState(user?.name ?? '');
  const [headline, setHeadline] = useState(user?.headline ?? '');
  const [location, setLocation] = useState(user?.location ?? '');
  const [bio, setBio] = useState(user?.bio ?? '');
  if (!user) { router.replace('/'); return null; }
  const active = user;
  const save = () => { AuthService.updateProfile(active.id, { name, headline, location, bio }); setEdit(false); };
  return <AppScreen><View style={styles.cover}/><Card style={styles.profile}>
    <View style={styles.avatar}><Text style={styles.avatarText}>{active.name.slice(0, 2).toUpperCase()}</Text></View>
    <Pressable style={styles.settings} onPress={() => router.push('/Configuracoes')}><Text style={styles.settingsText}>⚙</Text></Pressable>
    {edit ? <View style={styles.form}><Input value={name} onChangeText={setName} placeholder="Nome"/><Input value={headline} onChangeText={setHeadline} placeholder="Área de atuação"/><Input value={location} onChangeText={setLocation} placeholder="Cidade"/><Input value={bio} onChangeText={setBio} placeholder="Biografia" multiline/><Button title="Salvar perfil" onPress={save}/></View> : <>
      <Text style={styles.name}>{active.name}</Text><Text style={styles.handle}>@{active.email.split('@')[0]}</Text><Text style={styles.headline}>{active.headline}</Text>{active.location ? <Text style={styles.location}>{active.location}</Text> : null}<Text style={styles.bio}>{active.bio || 'Complete seu perfil para apresentar sua trajetória à comunidade.'}</Text>
      <View style={styles.stats}><Stat value="0" label="Conexões"/><Stat value="0" label="Seguidores"/><Stat value="0" label="Seguindo"/></View><Button title="Editar perfil" onPress={() => setEdit(true)}/>
    </>}
  </Card></AppScreen>;
}
function Stat({ value, label }: { value: string; label: string }) { return <View><Text style={styles.statValue}>{value}</Text><Text style={styles.statLabel}>{label}</Text></View>; }
const styles = StyleSheet.create({cover:{height:126,marginHorizontal:-18,marginTop:-18,backgroundColor:colors.accent},profile:{marginTop:-58},avatar:{width:88,height:88,borderRadius:44,backgroundColor:colors.card2,borderWidth:4,borderColor:colors.card,alignItems:'center',justifyContent:'center',marginTop:-64},avatarText:{color:colors.accent,fontSize:25,fontWeight:'900'},settings:{position:'absolute',right:18,top:18,padding:8},settingsText:{color:colors.text,fontSize:20},name:{color:colors.text,fontSize:24,fontWeight:'900',marginTop:14},handle:{color:colors.text3,fontSize:13,marginTop:2},headline:{color:colors.text2,fontSize:15,marginTop:12},location:{color:colors.accent,fontSize:13,marginTop:6},bio:{color:colors.text2,lineHeight:21,marginTop:16},stats:{flexDirection:'row',gap:28,borderTopWidth:1,borderColor:colors.border,paddingTop:16,marginTop:20,marginBottom:20},statValue:{color:colors.text,fontWeight:'900',fontSize:17},statLabel:{color:colors.text3,fontSize:11,marginTop:2},form:{gap:12,marginTop:18}});
