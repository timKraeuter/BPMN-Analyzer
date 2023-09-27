import {
    AfterContentInit,
    Component,
    ElementRef,
    Input,
    OnDestroy,
    ViewChild,
} from '@angular/core';
import { BPMNModelerService } from '../../services/bpmnmodeler.service';
import TokenModeler from '../../../../../../bpmn-token/lib/Modeler';

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
    private modeler: TokenModeler;

    @ViewChild('ref', { static: true }) private el!: ElementRef;
    @Input() public viewer: boolean = false;

    constructor(private bpmnModeler: BPMNModelerService) {
        this.modeler = bpmnModeler.getTokenModeler();
    }

    ngAfterContentInit(): void {
        this.modeler.attachTo(this.el.nativeElement);
    }

    ngOnDestroy(): void {
        this.modeler.destroy();
    }
}
