import { Component, HostListener, ViewChild } from '@angular/core';
import { StepperSelectionEvent } from '@angular/cdk/stepper';
import { BPMNModelerService } from './services/bpmnmodeler.service';
import { MatStepper } from '@angular/material/stepper';
import { PropositionComponent } from './pages/proposition/proposition.component';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
    standalone: false,
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
