export enum Suit {
  Hearts = 'hearts',
  Diamonds = 'diamonds',
  Clubs = 'clubs',
  Spades = 'spades'
}

export enum CardColor {
  Red = 'red',
  Black = 'black'
}

export interface Card {
  suit: Suit;
  value: number; // 1-13 (Ace-King)
  color: CardColor;
  faceUp: boolean;
  id: string;
}

export interface Pile {
  cards: Card[];
  type: 'tableau' | 'foundation' | 'stock' | 'waste';
  index?: number;
}
