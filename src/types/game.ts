export interface Player {
  id: string;
  name: string;
  avatar: string;
  color: string;
  balance: number;
  position: number;
  inJail: boolean;
  jailTurns: number;
  properties: string[]; // IDs of properties owned
  bills: Record<number, number>; // denomination -> count
}

export interface BoardProperty {
  id: string;
  name: string;
  group: string;
  price: number;
  baseRent: number;
  rentPerHouse: number;
  houses: number; // 0-4 houses, 5 = hotel
  ownerId: string | null;
  color: string;
  mortgaged: boolean;
  image?: string; // Vertical property card image
  description?: string; // Descriptive or thematic text
}

export interface SurpriseCard {
  id: string;
  title: string;
  description: string;
  type: 'PAY_BANK' | 'RECEIVE_BANK' | 'MOVE_TO' | 'GO_TO_JAIL' | 'PAY_PLAYERS' | 'RECEIVE_FROM_PLAYERS';
  amount?: number;
  targetPosition?: number;
  icon?: string;
  rarity?: 'comun' | 'rara' | 'trampa' | 'legendaria';
  backgroundImage?: string; // Background image for luck/trap card
  borderColor?: string;     // Custom border outline color
  textColor?: string;       // Custom text color overlay
}

export interface BillTemplate {
  denomination: number;
  color: string;
  textColor: string;
  label: string;
  iconName: string;
  signature: string;
  stampText: string;
}

export interface GameTransaction {
  id: string;
  timestamp: number;
  fromPlayerId: string | 'BANK';
  toPlayerId: string | 'BANK';
  amount: number;
  reason: string;
}

export interface BankerDialogue {
  id: string;
  text: string;
  mood: 'funny' | 'strict' | 'dramatic' | 'celebratory' | 'sarcastic';
  timestamp: number;
}

export interface RoomState {
  roomId: string;
  hostName: string;
  currentTurnPlayerId: string;
  roundNumber: number;
  dice: [number, number];
  isRolling: boolean;
  activeCard: SurpriseCard | null;
  players: Player[];
  properties: BoardProperty[];
  history: GameTransaction[];
  lastDialogue: BankerDialogue | null;
  customBoardImage?: string; // Custom uploaded 3D board image / texture
  settings: {
    passGoSalary: number;
    initialBalance: number;
    currencySymbol: string;
    currencyName: string;
    aiCommentaryEnabled: boolean;
    voiceEnabled: boolean;
    gameStatus?: 'setup' | 'playing' | 'ended';
    maxRounds?: number; // 0 = ilimitado, > 0 = límite de rondas
  };
}

export interface ModPack {
  id: string;
  name: string;
  author: string;
  version: string;
  description?: string;
  currencyName: string;
  currencySymbol: string;
  boardImage?: string;     // 3D board texture/image
  playerSpins?: string[];  // Custom tokens/spins for players
  bills: BillTemplate[];
  properties: BoardProperty[];
  cards: SurpriseCard[];
  isRelease?: boolean;     // Closed game release ready to play
  updatedAt?: number;
}
