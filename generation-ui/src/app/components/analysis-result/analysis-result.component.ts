import { Component, Input } from '@angular/core';
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
    @Input()
    public running: boolean = false;

    @Input()
    public properties: BPMNProperty[] = [];

    @Input()
    public ctlPropertyResult: ModelCheckingResponse | undefined = undefined;
}

export interface BPMNProperty {
    name: string;
    valid: boolean;
    additionalInfo: string;
}
