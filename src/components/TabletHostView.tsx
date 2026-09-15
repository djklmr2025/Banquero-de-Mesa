import React, { useState } from 'react';
import { 
  Dices, Volume2, VolumeX, Sparkles, Building, ArrowRight, RotateCcw, 
  Send, AlertTriangle, CheckCircle, Smartphone, Bot, QrCode, Camera, X, UserPlus,
  SlidersHorizontal, Lock, Play, Trophy
} from 'lucide-react';
import confetti from 'canvas-confetti';
import { BankerDialogue, Player, RoomState, SurpriseCard } from '../types/game';
import { soundFx } from '../services/soundService';
import { puterBanker } from '../services/puterAgentService';
import { PreGameSetupModal } from './PreGameSetupModal';
import { GameOverModal } from './GameOverModal';

interface TabletHostViewProps {
  roomState: RoomState;
  onUpdateRoom: (newState: RoomState) => void;
  onSwitchToPlayer: (playerId: string) => void;
}

export const TabletHostView: React.FC<TabletHostViewProps> = ({
  roomState,
  onUpdateRoom,
  onSwitchToPlayer
}) => {
  const [isRolling, setIsRolling] = useState(false);
  const [selectedPropertyId, setSelectedPropertyId] = useState<string | null>(null);
  const [manualAmount, setManualAmount] = useState<string>('');
  const [customBankerMsg, setCustomBankerMsg] = useState<string>('');
  const [isThinkingAI, setIsThinkingAI] = useState(false);
  const [showQrModal, setShowQrModal] = useState(false);
  const [showScannerModal, setShowScannerModal] = useState(false);
  const [showSetupModal, setShowSetupModal] = useState(false);

  const isGamePlaying = roomState.settings.gameStatus === 'playing';
  const isGameEnded = roomState.settings.gameStatus === 'ended';

  const currentPlayer = roomState.players.find(p => p.id === roomState.currentTurnPlayerId) || roomState.players[0];

  // Trigger Banker speech + Voice
  const announce = async (text: string, mood: BankerDialogue['mood'] = 'celebratory') => {
    const dialogue: BankerDialogue = {
      id: `diag_${Date.now()}`,
      text,
      mood,
      timestamp: Date.now()
    };
    const updatedState = { ...roomState, lastDialogue: dialogue };
    onUpdateRoom(updatedState);

    if (roomState.settings.voiceEnabled) {
      puterBanker.speak(text);
    }
  };

  // Roll 3D Dice
  const handleRollDice = async () => {
    if (isRolling) return;
    setIsRolling(true);
    soundFx.playDiceRoll();

    setTimeout(async () => {
      const d1 = Math.floor(Math.random() * 6) + 1;
      const d2 = Math.floor(Math.random() * 6) + 1;
      const total = d1 + d2;
      const isDoubles = d1 === d2;

      let newPos = (currentPlayer.position + total) % (roomState.properties.length + 2); // +2 for GO and Jail
      let passGo = false;
      let newBalance = currentPlayer.balance;

      if (newPos < currentPlayer.position) {
        passGo = true;
        newBalance += roomState.settings.passGoSalary;
        soundFx.playPassGo();
        confetti({ particleCount: 50, spread: 60, origin: { y: 0.7 } });
      }

      const updatedPlayers = roomState.players.map(p => {
        if (p.id === currentPlayer.id) {
          return {
            ...p,
            position: newPos,
            balance: newBalance
          };
        }
        return p;
      });

      const updatedState: RoomState = {
        ...roomState,
        dice: [d1, d2],
        isRolling: false,
        players: updatedPlayers
      };

      onUpdateRoom(updatedState);
      setIsRolling(false);

      // Trigger AI commentary
      if (passGo) {
        setIsThinkingAI(true);
        const commentary = await puterBanker.generateCommentary('PASS_GO', {
          player: currentPlayer,
          amount: roomState.settings.passGoSalary
        });
        setIsThinkingAI(false);
        announce(commentary.text, commentary.mood);
      } else if (isDoubles) {
        setIsThinkingAI(true);
        const commentary = await puterBanker.generateCommentary('DICE_DOUBLE', {
          player: currentPlayer,
          dice: [d1, d2]
        });
        setIsThinkingAI(false);
        announce(commentary.text, commentary.mood);
      }
    }, 600);
  };

  // Pass turn to next player with intelligent round counting
  const handleNextTurn = async () => {
    const currentIndex = roomState.players.findIndex(p => p.id === currentPlayer.id);
    const nextIndex = (currentIndex + 1) % roomState.players.length;
    const nextPlayer = roomState.players[nextIndex];
    const isCompletingRound = nextIndex === 0;
    const newRound = isCompletingRound ? roomState.roundNumber + 1 : roomState.roundNumber;

    // Check if max rounds limit has been reached!
    if (
      isCompletingRound && 
      roomState.settings.maxRounds && 
      roomState.settings.maxRounds > 0 && 
      roomState.roundNumber >= roomState.settings.maxRounds
    ) {
      soundFx.playPassGo();
      confetti({ particleCount: 120, spread: 100, origin: { y: 0.5 } });

      // Calculate leader by net worth
      const propPriceMap: Record<string, number> = {};
      roomState.properties.forEach(p => { propPriceMap[p.id] = p.price; });
      const sorted = [...roomState.players].sort((a, b) => {
        const worthA = a.balance + a.properties.reduce((s, id) => s + (propPriceMap[id] || 0), 0);
        const worthB = b.balance + b.properties.reduce((s, id) => s + (propPriceMap[id] || 0), 0);
        return worthB - worthA;
      });
      const winner = sorted[0];
      const totalWorth = winner.balance + winner.properties.reduce((s, id) => s + (propPriceMap[id] || 0), 0);

      const endedState: RoomState = {
        ...roomState,
        settings: {
          ...roomState.settings,
          gameStatus: 'ended'
        }
      };
      onUpdateRoom(endedState);

      setIsThinkingAI(true);
      const commentary = await puterBanker.generateCommentary('GAME_WON', {
        player: winner,
        totalWorth,
        roundNumber: roomState.settings.maxRounds
      });
      setIsThinkingAI(false);
      announce(commentary.text, 'celebratory');
      return;
    }

    onUpdateRoom({
      ...roomState,
      currentTurnPlayerId: nextPlayer.id,
      roundNumber: newRound
    });

    if (isCompletingRound) {
      soundFx.playPassGo();
      // Round End AI Intelligence
      const sortedByCash = [...roomState.players].sort((a, b) => b.balance - a.balance);
      const leader = sortedByCash[0];

      setIsThinkingAI(true);
      const commentary = await puterBanker.generateCommentary('ROUND_END', {
        player: leader,
        roundNumber: newRound
      });
      setIsThinkingAI(false);
      announce(commentary.text, 'celebratory');
    } else {
      announce(`Turno de ${nextPlayer.name}. ¡Veamos qué te depara la suerte!`, 'celebratory');
    }
  };

  // Give $20,000 Salida
  const handleGrantGoSalary = (player: Player) => {
    soundFx.playPassGo();
    confetti({ particleCount: 70, spread: 70 });
    const updated = roomState.players.map(p => {
      if (p.id === player.id) {
        return { ...p, balance: p.balance + roomState.settings.passGoSalary };
      }
      return p;
    });

    onUpdateRoom({
      ...roomState,
      players: updated
    });

    announce(`¡El Banco entrega formalmente el sueldo de $${roomState.settings.passGoSalary.toLocaleString()} a ${player.name}!`, 'celebratory');
  };

  // Draw surprise card
  const handleDrawCard = async () => {
    soundFx.playCardDraw();
    const cards = [
      { id: '1', title: '¡Premio de Lotería!', description: 'Cobra $10,000 en el Banco.', type: 'RECEIVE_BANK' as const, amount: 10000, rarity: 'legendaria' as const },
      { id: '2', title: 'Multa de Tránsito', description: 'Paga $3,000 de infracción al Banco.', type: 'PAY_BANK' as const, amount: 3000, rarity: 'comun' as const },
      { id: '3', title: '¡Cárcel Directa!', description: 'Ve a prisión por evasión de impuestos.', type: 'GO_TO_JAIL' as const, rarity: 'trampa' as const }
    ];
    const randomCard = cards[Math.floor(Math.random() * cards.length)];

    let updatedPlayers = [...roomState.players];
    if (randomCard.type === 'RECEIVE_BANK' && randomCard.amount) {
      soundFx.playCoin();
      updatedPlayers = updatedPlayers.map(p => p.id === currentPlayer.id ? { ...p, balance: p.balance + randomCard.amount! } : p);
    } else if (randomCard.type === 'PAY_BANK' && randomCard.amount) {
      soundFx.playBuzzer();
      updatedPlayers = updatedPlayers.map(p => p.id === currentPlayer.id ? { ...p, balance: Math.max(0, p.balance - randomCard.amount!) } : p);
    } else if (randomCard.type === 'GO_TO_JAIL') {
      soundFx.playBuzzer();
      updatedPlayers = updatedPlayers.map(p => p.id === currentPlayer.id ? { ...p, inJail: true } : p);
    }

    onUpdateRoom({
      ...roomState,
      activeCard: randomCard,
      players: updatedPlayers
    });

    setIsThinkingAI(true);
    const comm = await puterBanker.generateCommentary('CARD_DRAWN', {
      player: currentPlayer,
      card: randomCard
    });
    setIsThinkingAI(false);
    announce(comm.text, 'dramatic');
  };

  // Buy selected property
  const handleBuyProperty = async (propId: string) => {
    const prop = roomState.properties.find(p => p.id === propId);
    if (!prop) return;

    if (currentPlayer.balance < prop.price) {
      soundFx.playBuzzer();
      announce(`¡Fondos insuficientes! ${currentPlayer.name} no tiene los $${prop.price.toLocaleString()} para comprar ${prop.name}.`, 'strict');
      return;
    }

    soundFx.playCoin();
    const updatedProps = roomState.properties.map(p => p.id === propId ? { ...p, ownerId: currentPlayer.id } : p);
    const updatedPlayers = roomState.players.map(p => {
      if (p.id === currentPlayer.id) {
        return {
          ...p,
          balance: p.balance - prop.price,
          properties: [...p.properties, prop.id]
        };
      }
      return p;
    });

    onUpdateRoom({
      ...roomState,
      properties: updatedProps,
      players: updatedPlayers
    });

    setIsThinkingAI(true);
    const comm = await puterBanker.generateCommentary('BUY_PROPERTY', {
      player: currentPlayer,
      propertyName: prop.name,
      amount: prop.price
    });
    setIsThinkingAI(false);
    announce(comm.text, 'celebratory');
    setSelectedPropertyId(null);
  };

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 p-4 md:p-6 flex flex-col gap-6">
      {/* Top Bar: Room PIN, Title, Puter AI Voice Toggle */}
      <header className="flex flex-wrap items-center justify-between gap-4 bg-slate-900/90 border border-slate-800 p-4 rounded-2xl shadow-xl backdrop-blur-md">
        <div className="flex items-center gap-3">
          <div className="w-12 h-12 rounded-xl bg-gradient-to-tr from-amber-500 to-amber-600 flex items-center justify-center shadow-lg shadow-amber-500/20 text-slate-950 font-black text-2xl">
            FM
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h1 className="text-xl font-bold tracking-tight text-white">Banquero de Mesa IA</h1>
              <span className="text-xs bg-amber-500/20 text-amber-300 font-semibold px-2 py-0.5 rounded-full border border-amber-500/30">Fotorama Edition</span>
            </div>
            <p className="text-xs text-slate-400">
              Tablet Matriz de Sala • Ronda #{roomState.roundNumber}
              {roomState.settings.maxRounds && roomState.settings.maxRounds > 0 ? ` de ${roomState.settings.maxRounds}` : ' (Ilimitada)'}
            </p>
          </div>
        </div>

        {/* Buttons: Pre-Game Edit / Lock, Room PIN & QR Button */}
        <div className="flex flex-wrap items-center gap-2.5">
          {!isGamePlaying ? (
            <button
              onClick={() => setShowSetupModal(true)}
              className="flex items-center gap-1.5 bg-gradient-to-r from-emerald-500 to-emerald-600 hover:from-emerald-400 hover:to-emerald-500 text-slate-950 px-3.5 py-2 rounded-xl text-xs font-black shadow-lg shadow-emerald-500/25 transition-all cursor-pointer animate-pulse"
              title="Configura nombres, avatares, saldos a $0 y rondas antes de iniciar"
            >
              <SlidersHorizontal className="w-4 h-4" />
              <span>Edición antes del juego</span>
            </button>
          ) : (
            <button
              onClick={() => {
                if (confirm('La partida está en curso y los nombres y saldos iniciales están bloqueados.\n\n¿Deseas pausar o reiniciar para reconfigurar jugadores?')) {
                  setShowSetupModal(true);
                }
              }}
              className="flex items-center gap-1.5 bg-slate-950 border border-amber-500/30 text-amber-300 hover:bg-slate-800 px-3 py-2 rounded-xl text-xs font-bold transition-all cursor-pointer"
              title="Partida en curso: Edición bloqueada. Toca para pausar o reiniciar."
            >
              <Lock className="w-3.5 h-3.5 text-amber-400" />
              <span>Edición Bloqueada</span>
            </button>
          )}

          <button
            onClick={() => setShowQrModal(true)}
            className="flex items-center gap-2 bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-slate-950 px-3.5 py-2 rounded-xl font-black text-xs shadow-lg shadow-amber-500/20 transition-all cursor-pointer active:scale-95"
          >
            <QrCode className="w-4 h-4 text-slate-950" />
            <span className="hidden sm:inline">Conectar Celulares (QR)</span>
            <span className="sm:hidden">QR</span>
          </button>

          <div className="flex items-center gap-2 bg-slate-950/80 border border-slate-800 px-3 py-1.5 rounded-xl">
            <span className="text-[10px] text-slate-400 uppercase tracking-wider font-bold">PIN:</span>
            <span className="text-sm font-black tracking-widest text-amber-400 font-mono">{roomState.roomId}</span>
          </div>

          <button
            onClick={() => setShowScannerModal(true)}
            className="flex items-center gap-1.5 bg-slate-800 hover:bg-slate-700 text-slate-200 px-3 py-2 rounded-xl text-xs font-bold border border-slate-700 transition-all cursor-pointer"
            title="Escanear casilla física o carta con la cámara de la tablet"
          >
            <Camera className="w-4 h-4 text-amber-400" />
            <span className="hidden md:inline">Cámara IA</span>
          </button>

          {/* Voice and Puter AI Status */}
          <button 
            onClick={() => {
              const voiceOn = !roomState.settings.voiceEnabled;
              onUpdateRoom({ ...roomState, settings: { ...roomState.settings, voiceEnabled: voiceOn } });
              if (voiceOn) puterBanker.speak("Voz del banquero activada");
            }}
            className={`flex items-center gap-1.5 px-3 py-2 rounded-xl text-xs font-semibold transition-all border ${
              roomState.settings.voiceEnabled 
                ? 'bg-amber-500/20 border-amber-500/50 text-amber-300 shadow-lg shadow-amber-500/10' 
                : 'bg-slate-800 border-slate-700 text-slate-400'
            }`}
          >
            {roomState.settings.voiceEnabled ? <Volume2 className="w-3.5 h-3.5 text-amber-400" /> : <VolumeX className="w-3.5 h-3.5" />}
            <span className="hidden sm:inline">{roomState.settings.voiceEnabled ? 'Voz: ON' : 'Voz: OFF'}</span>
          </button>
        </div>
      </header>

      {/* Pre-Game Callout Notice Banner (When game has not started yet) */}
      {!isGamePlaying && (
        <div className="bg-gradient-to-r from-emerald-950/50 via-slate-900 to-amber-950/40 border border-emerald-500/40 rounded-2xl p-4 flex flex-col md:flex-row items-start md:items-center justify-between gap-4 shadow-xl">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-emerald-500/20 border border-emerald-400/40 flex items-center justify-center text-emerald-400 text-xl font-bold flex-shrink-0">
              🛠️
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h4 className="text-sm font-black text-white">Fase de Preparación y Edición Previa</h4>
                <span className="text-[10px] bg-emerald-500/20 text-emerald-300 font-mono px-2 py-0.5 rounded-full border border-emerald-500/30">
                  Saldo Base: ${roomState.settings.initialBalance.toLocaleString()}
                </span>
                {roomState.settings.maxRounds && roomState.settings.maxRounds > 0 && (
                  <span className="text-[10px] bg-amber-500/20 text-amber-300 font-mono px-2 py-0.5 rounded-full border border-amber-500/30">
                    Modo {roomState.settings.maxRounds} Rondas
                  </span>
                )}
              </div>
              <p className="text-xs text-slate-300 mt-0.5">
                Usa el botón <strong>"Edición antes del juego"</strong> para personalizar nombres, avatares, saldos iniciales (bajar a $0) y número de rondas. Una vez iniciada la partida, la edición quedará completamente bloqueada.
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2 w-full md:w-auto">
            <button
              onClick={() => setShowSetupModal(true)}
              className="flex-1 md:flex-initial px-4 py-2.5 bg-slate-800 hover:bg-slate-700 text-emerald-300 font-bold text-xs rounded-xl border border-emerald-500/30 transition-all cursor-pointer text-center"
            >
              Configurar Cuentas
            </button>
            <button
              onClick={() => {
                soundFx.playPassGo();
                onUpdateRoom({
                  ...roomState,
                  settings: {
                    ...roomState.settings,
                    gameStatus: 'playing'
                  }
                });
                announce('¡Partida iniciada! La edición queda oficialmente bloqueada por el Banquero. ¡Ronda #1!', 'celebratory');
              }}
              className="flex-1 md:flex-initial px-5 py-2.5 bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 text-slate-950 font-black text-xs rounded-xl shadow-lg shadow-amber-500/20 transition-all cursor-pointer flex items-center justify-center gap-1.5"
            >
              <Play className="w-3.5 h-3.5 fill-current" />
              <span>Iniciar Partida</span>
            </button>
          </div>
        </div>
      )}

      {/* Puter Banker Agent Live Dialogue Banner */}
      <div className="relative overflow-hidden bg-gradient-to-r from-amber-950/40 via-slate-900 to-slate-900 border-2 border-amber-500/30 rounded-2xl p-5 shadow-2xl">
        <div className="absolute -top-10 -right-10 w-40 h-40 bg-amber-500/10 rounded-full blur-3xl pointer-events-none"></div>
        <div className="flex items-start gap-4">
          <div className="relative">
            <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-amber-400 to-yellow-600 flex items-center justify-center text-slate-950 shadow-lg shadow-amber-500/30">
              <Bot className="w-8 h-8" />
            </div>
            {isThinkingAI && (
              <span className="absolute -bottom-1 -right-1 flex h-4 w-4">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-amber-400 opacity-75"></span>
                <span className="relative inline-flex rounded-full h-4 w-4 bg-amber-500"></span>
              </span>
            )}
          </div>
          <div className="flex-1">
            <div className="flex items-center gap-2 mb-1">
              <span className="text-xs font-bold uppercase tracking-wider text-amber-400 flex items-center gap-1">
                <Sparkles className="w-3.5 h-3.5" />
                Banquero Puter AI
              </span>
              {isThinkingAI && <span className="text-xs text-amber-300/80 animate-pulse">Pensando comentario...</span>}
            </div>
            <p className="text-lg font-medium text-slate-100 leading-relaxed italic">
              "{roomState.lastDialogue?.text || '¡Bienvenidos! Tira los dados y administra tu dinero sabiamente.'}"
            </p>
          </div>
        </div>
      </div>

      {/* Main Grid: Left Controls (Dice, Turn, Quick Actions) & Right Board (Players, Properties) */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* LEFT PANEL: 3D Dices, Current Turn, Actions */}
        <div className="lg:col-span-5 flex flex-col gap-6">
          {/* Current Turn Card */}
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl flex flex-col gap-5">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-slate-400 uppercase tracking-wider">Turno en Curso</span>
              <span className="text-xs bg-slate-800 text-slate-300 px-3 py-1 rounded-full font-mono">
                {currentPlayer.inJail ? '🔒 En Cárcel' : 'En Libertad'}
              </span>
            </div>

            <div className="flex items-center gap-4 bg-slate-950/60 p-4 rounded-xl border border-slate-800">
              <div 
                className="w-14 h-14 rounded-2xl flex items-center justify-center text-2xl font-bold text-white shadow-lg"
                style={{ backgroundColor: currentPlayer.color }}
              >
                {currentPlayer.avatar}
              </div>
              <div className="flex-1">
                <h3 className="text-xl font-black text-white">{currentPlayer.name}</h3>
                <p className="text-2xl font-black text-emerald-400 font-mono">
                  ${currentPlayer.balance.toLocaleString()}
                </p>
              </div>
            </div>

            {/* 3D Dice Rolling Station */}
            <div className="flex flex-col items-center justify-center p-6 bg-slate-950/80 rounded-2xl border border-slate-800 gap-4">
              <div className="flex items-center gap-6">
                <div className={`w-20 h-20 bg-gradient-to-br from-white to-slate-200 text-slate-950 rounded-2xl shadow-2xl flex items-center justify-center text-4xl font-black border-4 border-slate-300 ${isRolling ? 'animate-dice-roll' : ''}`}>
                  {roomState.dice[0]}
                </div>
                <div className={`w-20 h-20 bg-gradient-to-br from-white to-slate-200 text-slate-950 rounded-2xl shadow-2xl flex items-center justify-center text-4xl font-black border-4 border-slate-300 ${isRolling ? 'animate-dice-roll' : ''}`}>
                  {roomState.dice[1]}
                </div>
              </div>

              <div className="flex items-center gap-3 w-full mt-2">
                <button
                  onClick={handleRollDice}
                  disabled={isRolling}
                  className="flex-1 py-4 bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 active:scale-98 text-slate-950 font-black text-lg rounded-xl shadow-lg shadow-amber-500/25 flex items-center justify-center gap-2 transition-all cursor-pointer disabled:opacity-50"
                >
                  <Dices className="w-6 h-6" />
                  <span>{isRolling ? 'Lanzando Dados...' : 'Girar Dados (Espacio)'}</span>
                </button>

                <button
                  onClick={handleNextTurn}
                  className="px-5 py-4 bg-slate-800 hover:bg-slate-700 text-white font-bold rounded-xl transition-all cursor-pointer flex items-center gap-2 border border-slate-700"
                  title="Pasar al siguiente jugador"
                >
                  <span>Siguiente</span>
                  <ArrowRight className="w-5 h-5" />
                </button>
              </div>
            </div>

            {/* Quick Bank Operations */}
            <div className="grid grid-cols-2 gap-3">
              <button
                onClick={() => handleGrantGoSalary(currentPlayer)}
                className="p-3 bg-emerald-950/50 hover:bg-emerald-900/60 border border-emerald-500/30 rounded-xl text-left transition-all cursor-pointer"
              >
                <div className="flex items-center gap-2 text-emerald-400 font-bold text-sm">
                  <CheckCircle className="w-4 h-4" />
                  <span>Pagar Salida</span>
                </div>
                <p className="text-xs text-emerald-300/70 mt-1">+$20,000 al jugador actual</p>
              </button>

              <button
                onClick={handleDrawCard}
                className="p-3 bg-purple-950/50 hover:bg-purple-900/60 border border-purple-500/30 rounded-xl text-left transition-all cursor-pointer"
              >
                <div className="flex items-center gap-2 text-purple-400 font-bold text-sm">
                  <Sparkles className="w-4 h-4" />
                  <span>Carta Sorpresa</span>
                </div>
                <p className="text-xs text-purple-300/70 mt-1">Destapar carta de trampa o premio</p>
              </button>
            </div>
          </div>
        </div>

        {/* RIGHT PANEL: Players Overview & Interactive Properties */}
        <div className="lg:col-span-7 flex flex-col gap-6">
          {/* Players Roster */}
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl">
            <h3 className="text-base font-bold text-white mb-4 flex items-center justify-between">
              <span>Jugadores en la Mesa ({roomState.players.length})</span>
              <span className="text-xs text-slate-400">Toca para abrir su billetera móvil</span>
            </h3>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              {roomState.players.map(player => {
                const isCurrent = player.id === currentPlayer.id;
                return (
                  <div
                    key={player.id}
                    onClick={() => onSwitchToPlayer(player.id)}
                    className={`p-4 rounded-xl border transition-all cursor-pointer flex items-center gap-3 ${
                      isCurrent
                        ? 'bg-amber-500/10 border-amber-500/60 shadow-lg shadow-amber-500/10'
                        : 'bg-slate-950/60 border-slate-800 hover:border-slate-700'
                    }`}
                  >
                    <div 
                      className="w-12 h-12 rounded-xl flex items-center justify-center text-xl font-bold text-white shadow-md flex-shrink-0"
                      style={{ backgroundColor: player.color }}
                    >
                      {player.avatar}
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center justify-between">
                        <h4 className="font-bold text-white truncate text-sm">{player.name}</h4>
                        {isCurrent && <span className="text-[10px] bg-amber-500 text-slate-950 px-1.5 py-0.5 rounded font-black">TURNO</span>}
                      </div>
                      <p className="text-lg font-black text-emerald-400 font-mono">${player.balance.toLocaleString()}</p>
                      <p className="text-[11px] text-slate-400 truncate">
                        {player.properties.length} propiedades • Casilla #{player.position}
                      </p>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Properties Board Grid */}
          <div className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl">
            <h3 className="text-base font-bold text-white mb-4 flex items-center justify-between">
              <span>Propiedades Inmobiliarias Fotorama</span>
              <span className="text-xs text-slate-400">Compra directa desde la tablet</span>
            </h3>

            <div className="grid grid-cols-2 sm:grid-cols-3 gap-2.5 max-h-72 overflow-y-auto pr-1">
              {roomState.properties.map(prop => {
                const owner = roomState.players.find(p => p.id === prop.ownerId);
                return (
                  <div
                    key={prop.id}
                    onClick={() => setSelectedPropertyId(prop.id)}
                    className={`p-3 rounded-xl border transition-all cursor-pointer flex flex-col justify-between ${
                      selectedPropertyId === prop.id 
                        ? 'border-amber-400 bg-slate-800' 
                        : 'border-slate-800 bg-slate-950/50 hover:border-slate-700'
                    }`}
                  >
                    <div>
                      <div className="h-2 rounded-full mb-2" style={{ backgroundColor: prop.color }}></div>
                      <h4 className="font-bold text-xs text-white truncate">{prop.name}</h4>
                      <p className="text-xs text-amber-400 font-mono font-semibold">${prop.price.toLocaleString()}</p>
                    </div>

                    <div className="mt-2 pt-2 border-t border-slate-800 flex items-center justify-between text-[11px]">
                      {owner ? (
                        <span className="text-slate-300 font-medium truncate">Dueño: {owner.name}</span>
                      ) : (
                        <span className="text-emerald-400 font-semibold">Disponible</span>
                      )}
                      {prop.houses > 0 && <span className="text-amber-400">🏠x{prop.houses}</span>}
                    </div>
                  </div>
                );
              })}
            </div>

            {/* Selected Property Buy Action */}
            {selectedPropertyId && (
              <div className="mt-4 p-4 bg-slate-950 border border-amber-500/40 rounded-xl flex items-center justify-between gap-4">
                {(() => {
                  const p = roomState.properties.find(x => x.id === selectedPropertyId);
                  if (!p) return null;
                  return (
                    <>
                      <div>
                        <span className="text-xs text-slate-400">Comprar propiedad para {currentPlayer.name}:</span>
                        <h4 className="text-base font-bold text-white">{p.name} - ${p.price.toLocaleString()}</h4>
                      </div>
                      <div className="flex items-center gap-2">
                        <button
                          onClick={() => setSelectedPropertyId(null)}
                          className="px-3 py-2 text-xs font-semibold text-slate-400 hover:text-white"
                        >
                          Cancelar
                        </button>
                        <button
                          onClick={() => handleBuyProperty(p.id)}
                          className="px-4 py-2 bg-amber-500 hover:bg-amber-400 text-slate-950 font-bold text-xs rounded-lg transition-all"
                        >
                          Comprar Ahora
                        </button>
                      </div>
                    </>
                  );
                })()}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* MODAL 1: QR & PIN CONNECTION FOR ALL 8 PLAYERS */}
      {showQrModal && (
        <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-md flex items-center justify-center p-4 animate-in fade-in duration-200">
          <div className="bg-slate-900 border border-slate-700 rounded-3xl p-6 max-w-lg w-full shadow-2xl flex flex-col gap-5 relative">
            <button
              onClick={() => setShowQrModal(false)}
              className="absolute top-5 right-5 p-2 rounded-xl bg-slate-800 text-slate-400 hover:text-white transition-all cursor-pointer"
            >
              <X className="w-5 h-5" />
            </button>

            <div className="flex items-center gap-3">
              <div className="w-12 h-12 rounded-2xl bg-amber-500/20 border border-amber-500/30 flex items-center justify-center text-amber-400">
                <QrCode className="w-7 h-7" />
              </div>
              <div>
                <h3 className="text-lg font-black text-white">Conectar Billeteras Móviles (QR)</h3>
                <p className="text-xs text-slate-400">Escanea con la cámara de tu smartphone para jugar</p>
              </div>
            </div>

            {/* QR Code Card */}
            <div className="flex flex-col items-center justify-center bg-white p-6 rounded-2xl shadow-inner gap-3">
              <img
                src={`https://api.qrserver.com/v1/create-qr-code/?size=240x240&margin=8&data=${encodeURIComponent(
                  typeof window !== 'undefined' ? `${window.location.origin}?room=${roomState.roomId}&view=wallet` : `https://banquero-de-mesa.vercel.app?room=${roomState.roomId}&view=wallet`
                )}`}
                alt="Código QR de Conexión de Sala"
                className="w-52 h-52 rounded-xl shadow-sm"
              />
              <div className="text-center">
                <span className="text-[10px] uppercase tracking-widest font-black text-slate-500 block">PIN Directo de Sala</span>
                <span className="text-2xl font-black font-mono tracking-widest text-slate-950">{roomState.roomId}</span>
              </div>
            </div>

            {/* Players List quick selection */}
            <div>
              <span className="text-xs text-slate-400 font-bold block mb-2">
                Billeteras de la Mesa ({roomState.players.length}/8 jugadores):
              </span>
              <div className="grid grid-cols-2 gap-2 max-h-40 overflow-y-auto pr-1">
                {roomState.players.map(p => (
                  <button
                    key={p.id}
                    onClick={() => {
                      setShowQrModal(false);
                      onSwitchToPlayer(p.id);
                    }}
                    className="p-2 rounded-xl bg-slate-950/80 hover:bg-slate-800 border border-slate-800 flex items-center gap-2 text-left transition-all cursor-pointer"
                  >
                    <span className="text-base">{p.avatar}</span>
                    <div className="truncate">
                      <h5 className="text-xs font-bold text-white truncate">{p.name}</h5>
                      <span className="text-[10px] text-emerald-400 font-mono">${p.balance.toLocaleString()}</span>
                    </div>
                  </button>
                ))}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* MODAL 2: CAMERA VISION ARBITER */}
      {showScannerModal && (
        <div className="fixed inset-0 z-50 bg-black/85 backdrop-blur-md flex items-center justify-center p-4 animate-in fade-in duration-200">
          <div className="bg-slate-900 border border-slate-700 rounded-3xl p-6 max-w-md w-full shadow-2xl flex flex-col gap-4 relative">
            <button
              onClick={() => setShowScannerModal(false)}
              className="absolute top-5 right-5 p-2 rounded-xl bg-slate-800 text-slate-400 hover:text-white transition-all cursor-pointer"
            >
              <X className="w-5 h-5" />
            </button>

            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-amber-500/20 border border-amber-500/30 flex items-center justify-center text-amber-400">
                <Camera className="w-6 h-6" />
              </div>
              <div>
                <h3 className="text-base font-black text-white">Escáner de Visión IA Fotorama</h3>
                <p className="text-xs text-slate-400">Apunta la cámara a tu tablero o carta física</p>
              </div>
            </div>

            {/* Camera Viewfinder Mockup */}
            <div className="relative aspect-video bg-black rounded-2xl border-2 border-dashed border-amber-500/50 flex flex-col items-center justify-center overflow-hidden">
              <div className="absolute inset-0 bg-gradient-to-b from-amber-500/10 via-transparent to-amber-500/10 animate-pulse"></div>
              <Camera className="w-12 h-12 text-amber-400/50 mb-2" />
              <p className="text-xs text-slate-400 text-center px-4 relative z-10">
                Lente activo. Enfoca la casilla física donde cayó tu ficha o la carta de trampa para validación automática.
              </p>
            </div>

            <button
              onClick={() => {
                setShowScannerModal(false);
                soundFx.playCardDraw();
                announce(`¡Visión IA activada! El Banquero detectó la carta física en la mesa. Aplicando reglas de juego.`, 'celebratory');
              }}
              className="w-full py-3.5 bg-amber-500 hover:bg-amber-400 text-slate-950 font-black rounded-xl text-xs transition-all shadow-lg shadow-amber-500/20 cursor-pointer"
            >
              Simular Reconocimiento de Carta
            </button>
          </div>
        </div>
      )}

      {/* MODAL 3: PRE-GAME SETUP & CUSTOMIZATION MODAL */}
      {showSetupModal && (
        <PreGameSetupModal
          roomState={roomState}
          onClose={() => setShowSetupModal(false)}
          onQuickSave={(newState) => {
            onUpdateRoom(newState);
          }}
          onSaveAndStart={(newState) => {
            onUpdateRoom(newState);
            setShowSetupModal(false);
            announce('¡Partida iniciada! La edición de nombres y saldos queda formalmente bloqueada. ¡Ronda #1!', 'celebratory');
          }}
        />
      )}

      {/* MODAL 4: GAME OVER CELEBRATION & WINNER CEREMONY */}
      {isGameEnded && (
        <GameOverModal
          roomState={roomState}
          onContinueExtraRounds={() => {
            const extraRounds = (roomState.settings.maxRounds || roomState.roundNumber) + 5;
            onUpdateRoom({
              ...roomState,
              settings: {
                ...roomState.settings,
                maxRounds: extraRounds,
                gameStatus: 'playing'
              }
            });
            announce(`¡Tiempo extra concedido! Jugaremos hasta la ronda #${extraRounds}. ¡A defender las propiedades!`, 'celebratory');
          }}
          onRestartNewGame={() => {
            setShowSetupModal(true);
            onUpdateRoom({
              ...roomState,
              roundNumber: 1,
              settings: {
                ...roomState.settings,
                gameStatus: 'setup'
              }
            });
          }}
        />
      )}
    </div>
  );
};
