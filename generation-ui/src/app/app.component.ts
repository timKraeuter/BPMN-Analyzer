import { Component, HostListener, ViewChild } from '@angular/core';
import { StepperSelectionEvent } from '@angular/cdk/stepper';
import { BPMNModelerService } from './services/bpmnmodeler.service';
import { MatStepper } from '@angular/material/stepper';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
})
export class AppComponent {
    @ViewChild('stepper') private stepper!: MatStepper;

    constructor(private modeler: BPMNModelerService) {}

    async stepChanged(event: StepperSelectionEvent) {
        if (this.changedToProcessStateStep(event)) {
            // TODO: Sync issue with multiple propositions
            await this.modeler.updateTokenBPMNModelIfNeeded();
        }
        if (this.changedToAnalyzeStep(event)) {
            await this.modeler.updateViewerBPMNModel();
        }
    }

    private changedToAnalyzeStep(event: StepperSelectionEvent) {
        return event.selectedIndex == 2;
    }

    private changedToProcessStateStep(event: StepperSelectionEvent) {
        return event.previouslySelectedIndex == 0 && event.selectedIndex == 1;
    }

    @HostListener('document:keydown.ArrowRight')
    async stepForward() {
        this.stepper.next();
    }

    @HostListener('document:keydown.ArrowLeft')
    async stepBackward() {
        this.stepper.previous();
    }
}
