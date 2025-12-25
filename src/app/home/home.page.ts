import { Component, OnInit } from '@angular/core';
import { SolitaireService } from '../services/solitaire.service';
import { Card, Pile } from '../models/card.model';

@Component({
  selector: 'app-home',
  templateUrl: 'home.page.html',
  styleUrls: ['home.page.scss'],
})
export class HomePage implements OnInit {
  tableau: Pile[] = [];
  foundations: Pile[] = [];
  stock: Pile = { cards: [], type: 'stock' };
  waste: Pile = { cards: [], type: 'waste' };
  moves = 0;
  score = 0;

  draggedCards: Card[] = [];
  dragSource: Pile | null = null;
  validDropZones: Set<Pile> = new Set();

  constructor(private solitaireService: SolitaireService) {}

  ngOnInit() {
    this.solitaireService.getGameState().subscribe(state => {
      this.tableau = state.tableau;
      this.foundations = state.foundations;
      this.stock = state.stock;
      this.waste = state.waste;
      this.moves = state.moves;
      this.score = state.score;

      if (this.solitaireService.isGameWon()) {
        setTimeout(() => {
          alert('Congratulations! You won! 🎉');
        }, 100);
      }
    });
  }

  newGame() {
    this.solitaireService.newGame();
  }

  drawCard() {
    this.solitaireService.drawFromStock();
  }

  getCardDisplay(card: Card): string {
    return this.solitaireService.getCardDisplay(card);
  }

  getSuitSymbol(card: Card): string {
    return this.solitaireService.getSuitSymbol(card.suit);
  }

  getCardCount(pile: Pile): number {
    return pile.cards.length;
  }

  isValidDropZone(pile: Pile): boolean {
    return this.validDropZones.has(pile);
  }

  onDragStart(event: DragEvent, cards: Card[], pile: Pile) {
    if (!cards[0].faceUp) {
      event.preventDefault();
      return;
    }

    this.draggedCards = cards;
    this.dragSource = pile;
    event.dataTransfer!.effectAllowed = 'move';

    // Calculate valid drop zones
    this.updateValidDropZones();
  }

  onDragOver(event: DragEvent) {
    event.preventDefault();
    event.dataTransfer!.dropEffect = 'move';
  }

  onDrop(event: DragEvent, toPile: Pile) {
    event.preventDefault();

    if (this.draggedCards.length > 0 && this.dragSource) {
      this.solitaireService.moveCards(this.draggedCards, this.dragSource, toPile);
    }

    this.clearDragState();
  }

  onDragEnd() {
    this.clearDragState();
  }

  private clearDragState() {
    this.draggedCards = [];
    this.dragSource = null;
    this.validDropZones.clear();
  }

  private updateValidDropZones() {
    this.validDropZones.clear();

    if (this.draggedCards.length === 0) return;

    const testCard = this.draggedCards[0];

    // Check all tableau piles
    for (const pile of this.tableau) {
      if (pile !== this.dragSource && this.solitaireService.canMoveCard(testCard, pile)) {
        this.validDropZones.add(pile);
      }
    }

    // Check foundation piles (only for single cards)
    if (this.draggedCards.length === 1) {
      for (const pile of this.foundations) {
        if (this.solitaireService.canMoveCard(testCard, pile)) {
          this.validDropZones.add(pile);
        }
      }
    }
  }

  onDoubleClick(card: Card, pile: Pile) {
    if (!card.faceUp) return;

    // Get all cards from this card to the end
    const cardIndex = pile.cards.indexOf(card);
    const cardsToMove = pile.cards.slice(cardIndex);

    // Try to auto-move to foundation first (only for single cards)
    if (cardsToMove.length === 1) {
      for (const foundation of this.foundations) {
        if (this.solitaireService.canMoveCard(card, foundation)) {
          this.solitaireService.moveCards(cardsToMove, pile, foundation);
          return;
        }
      }
    }

    // Try to move to any valid tableau pile
    for (const tableau of this.tableau) {
      if (tableau !== pile && this.solitaireService.canMoveCard(card, tableau)) {
        this.solitaireService.moveCards(cardsToMove, pile, tableau);
        return;
      }
    }
  }

  onWasteDoubleClick() {
    const topCard = this.getTopWasteCard();
    if (topCard) {
      this.onDoubleClick(topCard, this.waste);
    }
  }

  getCardsFromIndex(pile: Pile, index: number): Card[] {
    return pile.cards.slice(index);
  }

  getTopWasteCard(): Card | null {
    return this.waste.cards.length > 0
      ? this.waste.cards[this.waste.cards.length - 1]
      : null;
  }

  onWasteCardDragStart(event: DragEvent) {
    const topCard = this.getTopWasteCard();
    if (topCard) {
      this.onDragStart(event, [topCard], this.waste);
    }
  }
}
