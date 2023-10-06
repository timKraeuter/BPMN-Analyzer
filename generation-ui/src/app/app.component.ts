import { Component, HostListener, ViewChild } from '@angular/core';
import { StepperSelectionEvent } from '@angular/cdk/stepper';
import { BPMNModelerService } from './services/bpmnmodeler.service';
import { MatStepper } from '@angular/material/stepper';
import { PropositionComponent } from './pages/proposition/proposition.component';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
})
export class AppComponent {
    @ViewChild('stepper') private stepper!: MatStepper;
    @ViewChild('proposition') propositionComponent!: PropositionComponent;

    constructor(private modeler: BPMNModelerService) {}

    async stepChanged(event: StepperSelectionEvent) {
        if (this.changedToModelStep(event)) {
            this.modeler.bindModelerKeyboard();
        }
        if (this.changedToPropositionStep(event)) {
            // TODO: Sync issue with multiple propositions
            await this.modeler.updateTokenBPMNModelIfNeeded();
            this.modeler.bindTokenModelerKeyboard();
        }
        if (this.changedToAnalyzeStep(event)) {
            this.modeler.unbindKeyboards();
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
    async stepForward(event: KeyboardEvent) {
        if (
            event.target &&
            // @ts-ignore Do not step forward when inputting something in the panel.
            event.target.className !== 'bio-properties-panel-input'
        ) {
            this.stepper.next();
        }
    }

    @HostListener('document:keydown.ArrowLeft', ['$event'])
    async stepBackward(event: KeyboardEvent) {
        this.stepper.previous();
    }

    private changedToModelStep(event: StepperSelectionEvent) {
        return event.selectedIndex == 0;
    }
}
