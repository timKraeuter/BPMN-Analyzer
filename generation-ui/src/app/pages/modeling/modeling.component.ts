import { Component } from '@angular/core';
import { saveAs } from 'file-saver-es';
import { BPMNModelerService } from '../../services/bpmnmodeler.service';
import { SharedStateService } from '../../services/shared-state.service';
import { SVG_FILE_EXTENSION } from '../proposition/proposition.component';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { DiagramComponent } from '../../components/diagram/diagram.component';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

export const BPMN_FILE_EXTENSION = '.bpmn';

@Component({
    selector: 'app-modeling',
    templateUrl: './modeling.component.html',
    styleUrls: ['./modeling.component.scss'],
    imports: [
        MatCardModule,
        MatButtonModule,
        MatIconModule,
        MatTooltipModule,
        MatSnackBarModule,
        DiagramComponent,
    ],
})
export class ModelingComponent {
    constructor(
        private readonly bpmnModeler: BPMNModelerService,
        private readonly sharedState: SharedStateService,
        private readonly snackBar: MatSnackBar,
    ) {}

    async downloadBPMN() {
        try {
            const result = await this.bpmnModeler.getBPMNModelXMLBlob();
            saveAs(
                result,
                this.sharedState.modelFileName + BPMN_FILE_EXTENSION,
            );
        } catch (error) {
            console.error('Failed to download BPMN model:', error);
            this.snackBar.open('Failed to download BPMN model.', 'close', {
                duration: 5000,
            });
        }
    }

    async uploadBPMN(event: Event) {
        try {
            const file = (event.target as HTMLInputElement).files?.[0];
            if (!file) {
                return;
            }
            // Remove file extension: https://stackoverflow.com/questions/4250364/how-to-trim-a-file-extension-from-a-string-in-javascript
            this.sharedState.modelFileName = file.name.replace(/\.[^/.]+$/, '');
            const fileText: string = await file.text();
            await this.bpmnModeler.getModeler().importXML(fileText);
        } catch (error) {
            console.error('Failed to upload BPMN model:', error);
            this.snackBar.open(
                'Failed to import BPMN model. Please check the file format.',
                'close',
                { duration: 5000 },
            );
        }
    }

    async downloadSVG() {
        try {
            const result = await this.bpmnModeler.getModeler().saveSVG();
            const svgBlob = new Blob([result.svg], {
                type: 'text/plain;charset=utf-8',
            });
            saveAs(
                svgBlob,
                this.sharedState.modelFileName + SVG_FILE_EXTENSION,
            );
        } catch (error) {
            console.error('Failed to download SVG:', error);
            this.snackBar.open('Failed to download SVG.', 'close', {
                duration: 5000,
            });
        }
    }
}
