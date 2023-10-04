import { Component } from '@angular/core';
// @ts-ignore
import { saveAs } from 'file-saver-es';
import { BPMNModelerService } from '../../services/bpmnmodeler.service';
import { SharedStateService } from '../../services/shared-state.service';

export const BPMN_FILE_EXTENSION = '.bpmn';

@Component({
    selector: 'app-modeling',
    templateUrl: './modeling.component.html',
    styleUrls: ['./modeling.component.scss'],
})
export class ModelingComponent {
    constructor(
        private bpmnModeler: BPMNModelerService,
        private sharedState: SharedStateService,
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
}
