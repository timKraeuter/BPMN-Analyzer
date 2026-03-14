import { Injectable } from '@angular/core';
import { Proposition } from '../models/proposition';

@Injectable({
    providedIn: 'root',
})
export class SharedStateService {
    public modelFileName: string = 'model';
    public propositions: Proposition[] = [];

    getPropositionNames(): string[] {
        return this.propositions.map((proposition) => proposition.name);
    }
}
