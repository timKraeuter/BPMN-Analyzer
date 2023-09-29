import { Component } from '@angular/core';
import { BPMNModelerService } from '../../services/bpmnmodeler.service';
// @ts-ignore
import { saveAs } from 'file-saver-es';
import { BPMN_FILE_EXTENSION } from '../modeling/modeling.component';

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

    async createNewProposition() {
        const newProposition = {
            name: 'newProposition',
            updated: new Date(),
            xml: await this.modeler.getBpmnXML(),
        };
        this.propositions.push(newProposition);
        await this.switchAndSaveAndLoadXML(newProposition);
    }

    private async switchAndSaveAndLoadXML(changeTo: Proposition) {
        this.currentProposition.xml = await this.modeler.getTokenXML();

        this.currentProposition = changeTo;
        await this.modeler.getTokenModeler().importXML(changeTo.xml);
    }

    async switchToProposition(proposition: Proposition) {
        if (proposition !== this.currentProposition) {
            await this.switchAndSaveAndLoadXML(proposition);
        }
    }

    async uploadTokenModel(event: Event) {
        // @ts-ignore
        let file = (event.target as HTMLInputElement).files[0];
        const fileText: string = await file.text();

        await this.modeler.getTokenModeler().importXML(fileText);
        this.currentProposition.xml = fileText;
    }

    downloadTokenModel() {
        this.modeler
            .getTokenModelXMLBlob()
            // @ts-ignore
            .then((result) => {
                saveAs(
                    result,
                    this.currentProposition.name + BPMN_FILE_EXTENSION,
                );
            });
    }
}

export interface Proposition {
    name: string;
    updated: Date;
    xml: string;
}
