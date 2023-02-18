import { Component, Input } from '@angular/core';
import { ModelCheckingResponse } from '../services/groove.service';

@Component({
    selector: 'verification-result-component',
    templateUrl: './verification-result-component.component.html',
    styleUrls: ['./verification-result-component.component.scss'],
})
export class VerificationResultComponentComponent {
    @Input()
    public running!: boolean;

    @Input()
    public properties!: BPMNProperty[];

    @Input()
    public ctlPropertyResult: ModelCheckingResponse | undefined = undefined;
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
