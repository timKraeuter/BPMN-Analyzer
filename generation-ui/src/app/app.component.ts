import { Component, HostListener, ViewChild } from '@angular/core';
import { StepperSelectionEvent } from '@angular/cdk/stepper';
import { BPMNModelerService } from './services/bpmnmodeler.service';
import { MatStepper, MatStepperModule } from '@angular/material/stepper';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { PropositionComponent } from './pages/proposition/proposition.component';
import { ModelingComponent } from './pages/modeling/modeling.component';
import { AnalysisComponent } from './pages/analysis/analysis.component';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
    imports: [
        MatStepperModule,
        MatIconModule,
        MatButtonModule,
        MatDividerModule,
        ModelingComponent,
        PropositionComponent,
        AnalysisComponent,
    ],
})
export class AppComponent {
    @ViewChild('stepper') private readonly stepper!: MatStepper;
    @ViewChild('proposition') propositionComponent!: PropositionComponent;

    constructor(private readonly modeler: BPMNModelerService) {}

    async stepChanged(event: StepperSelectionEvent) {
        if (this.changedToPropositionStep(event)) {
            await this.modeler.updateTokenBPMNModelIfNeeded();
        }
        if (this.changedToAnalyzeStep(event)) {
            await this.propositionComponent.saveCurrentProposition();
            await this.modeler.updateViewerBPMNModel();
        }
    }

    private changedToAnalyzeStep(event: StepperSelectionEvent) {
        return event.selectedIndex == 2;
    }

    private changedToPropositionStep(event: StepperSelectionEvent) {
        return event.selectedIndex == 1;
    }

    @HostListener('document:keydown.ArrowRight', ['$event'])
    async stepForward(event: Event) {
        if (
            event.target &&
            (event.target as HTMLElement).classList.contains(
                'bio-properties-panel-input',
            )
        ) {
            return;
        }
        this.stepper.next();
    }

    @HostListener('document:keydown.ArrowLeft', ['$event'])
    async stepBackward(_: Event) {
        this.stepper.previous();
    }
}
