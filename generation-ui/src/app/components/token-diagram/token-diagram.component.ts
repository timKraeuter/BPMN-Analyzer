import {
    AfterContentInit,
    Component,
    ElementRef,
    input,
    OnDestroy,
    ViewChild,
} from '@angular/core';
import { BPMNModelerService } from '../../services/bpmnmodeler.service';
import TokenModeler from 'bpmn-token/lib/Modeler';

@Component({
    selector: 'app-token-diagram',
    template: ` <div #ref class="diagram-container"></div> `,
    styles: [
        `
            .diagram-container {
                height: 100%;
                width: 100%;
            }
        `,
    ],
})
export class TokenDiagramComponent implements AfterContentInit, OnDestroy {
    private readonly modeler: TokenModeler;

    @ViewChild('ref', { static: true }) private readonly el!: ElementRef;
    public viewer = input(false);

    constructor(private readonly bpmnModeler: BPMNModelerService) {
        this.modeler = bpmnModeler.getTokenModeler();
    }

    ngAfterContentInit(): void {
        this.modeler.attachTo(this.el.nativeElement);
    }

    ngOnDestroy(): void {
        this.modeler.detach();
    }
}
