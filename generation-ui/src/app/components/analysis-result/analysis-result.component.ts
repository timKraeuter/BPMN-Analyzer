import { Component, input } from '@angular/core';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { BPMNProperty } from '../../models/bpmn-property';
import { ModelCheckingResponse } from '../../models/model-checking-response';

@Component({
    selector: 'app-analysis-result',
    templateUrl: './analysis-result.component.html',
    styleUrls: ['./analysis-result.component.scss'],
    imports: [
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
