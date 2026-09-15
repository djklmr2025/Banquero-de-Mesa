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
      balance: 15000,
      position: 0,
      inJail: false,
      jailTurns: 0,
      properties: ['prop_1'],
      bills: { 500: 4, 1000: 3, 2000: 2, 5000: 1 }
    },
    {
      id: 'p2',
      name: 'Sofía',
      avatar: '🏎️',
      color: '#EC4899',
      balance: 15000,
      position: 0,
      inJail: false,
      jailTurns: 0,
      properties: ['prop_3'],
      bills: { 500: 4, 1000: 3, 2000: 2, 5000: 1 }
    },
    {
      id: 'p3',
      name: 'Mateo',
      avatar: '🚀',
      color: '#3B82F6',
      balance: 15000,
      position: 0,
      inJail: false,
      jailTurns: 0,
      properties: [],
      bills: { 500: 4, 1000: 3, 2000: 2, 5000: 1 }
    },
    {
      id: 'p4',
      name: 'Valentina',
      avatar: '👑',
      color: '#10B981',
      balance: 15000,
      position: 0,
      inJail: false,
      jailTurns: 0,
      properties: ['prop_5'],
      bills: { 500: 4, 1000: 3, 2000: 2, 5000: 1 }
    }
  ],
  properties: DEFAULT_PROPERTIES,
  history: [],
  lastDialogue: {
    id: 'diag_init',
    text: '¡El Banco Central Fotorama está listo con liquidez total! Turno de Juan para lanzar los dados.',
    mood: 'celebratory',
    timestamp: Date.now()
  },
  settings: {
    passGoSalary: 20000,
    initialBalance: 15000,
    currencySymbol: '$',
    currencyName: 'Pesos FM',
    aiCommentaryEnabled: true,
    voiceEnabled: true
  }
};

export const StandaloneTabletApp: React.FC = () => {
  const [inspectedPlayerId, setInspectedPlayerId] = useState<string | null>(null);
  const [roomState, setRoomState] = useState<RoomState>(() => {
    const saved = localStorage.getItem('banquero_room_FM-77');
    if (saved) {
      try {
        return JSON.parse(saved);
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
