import { Component } from '@angular/core';
import { BPMNProperty } from './analysis-result/analysis-result.component';
import {
    GrooveService,
    ModelCheckingResponse,
} from '../services/groove.service';
import { TemporalLogicSyntaxComponent } from './temporal-logic-syntax/temporal-logic-syntax.component';
import { BPMNModelerService } from '../services/bpmnmodeler.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
    selector: 'app-analysis',
    templateUrl: './bpmn-analysis.component.html',
    styleUrls: ['./bpmn-analysis.component.scss'],
})
export class BpmnAnalysisComponent {
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
        private grooveService: GrooveService,
    ) {}

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
        const xmlModel = await this.bpmnModeler.getBPMNModelXML();
        this.grooveService
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
        const xmlModel = await this.bpmnModeler.getBPMNModelXML();
        this.bpmnSpecificVerificationRunning = true;
        this.grooveService
            .checkTemporalLogic('CTL', this.ctlProperty, xmlModel)
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
        const modeling: any = this.bpmnModeler.getBPMNJs().get('modeling');
        modeling.setColor(elementsToColor, {
            stroke: '#831311',
            fill: '#ffcdd2',
        });
    }

    private getElementsForIDs(ids: string[]) {
        const elementRegistry: any = this.bpmnModeler
            .getBPMNJs()
            .get('elementRegistry');
        return ids.map((id) => elementRegistry.get(id));
    }
}
