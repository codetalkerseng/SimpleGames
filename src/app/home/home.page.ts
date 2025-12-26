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
  version = '1.0.1';
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
  private stockRecycleCount = 0;
  private lastMoveCountAtRecycle = 0;
  hintSource: Pile | null = null;
  hintCard: Card | null = null;
  hintTarget: Pile | null = null;

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

      // Clear hint on any state change
      this.clearHint();

      if (this.solitaireService.isGameWon()) {
        setTimeout(() => {
          this.showWinAlert();
        }, 100);
      } else {
        // Try auto-cleanup if all cards are face-up
        setTimeout(() => {
          this.tryAutoCleanup();
        }, 100);

        // Check for no more moves (but only after at least one move has been made)
        if (state.moves > 0 && !this.hasShownNoMovesAlert) {
          setTimeout(() => {
            this.checkForNoMoreMoves();
          }, 300);
        }
      }
    });
  }

  newGame() {
    this.hasShownNoMovesAlert = false;
    this.stockRecycleCount = 0;
    this.lastMoveCountAtRecycle = 0;
    this.clearHint();
    this.solitaireService.newGame();
  }

  drawCard() {
    this.clearHint();

    // Track stock recycling for stuck detection
    const isRecycling = this.stock.cards.length === 0 && this.waste.cards.length > 0;

    if (isRecycling) {
      // If moves haven't changed since last recycle, increment counter
      if (this.moves === this.lastMoveCountAtRecycle) {
        this.stockRecycleCount++;

        // If recycled 2+ times without making moves, player is stuck in loop
        if (this.stockRecycleCount >= 2) {
          this.checkForNoMoreMoves();
        }
      } else {
        // Moves were made, reset counter
        this.stockRecycleCount = 1;
      }
      this.lastMoveCountAtRecycle = this.moves;
    }

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
      // Reset recycle counter when a move is made
      this.stockRecycleCount = 0;
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
          this.stockRecycleCount = 0; // Reset recycle counter on successful move
          return;
        }
      }
    }

    // Try to move to any valid tableau pile
    for (const tableau of this.tableau) {
      if (tableau !== pile && this.solitaireService.canMoveCard(card, tableau)) {
        this.solitaireService.moveCards(cardsToMove, pile, tableau);
        this.stockRecycleCount = 0; // Reset recycle counter on successful move
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

  showHint() {
    this.clearHint();

    // Try to find any valid move
    const move = this.findValidMove();

    if (move) {
      this.hintSource = move.source;
      this.hintCard = move.card;
      this.hintTarget = move.target;

      // Clear hint after 3 seconds
      setTimeout(() => {
        this.clearHint();
      }, 3000);
    } else {
      // No valid moves
      this.checkForNoMoreMoves();
    }
  }

  clearHint() {
    this.hintSource = null;
    this.hintCard = null;
    this.hintTarget = null;
  }

  isHintCard(card: Card, pile: Pile): boolean {
    return this.hintCard === card && this.hintSource === pile;
  }

  isHintTarget(pile: Pile): boolean {
    return this.hintTarget === pile;
  }

  private findValidMove(): { source: Pile; card: Card; target: Pile } | null {
    // Priority 1: Check if we can move any card to foundation
    // Check waste first
    const wasteCard = this.getTopWasteCard();
    if (wasteCard) {
      for (const foundation of this.foundations) {
        if (this.solitaireService.canMoveCard(wasteCard, foundation)) {
          return { source: this.waste, card: wasteCard, target: foundation };
        }
      }
    }

    // Check tableau cards to foundation
    for (const pile of this.tableau) {
      if (pile.cards.length > 0) {
        const topCard = pile.cards[pile.cards.length - 1];
        if (topCard.faceUp) {
          for (const foundation of this.foundations) {
            if (this.solitaireService.canMoveCard(topCard, foundation)) {
              return { source: pile, card: topCard, target: foundation };
            }
          }
        }
      }
    }

    // Priority 2: Check tableau to tableau moves
    // Check waste to tableau
    if (wasteCard) {
      for (const pile of this.tableau) {
        if (this.solitaireService.canMoveCard(wasteCard, pile)) {
          return { source: this.waste, card: wasteCard, target: pile };
        }
      }
    }

    // Check tableau to tableau
    for (const sourcePile of this.tableau) {
      for (let i = 0; i < sourcePile.cards.length; i++) {
        const card = sourcePile.cards[i];
        if (!card.faceUp) continue;

        for (const targetPile of this.tableau) {
          if (targetPile !== sourcePile && this.solitaireService.canMoveCard(card, targetPile)) {
            return { source: sourcePile, card: card, target: targetPile };
          }
        }
      }
    }

    // Priority 3: Draw from stock
    if (this.stock.cards.length > 0) {
      // Return a hint to click the stock
      return null; // User should draw more cards
    }

    return null;
  }

  tryAutoCleanup() {
    // Check if all cards are face-up
    const allFaceUp = this.tableau.every(pile =>
      pile.cards.every(card => card.faceUp)
    );

    if (!allFaceUp || this.waste.cards.length > 0 || this.stock.cards.length > 0) {
      return;
    }

    // Auto-move cards to foundations
    let movedAny = false;
    do {
      movedAny = false;

      // Try to move from tableau to foundations
      for (const pile of this.tableau) {
        if (pile.cards.length > 0) {
          const topCard = pile.cards[pile.cards.length - 1];

          for (const foundation of this.foundations) {
            if (this.solitaireService.canMoveCard(topCard, foundation)) {
              this.solitaireService.moveCards([topCard], pile, foundation);
              movedAny = true;
              break;
            }
          }

          if (movedAny) break;
        }
      }
    } while (movedAny);
  }

  private checkForNoMoreMoves() {
    if (this.hasAnyValidMoves()) {
      // Reset flag when moves are available again
      this.hasShownNoMovesAlert = false;
      return;
    }

    // Only show alert once per stuck state
    if (!this.hasShownNoMovesAlert) {
      this.hasShownNoMovesAlert = true;
      this.showNoMovesAlert();
    }
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
