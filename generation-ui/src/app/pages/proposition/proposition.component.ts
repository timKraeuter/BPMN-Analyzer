import { Component } from '@angular/core';

@Component({
    selector: 'app-process-state',
    templateUrl: './proposition.component.html',
    styleUrls: ['./proposition.component.scss'],
})
export class PropositionComponent {
    public propositions: ProcessState[] = [
        {
            name: 'Photos',
            updated: new Date('1/1/16'),
            xml: '',
        },
        {
            name: 'Recipes',
            updated: new Date('1/17/16'),
            xml: '',
        },
        {
            name: 'Work',
            updated: new Date('1/28/16'),
            xml: '',
        },
    ];

    newProcessState() {
        this.propositions.push({
            name: 'newProposition',
            updated: new Date(),
            xml: '',
        });
    }
}

export interface ProcessState {
    name: string;
    updated: Date;
    xml: string;
}
