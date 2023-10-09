import {
    AfterContentInit,
    Component,
    ElementRef,
    Input,
    OnDestroy,
    OnInit,
    ViewChild,
} from '@angular/core';
// @ts-ignore
import Modeler from 'bpmn-js/Modeler';
// @ts-ignore
import Viewer from 'bpmn-js/Viewer';
import { BPMNModelerService } from '../../services/bpmnmodeler.service';

@Component({
    selector: 'app-diagram',
    templateUrl: './diagram.component.html',
    styleUrls: ['./diagram.component.scss'],
})
export class DiagramComponent implements AfterContentInit, OnDestroy, OnInit {
    private modeler: Modeler | Viewer;

    @ViewChild('properties', { static: true }) private properties!: ElementRef;
    @ViewChild('diagram', { static: true }) private el!: ElementRef;
    @Input() public viewer: boolean = false;
    @Input() public propertiesPanel: boolean = false;
    @Input() public height: string = '750';

    constructor(private bpmnModeler: BPMNModelerService) {}

    ngOnInit(): void {
        if (this.viewer) {
            this.modeler = this.bpmnModeler.getViewer();
        } else {
            this.modeler = this.bpmnModeler.getModeler();
            this.modeler.importXML(this.initialDiagram);
        }
    }

    ngAfterContentInit(): void {
        this.modeler.attachTo(this.el.nativeElement);
        if (this.propertiesPanel) {
            const propertiesPanel = this.modeler.get('propertiesPanel');
            // @ts-ignore
            propertiesPanel.attachTo(this.properties.nativeElement);
        }
    }

    ngOnDestroy(): void {
        this.modeler.destroy();
    }

    keyDown($event: KeyboardEvent) {
        console.log($event);
    }

    public initialDiagram: string =
        '<?xml version="1.0" encoding="UTF-8"?>\n' +
        '<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC" xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" targetNamespace="" xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL http://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd">\n' +
        '  <collaboration id="Collaboration">\n' +
        '    <participant id="Customer" name="Order handling" processRef="Customer_1" />\n' +
        '  </collaboration>\n' +
        '  <process id="Customer_1" name="Customer" processType="None" isClosed="false" isExecutable="false">\n' +
        '    <extensionElements />\n' +
        '    <laneSet id="sid-b167d0d7-e761-4636-9200-76b7f0e8e83a">\n' +
        '      <lane id="lane">\n' +
        '        <flowNodeRef>start-event</flowNodeRef>\n' +
        '        <flowNodeRef>Event_0qcvv2g</flowNodeRef>\n' +
        '        <flowNodeRef>Activity_0lgvp3u</flowNodeRef>\n' +
        '        <flowNodeRef>Gateway_1x8m4ws</flowNodeRef>\n' +
        '        <flowNodeRef>Activity_1up8xq1</flowNodeRef>\n' +
        '        <flowNodeRef>Activity_1jgyh05</flowNodeRef>\n' +
        '        <flowNodeRef>Gateway_0eef44j</flowNodeRef>\n' +
        '      </lane>\n' +
        '    </laneSet>\n' +
        '    <startEvent id="start-event" name="Order placed">\n' +
        '      <outgoing>Flow_0wq8dog</outgoing>\n' +
        '    </startEvent>\n' +
        '    <sequenceFlow id="Flow_0wq8dog" sourceRef="start-event" targetRef="Gateway_0eef44j" />\n' +
        '    <sequenceFlow id="Flow_0u9a0g3" sourceRef="Gateway_0eef44j" targetRef="Activity_1jgyh05" />\n' +
        '    <sequenceFlow id="Flow_1mtm8jg" sourceRef="Gateway_0eef44j" targetRef="Activity_1up8xq1" />\n' +
        '    <sequenceFlow id="Flow_1flhoxp" sourceRef="Activity_1jgyh05" targetRef="Gateway_1x8m4ws" />\n' +
        '    <sequenceFlow id="Flow_1n9ng49" sourceRef="Activity_1up8xq1" targetRef="Gateway_1x8m4ws" />\n' +
        '    <sequenceFlow id="Flow_14i9c18" sourceRef="Gateway_1x8m4ws" targetRef="Activity_0lgvp3u" />\n' +
        '    <sequenceFlow id="Flow_14fhivy" sourceRef="Activity_0lgvp3u" targetRef="Event_0qcvv2g" />\n' +
        '    <endEvent id="Event_0qcvv2g" name="Order delivered">\n' +
        '      <incoming>Flow_14fhivy</incoming>\n' +
        '    </endEvent>\n' +
        '    <userTask id="Activity_0lgvp3u" name="Ship goods">\n' +
        '      <incoming>Flow_14i9c18</incoming>\n' +
        '      <outgoing>Flow_14fhivy</outgoing>\n' +
        '    </userTask>\n' +
        '    <parallelGateway id="Gateway_1x8m4ws">\n' +
        '      <incoming>Flow_1flhoxp</incoming>\n' +
        '      <incoming>Flow_1n9ng49</incoming>\n' +
        '      <outgoing>Flow_14i9c18</outgoing>\n' +
        '    </parallelGateway>\n' +
        '    <userTask id="Activity_1up8xq1" name="Fetch goods">\n' +
        '      <incoming>Flow_1mtm8jg</incoming>\n' +
        '      <outgoing>Flow_1n9ng49</outgoing>\n' +
        '    </userTask>\n' +
        '    <serviceTask id="Activity_1jgyh05" name="Retrieve payment">\n' +
        '      <incoming>Flow_0u9a0g3</incoming>\n' +
        '      <outgoing>Flow_1flhoxp</outgoing>\n' +
        '    </serviceTask>\n' +
        '    <parallelGateway id="Gateway_0eef44j">\n' +
        '      <incoming>Flow_0wq8dog</incoming>\n' +
        '      <outgoing>Flow_0u9a0g3</outgoing>\n' +
        '      <outgoing>Flow_1mtm8jg</outgoing>\n' +
        '    </parallelGateway>\n' +
        '  </process>\n' +
        '  <bpmndi:BPMNDiagram id="sid-74620812-92c4-44e5-949c-aa47393d3830">\n' +
        '    <bpmndi:BPMNPlane id="sid-cdcae759-2af7-4a6d-bd02-53f3352a731d" bpmnElement="Collaboration">\n' +
        '      <bpmndi:BPMNShape id="sid-87F4C1D6-25E1-4A45-9DA7-AD945993D06F_gui" bpmnElement="Customer" isHorizontal="true">\n' +
        '        <omgdc:Bounds x="170" y="30" width="720" height="260" />\n' +
        '        <bpmndi:BPMNLabel labelStyle="sid-84cb49fd-2f7c-44fb-8950-83c3fa153d3b" />\n' +
        '      </bpmndi:BPMNShape>\n' +
        '      <bpmndi:BPMNShape id="sid-57E4FE0D-18E4-478D-BC5D-B15164E93254_gui" bpmnElement="lane" isHorizontal="true">\n' +
        '        <omgdc:Bounds x="200" y="30" width="690" height="260" />\n' +
        '      </bpmndi:BPMNShape>\n' +
        '      <bpmndi:BPMNShape id="StartEvent_0l6sgn0_di" bpmnElement="start-event">\n' +
        '        <omgdc:Bounds x="221" y="102" width="36" height="36" />\n' +
        '        <bpmndi:BPMNLabel>\n' +
        '          <omgdc:Bounds x="208" y="139" width="64" height="14" />\n' +
        '        </bpmndi:BPMNLabel>\n' +
        '      </bpmndi:BPMNShape>\n' +
        '      <bpmndi:BPMNShape id="Event_0qcvv2g_di" bpmnElement="Event_0qcvv2g">\n' +
        '        <omgdc:Bounds x="772" y="102" width="36" height="36" />\n' +
        '        <bpmndi:BPMNLabel>\n' +
        '          <omgdc:Bounds x="752" y="145" width="76" height="14" />\n' +
        '        </bpmndi:BPMNLabel>\n' +
        '      </bpmndi:BPMNShape>\n' +
        '      <bpmndi:BPMNShape id="Activity_1w8qyby_di" bpmnElement="Activity_0lgvp3u">\n' +
        '        <omgdc:Bounds x="630" y="80" width="100" height="80" />\n' +
        '      </bpmndi:BPMNShape>\n' +
        '      <bpmndi:BPMNShape id="Gateway_0j0fumn_di" bpmnElement="Gateway_1x8m4ws">\n' +
        '        <omgdc:Bounds x="545" y="95" width="50" height="50" />\n' +
        '      </bpmndi:BPMNShape>\n' +
        '      <bpmndi:BPMNShape id="Activity_11kif52_di" bpmnElement="Activity_1up8xq1">\n' +
        '        <omgdc:Bounds x="380" y="190" width="100" height="80" />\n' +
        '      </bpmndi:BPMNShape>\n' +
        '      <bpmndi:BPMNShape id="Activity_1oob6z8_di" bpmnElement="Activity_1jgyh05">\n' +
        '        <omgdc:Bounds x="380" y="80" width="100" height="80" />\n' +
        '        <bpmndi:BPMNLabel />\n' +
        '      </bpmndi:BPMNShape>\n' +
        '      <bpmndi:BPMNShape id="Gateway_1iluv37_di" bpmnElement="Gateway_0eef44j">\n' +
        '        <omgdc:Bounds x="295" y="95" width="50" height="50" />\n' +
        '      </bpmndi:BPMNShape>\n' +
        '      <bpmndi:BPMNEdge id="Flow_0wq8dog_di" bpmnElement="Flow_0wq8dog">\n' +
        '        <omgdi:waypoint x="257" y="120" />\n' +
        '        <omgdi:waypoint x="295" y="120" />\n' +
        '      </bpmndi:BPMNEdge>\n' +
        '      <bpmndi:BPMNEdge id="Flow_0u9a0g3_di" bpmnElement="Flow_0u9a0g3">\n' +
        '        <omgdi:waypoint x="345" y="120" />\n' +
        '        <omgdi:waypoint x="380" y="120" />\n' +
        '      </bpmndi:BPMNEdge>\n' +
        '      <bpmndi:BPMNEdge id="Flow_1mtm8jg_di" bpmnElement="Flow_1mtm8jg">\n' +
        '        <omgdi:waypoint x="320" y="145" />\n' +
        '        <omgdi:waypoint x="320" y="230" />\n' +
        '        <omgdi:waypoint x="380" y="230" />\n' +
        '      </bpmndi:BPMNEdge>\n' +
        '      <bpmndi:BPMNEdge id="Flow_1flhoxp_di" bpmnElement="Flow_1flhoxp">\n' +
        '        <omgdi:waypoint x="480" y="120" />\n' +
        '        <omgdi:waypoint x="545" y="120" />\n' +
        '      </bpmndi:BPMNEdge>\n' +
        '      <bpmndi:BPMNEdge id="Flow_1n9ng49_di" bpmnElement="Flow_1n9ng49">\n' +
        '        <omgdi:waypoint x="480" y="230" />\n' +
        '        <omgdi:waypoint x="570" y="230" />\n' +
        '        <omgdi:waypoint x="570" y="145" />\n' +
        '      </bpmndi:BPMNEdge>\n' +
        '      <bpmndi:BPMNEdge id="Flow_14i9c18_di" bpmnElement="Flow_14i9c18">\n' +
        '        <omgdi:waypoint x="595" y="120" />\n' +
        '        <omgdi:waypoint x="630" y="120" />\n' +
        '      </bpmndi:BPMNEdge>\n' +
        '      <bpmndi:BPMNEdge id="Flow_14fhivy_di" bpmnElement="Flow_14fhivy">\n' +
        '        <omgdi:waypoint x="730" y="120" />\n' +
        '        <omgdi:waypoint x="772" y="120" />\n' +
        '      </bpmndi:BPMNEdge>\n' +
        '    </bpmndi:BPMNPlane>\n' +
        '    <bpmndi:BPMNLabelStyle id="sid-e0502d32-f8d1-41cf-9c4a-cbb49fecf581">\n' +
        '      <omgdc:Font name="Arial" size="11" isBold="false" isItalic="false" isUnderline="false" isStrikeThrough="false" />\n' +
        '    </bpmndi:BPMNLabelStyle>\n' +
        '    <bpmndi:BPMNLabelStyle id="sid-84cb49fd-2f7c-44fb-8950-83c3fa153d3b">\n' +
        '      <omgdc:Font name="Arial" size="12" isBold="false" isItalic="false" isUnderline="false" isStrikeThrough="false" />\n' +
        '    </bpmndi:BPMNLabelStyle>\n' +
        '  </bpmndi:BPMNDiagram>\n' +
        '</definitions>\n';
}
