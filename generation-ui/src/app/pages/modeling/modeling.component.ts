import { Component } from '@angular/core';
// @ts-ignore
import { saveAs } from 'file-saver-es';
import { BPMNModelerService } from '../../services/bpmnmodeler.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { GrooveService } from '../../services/groove.service';

export const BPMN_FILE_EXTENSION = '.bpmn';

@Component({
    selector: 'app-modeling',
    templateUrl: './modeling.component.html',
    styleUrls: ['./modeling.component.scss'],
})
export class ModelingComponent {
    public fileName: string = 'model';

    public graphGrammarGenerationRunning: boolean = false;

    constructor(
        private bpmnModeler: BPMNModelerService,
        private snackBar: MatSnackBar,
        private grooveService: GrooveService,
    ) {}

    downloadBPMN() {
        this.bpmnModeler
            .getBPMNModelXMLBlob()
            // @ts-ignore
            .then((result) => {
                saveAs(result, this.fileName + BPMN_FILE_EXTENSION);
            });
    }

    async uploadBPMN(event: Event) {
        // @ts-ignore
        let file = (event.target as HTMLInputElement).files[0];
        // Remove file extension: https://stackoverflow.com/questions/4250364/how-to-trim-a-file-extension-from-a-string-in-javascript
        this.fileName = file.name.replace(/\.[^/.]+$/, '');
        const fileText: string = await file.text();
        await this.bpmnModeler.getModeler().importXML(fileText);
    }

    async downloadGGClicked() {
        this.graphGrammarGenerationRunning = true;
        const xmlModel = await this.bpmnModeler.getBPMNModelXMLBlob();

        this.grooveService
            .downloadGG(xmlModel)
            .subscribe({
                error: (error) => {
                    const errorObject = JSON.parse(
                        new TextDecoder().decode(error.error),
                    );
                    console.log(errorObject);
                    this.snackBar.open(errorObject.message, 'close');
                },
                next: (data: ArrayBuffer) => {
                    // Receive and save as zip.
                    const blob = new Blob([data], {
                        type: 'application/zip',
                    });
                    saveAs(blob, this.fileName + '.gps.zip');
                },
            })
            .add(() => (this.graphGrammarGenerationRunning = false));
    }

    ggInfoClicked() {
        this.snackBar.open(
            'Graph transformation systems are generated for the graph transformation tool Groove. You can find Groove at https://groove.ewi.utwente.nl/.',
            'close',
        );
    }
}
