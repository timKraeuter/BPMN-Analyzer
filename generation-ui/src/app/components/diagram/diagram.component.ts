import {
    AfterContentInit,
    Component,
    ElementRef,
    input,
    OnDestroy,
    OnInit,
    ViewChild,
} from '@angular/core';
import Modeler from 'bpmn-js/lib/Modeler';
import Viewer from 'bpmn-js/lib/Viewer';
import { BPMNModelerService } from '../../services/bpmnmodeler.service';
import { INITIAL_BPMN_DIAGRAM } from '../../constants/initial-diagram';

@Component({
    selector: 'app-diagram',
    templateUrl: './diagram.component.html',
    styleUrls: ['./diagram.component.scss'],
})
export class DiagramComponent implements AfterContentInit, OnDestroy, OnInit {
    private modeler!: Modeler | Viewer;

    @ViewChild('properties', { static: true })
    private readonly properties!: ElementRef;
    @ViewChild('diagram', { static: true }) private readonly el!: ElementRef;
    public viewer = input(false);
    public propertiesPanel = input(false);
    public height = input('750');

    constructor(private readonly bpmnModeler: BPMNModelerService) {}

    ngOnInit(): void {
        if (this.viewer()) {
            this.modeler = this.bpmnModeler.getViewer();
        } else {
            this.modeler = this.bpmnModeler.getModeler();
            this.modeler.importXML(INITIAL_BPMN_DIAGRAM);
        }
    }

    ngAfterContentInit(): void {
        this.modeler.attachTo(this.el.nativeElement);
        if (this.propertiesPanel()) {
            const propertiesPanel = this.modeler.get('propertiesPanel') as {
                attachTo: (element: HTMLElement) => void;
            };
            propertiesPanel.attachTo(this.properties.nativeElement);
        }
    }

    ngOnDestroy(): void {
        this.modeler?.detach();
    }
}
