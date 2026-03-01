import { Component, DestroyRef, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
    BPMNProperty,
    AnalysisResultComponent,
} from '../../components/analysis-result/analysis-result.component';
import {
    BPMNPropertyResult,
    BPMNSpecificPropertyResponse,
    ModelCheckingResponse,
    ModelCheckingService,
} from '../../services/model-checking.service';
import { TemporalLogicSyntaxComponent } from '../../components/temporal-logic-syntax/temporal-logic-syntax.component';
import { BPMNModelerService } from '../../services/bpmnmodeler.service';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { SharedStateService } from '../../services/shared-state.service';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatDividerModule } from '@angular/material/divider';
import { MatTabsModule } from '@angular/material/tabs';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { DiagramComponent } from '../../components/diagram/diagram.component';
import { saveAs } from 'file-saver-es';

export interface CTLTemplate {
    template: (proposition1: string, proposition2?: string) => string;
    description: string;
    twoPropositions: boolean;
}

/** Represents a BPMN element from the element registry */
interface BPMNElement {
    id: string;
    businessObject: {
        name?: string;
    };
}

@Component({
    selector: 'app-analysis',
    templateUrl: './analysis.component.html',
    styleUrls: ['./analysis.component.scss'],
    imports: [
        CommonModule,
        FormsModule,
        MatCardModule,
        MatButtonModule,
        MatIconModule,
        MatFormFieldModule,
        MatInputModule,
        MatSelectModule,
        MatTooltipModule,
        MatButtonToggleModule,
        MatDividerModule,
        MatTabsModule,
        MatProgressSpinnerModule,
        MatSnackBarModule,
        AnalysisResultComponent,
        DiagramComponent,
    ],
})
export class AnalysisComponent {
    private readonly destroyRef = inject(DestroyRef);

    // GG generation
    public graphGrammarGenerationRunning: boolean = false;

    // General BPMN property checking.
    public bpmnSpecificPropertiesToBeChecked: string[] = [];
    public bpmnSpecificVerificationRunning: boolean = false;
    public bpmnPropertyCheckingResults: BPMNProperty[] = [];

    // CTL property checking with templates
    public selectedTemplate: CTLTemplate | undefined;
    public selectedProposition1: string = ''; // We only support one or two propositions.
    public selectedProposition2: string = '';

    ctlTemplates: CTLTemplate[] = [
        {
            template: (proposition: string) => `AG(!${proposition})`,
            description: 'Never reaches',
            twoPropositions: false,
        },
        {
            template: (proposition: string) => `EF(${proposition})`,
            description: 'Can reach',
            twoPropositions: false,
        },
        {
            template: (proposition: string) => `AF(${proposition})`,
            description: 'Always reaches',
            twoPropositions: false,
        },
        {
            template: (proposition1: string, proposition2?: string) =>
                `AG(${proposition1} -> AF(${proposition2}))`,
            description: 'Response',
            twoPropositions: true,
        },
    ];

    // CTL property checking
    public ctlProperty: string = '';
    public ctlPropertyResult: ModelCheckingResponse | undefined;
    public ltlProperty: string = '';

    constructor(
        private readonly bpmnModeler: BPMNModelerService,
        private readonly snackBar: MatSnackBar,
        private readonly modelCheckingService: ModelCheckingService,
        private readonly sharedState: SharedStateService,
    ) {}

    async downloadGGClicked() {
        this.graphGrammarGenerationRunning = true;
        try {
            const xmlModel = await this.bpmnModeler.getBPMNModelXMLBlob();

            this.modelCheckingService
                .downloadGG(xmlModel, this.sharedState.propositions)
                .pipe(takeUntilDestroyed(this.destroyRef))
                .subscribe({
                    error: (error) => {
                        this.graphGrammarGenerationRunning = false;
                        const errorMessage = this.extractErrorMessage(error);
                        console.error(
                            'Failed to download graph grammar:',
                            errorMessage,
                        );
                        this.snackBar.open(errorMessage, 'close');
                    },
                    next: (data: ArrayBuffer) => {
                        // Receive and save as zip.
                        const blob = new Blob([data], {
                            type: 'application/zip',
                        });
                        saveAs(
                            blob,
                            this.sharedState.modelFileName + '.gps.zip',
                        );
                    },
                    complete: () => {
                        this.graphGrammarGenerationRunning = false;
                    },
                });
        } catch (error) {
            this.graphGrammarGenerationRunning = false;
            console.error('Failed to prepare model for download:', error);
            this.snackBar.open('Failed to prepare the BPMN model.', 'close');
        }
    }

    ggInfoClicked() {
        this.snackBar.open(
            'Graph transformation systems are generated for the graph transformation tool Groove. You can find Groove at https://groove.ewi.utwente.nl/.',
            'close',
        );
    }

    async checkBPMNSpecificPropertiesClicked() {
        if (this.bpmnSpecificPropertiesToBeChecked.length === 0) {
            this.snackBar.open(
                'Please select at least one property for verification.',
                'close',
                {
                    duration: 5000,
                },
            );
            return;
        }
        this.bpmnSpecificVerificationRunning = true;
        try {
            const xmlModel = await this.bpmnModeler.getBPMNModelXMLBlob();
            this.modelCheckingService
                .checkBPMNSpecificProperties(
                    this.bpmnSpecificPropertiesToBeChecked,
                    xmlModel,
                )
                .pipe(takeUntilDestroyed(this.destroyRef))
                .subscribe({
                    error: (error) => {
                        this.bpmnSpecificVerificationRunning = false;
                        const errorMessage = this.extractErrorMessage(error);
                        console.error(
                            'BPMN property check failed:',
                            errorMessage,
                        );
                        this.snackBar.open(errorMessage, 'close');
                        this.bpmnPropertyCheckingResults = [];
                    },
                    next: (data: BPMNSpecificPropertyResponse) => {
                        this.bpmnPropertyCheckingResults = structuredClone(
                            data.propertyCheckingResults,
                        );
                        this.setProperCompletionHintsIfNeeded();
                        this.colorDeadActivitiesAndSetNamesIfNeeded();
                    },
                    complete: () => {
                        this.bpmnSpecificVerificationRunning = false;
                    },
                });
        } catch (error) {
            this.bpmnSpecificVerificationRunning = false;
            console.error('Failed to prepare model for checking:', error);
            this.snackBar.open('Failed to prepare the BPMN model.', 'close');
        }
    }

    checkLTLPropertyClicked() {
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
        this.bpmnSpecificVerificationRunning = true;
        try {
            const xmlModel = await this.bpmnModeler.getBPMNModelXMLBlob();
            this.modelCheckingService
                .checkTemporalLogic(
                    'CTL',
                    this.ctlProperty,
                    xmlModel,
                    this.sharedState.propositions,
                )
                .pipe(takeUntilDestroyed(this.destroyRef))
                .subscribe({
                    error: (error) => {
                        this.bpmnSpecificVerificationRunning = false;
                        const errorMessage = this.extractErrorMessage(error);
                        console.error(
                            'CTL property check failed:',
                            errorMessage,
                        );
                        this.snackBar.open(errorMessage, 'close');
                    },
                    next: (response: ModelCheckingResponse) => {
                        this.ctlPropertyResult = response;
                    },
                    complete: () => {
                        this.bpmnSpecificVerificationRunning = false;
                    },
                });
        } catch (error) {
            this.bpmnSpecificVerificationRunning = false;
            console.error('Failed to prepare model for CTL check:', error);
            this.snackBar.open('Failed to prepare the BPMN model.', 'close');
        }
    }

    private setProperCompletionHintsIfNeeded(): void {
        this.bpmnPropertyCheckingResults.forEach((value) => {
            if (value.name === 'Proper completion' && value.additionalInfo) {
                const unproperEndEvents = this.getElementsForIDs([
                    value.additionalInfo,
                ]);
                this.colorElementsInRed(unproperEndEvents);
                this.setEndNameAsInfo(value, unproperEndEvents);
                this.bpmnModeler.updateViewerBPMNModel();
            }
        });
    }

    private setEndNameAsInfo(
        value: BPMNProperty,
        unproperEndEvents: BPMNElement[],
    ) {
        const flowNodeNameOrIdList =
            this.getFlowNodeNameOrIdList(unproperEndEvents);
        value.additionalInfo = `The end event ${flowNodeNameOrIdList} consumed more than one token.`;
    }

    private colorDeadActivitiesAndSetNamesIfNeeded(): void {
        this.bpmnPropertyCheckingResults.forEach((value) => {
            if (value.name === 'No dead activities' && value.additionalInfo) {
                const deadActivities = this.getElementsForIDs(
                    value.additionalInfo.split(','),
                );
                this.colorElementsInRed(deadActivities);
                this.setActivityNamesAsInfo(value, deadActivities);
                this.bpmnModeler.updateViewerBPMNModel();
            }
        });
    }

    private setActivityNamesAsInfo(
        value: BPMNProperty,
        deadActivities: BPMNElement[],
    ) {
        const deadActivityNames = this.getFlowNodeNameOrIdList(deadActivities);
        if (deadActivities.length > 1) {
            value.additionalInfo = `The dead activities are ${deadActivityNames}.`;
        } else {
            value.additionalInfo = `The dead activity is ${deadActivityNames}.`;
        }
    }

    private getFlowNodeNameOrIdList(elements: BPMNElement[]): string {
        return elements
            .map((element) => {
                if (!element.businessObject.name) {
                    return element.id;
                }
                return element.businessObject.name;
            })
            .map((name) => `"${name}"`)
            .join(', ');
    }

    private colorElementsInRed(elementsToColor: BPMNElement[]) {
        const modeling = this.bpmnModeler.getModeler().get('modeling') as {
            setColor: (
                elements: BPMNElement[],
                colors: { stroke: string; fill: string },
            ) => void;
        };
        modeling.setColor(elementsToColor, {
            stroke: '#831311',
            fill: '#ffcdd2',
        });
    }

    private getElementsForIDs(ids: string[]): BPMNElement[] {
        const elementRegistry = this.bpmnModeler
            .getModeler()
            .get('elementRegistry') as {
            get: (id: string) => BPMNElement;
        };
        return ids.map((id) => elementRegistry.get(id));
    }

    getPropositions(): string[] {
        return this.sharedState.getPropositionNames();
    }

    getPropositionsNames(): string {
        return this.getPropositions().join(', ');
    }

    stopEventPropagation($event: KeyboardEvent) {
        // Stops event propagation so steps are not changed while inputting.
        if ($event.key === 'ArrowLeft' || $event.key === 'ArrowRight') {
            $event.stopPropagation();
        }
    }

    createCTLProperty() {
        if (this.selectedTemplate && this.selectedProposition1) {
            this.ctlProperty = this.selectedTemplate.template(
                this.selectedProposition1,
                this.selectedProposition2,
            );
        }
    }

    showCreateCTLPropertyButton(): boolean {
        if (this.selectedTemplate?.twoPropositions) {
            return (
                this.selectedProposition1.length > 0 &&
                this.selectedProposition2.length > 0
            );
        }
        return !!this.selectedTemplate && !!this.selectedProposition1;
    }

    /**
     * Safely extracts an error message from an HTTP error response.
     * Handles both JSON ArrayBuffer errors and standard error objects.
     */
    private extractErrorMessage(error: unknown): string {
        const defaultMessage = 'An unexpected error occurred.';
        if (!error || typeof error !== 'object') {
            return defaultMessage;
        }
        const httpError = error as {
            error?: ArrayBuffer | { message?: string };
        };

        // Handle ArrayBuffer error body (e.g., from arraybuffer responseType)
        if (httpError.error instanceof ArrayBuffer) {
            try {
                const errorObject = JSON.parse(
                    new TextDecoder().decode(httpError.error),
                );
                return errorObject?.message || defaultMessage;
            } catch {
                return defaultMessage;
            }
        }

        // Handle standard JSON error body
        if (
            httpError.error &&
            typeof httpError.error === 'object' &&
            'message' in httpError.error
        ) {
            return httpError.error.message || defaultMessage;
        }

        return defaultMessage;
    }
}
