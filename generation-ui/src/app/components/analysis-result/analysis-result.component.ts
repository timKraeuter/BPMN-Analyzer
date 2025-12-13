import { Component, Input, ChangeDetectorRef } from '@angular/core';
import { ModelCheckingResponse } from '../../services/model-checking.service';

@Component({
    selector: 'analysis-result-component',
    templateUrl: './analysis-result.component.html',
    styleUrls: ['./analysis-result.component.scss'],
    standalone: false,
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
    public properties!: BPMNProperty[];

    @Input()
    public ctlPropertyResult: ModelCheckingResponse | undefined = undefined;

    constructor(private cdr: ChangeDetectorRef) {}
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
