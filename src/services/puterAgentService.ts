// Puter AI Agent & Cloud Integration for Banquero de Mesa (Fotorama de México)

import { BankerDialogue, Player, RoomState, SurpriseCard } from '../types/game';

declare global {
  interface Window {
    puter?: {
      ai?: {
        chat: (prompt: string, options?: { model?: string }) => Promise<{ message?: { content?: string } } | string>;
        txt2speech?: (text: string) => Promise<HTMLAudioElement>;
      };
      kv?: {
        set: (key: string, value: string) => Promise<boolean>;
        get: (key: string) => Promise<string | null>;
        del: (key: string) => Promise<boolean>;
        list: () => Promise<string[]>;
      };
      hosting?: {
        create: (subdomain: string, dir: string) => Promise<{ subdomain: string; url: string }>;
      };
      auth?: {
        getUser: () => Promise<{ username: string } | null>;
        signIn: () => Promise<void>;
      };
    };
  }
}

export class PuterBankerAgent {
  private static instance: PuterBankerAgent;
  private currentVoiceUtterance: SpeechSynthesisUtterance | null = null;

  public static getInstance(): PuterBankerAgent {
    if (!PuterBankerAgent.instance) {
      PuterBankerAgent.instance = new PuterBankerAgent();
    }
    return PuterBankerAgent.instance;
  }

  // Check if Puter SDK is loaded and operational
  isPuterAvailable(): boolean {
    return typeof window !== 'undefined' && typeof window.puter !== 'undefined';
  }

  // 1. Banker AI Commentary Generator
  async generateCommentary(
    event: 'PASS_GO' | 'PAY_RENT' | 'BUY_PROPERTY' | 'GO_TO_JAIL' | 'CARD_DRAWN' | 'BANKRUPT' | 'DICE_DOUBLE' | 'GAME_START' | 'ROUND_END' | 'GAME_WON',
    context: {
      player: Player;
      targetPlayer?: Player;
      amount?: number;
      propertyName?: string;
      card?: SurpriseCard;
      dice?: [number, number];
      roundNumber?: number;
      totalWorth?: number;
    }
  ): Promise<BankerDialogue> {
    const defaultPhrases: Record<string, string[]> = {
      PASS_GO: [
        `¡Salud por esa vuelta, ${context.player.name}! El banco te premia con $20,000 en efectivo contante y sonante. ¡No te lo vayas a gastar en golosinas!`,
        `¡Vuelta completada para ${context.player.name}! Pasa a la ventanilla por tu sueldo de $20,000. ¡El dinero nunca duerme en este tablero!`,
        `¡Bingo! ${context.player.name} cruzó la salida. El banco deposita tus $20,000. ¡Disfrútalos mientras los tengas!`
      ],
      PAY_RENT: [
        `¡Uff, dolor de bolsillo! ${context.player.name} le acaba de soltar $${context.amount?.toLocaleString()} a ${context.targetPlayer?.name || 'su rival'} por hospedarse en ${context.propertyName}. ¡Las vacaciones salen caras!`,
        `¡Caja registradora sonando! ${context.player.name} paga $${context.amount?.toLocaleString()} de renta. ${context.targetPlayer?.name} ya está contando los fajos de billetes con una sonrisa burlona.`,
        `¡Ay caray! La renta en ${context.propertyName} no perdona. ${context.player.name}, despídete de $${context.amount?.toLocaleString()}. ¡A levantar la guardia!`
      ],
      BUY_PROPERTY: [
        `¡Nuevo magnate inmobiliario! ${context.player.name} adquiere la escritura de ${context.propertyName} por $${context.amount?.toLocaleString()}. ¡Que tiemble el tablero!`,
        `¡Firma el contrato! ${context.propertyName} ahora tiene nuevo dueño: ${context.player.name}. ¡A edificar casas pronto!`,
        `¡Excelente inversión de ${context.player.name}! Ya es dueño de ${context.propertyName}. Quien caiga aquí la va a pasar mal.`
      ],
      GO_TO_JAIL: [
        `¡Sirenas de patrulla! ${context.player.name} se va directo a la cárcel sin pasar por la salida ni cobrar un solo peso. ¡A reflexionar tras las rejas!`,
        `¡Al calabozo! Parece que a ${context.player.name} lo atraparon haciendo trampas fiscales. ¡3 turnos a la sombra o fianza inmediata!`,
        `¡Tras las rejas! ${context.player.name}, hoy no cenas en hotel de lujo, hoy te toca celda común.`
      ],
      CARD_DRAWN: [
        `¡Carta sorpresa en juego! ${context.player.name} destapa: "${context.card?.title}". ${context.card?.description}. ¡El destino no perdona!`,
        `¡Atención todos! ${context.player.name} sacó una carta misteriosa: ${context.card?.title}. ¡Prepárense para el desenlace!`
      ],
      BANKRUPT: [
        `¡BANCARROTA TOTAL! ${context.player.name} se ha quedado sin un solo centavo y sin propiedades. ¡El banco te despide con honores y aplausos de consuelo!`,
        `¡Tragedia financiera! ${context.player.name} declara quiebra. El juego se cobra otra víctima. ¡Gracias por participar!`
      ],
      DICE_DOUBLE: [
        `¡DOBLES! ¡${context.player.name} sacó dados gemelos (${context.dice?.[0]} y ${context.dice?.[1]})! Tira de nuevo, pero cuidado con sacar tres seguidos o vas al tambo.`,
        `¡Racha de suerte! Dados dobles para ${context.player.name}. ¡Tienes turno extra!`
      ],
      ROUND_END: [
        `¡Ronda concluida! Todos completaron su turno y ${context.player.name} va a la cabeza con $${context.player.balance.toLocaleString()}. ¡Comienza la siguiente ronda!`,
        `¡Cambio de ronda en el tablero! La mesa está en movimiento y ${context.player.name} lidera las finanzas. ¡A rodar esos dados!`,
        `¡Vuelta general a la mesa! El banquero audita las cuentas: ${context.player.name} va al frente. ¡Nadie tiene la victoria asegurada!`
      ],
      GAME_WON: [
        `¡TENEMOS UN GRAN CAMPEÓN! Se han completado las rondas pactadas y ${context.player.name} se corona con un patrimonio glorioso de $${context.totalWorth?.toLocaleString() || context.player.balance.toLocaleString()}. ¡Una ovación de pie!`,
        `¡FIN DE LA PARTIDA! ${context.player.name} es el nuevo magnate supremo de Fotorama. ¡El banco le rinde homenaje con honores!`
      ],
      GAME_START: [
        `¡Bienvenidos al Banquero de Mesa de Fotorama! Soy su Banquero Inteligente y Árbitro Oficial. Edición de cuentas bloqueada. ¡Que rueden los dados y que gane el más hábil!`,
        `¡La mesa está servida! El Banco abre sus bóvedas con 100% de liquidez. ¡Jugadores, a sus posiciones!`
      ]
    };

    const phrases = defaultPhrases[event] || defaultPhrases.GAME_START;
    const fallbackText = phrases[Math.floor(Math.random() * phrases.length)];

    // Try Puter AI prompt if available
    if (this.isPuterAvailable() && window.puter?.ai?.chat) {
      try {
        const prompt = `Eres el "Banquero Inteligente" de un juego de mesa de mesa familiar (estilo Fotorama / Monopoly moderno).
Tu personalidad es alegre, un poco sarcástica, carismática y emocionante como un presentador de televisión mexicano.
Evento del juego: ${event}
${context.roundNumber ? `Ronda número: ${context.roundNumber}` : ''}
Jugador destacado: ${context.player.name} (Saldo: $${context.player.balance})
${context.totalWorth ? `Patrimonio total calculado: $${context.totalWorth}` : ''}
${context.targetPlayer ? `Rival afectado: ${context.targetPlayer.name}` : ''}
${context.amount ? `Monto en juego: $${context.amount}` : ''}
${context.propertyName ? `Propiedad: ${context.propertyName}` : ''}
${context.card ? `Carta: "${context.card.title} - ${context.card.description}"` : ''}
${context.dice ? `Dados: [${context.dice[0]}, ${context.dice[1]}]` : ''}

Escribe una sola frase (máximo 2 líneas) para decir en voz alta a los jugadores en la mesa. No uses comillas extras ni prefijos como "Banquero:". Habla directo a los jugadores.`;

        const response = await window.puter.ai.chat(prompt, { model: 'gemini-2.0-flash' });
        const text = typeof response === 'string' ? response : response?.message?.content;
        if (text && text.trim().length > 5) {
          return {
            id: `diag_${Date.now()}`,
            text: text.trim().replace(/^["']|["']$/g, ''),
            mood: event === 'BANKRUPT' ? 'dramatic' : event === 'GO_TO_JAIL' ? 'strict' : event === 'PAY_RENT' ? 'sarcastic' : 'celebratory',
            timestamp: Date.now()
          };
        }
      } catch (err) {
        console.warn('Puter AI fallback to local phrases:', err);
      }
    }

    return {
      id: `diag_${Date.now()}`,
      text: fallbackText,
      mood: event === 'BANKRUPT' ? 'dramatic' : event === 'GO_TO_JAIL' ? 'strict' : 'celebratory',
      timestamp: Date.now()
    };
  }

  // 2. Banker Voice (Text to Speech)
  async speak(text: string): Promise<void> {
    if (!text) return;

    // Try Puter AI txt2speech if available
    if (this.isPuterAvailable() && window.puter?.ai?.txt2speech) {
      try {
        const audio = await window.puter.ai.txt2speech(text);
        if (audio) {
          audio.play();
          return;
        }
      } catch (err) {
        console.warn('Puter TTS failed, falling back to Web Speech API:', err);
      }
    }

    // Web Speech API fallback (Native & offline)
    if (typeof window !== 'undefined' && 'speechSynthesis' in window) {
      window.speechSynthesis.cancel(); // Stop any pending speech
      const utterance = new SpeechSynthesisUtterance(text);
      utterance.lang = 'es-MX';
      utterance.pitch = 1.05;
      utterance.rate = 1.05;

      // Find Spanish voice if available
      const voices = window.speechSynthesis.getVoices();
      const esVoice = voices.find(v => v.lang.startsWith('es-MX') || v.lang.startsWith('es-ES') || v.lang.startsWith('es'));
      if (esVoice) utterance.voice = esVoice;

      this.currentVoiceUtterance = utterance;
      window.speechSynthesis.speak(utterance);
    }
  }

  // 3. Puter Cloud KV Room Sync
  async saveRoomState(roomId: string, state: RoomState): Promise<boolean> {
    const payload = JSON.stringify(state);
    
    // Puter KV storage
    if (this.isPuterAvailable() && window.puter?.kv?.set) {
      try {
        await window.puter.kv.set(`room_${roomId}`, payload);
      } catch (err) {
        console.warn('Puter KV set error:', err);
      }
    }

    // LocalStorage backup
    try {
      localStorage.setItem(`banquero_room_${roomId}`, payload);
      // Dispatch storage event for same-window tabs
      window.dispatchEvent(new CustomEvent('banquero_room_update', { detail: state }));
      return true;
    } catch {
      return false;
    }
  }

  async loadRoomState(roomId: string): Promise<RoomState | null> {
    // Try Puter KV first
    if (this.isPuterAvailable() && window.puter?.kv?.get) {
      try {
        const cloudData = await window.puter.kv.get(`room_${roomId}`);
        if (cloudData) {
          return JSON.parse(cloudData) as RoomState;
        }
      } catch (err) {
        console.warn('Puter KV get error:', err);
      }
    }

    // Fallback to localStorage
    try {
      const localData = localStorage.getItem(`banquero_room_${roomId}`);
      if (localData) {
        return JSON.parse(localData) as RoomState;
      }
    } catch (err) {
      console.error('Failed to parse local room state:', err);
    }

    return null;
  }
}

export const puterBanker = PuterBankerAgent.getInstance();
