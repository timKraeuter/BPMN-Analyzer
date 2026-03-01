import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
    providedIn: 'root',
})
export class SharedStateService {
    public modelFileName: string = 'model';

    private readonly _propositions$ = new BehaviorSubject<Proposition[]>([]);

    /** Observable stream of propositions */
    public readonly propositions$: Observable<Proposition[]> =
        this._propositions$.asObservable();

    /** Observable stream of proposition names */
    public readonly propositionNames$: Observable<string[]> =
        this._propositions$.pipe(
            map((propositions) =>
                propositions.map((proposition) => proposition.name),
            ),
        );

    /** Get current propositions snapshot */
    get propositions(): Proposition[] {
        return this._propositions$.getValue();
    }

    getPropositionNames(): string[] {
        return this.propositions.map((proposition) => proposition.name);
    }

    addProposition(proposition: Proposition): void {
        this._propositions$.next([...this.propositions, proposition]);
    }

    removeProposition(proposition: Proposition): void {
        this._propositions$.next(
            this.propositions.filter((p) => p !== proposition),
        );
    }

    renameProposition(proposition: Proposition, newName: string): void {
        proposition.name = newName;
        // Emit to notify subscribers of the change
        this._propositions$.next([...this.propositions]);
    }

    /** Replace the entire propositions array (e.g., for reset) */
    setPropositions(propositions: Proposition[]): void {
        this._propositions$.next(propositions);
    }
}

export interface Proposition {
    name: string;
    xml: string;
}
