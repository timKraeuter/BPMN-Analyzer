import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ModelCheckingResponse } from '../../services/model-checking.service';

@Component({
    selector: 'analysis-result-component',
    templateUrl: './analysis-result.component.html',
    styleUrls: ['./analysis-result.component.scss'],
    imports: [
        CommonModule,
        MatListModule,
        MatIconModule,
        MatCardModule,
        MatProgressSpinnerModule,
    ],
})
export class AnalysisResultComponent {
    public running = input(false);
    public properties = input<BPMNProperty[]>([]);
    public ctlPropertyResult = input<ModelCheckingResponse | undefined>(
        undefined,
    );
}

export class BPMNProperty {
    public name: string;
    public valid: boolean;
    public additionalInfo: string;

    constructor(name: string, valid: boolean, additionalInfo: string = '') {
        this.name = name;
        this.valid = valid;
        this.additionalInfo = additionalInfo;
    }
}
