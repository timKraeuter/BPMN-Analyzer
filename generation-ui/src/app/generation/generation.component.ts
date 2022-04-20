import { Component } from '@angular/core';
// @ts-ignore
import { saveAs } from 'file-saver';
import { BPMNModelerService } from '../services/bpmnmodeler.service';
import { HttpClient } from '@angular/common/http';

@Component({
    selector: 'app-generation',
    templateUrl: './generation.component.html',
    styleUrls: ['./generation.component.scss'],
})
export class GenerationComponent {
    diagramUrl =
        'https://cdn.staticaly.com/gh/bpmn-io/bpmn-js-examples/dfceecba/starter/diagram.bpmn';
    importError?: Error;

    constructor(
        private bpmnModeler: BPMNModelerService,
        private httpClient: HttpClient
    ) {}

    // @ts-ignore
    handleImported(event) {
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
                    'model.bpmn'
                );
            });
    }

    async uploadFile(event: Event) {
        // @ts-ignore
        let file = (event.target as HTMLInputElement).files[0];
        const fileText: string = await file.text();
        this.bpmnModeler.getBPMNJs().importXML(fileText);
    }

    async downloadGGClicked() {
        const options = {
            responseType: 'arraybuffer',
        } as any; // Set any options you like
        const formData = new FormData();

        // Append bpmn file.
        const xmlResult = await this.bpmnModeler
            .getBPMNJs()
            .saveXML({ format: true });
        formData.append('file', new Blob([xmlResult.xml]));

        // Send it.
        return this.httpClient
            .post('http://localhost:8080/zip', formData, options)
            .subscribe((data) => {
                // Receive and save as zip.
                const blob = new Blob([data], {
                    type: 'application/zip',
                });
                saveAs(blob, 'model.gps.zip');
            });
    }
}
