import React, { useState, useEffect } from 'react';
import ReactDOM from 'react-dom/client';
import { Player, RoomState } from './types/game';
import { PlayerJoinLobby } from './components/PlayerJoinLobby';
import { PlayerPocketWallet } from './components/PlayerPocketWallet';
import { puterBanker } from './services/puterAgentService';
import './index.css';

export const StandaloneWalletApp: React.FC = () => {
  const [activePlayer, setActivePlayer] = useState<Player | null>(null);
  const [roomState, setRoomState] = useState<RoomState | null>(null);

  useEffect(() => {
    // Read room ID from query string if available (e.g. ?room=FM-77)
    const urlParams = new URLSearchParams(window.location.search);
    const roomId = urlParams.get('room') || 'FM-77';

    // Load initial room state
    puterBanker.loadRoomState(roomId).then(state => {
      if (state) {
        setRoomState(state);
      }
    });

    // Cross-tab sync
    const handleStorageUpdate = (e: CustomEvent<RoomState>) => {
      if (e.detail && e.detail.roomId === roomId) {
        setRoomState(e.detail);
        if (activePlayer) {
          const fresh = e.detail.players.find(p => p.id === activePlayer.id);
          if (fresh) setActivePlayer(fresh);
        }
      }
    };

    window.addEventListener('banquero_room_update' as unknown as keyof WindowEventMap, handleStorageUpdate as EventListener);
    return () => {
      window.removeEventListener('banquero_room_update' as unknown as keyof WindowEventMap, handleStorageUpdate as EventListener);
    };
  }, [activePlayer]);

  const handleUpdateRoom = (newState: RoomState) => {
    setRoomState(newState);
    puterBanker.saveRoomState(newState.roomId, newState);
    if (activePlayer) {
      const fresh = newState.players.find(p => p.id === activePlayer.id);
      if (fresh) setActivePlayer(fresh);
    }
  };

  const handleNewPlayerRegister = (name: string, avatar: string) => {
    if (!roomState) return;
    const colors = ['#F59E0B', '#EC4899', '#3B82F6', '#10B981', '#8B5CF6', '#F97316', '#06B6D4', '#E11D48'];
    const newP: Player = {
      id: `p_${Date.now()}`,
      name,
      avatar,
      color: colors[roomState.players.length % colors.length],
      balance: roomState.settings.initialBalance || 0,
      position: 0,
      inJail: false,
      jailTurns: 0,
      properties: [],
      bills: roomState.settings.initialBalance > 0 ? { 500: 4, 1000: 3, 2000: 2, 5000: 1 } : {}
    };
    const updatedState = { ...roomState, players: [...roomState.players, newP] };
    handleUpdateRoom(updatedState);
    setActivePlayer(newP);
  };

  if (!roomState) {
    return (
      <div className="min-h-screen bg-slate-950 flex flex-col items-center justify-center text-slate-100 p-4">
        <div className="w-10 h-10 border-4 border-amber-500 border-t-transparent rounded-full animate-spin mb-3"></div>
        <p className="text-xs text-slate-400 font-mono">Conectando con la Bóveda de la Sala...</p>
      </div>
    );
  }

  if (!activePlayer) {
    return (
      <PlayerJoinLobby
        roomState={roomState}
        onJoinAsPlayer={player => setActivePlayer(player)}
        onNewPlayerRegister={handleNewPlayerRegister}
      />
    );
  }

  return (
    <PlayerPocketWallet
      player={activePlayer}
      roomState={roomState}
      onBackToHost={() => setActivePlayer(null)}
      onUpdateRoom={handleUpdateRoom}
    />
  );
};

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <StandaloneWalletApp />
  </React.StrictMode>
);
