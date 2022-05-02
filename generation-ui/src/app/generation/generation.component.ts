import {Component} from '@angular/core';
// @ts-ignore
import {saveAs} from 'file-saver';
import {BPMNModelerService} from '../services/bpmnmodeler.service';
import {HttpClient} from '@angular/common/http';
import {MatSnackBar} from '@angular/material/snack-bar';
import {LTlSyntaxComponent} from '../ltl-syntax/ltl-syntax.component';
import {environment} from '../../environments/environment';

const baseURL = environment.production ? window.location.href : environment.apiURL;
const zipURL = baseURL + 'zip';
const modelCheckURL = baseURL + 'modelCheckBPMN';

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
    private httpClient: HttpClient,
    private snackBar: MatSnackBar
  ) {
    this.bpmnProperties = [];
    this.ltlProperty = '';
  }

  bpmnProperties: string[];
  ltlProperty: string;

  // @ts-ignore
  handleImported(event) {
    const {type, error, warnings} = event;

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
      .saveXML({format: true})
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
    } as any; // Expect a zip/file response type.
    const formData = await this.createBPMNFileFormData();

    this.httpClient
      .post(zipURL, formData, options)
      .subscribe((data) => {
        // Receive and save as zip.
        const blob = new Blob([data], {
          type: 'application/zip',
        });
        saveAs(blob, 'model.gps.zip');
      });
  }

  private async createBPMNFileFormData() {
    const formData = new FormData();

    // Append bpmn file.
    const xmlResult = await this.bpmnModeler
      .getBPMNJs()
      .saveXML({format: true});
    formData.append('file', new Blob([xmlResult.xml]));
    return formData;
  }

  async checkBPMNSpecificPropertiesClicked() {
    if (this.bpmnProperties.length == 0) {
      this.snackBar.open(
        'Please select at least one property for verification.',
        'close',
        {
          duration: 5000,
        }
      );
    }
    console.log(
      'Check general properties clicked with the following selection: ' +
      this.bpmnProperties
    );
    const formData = await this.createBPMNFileFormData();
    this.bpmnProperties.forEach(property =>
      formData.append("propertiesToBeChecked[]", property));

    this.httpClient
      .post(modelCheckURL, formData)
      .subscribe((data) => {
        console.log(data);
      });
  }

  checkLTLPropertyClicked() {
    console.log(
      'Check LTL property clicked with input: ' + this.ltlProperty
    );
  }

  ltlInfoClicked() {
    this.snackBar.openFromComponent(LTlSyntaxComponent, {
      duration: 10000,
    });
  }

  ggInfoClicked() {
    this.snackBar.open(
      'Graph grammars are generated for the graph transformation tool Groove. You can find Groove at https://groove.ewi.utwente.nl/.',
      'close'
    );
  }
}
