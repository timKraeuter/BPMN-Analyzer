import { Component } from '@angular/core';
// @ts-ignore
import { saveAs } from 'file-saver-es';
import { BPMNModelerService } from '../services/bpmnmodeler.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { GrooveService } from '../services/groove.service';

const BPMN_FILE_EXTENSION = '.bpmn';

@Component({
    selector: 'app-generation',
    templateUrl: './generation.component.html',
    styleUrls: ['./generation.component.scss'],
})
export class GenerationComponent {
    diagramUrl =
        'https://raw.githubusercontent.com/timKraeuter/Rewrite_Rule_Generation/master/generation-ui/initial.bpmn';
    importError?: Error;

    public fileName: string = 'model';

    public graphGrammarGenerationRunning: boolean = false;

    constructor(
        private bpmnModeler: BPMNModelerService,
        private snackBar: MatSnackBar,
        private grooveService: GrooveService,
    ) {}

    handleImported(event: any) {
        const { type, error, warnings } = event;

        if (type === 'success') {
            console.log(`Rendered diagram (%s warnings)`, warnings.length);
        }

        if (type === 'error') {
            console.error('Failed to render diagram', error);
        }

        this.importError = error;
    }

    downloadBPMNClicked() {
        this.bpmnModeler
            .getBPMNJs()
            .saveXML({ format: true })
            // @ts-ignore
            .then((result) => {
                saveAs(
                    new Blob([result.xml], {
                        type: 'text/xml;charset=utf-8',
                    }),
                    this.fileName + BPMN_FILE_EXTENSION,
                );
            });
    }

    async uploadFile(event: Event) {
        // @ts-ignore
        let file = (event.target as HTMLInputElement).files[0];
        // Remove file extension: https://stackoverflow.com/questions/4250364/how-to-trim-a-file-extension-from-a-string-in-javascript
        this.fileName = file.name.replace(/\.[^/.]+$/, '');
        const fileText: string = await file.text();
        this.bpmnModeler.getBPMNJs().importXML(fileText);
    }

    async downloadGGClicked() {
        this.graphGrammarGenerationRunning = true;
        const xmlModel = await this.bpmnModeler.getBPMNModelXML();

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
