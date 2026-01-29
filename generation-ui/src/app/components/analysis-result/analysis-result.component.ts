import { Component, Input, ChangeDetectorRef } from '@angular/core';
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
    private _running: boolean = false;

    @Input()
    public set running(value: boolean) {
        this._running = value;
        this.cdr.detectChanges();
    }

    public get running(): boolean {
        return this._running;
    }

    @Input()
    public properties: BPMNProperty[] = [];

    @Input()
    public ctlPropertyResult: ModelCheckingResponse | undefined = undefined;

    constructor(private readonly cdr: ChangeDetectorRef) {}
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
