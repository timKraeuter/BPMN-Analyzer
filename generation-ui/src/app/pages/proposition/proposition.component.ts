import { Component, HostListener } from '@angular/core';
import { BPMNModelerService } from '../../services/bpmnmodeler.service';
// @ts-ignore
import { saveAs } from 'file-saver-es';
import { BPMN_FILE_EXTENSION } from '../modeling/modeling.component';
import {
    Proposition,
    SharedStateService,
} from '../../services/shared-state.service';
import { MatDialog } from '@angular/material/dialog';
import { RenamePropositionDialogComponent } from '../../components/rename-proposition-dialog/rename-proposition-dialog.component';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
    selector: 'app-proposition',
    templateUrl: './proposition.component.html',
    styleUrls: ['./proposition.component.scss'],
})
export class PropositionComponent {
    public currentProposition: Proposition = {
        name: 'Proposition1',
        xml: '',
    };

    constructor(
        private modeler: BPMNModelerService,
        private propService: SharedStateService,
        private dialog: MatDialog,
        private snackBar: MatSnackBar,
    ) {
        this.propositions.push(this.currentProposition);
    }

    async createNewProposition() {
        const newProposition = {
            name: 'newProposition',
            xml: await this.modeler.getBpmnXML(),
        };
        this.propositions.push(newProposition);
        await this.switchAndSaveAndLoadXML(newProposition);
    }

    private async switchAndSaveAndLoadXML(changeTo: Proposition) {
        if (this.currentProposition) {
            this.currentProposition.xml = await this.modeler.getTokenXML();
        }

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

    @HostListener('document:keydown.delete')
    async deletePressed() {
        if (this.propositions.length === 1) {
            return;
        }
        await this.deleteProposition(this.currentProposition);
    }

    public async deleteProposition(prop: Proposition) {
        if (this.propositions.length === 1) {
            this.snackBar.open(
                'There has to be at least one proposition.',
                'close',
                {
                    duration: 5000,
                },
            );
            return;
        }
        const index = this.propositions.indexOf(prop);
        // Propositions should always exist.
        this.propositions.splice(index, 1);
        if (prop === this.currentProposition) {
            await this.switchToProposition(this.propositions[0]);
        }
    }

    async saveCurrentProposition() {
        if (this.currentProposition.xml === '') {
            await this.modeler.updateTokenBPMNModelIfNeeded();
        }
        this.currentProposition.xml = await this.modeler.getTokenXML();
    }
}
