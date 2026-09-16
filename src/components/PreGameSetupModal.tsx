import React, { useState } from 'react';
import { 
  X, Users, DollarSign, ShieldAlert, Sparkles, Plus, Trash2, 
  RotateCcw, Check, Trophy, Clock, Play, Building, Image as ImageIcon, Upload
} from 'lucide-react';
import { BoardProperty, Player, RoomState } from '../types/game';
import { soundFx } from '../services/soundService';
import { VerticalPropertyCard } from './VerticalPropertyCard';
import { DEFAULT_PROPERTIES } from '../data/defaultGameData';

interface PreGameSetupModalProps {
  roomState: RoomState;
  onClose: () => void;
  onSaveAndStart: (updatedState: RoomState) => void;
  onQuickSave: (updatedState: RoomState) => void;
}

const AVATAR_OPTIONS = [
  '🎩', '🏎️', '🚀', '👑', '🐶', '🐱', '⚽', '🍕', 
  '💎', '🎲', '🦄', '🤖', '🐉', '🛸', '🏆', '🥑', 
  '🎸', '⚡', '🦁', '🥊'
];

const COLOR_PALETTE = [
  '#F59E0B', '#EC4899', '#3B82F6', '#10B981', 
  '#8B5CF6', '#F97316', '#06B6D4', '#E11D48',
  '#84CC16', '#6366F1'
];

const PROPERTY_COLORS = [
  '#8B4513', '#38BDF8', '#EC4899', '#F97316',
  '#EF4444', '#EAB308', '#22C55E', '#1D4ED8'
];

export const PreGameSetupModal: React.FC<PreGameSetupModalProps> = ({
  roomState,
  onClose,
  onSaveAndStart,
  onQuickSave
}) => {
  const [activeTab, setActiveTab] = useState<'players' | 'properties'>('players');
  const [players, setPlayers] = useState<Player[]>(() => 
    roomState.players.map(p => ({ ...p }))
  );
  const [properties, setProperties] = useState<BoardProperty[]>(() =>
    roomState.properties.map(p => ({ ...p }))
  );
  const [globalBalance, setGlobalBalance] = useState<number>(roomState.settings.initialBalance || 0);
  const [passGoSalary, setPassGoSalary] = useState<number>(roomState.settings.passGoSalary || 20000);
  const [roundMode, setRoundMode] = useState<'rounds' | 'unlimited'>(
    roomState.settings.maxRounds && roomState.settings.maxRounds > 0 ? 'rounds' : 'rounds'
  );
  const [maxRounds, setMaxRounds] = useState<number>(
    roomState.settings.maxRounds && roomState.settings.maxRounds > 0 ? roomState.settings.maxRounds : 10
  );
  const [activeAvatarPlayerId, setActiveAvatarPlayerId] = useState<string | null>(null);

  // Apply balance to all players
  const handleApplyGlobalBalance = (amount: number) => {
    setGlobalBalance(amount);
    setPlayers(prev => prev.map(p => ({ ...p, balance: amount })));
    soundFx.playCoin();
  };

  // Reset all to $0 explicitly
  const handleResetAllToZero = () => {
    setGlobalBalance(0);
    setPlayers(prev => prev.map(p => ({
      ...p,
      balance: 0,
      position: 0,
      inJail: false,
      jailTurns: 0,
      properties: [],
      bills: {}
    })));
    soundFx.playCoin();
  };

  // Update specific player name
  const handlePlayerNameChange = (id: string, newName: string) => {
    setPlayers(prev => prev.map(p => p.id === id ? { ...p, name: newName } : p));
  };

  // Update specific player balance
  const handlePlayerBalanceChange = (id: string, amount: number) => {
    setPlayers(prev => prev.map(p => p.id === id ? { ...p, balance: isNaN(amount) ? 0 : amount } : p));
  };

  // Update player avatar
  const handlePlayerAvatarChange = (id: string, avatar: string) => {
    setPlayers(prev => prev.map(p => p.id === id ? { ...p, avatar } : p));
    setActiveAvatarPlayerId(null);
  };

  // Update player color
  const handlePlayerColorChange = (id: string, color: string) => {
    setPlayers(prev => prev.map(p => p.id === id ? { ...p, color } : p));
  };

  // Add new player
  const handleAddPlayer = () => {
    if (players.length >= 8) return;
    const newIndex = players.length + 1;
    const defaultAvatars = ['🎩', '🏎️', '🚀', '👑', '🐶', '🐱', '⚽', '🍕'];
    const newPlayer: Player = {
      id: `p_${Date.now()}`,
      name: `Jugador ${newIndex}`,
      avatar: defaultAvatars[(newIndex - 1) % defaultAvatars.length],
      color: COLOR_PALETTE[(newIndex - 1) % COLOR_PALETTE.length],
      balance: globalBalance,
      position: 0,
      inJail: false,
      jailTurns: 0,
      properties: [],
      bills: {}
    };
    setPlayers(prev => [...prev, newPlayer]);
  };

  // Remove player
  const handleRemovePlayer = (id: string) => {
    if (players.length <= 2) {
      alert('Se requieren mínimo 2 jugadores para iniciar una partida.');
      return;
    }
    setPlayers(prev => prev.filter(p => p.id !== id));
  };

  // Properties Management Handlers
  const handleUpdateProperty = (updated: BoardProperty) => {
    setProperties(prev => prev.map(p => p.id === updated.id ? updated : p));
  };

  const handleDeleteProperty = (id: string) => {
    if (properties.length <= 2) {
      alert('Debes mantener al menos 2 propiedades en el tablero.');
      return;
    }
    setProperties(prev => prev.filter(p => p.id !== id));
  };

  const handleAddProperty = () => {
    const newNum = properties.length + 1;
    const newProp: BoardProperty = {
      id: `prop_${Date.now()}`,
      name: `Nueva Propiedad ${newNum}`,
      group: 'General',
      price: 1500,
      baseRent: 150,
      rentPerHouse: 400,
      houses: 0,
      ownerId: null,
      color: PROPERTY_COLORS[properties.length % PROPERTY_COLORS.length],
      mortgaged: false
    };
    setProperties(prev => [...prev, newProp]);
  };

  const handleResetPropertiesToDefault = () => {
    setProperties(DEFAULT_PROPERTIES.map(p => ({ ...p })));
    soundFx.playCoin();
  };

  // Construct updated state
  const buildUpdatedState = (isStartingGame: boolean): RoomState => {
    return {
      ...roomState,
      currentTurnPlayerId: players[0]?.id || roomState.currentTurnPlayerId,
      roundNumber: 1,
      players: players.map(p => ({
        ...p,
        name: p.name.trim() || 'Jugador',
        position: 0,
        inJail: false,
        properties: isStartingGame ? [] : p.properties
      })),
      properties: properties.map(p => ({
        ...p,
        name: p.name.trim() || 'Propiedad',
        price: Math.max(0, p.price),
        baseRent: Math.max(0, p.baseRent),
        ownerId: isStartingGame ? null : p.ownerId
      })),
      settings: {
        ...roomState.settings,
        initialBalance: globalBalance,
        passGoSalary,
        gameStatus: isStartingGame ? 'playing' : 'setup',
        maxRounds: roundMode === 'rounds' ? maxRounds : 0
      }
    };
  };

  // Start game and lock edits
  const handleConfirmAndStart = () => {
    soundFx.playPassGo();
    const updated = buildUpdatedState(true);
    onSaveAndStart(updated);
  };

  // Quick save without locking (stays in setup)
  const handleQuickSave = () => {
    const updated = buildUpdatedState(false);
    onQuickSave(updated);
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/85 backdrop-blur-md flex items-center justify-center p-3 sm:p-6 overflow-y-auto animate-in fade-in duration-200">
      <div className="bg-slate-900 border-2 border-amber-500/40 rounded-3xl p-5 sm:p-7 max-w-4xl w-full shadow-2xl flex flex-col gap-6 relative my-auto max-h-[92vh] overflow-y-auto">
        
        {/* Header */}
        <div className="flex items-start justify-between gap-4 border-b border-slate-800 pb-4">
          <div className="flex items-center gap-3.5">
            <div className="w-12 h-12 rounded-2xl bg-gradient-to-tr from-amber-500 to-amber-600 flex items-center justify-center text-slate-950 font-black text-2xl shadow-lg shadow-amber-500/30">
              🛠️
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h2 className="text-xl sm:text-2xl font-black text-white">Edición Antes del Juego</h2>
                <span className="text-[10px] bg-amber-500/20 text-amber-300 font-bold px-2 py-0.5 rounded-full border border-amber-500/30">
                  Pre-Partida
                </span>
              </div>
              <p className="text-xs text-slate-400">
                Personaliza nombres, fichas, saldos iniciales, tarjetas verticales de propiedades y límite de rondas. En el juego la edición queda bloqueada.
              </p>
            </div>
          </div>

          <button
            onClick={onClose}
            className="p-2 rounded-xl bg-slate-800 text-slate-400 hover:text-white transition-all cursor-pointer"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Section Tabs: Jugadores vs Propiedades */}
        <div className="flex items-center gap-2 bg-slate-950 p-1.5 rounded-2xl border border-slate-800">
          <button
            type="button"
            onClick={() => setActiveTab('players')}
            className={`flex-1 py-2.5 rounded-xl text-xs font-black flex items-center justify-center gap-2 transition-all cursor-pointer ${
              activeTab === 'players'
                ? 'bg-amber-500 text-slate-950 shadow-md shadow-amber-500/20'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            <Users className="w-4 h-4" />
            <span>1. Jugadores y Reglas ({players.length})</span>
          </button>
          <button
            type="button"
            onClick={() => setActiveTab('properties')}
            className={`flex-1 py-2.5 rounded-xl text-xs font-black flex items-center justify-center gap-2 transition-all cursor-pointer ${
              activeTab === 'properties'
                ? 'bg-amber-500 text-slate-950 shadow-md shadow-amber-500/20'
                : 'text-slate-400 hover:text-white'
            }`}
          >
            <Building className="w-4 h-4" />
            <span>2. Propiedades e Imágenes Verticales ({properties.length})</span>
          </button>
        </div>

        {/* TAB 1: PLAYERS AND ROUND RULES */}
        {activeTab === 'players' && (
          <div className="flex flex-col gap-6">
            {/* Global Balance & Reset to $0 Quick Action */}
            <div className="bg-slate-950/80 border border-slate-800 rounded-2xl p-4 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
              <div>
                <span className="text-xs text-slate-400 font-bold uppercase tracking-wider block mb-1">
                  Saldo Inicial Rápido para Todas las Cuentas
                </span>
                <div className="flex flex-wrap items-center gap-2">
                  <button
                    type="button"
                    onClick={handleResetAllToZero}
                    className={`px-3 py-1.5 rounded-xl text-xs font-black transition-all cursor-pointer border ${
                      globalBalance === 0 
                        ? 'bg-emerald-500 text-slate-950 border-emerald-400 shadow-md shadow-emerald-500/30' 
                        : 'bg-slate-800 text-emerald-300 border-slate-700 hover:bg-slate-700'
                    }`}
                  >
                    ⚡ Bajar a $0 a Todos
                  </button>
                  {[1000, 5000, 10000, 15000].map(amt => (
                    <button
                      key={amt}
                      type="button"
                      onClick={() => handleApplyGlobalBalance(amt)}
                      className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all cursor-pointer border ${
                        globalBalance === amt 
                          ? 'bg-amber-500 text-slate-950 border-amber-400' 
                          : 'bg-slate-800 text-slate-300 border-slate-700 hover:bg-slate-700'
                      }`}
                    >
                      ${amt.toLocaleString()}
                    </button>
                  ))}
                </div>
              </div>

              <div className="flex items-center gap-2 w-full sm:w-auto">
                <span className="text-xs text-slate-400 font-semibold">Saldo Global:</span>
                <div className="relative flex-1 sm:w-32">
                  <span className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-xs font-mono font-bold">$</span>
                  <input
                    type="number"
                    min="0"
                    step="500"
                    value={globalBalance}
                    onChange={e => handleApplyGlobalBalance(parseInt(e.target.value) || 0)}
                    className="w-full pl-7 pr-3 py-1.5 bg-slate-900 border border-slate-700 rounded-xl text-sm font-mono font-bold text-white focus:outline-none focus:border-amber-400 text-right"
                  />
                </div>
              </div>
            </div>

            {/* Players List */}
            <div>
              <div className="flex items-center justify-between mb-3">
                <div className="flex items-center gap-2">
                  <Users className="w-4 h-4 text-amber-400" />
                  <h3 className="text-sm font-bold text-white uppercase tracking-wider">
                    Configuración de Jugadores ({players.length}/8)
                  </h3>
                </div>
                {players.length < 8 && (
                  <button
                    type="button"
                    onClick={handleAddPlayer}
                    className="flex items-center gap-1.5 px-3 py-1 bg-amber-500/20 hover:bg-amber-500/30 text-amber-300 border border-amber-500/40 rounded-xl text-xs font-bold transition-all cursor-pointer"
                  >
                    <Plus className="w-3.5 h-3.5" />
                    <span>Agregar Jugador</span>
                  </button>
                )}
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 max-h-60 overflow-y-auto pr-1">
                {players.map((p, idx) => (
                  <div 
                    key={p.id}
                    className="bg-slate-950/70 border border-slate-800 rounded-2xl p-3.5 flex flex-col gap-3 relative group hover:border-slate-700 transition-all"
                  >
                    <div className="flex items-center gap-3">
                      {/* Avatar Picker */}
                      <div className="relative">
                        <button
                          type="button"
                          onClick={() => setActiveAvatarPlayerId(activeAvatarPlayerId === p.id ? null : p.id)}
                          className="w-12 h-12 rounded-xl flex items-center justify-center text-2xl shadow-md border border-white/20 transition-transform active:scale-95 cursor-pointer hover:ring-2 hover:ring-amber-400"
                          style={{ backgroundColor: p.color }}
                          title="Haz clic para cambiar ficha/emoji"
                        >
                          {p.avatar}
                        </button>

                        {activeAvatarPlayerId === p.id && (
                          <div className="absolute left-0 top-14 z-30 bg-slate-900 border border-slate-700 rounded-2xl p-3 shadow-2xl w-60 grid grid-cols-5 gap-2 animate-in fade-in zoom-in-95">
                            {AVATAR_OPTIONS.map(emoji => (
                              <button
                                key={emoji}
                                type="button"
                                onClick={() => handlePlayerAvatarChange(p.id, emoji)}
                                className={`w-9 h-9 rounded-lg flex items-center justify-center text-lg hover:bg-slate-800 transition-colors cursor-pointer ${
                                  p.avatar === emoji ? 'bg-amber-500/30 border border-amber-400' : ''
                                }`}
                              >
                                {emoji}
                              </button>
                            ))}
                          </div>
                        )}
                      </div>

                      {/* Player Name */}
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center justify-between mb-1">
                          <span className="text-[10px] text-slate-400 font-bold uppercase">
                            Jugador #{idx + 1} {idx === 0 ? '(Host)' : ''}
                          </span>
                          {players.length > 2 && (
                            <button
                              type="button"
                              onClick={() => handleRemovePlayer(p.id)}
                              className="text-slate-500 hover:text-rose-400 p-1 transition-colors cursor-pointer"
                              title="Eliminar jugador"
                            >
                              <Trash2 className="w-3.5 h-3.5" />
                            </button>
                          )}
                        </div>
                        <input
                          type="text"
                          value={p.name}
                          maxLength={20}
                          onChange={e => handlePlayerNameChange(p.id, e.target.value)}
                          placeholder="Nombre del jugador"
                          className="w-full bg-slate-900 border border-slate-800 focus:border-amber-400 rounded-lg px-2.5 py-1 text-sm font-bold text-white focus:outline-none"
                        />
                      </div>
                    </div>

                    {/* Color and Initial Balance */}
                    <div className="flex items-center justify-between gap-3 pt-2 border-t border-slate-900">
                      <div className="flex items-center gap-1.5 overflow-x-auto py-1">
                        {COLOR_PALETTE.slice(0, 5).map(c => (
                          <button
                            key={c}
                            type="button"
                            onClick={() => handlePlayerColorChange(p.id, c)}
                            className={`w-4 h-4 rounded-full transition-transform cursor-pointer ${
                              p.color === c ? 'ring-2 ring-white scale-110' : 'opacity-70 hover:opacity-100'
                            }`}
                            style={{ backgroundColor: c }}
                          />
                        ))}
                      </div>

                      <div className="flex items-center gap-1.5">
                        <span className="text-[11px] text-slate-400 font-semibold">Saldo:</span>
                        <div className="relative w-24">
                          <span className="absolute left-2 top-1/2 -translate-y-1/2 text-slate-400 text-xs font-mono">$</span>
                          <input
                            type="number"
                            min="0"
                            step="500"
                            value={p.balance}
                            onChange={e => handlePlayerBalanceChange(p.id, parseInt(e.target.value) || 0)}
                            className="w-full pl-5 pr-2 py-0.5 bg-slate-900 border border-slate-800 rounded-lg text-xs font-mono font-bold text-emerald-400 focus:outline-none focus:border-amber-400 text-right"
                          />
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Game Intelligence & Round Mode Settings */}
            <div className="bg-slate-950/80 border border-slate-800 rounded-2xl p-4 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
              <div>
                <div className="flex items-center gap-2 mb-1">
                  <Clock className="w-4 h-4 text-amber-400" />
                  <span className="text-xs text-white font-bold uppercase tracking-wider">
                    Modalidad de Juego y Límite de Rondas
                  </span>
                </div>
                <p className="text-[11px] text-slate-400 max-w-md">
                  El Banquero IA cuenta cada ronda al pasar todos los jugadores. En modo por rondas, coronará al Gran Campeón al terminar la ronda máxima.
                </p>
              </div>

              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={() => setRoundMode('rounds')}
                  className={`px-3 py-1.5 rounded-xl text-xs font-bold border transition-all cursor-pointer ${
                    roundMode === 'rounds'
                      ? 'bg-amber-500 text-slate-950 border-amber-400'
                      : 'bg-slate-900 text-slate-400 border-slate-800 hover:text-white'
                  }`}
                >
                  ⏱️ Por Rondas
                </button>
                <button
                  type="button"
                  onClick={() => setRoundMode('unlimited')}
                  className={`px-3 py-1.5 rounded-xl text-xs font-bold border transition-all cursor-pointer ${
                    roundMode === 'unlimited'
                      ? 'bg-amber-500 text-slate-950 border-amber-400'
                      : 'bg-slate-900 text-slate-400 border-slate-800 hover:text-white'
                  }`}
                >
                  ♾️ Ilimitado
                </button>

                {roundMode === 'rounds' && (
                  <select
                    value={maxRounds}
                    onChange={e => setMaxRounds(parseInt(e.target.value) || 10)}
                    className="bg-slate-900 border border-slate-700 text-amber-400 font-mono font-bold text-xs rounded-xl px-2.5 py-1.5 focus:outline-none"
                  >
                    <option value={5}>5 Rondas (Rápida)</option>
                    <option value={10}>10 Rondas (Estándar)</option>
                    <option value={15}>15 Rondas (Estratégica)</option>
                    <option value={20}>20 Rondas (Torneo)</option>
                    <option value={30}>30 Rondas (Maratón)</option>
                  </select>
                )}
              </div>
            </div>
          </div>
        )}

        {/* TAB 2: PROPERTIES & VERTICAL IMAGES */}
        {activeTab === 'properties' && (
          <div className="flex flex-col gap-5">
            <div className="flex flex-wrap items-center justify-between gap-3 bg-slate-950/80 p-4 rounded-2xl border border-slate-800">
              <div>
                <h3 className="text-sm font-bold text-white flex items-center gap-2">
                  <span>Inmuebles y Escrituras de la Mesa ({properties.length})</span>
                  <span className="text-[10px] bg-amber-500/20 text-amber-300 font-mono px-2 py-0.5 rounded-md">
                    Fotos Verticales
                  </span>
                </h3>
                <p className="text-xs text-slate-400">
                  Asigna nombre, cantidad de compra, rentas y adjunta una imagen vertical para cada tarjeta de propiedad.
                </p>
              </div>

              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={handleResetPropertiesToDefault}
                  className="px-3 py-1.5 bg-slate-800 hover:bg-slate-700 text-slate-300 rounded-xl text-xs font-bold border border-slate-700 transition-all cursor-pointer"
                >
                  Restablecer
                </button>
                <button
                  type="button"
                  onClick={handleAddProperty}
                  className="flex items-center gap-1.5 px-3.5 py-1.5 bg-amber-500 hover:bg-amber-400 text-slate-950 rounded-xl text-xs font-black shadow-md transition-all cursor-pointer"
                >
                  <Plus className="w-3.5 h-3.5" />
                  <span>Nueva Propiedad</span>
                </button>
              </div>
            </div>

            {/* Vertical Properties Grid */}
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 max-h-96 overflow-y-auto pr-1">
              {properties.map(prop => (
                <div key={prop.id} className="flex justify-center">
                  <VerticalPropertyCard
                    property={prop}
                    isEditable={true}
                    onUpdate={handleUpdateProperty}
                    onDelete={() => handleDeleteProperty(prop.id)}
                  />
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Bottom Actions: Save or Start & Lock */}
        <div className="flex flex-col sm:flex-row items-center justify-between gap-3 pt-3 border-t border-slate-800">
          <button
            type="button"
            onClick={handleQuickSave}
            className="w-full sm:w-auto px-5 py-3 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 font-bold text-xs transition-all cursor-pointer text-center"
          >
            Guardar Cambios (Sin Iniciar)
          </button>

          <button
            type="button"
            onClick={handleConfirmAndStart}
            className="w-full sm:w-auto px-6 py-3.5 rounded-xl bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-slate-950 font-black text-sm shadow-xl shadow-amber-500/25 flex items-center justify-center gap-2 transition-all cursor-pointer active:scale-98"
          >
            <Play className="w-4 h-4 fill-current" />
            <span>🎮 Iniciar Partida (Bloquear Edición)</span>
          </button>
        </div>

      </div>
    </div>
  );
};
