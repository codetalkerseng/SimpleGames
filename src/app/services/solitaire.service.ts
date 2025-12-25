import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Card, Suit, CardColor, Pile } from '../models/card.model';

interface GameState {
  tableau: Pile[];
  foundations: Pile[];
  stock: Pile;
  waste: Pile;
  moves: number;
  score: number;
}

@Injectable({
  providedIn: 'root'
})
export class SolitaireService {
  private gameState$ = new BehaviorSubject<GameState>(this.initializeGame());

  constructor() {}

  getGameState() {
    return this.gameState$.asObservable();
  }

  getCurrentState(): GameState {
    return this.gameState$.value;
  }

  newGame() {
    this.gameState$.next(this.initializeGame());
  }

  private initializeGame(): GameState {
    const deck = this.createDeck();
    const shuffled = this.shuffle(deck);

    // Create 7 tableau piles
    const tableau: Pile[] = [];
    let cardIndex = 0;

    for (let i = 0; i < 7; i++) {
      const cards: Card[] = [];
      for (let j = 0; j <= i; j++) {
        const card = shuffled[cardIndex++];
        card.faceUp = j === i; // Only top card is face up
        cards.push(card);
      }
      tableau.push({ cards, type: 'tableau', index: i });
    }

    // Remaining cards go to stock
    const stockCards = shuffled.slice(cardIndex).map(card => {
      card.faceUp = false;
      return card;
    });

    return {
      tableau,
      foundations: [
        { cards: [], type: 'foundation', index: 0 },
        { cards: [], type: 'foundation', index: 1 },
        { cards: [], type: 'foundation', index: 2 },
        { cards: [], type: 'foundation', index: 3 }
      ],
      stock: { cards: stockCards, type: 'stock' },
      waste: { cards: [], type: 'waste' },
      moves: 0,
      score: 0
    };
  }

  private createDeck(): Card[] {
    const deck: Card[] = [];
    const suits = [Suit.Hearts, Suit.Diamonds, Suit.Clubs, Suit.Spades];

    suits.forEach(suit => {
      const color = (suit === Suit.Hearts || suit === Suit.Diamonds)
        ? CardColor.Red
        : CardColor.Black;

      for (let value = 1; value <= 13; value++) {
        deck.push({
          suit,
          value,
          color,
          faceUp: false,
          id: `${suit}-${value}`
        });
      }
    });

    return deck;
  }

  private shuffle(deck: Card[]): Card[] {
    const shuffled = [...deck];
    for (let i = shuffled.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]];
    }
    return shuffled;
  }

  drawFromStock() {
    const state = this.getCurrentState();
    if (state.stock.cards.length === 0) {
      // Reset stock from waste
      const wasteCards = [...state.waste.cards].reverse();
      wasteCards.forEach(card => card.faceUp = false);
      state.stock.cards = wasteCards;
      state.waste.cards = [];
    } else {
      // Draw 3 cards (or remaining if less than 3)
      const drawCount = Math.min(3, state.stock.cards.length);
      for (let i = 0; i < drawCount; i++) {
        const card = state.stock.cards.pop()!;
        card.faceUp = true;
        state.waste.cards.push(card);
      }
    }
    state.moves++;
    this.gameState$.next(state);
  }

  canMoveCard(card: Card, toPile: Pile): boolean {
    if (toPile.type === 'foundation') {
      if (toPile.cards.length === 0) {
        return card.value === 1; // Only Ace can start foundation
      }
      const topCard = toPile.cards[toPile.cards.length - 1];
      return card.suit === topCard.suit && card.value === topCard.value + 1;
    }

    if (toPile.type === 'tableau') {
      if (toPile.cards.length === 0) {
        return card.value === 13; // Only King can go on empty tableau
      }
      const topCard = toPile.cards[toPile.cards.length - 1];
      return card.color !== topCard.color && card.value === topCard.value - 1;
    }

    return false;
  }

  moveCards(cards: Card[], fromPile: Pile, toPile: Pile) {
    const state = this.getCurrentState();

    if (cards.length === 0 || !this.canMoveCard(cards[0], toPile)) {
      return;
    }

    // Remove cards from source pile
    const fromIndex = fromPile.cards.indexOf(cards[0]);
    const movedCards = fromPile.cards.splice(fromIndex);

    // Flip the new top card if any remain
    if (fromPile.cards.length > 0 && fromPile.type === 'tableau') {
      fromPile.cards[fromPile.cards.length - 1].faceUp = true;
    }

    // Add cards to destination pile
    toPile.cards.push(...movedCards);

    // Update score
    if (toPile.type === 'foundation') {
      state.score += 10;
    }

    state.moves++;
    this.gameState$.next(state);
  }

  isGameWon(): boolean {
    const state = this.getCurrentState();
    return state.foundations.every(f => f.cards.length === 13);
  }

  getCardDisplay(card: Card): string {
    if (!card.faceUp) return '';

    const values = ['', 'A', '2', '3', '4', '5', '6', '7', '8', '9', '10', 'J', 'Q', 'K'];
    return values[card.value];
  }

  getSuitSymbol(suit: Suit): string {
    const symbols = {
      [Suit.Hearts]: '♥',
      [Suit.Diamonds]: '♦',
      [Suit.Clubs]: '♣',
      [Suit.Spades]: '♠'
    };
    return symbols[suit];
  }
}
