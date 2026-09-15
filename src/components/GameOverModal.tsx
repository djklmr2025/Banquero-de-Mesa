import React from 'react';
import { Trophy, Award, Sparkles, RotateCcw, ArrowRight, DollarSign, Home } from 'lucide-react';
import { Player, RoomState } from '../types/game';

interface GameOverModalProps {
  roomState: RoomState;
  onRestartNewGame: () => void;
  onContinueExtraRounds: () => void;
}

export const GameOverModal: React.FC<GameOverModalProps> = ({
  roomState,
  onRestartNewGame,
  onContinueExtraRounds
}) => {
  // Calculate net worth for each player: Cash + Total Property Value
  const propPriceMap: Record<string, number> = {};
  roomState.properties.forEach(p => {
    propPriceMap[p.id] = p.price;
  });

  const rankedPlayers = [...roomState.players].map(p => {
    const propertiesWorth = p.properties.reduce((sum, propId) => sum + (propPriceMap[propId] || 0), 0);
    const totalWorth = p.balance + propertiesWorth;
    return {
      ...p,
      propertiesWorth,
      totalWorth
    };
  }).sort((a, b) => b.totalWorth - a.totalWorth);

  const champion = rankedPlayers[0];

  return (
    <div className="fixed inset-0 z-50 bg-black/90 backdrop-blur-lg flex items-center justify-center p-4 overflow-y-auto animate-in fade-in duration-300">
      <div className="bg-gradient-to-b from-slate-900 via-slate-900 to-amber-950/40 border-2 border-amber-400 rounded-3xl p-6 sm:p-8 max-w-xl w-full shadow-2xl flex flex-col items-center text-center gap-6 relative">
        
        {/* Glowing Trophy Icon */}
        <div className="relative">
          <div className="absolute inset-0 bg-amber-400/20 blur-2xl rounded-full"></div>
          <div className="w-20 h-20 rounded-3xl bg-gradient-to-tr from-amber-400 to-yellow-600 flex items-center justify-center text-slate-950 shadow-xl shadow-amber-500/40 relative z-10 animate-bounce">
            <Trophy className="w-10 h-10" />
          </div>
        </div>

        {/* Title & Celebration */}
        <div>
          <span className="text-xs font-black uppercase tracking-widest text-amber-400 bg-amber-400/10 px-3 py-1 rounded-full border border-amber-400/30">
            ¡FIN DE LA PARTIDA!
          </span>
          <h2 className="text-3xl sm:text-4xl font-black text-white mt-2">
            ¡{champion?.name} es el Gran Campeón!
          </h2>
          <p className="text-xs sm:text-sm text-slate-300 mt-1">
            Se completaron las {roomState.settings.maxRounds} rondas acordadas. ¡El Banquero IA concluye la auditoría oficial!
          </p>
        </div>

        {/* Champion Showcase Card */}
        {champion && (
          <div className="w-full bg-slate-950/80 border-2 border-amber-400/50 p-5 rounded-2xl flex items-center justify-between gap-4 shadow-xl">
            <div className="flex items-center gap-3.5">
              <div 
                className="w-14 h-14 rounded-2xl flex items-center justify-center text-3xl shadow-lg border border-white/20"
                style={{ backgroundColor: champion.color }}
              >
                {champion.avatar}
              </div>
              <div className="text-left">
                <span className="text-[10px] text-amber-400 font-bold uppercase tracking-wider block">1er Lugar • Campeón Supremo</span>
                <h4 className="text-xl font-black text-white">{champion.name}</h4>
                <div className="flex items-center gap-3 text-xs text-slate-400 mt-0.5">
                  <span>Efectivo: ${champion.balance.toLocaleString()}</span>
                  <span>•</span>
                  <span>Propiedades: ${champion.propertiesWorth.toLocaleString()}</span>
                </div>
              </div>
            </div>
            <div className="text-right">
              <span className="text-[10px] text-slate-400 uppercase font-bold block">Patrimonio Total</span>
              <span className="text-2xl font-black text-emerald-400 font-mono">
                ${champion.totalWorth.toLocaleString()}
              </span>
            </div>
          </div>
        )}

        {/* Full Leaderboard Table */}
        <div className="w-full">
          <span className="text-xs font-bold text-slate-400 uppercase tracking-wider block text-left mb-2">
            Tabla Oficial de Posiciones:
          </span>
          <div className="flex flex-col gap-2 max-h-48 overflow-y-auto pr-1">
            {rankedPlayers.map((player, idx) => (
              <div 
                key={player.id}
                className={`p-3 rounded-xl border flex items-center justify-between text-xs transition-all ${
                  idx === 0 
                    ? 'bg-amber-500/10 border-amber-500/40 text-white' 
                    : 'bg-slate-950/60 border-slate-800 text-slate-300'
                }`}
              >
                <div className="flex items-center gap-2.5">
                  <span className="font-mono font-black text-amber-400 w-4">#{idx + 1}</span>
                  <span className="text-base">{player.avatar}</span>
                  <span className="font-bold">{player.name}</span>
                </div>
                <div className="flex items-center gap-4">
                  <span className="text-slate-400 text-[11px] hidden sm:inline">
                    {player.properties.length} props (${player.propertiesWorth.toLocaleString()})
                  </span>
                  <span className="font-mono font-black text-emerald-400 text-sm">
                    ${player.totalWorth.toLocaleString()}
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Buttons: Restart or Extra Rounds */}
        <div className="flex flex-col sm:flex-row items-center justify-center gap-3 w-full pt-2">
          <button
            type="button"
            onClick={onContinueExtraRounds}
            className="w-full sm:w-auto px-5 py-3 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-200 font-bold text-xs transition-all cursor-pointer"
          >
            +5 Rondas Extra
          </button>
          <button
            type="button"
            onClick={onRestartNewGame}
            className="w-full sm:w-auto px-6 py-3 rounded-xl bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-slate-950 font-black text-xs shadow-lg shadow-amber-500/25 flex items-center justify-center gap-2 transition-all cursor-pointer"
          >
            <RotateCcw className="w-4 h-4" />
            <span>Configurar Nueva Partida</span>
          </button>
        </div>

      </div>
    </div>
  );
};
