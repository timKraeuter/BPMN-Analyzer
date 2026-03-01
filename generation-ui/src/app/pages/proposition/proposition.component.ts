import { Component, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BPMNModelerService } from '../../services/bpmnmodeler.service';
import { saveAs } from 'file-saver-es';
import { BPMN_FILE_EXTENSION } from '../modeling/modeling.component';
import {
    Proposition,
    SharedStateService,
} from '../../services/shared-state.service';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { RenamePropositionDialogComponent } from '../../components/rename-proposition-dialog/rename-proposition-dialog.component';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatListModule } from '@angular/material/list';
import { TokenDiagramComponent } from '../../components/token-diagram/token-diagram.component';

export const SVG_FILE_EXTENSION = '.svg';

@Component({
    selector: 'app-proposition',
    templateUrl: './proposition.component.html',
    styleUrls: ['./proposition.component.scss'],
    imports: [
        CommonModule,
        MatCardModule,
        MatButtonModule,
        MatIconModule,
        MatTooltipModule,
        MatListModule,
        MatDialogModule,
        MatSnackBarModule,
        TokenDiagramComponent,
    ],
})
export class PropositionComponent {
    public currentProposition: Proposition = {
        name: 'Proposition1',
        xml: '',
    };

    constructor(
        private readonly modeler: BPMNModelerService,
        private readonly propService: SharedStateService,
        private readonly dialog: MatDialog,
        private readonly snackBar: MatSnackBar,
    ) {
        this.propService.addProposition(this.currentProposition);
    }

    get propositions(): Proposition[] {
        return this.propService.propositions;
    }

    async createNewProposition() {
        try {
            const newProposition: Proposition = {
                name: 'newProposition',
                xml: await this.modeler.getBpmnXML(),
            };
            this.propService.addProposition(newProposition);
            await this.switchAndSaveAndLoadXML(newProposition);
        } catch (error) {
            console.error('Failed to create new proposition:', error);
            this.snackBar.open('Failed to create new proposition.', 'close', {
                duration: 5000,
            });
        }
    }

    private async switchAndSaveAndLoadXML(changeTo: Proposition) {
        if (this.currentProposition) {
            this.currentProposition.xml = await this.modeler.getTokenXML();
        }

        this.currentProposition = changeTo;
        await this.modeler.getTokenModeler().importXML(changeTo.xml);
    }

    async switchToProposition(proposition: Proposition) {
        try {
            if (proposition !== this.currentProposition) {
                await this.switchAndSaveAndLoadXML(proposition);
            }
        } catch (error) {
            console.error('Failed to switch proposition:', error);
            this.snackBar.open(
                'Failed to switch to the selected proposition.',
                'close',
                { duration: 5000 },
            );
        }
    }

    async uploadTokenModel(event: Event) {
        try {
            const file = (event.target as HTMLInputElement).files?.[0];
            if (!file) {
                return;
            }
            const fileText: string = await file.text();

            await this.modeler.getTokenModeler().importXML(fileText);
            this.currentProposition.xml = fileText;
            if (file.name) {
                // Remove file extension: https://stackoverflow.com/questions/4250364/how-to-trim-a-file-extension-from-a-string-in-javascript
                this.currentProposition.name = file.name.replace(
                    /\.[^/.]+$/,
                    '',
                );
            }
        } catch (error) {
            console.error('Failed to upload token model:', error);
            this.snackBar.open(
                'Failed to import token model. Please check the file format.',
                'close',
                { duration: 5000 },
            );
        }
    }

    async downloadTokenModel() {
        try {
            const result = await this.modeler.getTokenModelXMLBlob();
            saveAs(result, this.currentProposition.name + BPMN_FILE_EXTENSION);
        } catch (error) {
            console.error('Failed to download token model:', error);
            this.snackBar.open('Failed to download token model.', 'close', {
                duration: 5000,
            });
        }
    }

    editProposition(proposition: Proposition) {
        this.dialog.open(RenamePropositionDialogComponent, {
            data: {
                proposition,
            },
        });
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
        this.propService.removeProposition(prop);
        if (prop === this.currentProposition) {
            await this.switchToProposition(this.propositions[0]);
        }
    }

    async saveCurrentProposition() {
        try {
            if (this.currentProposition.xml === '') {
                await this.modeler.updateTokenBPMNModelIfNeeded();
            }
            this.currentProposition.xml = await this.modeler.getTokenXML();
        } catch (error) {
            console.error('Failed to save current proposition:', error);
        }
    }

    async downloadTokenModelSVG() {
        try {
            const result = await this.modeler.getTokenModeler().saveSVG();
            const svgBlob = new Blob([result.svg], {
                type: 'text/plain;charset=utf-8',
            });
            saveAs(svgBlob, this.currentProposition.name + SVG_FILE_EXTENSION);
        } catch (error) {
            console.error('Failed to download SVG:', error);
            this.snackBar.open('Failed to download SVG.', 'close', {
                duration: 5000,
            });
        }
    }

    @HostListener('document:keydown.ArrowDown', ['$event'])
    async propositionDown(event: Event) {
        if (
            event.target &&
            (event.target as HTMLElement).className !==
                'bio-properties-panel-input'
        ) {
            const currentIndex = this.propositions.findIndex(
                (proposition) => proposition === this.currentProposition,
            );
            const nextProposition = this.propositions[currentIndex + 1];
            if (nextProposition) {
                await this.switchToProposition(nextProposition);
            }
        }
    }

    @HostListener('document:keydown.ArrowUp', ['$event'])
    async propositionUp(event: Event) {
        if (
            event.target &&
            (event.target as HTMLElement).className !==
                'bio-properties-panel-input'
        ) {
            const currentIndex = this.propositions.findIndex(
                (proposition) => proposition === this.currentProposition,
            );
            const nextProposition = this.propositions[currentIndex - 1];
            if (nextProposition) {
                await this.switchToProposition(nextProposition);
            }
        }
    }
}
