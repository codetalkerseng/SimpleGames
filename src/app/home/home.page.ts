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

  onDragStart(event: DragEvent, cards: Card[], pile: Pile) {
    if (!cards[0].faceUp) {
      event.preventDefault();
      return;
    }

    this.draggedCards = cards;
    this.dragSource = pile;
    event.dataTransfer!.effectAllowed = 'move';
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

    this.draggedCards = [];
    this.dragSource = null;
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
