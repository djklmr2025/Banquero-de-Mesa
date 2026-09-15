import React, { useState, useEffect } from 'react';
import ReactDOM from 'react-dom/client';
import { RoomState } from './types/game';
import { TabletHostView } from './components/TabletHostView';
import { PlayerPocketWallet } from './components/PlayerPocketWallet';
import { DEFAULT_PROPERTIES } from './data/defaultGameData';
import { puterBanker } from './services/puterAgentService';
import './index.css';

const INITIAL_ROOM_STATE: RoomState = {
  roomId: 'FM-77',
  hostName: 'Tablet Matriz (Fotorama)',
  currentTurnPlayerId: 'p1',
  roundNumber: 1,
  dice: [1, 1],
  isRolling: false,
  activeCard: null,
  players: [
    {
      id: 'p1',
      name: 'Juan (Host)',
      avatar: '🎩',
      color: '#F59E0B',
      balance: 0,
      position: 0,
      inJail: false,
      jailTurns: 0,
      properties: [],
      bills: {}
    },
    {
      id: 'p2',
      name: 'Sofía',
      avatar: '🏎️',
      color: '#EC4899',
      balance: 0,
      position: 0,
      inJail: false,
      jailTurns: 0,
      properties: [],
      bills: {}
    },
    {
      id: 'p3',
      name: 'Mateo',
      avatar: '🚀',
      color: '#3B82F6',
      balance: 0,
      position: 0,
      inJail: false,
      jailTurns: 0,
      properties: [],
      bills: {}
    },
    {
      id: 'p4',
      name: 'Valentina',
      avatar: '👑',
      color: '#10B981',
      balance: 0,
      position: 0,
      inJail: false,
      jailTurns: 0,
      properties: [],
      bills: {}
    }
  ],
  properties: DEFAULT_PROPERTIES,
  history: [],
  lastDialogue: {
    id: 'diag_init',
    text: '¡El Banco Central Fotorama está listo! Configura los jugadores o inicia la partida con edición bloqueada.',
    mood: 'celebratory',
    timestamp: Date.now()
  },
  settings: {
    passGoSalary: 20000,
    initialBalance: 0,
    currencySymbol: '$',
    currencyName: 'Pesos FM',
    aiCommentaryEnabled: true,
    voiceEnabled: true,
    gameStatus: 'setup',
    maxRounds: 10
  }
};

export const StandaloneTabletApp: React.FC = () => {
  const [inspectedPlayerId, setInspectedPlayerId] = useState<string | null>(null);
  const [roomState, setRoomState] = useState<RoomState>(() => {
    const saved = localStorage.getItem('banquero_room_FM-77');
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        if (parsed.settings && (parsed.settings.initialBalance === 15000 || parsed.settings.gameStatus === undefined)) {
          return {
            ...parsed,
            players: parsed.players.map((p: any) => ({
              ...p,
              balance: p.balance === 15000 ? 0 : p.balance,
              properties: p.properties || [],
              bills: p.balance === 15000 ? {} : (p.bills || {})
            })),
            settings: {
              ...parsed.settings,
              initialBalance: parsed.settings.initialBalance === 15000 ? 0 : parsed.settings.initialBalance,
              gameStatus: parsed.settings.gameStatus || 'setup',
              maxRounds: parsed.settings.maxRounds || 10
            }
          };
        }
        return parsed;
      } catch {
        return INITIAL_ROOM_STATE;
      }
    }
    return INITIAL_ROOM_STATE;
  });

  const handleUpdateRoom = (newState: RoomState) => {
    setRoomState(newState);
    puterBanker.saveRoomState(newState.roomId, newState);
  };

  useEffect(() => {
    const handleStorageUpdate = (e: CustomEvent<RoomState>) => {
      if (e.detail) {
        setRoomState(e.detail);
      }
    };
    window.addEventListener('banquero_room_update' as unknown as keyof WindowEventMap, handleStorageUpdate as EventListener);
    return () => {
      window.removeEventListener('banquero_room_update' as unknown as keyof WindowEventMap, handleStorageUpdate as EventListener);
    };
  }, []);

  const inspectedPlayer = roomState.players.find(p => p.id === inspectedPlayerId);

  if (inspectedPlayer) {
    return (
      <PlayerPocketWallet
        player={inspectedPlayer}
        roomState={roomState}
        onBackToHost={() => setInspectedPlayerId(null)}
        onUpdateRoom={handleUpdateRoom}
      />
    );
  }

  return (
    <TabletHostView
      roomState={roomState}
      onUpdateRoom={handleUpdateRoom}
      onSwitchToPlayer={(pId) => setInspectedPlayerId(pId)}
    />
  );
};

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <StandaloneTabletApp />
  </React.StrictMode>
);
