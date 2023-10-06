import { Component } from '@angular/core';
import { BPMNProperty } from '../../components/analysis-result/analysis-result.component';
import {
    ModelCheckingResponse,
    ModelCheckingService,
} from '../../services/model-checking.service';
import { TemporalLogicSyntaxComponent } from '../../components/temporal-logic-syntax/temporal-logic-syntax.component';
import { BPMNModelerService } from '../../services/bpmnmodeler.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { SharedStateService } from '../../services/shared-state.service';
// @ts-ignore
import { saveAs } from 'file-saver-es';

@Component({
    selector: 'app-analysis',
    templateUrl: './analysis.component.html',
    styleUrls: ['./analysis.component.scss'],
})
export class AnalysisComponent {
    // GG generation
    public graphGrammarGenerationRunning: boolean = false;

    // General BPMN property checking.
    public bpmnSpecificPropertiesToBeChecked: string[] = [];
    public bpmnSpecificVerificationRunning: boolean = false;
    public bpmnPropertyCheckingResults: BPMNProperty[] = [];

    // CTL property checking
    public ctlProperty: string = '';
    public ctlPropertyResult: ModelCheckingResponse | undefined;
    public ltlProperty: string = '';

    constructor(
        private bpmnModeler: BPMNModelerService,
        private snackBar: MatSnackBar,
        private modelCheckingService: ModelCheckingService,
        private sharedState: SharedStateService,
    ) {}

    async downloadGGClicked() {
        this.graphGrammarGenerationRunning = true;
        const xmlModel = await this.bpmnModeler.getBPMNModelXMLBlob();

        this.modelCheckingService
            .downloadGG(xmlModel, this.sharedState.propositions)
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
                    saveAs(blob, this.sharedState.modelFileName + '.gps.zip');
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

    async checkBPMNSpecificPropertiesClicked() {
        if (this.bpmnSpecificPropertiesToBeChecked.length == 0) {
            this.snackBar.open(
                'Please select at least one property for verification.',
                'close',
                {
                    duration: 5000,
                },
            );
        }
        this.bpmnSpecificVerificationRunning = true;
        const xmlModel = await this.bpmnModeler.getBPMNModelXMLBlob();
        this.modelCheckingService
            .checkBPMNSpecificProperties(
                this.bpmnSpecificPropertiesToBeChecked,
                xmlModel,
            )
            .subscribe({
                error: (error) => {
                    console.error(error);
                    this.snackBar.open(error.error.message, 'close');
                    this.bpmnPropertyCheckingResults = [];
                },
                next: (data: any) => {
                    // @ts-ignore
                    this.bpmnPropertyCheckingResults = JSON.parse(
                        JSON.stringify(data['propertyCheckingResults']),
                    );
                    this.colorDeadActivitiesAndSetNamesIfNeeded();
                },
            })
            .add(() => (this.bpmnSpecificVerificationRunning = false));
    }

    checkLTLPropertyClicked() {
        console.log(
            'Check LTL property clicked with input: ' + this.ltlProperty,
        );
        this.snackBar.open(
            'Checking LTL properties is not implemented in the web interface yet due to the following bug in Groove https://sourceforge.net/p/groove/bugs/499/.',
            'close',
            {
                duration: 5000,
            },
        );
    }

    temporalLogicInfoClicked() {
        this.snackBar.openFromComponent(TemporalLogicSyntaxComponent, {
            duration: 10000,
        });
    }

    async checkCTLPropertyClicked() {
        const xmlModel = await this.bpmnModeler.getBPMNModelXMLBlob();
        this.bpmnSpecificVerificationRunning = true;
        this.modelCheckingService
            .checkTemporalLogic(
                'CTL',
                this.ctlProperty,
                xmlModel,
                this.sharedState.propositions,
            )
            .subscribe({
                error: (error) => {
                    console.error(error);
                    this.snackBar.open(error.error.message, 'close');
                },
                next: (response: ModelCheckingResponse) => {
                    console.log(response);
                    this.ctlPropertyResult = response;
                },
            })
            .add(() => (this.bpmnSpecificVerificationRunning = false));
    }

    private colorDeadActivitiesAndSetNamesIfNeeded(): void {
        this.bpmnPropertyCheckingResults.forEach((value) => {
            if (value.name === 'No dead activities' && value.additionalInfo) {
                const deadActivities = this.getElementsForIDs(
                    value.additionalInfo.split(','),
                );
                this.colorElementsInRed(deadActivities);
                this.setActivityNamesAsInfo(value, deadActivities);
            }
        });
    }

    private setActivityNamesAsInfo(value: BPMNProperty, deadActivities: any[]) {
        value.additionalInfo =
            'Dead activities: ' + this.getActivityNameOrIdList(deadActivities);
    }

    private getActivityNameOrIdList(deadActivities: any[]): string {
        return deadActivities
            .map((value1) => {
                if (!value1.businessObject.name) {
                    return value1.id;
                }
                return value1.businessObject.name;
            })
            .join(', ');
    }

    private colorElementsInRed(elementsToColor: any[]) {
        const modeling: any = this.bpmnModeler.getModeler().get('modeling');
        modeling.setColor(elementsToColor, {
            stroke: '#831311',
            fill: '#ffcdd2',
        });
    }

    private getElementsForIDs(ids: string[]) {
        const elementRegistry: any = this.bpmnModeler
            .getModeler()
            .get('elementRegistry');
        return ids.map((id) => elementRegistry.get(id));
    }

    getPropNames() {
        return this.sharedState.getPropositionNames().join(', ');
    }

    stopEventPropagation($event: KeyboardEvent) {
        // Stops event propagation so steps are not changed while inputting.
        if ($event.key === 'ArrowLeft' || $event.key === 'ArrowRight') {
            $event.stopPropagation();
        }
    }
}
