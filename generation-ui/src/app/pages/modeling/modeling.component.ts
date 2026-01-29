import { Component } from '@angular/core';
// @ts-ignore
import { saveAs } from 'file-saver-es';
import { BPMNModelerService } from '../../services/bpmnmodeler.service';
import { SharedStateService } from '../../services/shared-state.service';
import { SVG_FILE_EXTENSION } from '../proposition/proposition.component';

export const BPMN_FILE_EXTENSION = '.bpmn';

@Component({
    selector: 'app-modeling',
    templateUrl: './modeling.component.html',
    styleUrls: ['./modeling.component.scss'],
    standalone: false,
})
export class ModelingComponent {
    constructor(
        private readonly bpmnModeler: BPMNModelerService,
        private readonly sharedState: SharedStateService,
    ) {}

    downloadBPMN() {
        this.bpmnModeler
            .getBPMNModelXMLBlob()
            // @ts-ignore
            .then((result) => {
                saveAs(
                    result,
                    this.sharedState.modelFileName + BPMN_FILE_EXTENSION,
                );
            });
    }

    async uploadBPMN(event: Event) {
        // @ts-ignore
        let file = (event.target as HTMLInputElement).files[0];
        // Remove file extension: https://stackoverflow.com/questions/4250364/how-to-trim-a-file-extension-from-a-string-in-javascript
        this.sharedState.modelFileName = file.name.replace(/\.[^/.]+$/, '');
        const fileText: string = await file.text();
        await this.bpmnModeler.getModeler().importXML(fileText);
    }

    downloadSVG() {
        this.bpmnModeler
            .getModeler()
            .saveSVG()
            .then((result) => {
                const svgBlob = new Blob([result.svg], {
                    type: 'text/plain;charset=utf-8',
                });
                saveAs(
                    svgBlob,
                    this.sharedState.modelFileName + SVG_FILE_EXTENSION,
                );
            });
    }
}
