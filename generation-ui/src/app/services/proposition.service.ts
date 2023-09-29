import { Injectable } from '@angular/core';

@Injectable({
    providedIn: 'root',
})
export class PropositionService {
    public propositions: Proposition[] = [];

    getPropositionNames(): string[] {
        return this.propositions.map((proposition) => proposition.name);
    }
}

export interface Proposition {
    name: string;
    xml: string;
}
