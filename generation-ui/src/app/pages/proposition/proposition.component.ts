import { Component } from '@angular/core';
import { BPMNModelerService } from '../../services/bpmnmodeler.service';
// @ts-ignore
import { saveAs } from 'file-saver-es';
import { BPMN_FILE_EXTENSION } from '../modeling/modeling.component';
import {
    Proposition,
    PropositionService,
} from '../../services/proposition.service';
import { MatDialog } from '@angular/material/dialog';
import { RenamePropositionDialogComponent } from '../../components/rename-proposition-dialog/rename-proposition-dialog.component';

@Component({
    selector: 'app-process-state',
    templateUrl: './proposition.component.html',
    styleUrls: ['./proposition.component.scss'],
})
export class PropositionComponent {
    public currentProposition: Proposition = {
        name: 'Proposition1',
        updated: new Date(),
        xml: '',
    };

    constructor(
        private modeler: BPMNModelerService,
        private propService: PropositionService,
        private dialog: MatDialog,
    ) {
        this.propositions.push(this.currentProposition);
    }

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

    get propositions() {
        return this.propService.propositions;
    }

    editProposition(proposition: Proposition) {
        this.dialog.open(RenamePropositionDialogComponent, {
            data: {
                proposition,
            },
        });
    }
}
