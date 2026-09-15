import React, { useState } from 'react';
import { Smartphone, LogIn, Sparkles, UserCheck, ShieldCheck, ArrowRight } from 'lucide-react';
import { Player, RoomState } from '../types/game';
import { soundFx } from '../services/soundService';

interface PlayerJoinLobbyProps {
  roomState: RoomState;
  onJoinAsPlayer: (player: Player) => void;
  onNewPlayerRegister: (name: string, avatar: string) => void;
}

export const PlayerJoinLobby: React.FC<PlayerJoinLobbyProps> = ({
  roomState,
  onJoinAsPlayer,
  onNewPlayerRegister
}) => {
  const [selectedPlayerId, setSelectedPlayerId] = useState<string>(roomState.players[0]?.id || '');
  const [isRegistering, setIsRegistering] = useState(false);
  const [newName, setNewName] = useState('');
  const [newAvatar, setNewAvatar] = useState('🎲');

  const avatars = ['🎩', '🏎️', '🚀', '👑', '🦁', '🛸', '💎', '🔥'];

  const handleSelectAndEnter = () => {
    const p = roomState.players.find(x => x.id === selectedPlayerId);
    if (p) {
      soundFx.playCoin();
      onJoinAsPlayer(p);
    }
  };

  const handleRegister = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newName.trim()) return;
    soundFx.playPassGo();
    onNewPlayerRegister(newName.trim(), newAvatar);
    setIsRegistering(false);
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col justify-center items-center p-4 selection:bg-amber-500 selection:text-slate-950">
      <div className="w-full max-w-sm flex flex-col gap-6">
        
        {/* Header Branding */}
        <div className="text-center flex flex-col items-center">
          <div className="w-16 h-16 rounded-3xl bg-gradient-to-tr from-amber-500 to-amber-600 flex items-center justify-center text-slate-950 shadow-xl shadow-amber-500/20 font-black text-2xl mb-3">
            FM
          </div>
          <span className="text-xs font-bold uppercase tracking-widest text-amber-400">Fotorama de México</span>
          <h1 className="text-2xl font-black text-white tracking-tight mt-0.5">Billetera Móvil Oficial</h1>
          <p className="text-xs text-slate-400 mt-1">Conectado a Sala: <strong className="text-amber-400 font-mono">{roomState.roomId}</strong></p>
        </div>

        {/* Card */}
        <div className="bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-2xl flex flex-col gap-5">
          {!isRegistering ? (
            <>
              <div className="flex items-center justify-between border-b border-slate-800 pb-3">
                <span className="text-xs text-slate-400 font-bold uppercase tracking-wider">Elige tu Jugador</span>
                <span className="text-xs text-emerald-400 font-semibold flex items-center gap-1">
                  <ShieldCheck className="w-3.5 h-3.5" /> Sala Activa
                </span>
              </div>

              {/* Roster Selection */}
              <div className="flex flex-col gap-2.5 max-h-64 overflow-y-auto pr-1">
                {roomState.players.map(p => {
                  const isSel = p.id === selectedPlayerId;
                  return (
                    <div
                      key={p.id}
                      onClick={() => setSelectedPlayerId(p.id)}
                      className={`p-3.5 rounded-2xl border transition-all cursor-pointer flex items-center justify-between ${
                        isSel 
                          ? 'bg-amber-500/15 border-amber-500/80 shadow-md shadow-amber-500/10' 
                          : 'bg-slate-950/60 border-slate-800 hover:border-slate-700'
                      }`}
                    >
                      <div className="flex items-center gap-3">
                        <div 
                          className="w-10 h-10 rounded-xl flex items-center justify-center text-lg font-bold text-white shadow"
                          style={{ backgroundColor: p.color }}
                        >
                          {p.avatar}
                        </div>
                        <div>
                          <h4 className="font-bold text-sm text-white">{p.name}</h4>
                          <span className="text-xs font-mono font-bold text-emerald-400">${p.balance.toLocaleString()}</span>
                        </div>
                      </div>

                      {isSel && (
                        <div className="w-6 h-6 rounded-full bg-amber-500 text-slate-950 flex items-center justify-center text-xs font-black">
                          ✓
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>

              <button
                onClick={handleSelectAndEnter}
                className="w-full py-4 bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 active:scale-98 text-slate-950 font-black rounded-2xl shadow-xl shadow-amber-500/25 flex items-center justify-center gap-2 text-sm transition-all cursor-pointer"
              >
                <span>Entrar a mi Billetera</span>
                <ArrowRight className="w-4 h-4" />
              </button>

              {roomState.players.length < 8 && (
                <button
                  onClick={() => setIsRegistering(true)}
                  className="text-xs text-center text-slate-400 hover:text-amber-400 font-bold transition-colors cursor-pointer py-1"
                >
                  + Unir nuevo jugador a la mesa
                </button>
              )}
            </>
          ) : (
            <form onSubmit={handleRegister} className="flex flex-col gap-4">
              <h3 className="text-sm font-bold text-white">Nuevo Jugador en la Mesa</h3>

              <div>
                <label className="text-xs text-slate-400 font-bold block mb-1">Nombre o Apodo:</label>
                <input
                  type="text"
                  placeholder="Ej. Carlos"
                  value={newName}
                  onChange={e => setNewName(e.target.value)}
                  className="w-full bg-slate-950 border border-slate-700 rounded-xl p-3 text-sm text-white font-bold"
                  required
                />
              </div>

              <div>
                <label className="text-xs text-slate-400 font-bold block mb-2">Elige tu Ficha / Avatar:</label>
                <div className="grid grid-cols-4 gap-2">
                  {avatars.map(av => (
                    <button
                      key={av}
                      type="button"
                      onClick={() => setNewAvatar(av)}
                      className={`p-3 rounded-xl text-xl border transition-all ${
                        newAvatar === av ? 'border-amber-400 bg-slate-800' : 'border-slate-800 bg-slate-950 text-slate-400'
                      }`}
                    >
                      {av}
                    </button>
                  ))}
                </div>
              </div>

              <div className="flex items-center gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setIsRegistering(false)}
                  className="flex-1 py-3 text-xs font-bold text-slate-400 hover:text-white"
                >
                  Cancelar
                </button>
                <button
                  type="submit"
                  className="flex-1 py-3 bg-amber-500 hover:bg-amber-400 text-slate-950 font-black rounded-xl text-xs transition-all shadow-md cursor-pointer"
                >
                  Guardar y Entrar
                </button>
              </div>
            </form>
          )}
        </div>

        <div className="text-center text-[11px] text-slate-500 font-mono">
          Ecosistema Banquero de Mesa &bull; Sincronización Puter Cloud
        </div>
      </div>
    </div>
  );
};
