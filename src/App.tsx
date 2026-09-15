import React, { useState, useEffect } from 'react';
import { TabletHostView } from './components/TabletHostView';
import { PlayerPocketWallet } from './components/PlayerPocketWallet';
import { CreatorStudioView } from './components/CreatorStudioView';
import { RoomState, ModPack } from './types/game';
import { DEFAULT_PROPERTIES } from './data/defaultGameData';
import { puterBanker } from './services/puterAgentService';
import { Tablet, Smartphone, Palette } from 'lucide-react';

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

export const App: React.FC = () => {
  const [currentView, setCurrentView] = useState<'host' | 'player' | 'studio'>('host');
  const [selectedPlayerId, setSelectedPlayerId] = useState<string>('p1');
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

  // Sync to Puter KV & LocalStorage on state change
  const handleUpdateRoom = (newState: RoomState) => {
    setRoomState(newState);
    puterBanker.saveRoomState(newState.roomId, newState);
  };

  // Listen to cross-window updates (if running in multiple tabs/devices)
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

  // Handle ModPack apply from studio
  const handleApplyModPack = (pack: ModPack) => {
    const updatedState: RoomState = {
      ...roomState,
      properties: pack.properties,
      settings: {
        ...roomState.settings,
        currencyName: pack.currencyName,
        currencySymbol: pack.currencySymbol
      }
    };
    handleUpdateRoom(updatedState);
    setCurrentView('host');
  };

  const selectedPlayer = roomState.players.find(p => p.id === selectedPlayerId) || roomState.players[0];

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans">
      {/* Universal Ecosystem Navigation Bar */}
      <nav className="bg-slate-900 border-b border-slate-800 px-4 py-2 flex items-center justify-between text-xs sticky top-0 z-50">
        <div className="flex items-center gap-2">
          <span className="font-black tracking-wider text-amber-400">FOTORAMA • BANQUERO IA</span>
          <span className="text-slate-500">|</span>
          <span className="text-slate-400 font-mono">Puter Agent Active</span>
        </div>

        <div className="flex items-center gap-1.5 bg-slate-950 p-1 rounded-xl border border-slate-800">
          <button
            onClick={() => setCurrentView('host')}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg font-bold transition-all ${
              currentView === 'host' ? 'bg-amber-500 text-slate-950 shadow' : 'text-slate-400 hover:text-white'
            }`}
          >
            <Tablet className="w-3.5 h-3.5" />
            <span>Tablet Matriz</span>
          </button>

          <button
            onClick={() => setCurrentView('player')}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg font-bold transition-all ${
              currentView === 'player' ? 'bg-amber-500 text-slate-950 shadow' : 'text-slate-400 hover:text-white'
            }`}
          >
            <Smartphone className="w-3.5 h-3.5" />
            <span>Billetera Celular</span>
          </button>

          <button
            onClick={() => setCurrentView('studio')}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg font-bold transition-all ${
              currentView === 'studio' ? 'bg-amber-500 text-slate-950 shadow' : 'text-slate-400 hover:text-white'
            }`}
          >
            <Palette className="w-3.5 h-3.5" />
            <span>FM Creator Studio</span>
          </button>
        </div>
      </nav>

      {/* Main View Router */}
      <main className="flex-1">
        {currentView === 'host' && (
          <TabletHostView
            roomState={roomState}
            onUpdateRoom={handleUpdateRoom}
            onSwitchToPlayer={(pId) => {
              setSelectedPlayerId(pId);
              setCurrentView('player');
            }}
          />
        )}

        {currentView === 'player' && (
          <PlayerPocketWallet
            player={selectedPlayer}
            roomState={roomState}
            onBackToHost={() => setCurrentView('host')}
            onUpdateRoom={handleUpdateRoom}
          />
        )}

        {currentView === 'studio' && (
          <CreatorStudioView
            onBackToHost={() => setCurrentView('host')}
            onApplyModPack={handleApplyModPack}
          />
        )}
      </main>
    </div>
  );
};

export default App;
