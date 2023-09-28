import { Component } from '@angular/core';
import { BPMNModelerService } from '../../services/bpmnmodeler.service';

@Component({
    selector: 'app-process-state',
    templateUrl: './proposition.component.html',
    styleUrls: ['./proposition.component.scss'],
})
export class PropositionComponent {
    public currentProposition: Proposition = {
        name: 'First proposition',
        updated: new Date(),
        xml: '',
    };
    public propositions: Proposition[] = [this.currentProposition];

    constructor(private modeler: BPMNModelerService) {}

    createNewProposition() {
        const newProposition = {
            name: 'newProposition',
            updated: new Date(),
            xml: '',
        };
        this.propositions.push(newProposition);
        this.switchAndSaveAndLoadXML(newProposition);
    }

    private switchAndSaveAndLoadXML(newProposition: {
        xml: string;
        name: string;
        updated: Date;
    }) {
        // TODO: Save xml of old proposition
        this.currentProposition = newProposition;
        // TODO: Load plain xml from modeler
    }

    switchToProposition(proposition: Proposition) {
        if (proposition !== this.currentProposition) {
            this.switchAndSaveAndLoadXML(proposition);
        }
    }
}

export interface Proposition {
    name: string;
    updated: Date;
    xml: string;
}
