import {Component} from '@angular/core';
// @ts-ignore
import {saveAs} from 'file-saver-es';
import {BPMNModelerService} from '../services/bpmnmodeler.service';
import {HttpClient} from '@angular/common/http';
import {MatSnackBar} from '@angular/material/snack-bar';
import {
  TemporalLogicSyntaxComponent
} from '../temporal-logic-syntax/temporal-logic-syntax.component';
import {
  BPMNProperty
} from '../verification-result-component/verification-result-component.component';
import {GrooveService, ModelCheckingResponse} from "../services/groove.service";

@Component({
  selector: 'app-generation',
  templateUrl: './generation.component.html',
  styleUrls: ['./generation.component.scss'],
})
export class GenerationComponent {
  diagramUrl =
    'https://raw.githubusercontent.com/timKraeuter/Rewrite_Rule_Generation/master/generation-ui/initial.bpmn';
  importError?: Error;

  public graphGrammarGenerationRunning: boolean = false;

  // BPMN-specific property checking.
  public bpmnSpecificPropertiesToBeChecked: string[];
  public bpmnSpecificVerificationRunning: boolean = false;
  public bpmnPropertyCheckingResults: BPMNProperty[] = [];

  public ltlProperty: string;
  public ctlProperty: string;

  constructor(
    private bpmnModeler: BPMNModelerService,
    private httpClient: HttpClient,
    private snackBar: MatSnackBar,
    private grooveService: GrooveService
  ) {
    this.bpmnSpecificPropertiesToBeChecked = [];
    this.ltlProperty = '';
    this.ctlProperty = '';
  }

  handleImported(event: any) {
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
    this.graphGrammarGenerationRunning = true;
    const xmlModel = await this.getBPMNModelXML()

    this.grooveService.downloadGG(xmlModel).subscribe({
      error: (error) => {
        console.log(error);
        this.snackBar.open(error.error.message, 'close');
      },
      next: (data: ArrayBuffer) => {
        // Receive and save as zip.
        const blob = new Blob([data], {
          type: 'application/zip',
        });
        saveAs(blob, 'model.gps.zip');
      },
    })
    .add(() => (this.graphGrammarGenerationRunning = false));
  }
  private async getBPMNModelXML(): Promise<Blob> {
    const xmlResult = await this.bpmnModeler
    .getBPMNJs()
    .saveXML({format: true});

    return new Blob([xmlResult.xml]);
  }

  async checkBPMNSpecificPropertiesClicked() {
    if (this.bpmnSpecificPropertiesToBeChecked.length == 0) {
      this.snackBar.open(
        'Please select at least one property for verification.',
        'close',
        {
          duration: 5000,
        }
      );
    }
    this.bpmnSpecificVerificationRunning = true;
    const xmlModel = await this.getBPMNModelXML()
    this.grooveService.checkBPMNSpecificProperties(this.bpmnSpecificPropertiesToBeChecked, xmlModel)
    .subscribe({
      error: (error) => {
        console.log(error);
        this.snackBar.open(error.error.message, 'close');
        this.bpmnPropertyCheckingResults = [];
      },
      next: (data: any) => {
        // @ts-ignore
        this.bpmnPropertyCheckingResults = JSON.parse(
          JSON.stringify(data['propertyCheckingResults'])
        );
      },
    })
    .add(() => (this.bpmnSpecificVerificationRunning = false));
  }

  checkLTLPropertyClicked() {
    console.log(
      'Check LTL property clicked with input: ' + this.ltlProperty
    );
    this.snackBar.open(
      'Checking LTL properties is not implemented in the web interface yet due to the following bug in Groove https://sourceforge.net/p/groove/bugs/499/.',
      'close',
      {
        duration: 5000,
      }
    );
  }

  temporalLogicInfoClicked() {
    this.snackBar.openFromComponent(TemporalLogicSyntaxComponent, {
      duration: 10000,
    });
  }

  ggInfoClicked() {
    this.snackBar.open(
      'Graph transformation systems are generated for the graph transformation tool Groove. You can find Groove at https://groove.ewi.utwente.nl/.',
      'close'
    );
  }

  async checkCTLPropertyClicked() {
    const xmlModel = await this.getBPMNModelXML()
    this.grooveService.checkTemporalLogic("CTL", this.ctlProperty, xmlModel).subscribe({
      error: (error) => {
        console.log(error);
        this.snackBar.open(error.error.message, 'close');
      },
      next: (response: ModelCheckingResponse) => {
        console.log(response); // TODO: Show somewhere in the UI.
      },
    });
  }
}
