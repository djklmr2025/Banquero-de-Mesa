import React, { useState } from 'react';
import { 
  ArrowLeft, ArrowUpRight, ArrowDownLeft, Building, CreditCard, 
  Send, ShieldCheck, Sparkles, Check, Home, DollarSign 
} from 'lucide-react';
import { BillTemplate, Player, RoomState } from '../types/game';
import { soundFx } from '../services/soundService';
import { DEFAULT_BILLS } from '../data/defaultGameData';

interface PlayerPocketWalletProps {
  player: Player;
  roomState: RoomState;
  onBackToHost: () => void;
  onUpdateRoom: (newState: RoomState) => void;
}

export const PlayerPocketWallet: React.FC<PlayerPocketWalletProps> = ({
  player,
  roomState,
  onBackToHost,
  onUpdateRoom
}) => {
  const [activeTab, setActiveTab] = useState<'wallet' | 'properties' | 'pay'>('wallet');
  const [selectedPayTarget, setSelectedPayTarget] = useState<string>('BANK');
  const [payAmount, setPayAmount] = useState<string>('');
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  // Filter owned properties
  const myProperties = roomState.properties.filter(p => player.properties.includes(p.id));

  // Perform quick payment
  const handleExecutePayment = (amount: number, targetId: string, reason: string) => {
    if (player.balance < amount) {
      soundFx.playBuzzer();
      alert('Fondos insuficientes para realizar este pago.');
      return;
    }

    soundFx.playCoin();

    const updatedPlayers = roomState.players.map(p => {
      if (p.id === player.id) {
        return { ...p, balance: p.balance - amount };
      }
      if (p.id === targetId) {
        return { ...p, balance: p.balance + amount };
      }
      return p;
    });

    onUpdateRoom({
      ...roomState,
      players: updatedPlayers
    });

    setSuccessMsg(`¡Pago exitoso de $${amount.toLocaleString()}!`);
    setTimeout(() => setSuccessMsg(null), 3000);
    setPayAmount('');
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col max-w-md mx-auto border-x border-slate-800 shadow-2xl">
      {/* Mobile Top Navigation */}
      <header className="p-4 bg-slate-900/90 border-b border-slate-800 backdrop-blur-md flex items-center justify-between sticky top-0 z-20">
        <button
          onClick={onBackToHost}
          className="p-2 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-300 transition-all flex items-center gap-1.5 text-xs font-bold"
        >
          <ArrowLeft className="w-4 h-4" />
          <span>Tablet Matriz</span>
        </button>

        <div className="flex items-center gap-2">
          <div 
            className="w-7 h-7 rounded-lg flex items-center justify-center text-sm font-bold text-white shadow-sm"
            style={{ backgroundColor: player.color }}
          >
            {player.avatar}
          </div>
          <span className="font-bold text-sm text-white truncate max-w-[120px]">{player.name}</span>
        </div>

        <span className="text-[10px] font-mono bg-amber-500/20 text-amber-300 px-2 py-1 rounded-md border border-amber-500/30">
          PIN: {roomState.roomId}
        </span>
      </header>

      {/* Main Balance Card (Digital Wallet Style) */}
      <div className="p-5">
        <div className="relative overflow-hidden bg-gradient-to-br from-slate-900 via-slate-900 to-amber-950/40 p-6 rounded-3xl border border-amber-500/30 shadow-2xl">
          <div className="absolute top-0 right-0 p-4 opacity-10">
            <CreditCard className="w-32 h-32 text-amber-400" />
          </div>

          <span className="text-xs text-slate-400 uppercase tracking-widest font-semibold block mb-1">
            Billetera Digital Oficial
          </span>
          <h2 className="text-4xl font-black text-white font-mono tracking-tight">
            ${player.balance.toLocaleString()}
          </h2>
          <div className="mt-4 flex items-center justify-between text-xs text-slate-400 border-t border-slate-800 pt-3">
            <span>{myProperties.length} propiedades</span>
            <span className="text-emerald-400 flex items-center gap-1 font-semibold">
              <ShieldCheck className="w-4 h-4" /> Verificada por Banco FM
            </span>
          </div>
        </div>

        {/* Success Alert */}
        {successMsg && (
          <div className="mt-3 p-3 bg-emerald-500/20 border border-emerald-500/40 rounded-xl text-emerald-300 text-xs font-bold flex items-center gap-2">
            <Check className="w-4 h-4" />
            <span>{successMsg}</span>
          </div>
        )}

        {/* Navigation Tabs */}
        <div className="grid grid-cols-3 gap-2 mt-4 bg-slate-900 p-1.5 rounded-2xl border border-slate-800 text-xs font-bold">
          <button
            onClick={() => setActiveTab('wallet')}
            className={`py-2.5 rounded-xl transition-all ${
              activeTab === 'wallet' ? 'bg-amber-500 text-slate-950 shadow-md' : 'text-slate-400 hover:text-white'
            }`}
          >
            Billetes
          </button>
          <button
            onClick={() => setActiveTab('properties')}
            className={`py-2.5 rounded-xl transition-all ${
              activeTab === 'properties' ? 'bg-amber-500 text-slate-950 shadow-md' : 'text-slate-400 hover:text-white'
            }`}
          >
            Escrituras ({myProperties.length})
          </button>
          <button
            onClick={() => setActiveTab('pay')}
            className={`py-2.5 rounded-xl transition-all ${
              activeTab === 'pay' ? 'bg-amber-500 text-slate-950 shadow-md' : 'text-slate-400 hover:text-white'
            }`}
          >
            Transferir
          </button>
        </div>
      </div>

      {/* TAB 1: Digital Bills Stack */}
      {activeTab === 'wallet' && (
        <div className="px-5 pb-6 flex-1 flex flex-col gap-3">
          <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider">Tus Billetes de Juego</h3>
          <div className="flex flex-col gap-3">
            {DEFAULT_BILLS.map(bill => (
              <div
                key={bill.denomination}
                className={`relative overflow-hidden bg-gradient-to-r ${bill.color} p-4 rounded-2xl border border-white/20 shadow-lg text-white`}
              >
                <div className="absolute right-3 top-2 text-3xl font-black opacity-15 font-mono">
                  ${bill.denomination}
                </div>
                <div className="flex items-center justify-between relative z-10">
                  <div>
                    <span className="text-[10px] tracking-widest uppercase font-bold opacity-80">{bill.stampText}</span>
                    <h4 className="text-xl font-black tracking-tight">{bill.label}</h4>
                  </div>
                  <div className="text-right">
                    <span className="text-xs opacity-80 block">Valor Facial</span>
                    <span className="text-lg font-black font-mono">${bill.denomination.toLocaleString()}</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* TAB 2: Properties & Deeds */}
      {activeTab === 'properties' && (
        <div className="px-5 pb-6 flex-1 flex flex-col gap-3">
          <h3 className="text-xs font-bold text-slate-400 uppercase tracking-wider">
            Tus Títulos Inmobiliarios ({myProperties.length})
          </h3>
          {myProperties.length === 0 ? (
            <div className="p-8 text-center bg-slate-900 rounded-2xl border border-slate-800">
              <Building className="w-12 h-12 text-slate-600 mx-auto mb-3" />
              <p className="text-slate-400 text-sm font-medium">Aún no tienes propiedades compradas.</p>
              <p className="text-slate-500 text-xs mt-1">¡Cae en casillas sin dueño en el tablero para adquirirlas!</p>
            </div>
          ) : (
            <div className="flex flex-col gap-3">
              {myProperties.map(prop => (
                <div
                  key={prop.id}
                  className="bg-slate-900 p-4 rounded-2xl border border-slate-800 flex flex-col gap-2"
                >
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <div className="w-4 h-4 rounded-full" style={{ backgroundColor: prop.color }}></div>
                      <h4 className="font-bold text-white text-sm">{prop.name}</h4>
                    </div>
                    <span className="text-xs text-amber-400 font-mono font-bold">${prop.price.toLocaleString()}</span>
                  </div>
                  <div className="grid grid-cols-2 gap-2 text-xs text-slate-400 bg-slate-950 p-2.5 rounded-xl">
                    <span>Renta Base: ${prop.baseRent}</span>
                    <span>Por Casa: +${prop.rentPerHouse}</span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* TAB 3: Direct Pay & Transfer */}
      {activeTab === 'pay' && (
        <div className="px-5 pb-6 flex-1 flex flex-col gap-4">
          <div className="bg-slate-900 p-5 rounded-2xl border border-slate-800 flex flex-col gap-4">
            <h3 className="text-sm font-bold text-white">Transferencia Inmediata</h3>

            {/* Target Select */}
            <div>
              <label className="text-xs text-slate-400 font-bold block mb-2">Destinatario:</label>
              <select
                value={selectedPayTarget}
                onChange={e => setSelectedPayTarget(e.target.value)}
                className="w-full bg-slate-950 border border-slate-700 rounded-xl p-3 text-sm text-white font-medium"
              >
                <option value="BANK">🏛️ Banco Central Fotorama</option>
                {roomState.players
                  .filter(p => p.id !== player.id)
                  .map(p => (
                    <option key={p.id} value={p.id}>
                      👤 {p.name}
                    </option>
                  ))}
              </select>
            </div>

            {/* Quick Amounts */}
            <div>
              <label className="text-xs text-slate-400 font-bold block mb-2">Montos Rápidos:</label>
              <div className="grid grid-cols-3 gap-2">
                {[500, 1000, 2000, 3000, 5000, 10000].map(amt => (
                  <button
                    key={amt}
                    type="button"
                    onClick={() => setPayAmount(amt.toString())}
                    className="p-2 bg-slate-950 hover:bg-slate-800 border border-slate-800 rounded-lg text-xs font-mono font-bold text-amber-400"
                  >
                    ${amt.toLocaleString()}
                  </button>
                ))}
              </div>
            </div>

            {/* Custom Amount */}
            <div>
              <label className="text-xs text-slate-400 font-bold block mb-2">Monto a Enviar ($):</label>
              <input
                type="number"
                placeholder="Ej. 1500"
                value={payAmount}
                onChange={e => setPayAmount(e.target.value)}
                className="w-full bg-slate-950 border border-slate-700 rounded-xl p-3 text-base text-emerald-400 font-mono font-bold"
              />
            </div>

            <button
              onClick={() => {
                const val = parseFloat(payAmount);
                if (!val || val <= 0) return;
                handleExecutePayment(val, selectedPayTarget, 'Transferencia manual');
              }}
              className="w-full py-4 bg-gradient-to-r from-emerald-500 to-emerald-600 hover:from-emerald-400 hover:to-emerald-500 text-slate-950 font-black rounded-xl shadow-lg shadow-emerald-500/20 flex items-center justify-center gap-2 cursor-pointer transition-all"
            >
              <Send className="w-5 h-5" />
              <span>Enviar Dinero</span>
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
