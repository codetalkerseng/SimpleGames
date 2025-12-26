import { Component, OnInit } from '@angular/core';
import { AlertController } from '@ionic/angular';
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
  private hasShownNoMovesAlert = false;

  constructor(
    private solitaireService: SolitaireService,
    private alertController: AlertController
  ) {}

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
          this.showWinAlert();
        }, 100);
      } else if (state.moves > 0 && !this.hasShownNoMovesAlert) {
        // Check for no more moves (but only after at least one move has been made)
        setTimeout(() => {
          this.checkForNoMoreMoves();
        }, 300);
      }
    });
  }

  newGame() {
    this.hasShownNoMovesAlert = false;
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

  private checkForNoMoreMoves() {
    if (this.hasAnyValidMoves()) {
      return;
    }

    this.hasShownNoMovesAlert = true;
    this.showNoMovesAlert();
  }

  private hasAnyValidMoves(): boolean {
    // Check if stock has cards to draw
    if (this.stock.cards.length > 0) {
      return true;
    }

    // Check if waste can be recycled (stock is empty but waste has cards)
    if (this.stock.cards.length === 0 && this.waste.cards.length > 0) {
      return true;
    }

    // Check if any waste card can move
    const wasteCard = this.getTopWasteCard();
    if (wasteCard) {
      // Check foundations
      for (const foundation of this.foundations) {
        if (this.solitaireService.canMoveCard(wasteCard, foundation)) {
          return true;
        }
      }
      // Check tableau
      for (const pile of this.tableau) {
        if (this.solitaireService.canMoveCard(wasteCard, pile)) {
          return true;
        }
      }
    }

    // Check if any tableau card can move
    for (const sourcePile of this.tableau) {
      for (let i = 0; i < sourcePile.cards.length; i++) {
        const card = sourcePile.cards[i];
        if (!card.faceUp) continue;

        // Check foundations (only single cards)
        for (const foundation of this.foundations) {
          if (this.solitaireService.canMoveCard(card, foundation)) {
            return true;
          }
        }

        // Check other tableau piles
        for (const targetPile of this.tableau) {
          if (targetPile !== sourcePile && this.solitaireService.canMoveCard(card, targetPile)) {
            return true;
          }
        }
      }
    }

    return false;
  }

  private async showWinAlert() {
    const alert = await this.alertController.create({
      header: 'Congratulations!',
      message: 'You won! 🎉',
      buttons: [
        {
          text: 'New Game',
          handler: () => {
            this.newGame();
          }
        }
      ]
    });

    await alert.present();
  }

  private async showNoMovesAlert() {
    const alert = await this.alertController.create({
      header: 'No More Moves',
      message: 'There are no more valid moves available. Would you like to start a new game?',
      buttons: [
        {
          text: 'Cancel',
          role: 'cancel'
        },
        {
          text: 'New Game',
          handler: () => {
            this.newGame();
          }
        }
      ]
    });

    await alert.present();
  }
}
