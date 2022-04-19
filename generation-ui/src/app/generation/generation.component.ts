import {
  Component,
} from '@angular/core';
// @ts-ignore
import { saveAs } from 'file-saver';
import {BPMNModelerService} from "../services/bpmnmodeler.service";

@Component({
  selector: 'app-generation',
  templateUrl: './generation.component.html',
  styleUrls: ['./generation.component.scss']
})
export class GenerationComponent {
  diagramUrl = 'https://cdn.staticaly.com/gh/bpmn-io/bpmn-js-examples/dfceecba/starter/diagram.bpmn';
  importError?: Error;

  constructor(private bpmnModeler: BPMNModelerService) {
  }

  // @ts-ignore
  handleImported(event) {
    const {
      type,
      error,
      warnings
    } = event;

    if (type === 'success') {
      console.log(`Rendered diagram (%s warnings)`, warnings.length);
    }

    if (type === 'error') {
      console.error('Failed to render diagram', error);
    }

    this.importError = error;
  }


  downloadBPMNClicked() {
    // @ts-ignore
    this.bpmnModeler.getBPMNJs().saveXML({ format: true }).then((result) => {
      saveAs(
        new Blob([result.xml], {
          type: 'text/xml;charset=utf-8',
        }),
        "model.bpmn"
      );
    });
  }
}
