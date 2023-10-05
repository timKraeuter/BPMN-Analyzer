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
    @ViewChild('ref', { static: true }) private el!: ElementRef;
    @Input() public viewer: boolean = false;
    @Input() public propertiesPanel: boolean = false;

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

    public initialDiagram: string =
        '<?xml version="1.0" encoding="UTF-8"?>\n' +
        '<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC" xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" targetNamespace="" xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL http://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd">\n' +
        '  <collaboration id="Collaboration">\n' +
        '    <participant id="Customer" name="Customer" processRef="Customer_1" />\n' +
        '  </collaboration>\n' +
        '  <process id="Customer_1" name="Customer" processType="None" isClosed="false" isExecutable="false">\n' +
        '    <extensionElements />\n' +
        '    <laneSet id="sid-b167d0d7-e761-4636-9200-76b7f0e8e83a">\n' +
        '      <lane id="lane">\n' +
        '        <flowNodeRef>Scan_QR_code</flowNodeRef>\n' +
        '        <flowNodeRef>Open_Product</flowNodeRef>\n' +
        '        <flowNodeRef>start-event</flowNodeRef>\n' +
        '        <flowNodeRef>end_event</flowNodeRef>\n' +
        '        <flowNodeRef>gateway_1</flowNodeRef>\n' +
        '        <flowNodeRef>SCAN_OK</flowNodeRef>\n' +
        '      </lane>\n' +
        '    </laneSet>\n' +
        '    <task id="Scan_QR_code" name="Scan QR code">\n' +
        '      <incoming>scan_flow_in</incoming>\n' +
        '      <outgoing>scan_flow_out</outgoing>\n' +
        '    </task>\n' +
        '    <task id="Open_Product" name="Open product information in mobile  app">\n' +
        '      <incoming>No</incoming>\n' +
        '      <outgoing>end_flow</outgoing>\n' +
        '    </task>\n' +
        '    <startEvent id="start-event" name="Notices&#10;QR code">\n' +
        '      <outgoing>gateway_in</outgoing>\n' +
        '    </startEvent>\n' +
        '    <endEvent id="end_event" name="Is informed">\n' +
        '      <incoming>end_flow</incoming>\n' +
        '    </endEvent>\n' +
        '    <exclusiveGateway id="gateway_1">\n' +
        '      <incoming>gateway_in</incoming>\n' +
        '      <incoming>Yes</incoming>\n' +
        '      <outgoing>scan_flow_in</outgoing>\n' +
        '    </exclusiveGateway>\n' +
        '    <exclusiveGateway id="SCAN_OK" name="Scan successful?&#10;">\n' +
        '      <incoming>scan_flow_out</incoming>\n' +
        '      <outgoing>No</outgoing>\n' +
        '      <outgoing>Yes</outgoing>\n' +
        '    </exclusiveGateway>\n' +
        '    <sequenceFlow id="gateway_in" sourceRef="start-event" targetRef="gateway_1" />\n' +
        '    <sequenceFlow id="scan_flow_out" sourceRef="Scan_QR_code" targetRef="SCAN_OK" />\n' +
        '    <sequenceFlow id="end_flow" sourceRef="Open_Product" targetRef="end_event" />\n' +
        '    <sequenceFlow id="No" name="No" sourceRef="SCAN_OK" targetRef="Open_Product" />\n' +
        '    <sequenceFlow id="scan_flow_in" sourceRef="gateway_1" targetRef="Scan_QR_code" />\n' +
        '    <sequenceFlow id="Yes" name="Yes" sourceRef="SCAN_OK" targetRef="gateway_1" />\n' +
        '  </process>\n' +
        '  <bpmndi:BPMNDiagram id="sid-74620812-92c4-44e5-949c-aa47393d3830">\n' +
        '    <bpmndi:BPMNPlane id="sid-cdcae759-2af7-4a6d-bd02-53f3352a731d" bpmnElement="sid-c0e745ff-361e-4afb-8c8d-2a1fc32b1424">\n' +
        '      <bpmndi:BPMNShape id="sid-87F4C1D6-25E1-4A45-9DA7-AD945993D06F_gui" bpmnElement="Customer" isHorizontal="true">\n' +
        '        <omgdc:Bounds x="158" y="55" width="933" height="250" />\n' +
        '        <bpmndi:BPMNLabel labelStyle="sid-84cb49fd-2f7c-44fb-8950-83c3fa153d3b">\n' +
        '          <omgdc:Bounds x="47.49999999999999" y="170.42857360839844" width="12.000000000000014" height="59.142852783203125" />\n' +
        '        </bpmndi:BPMNLabel>\n' +
        '      </bpmndi:BPMNShape>\n' +
        '      <bpmndi:BPMNShape id="sid-57E4FE0D-18E4-478D-BC5D-B15164E93254_gui" bpmnElement="lane" isHorizontal="true">\n' +
        '        <omgdc:Bounds x="188" y="55" width="903" height="250" />\n' +
        '      </bpmndi:BPMNShape>\n' +
        '      <bpmndi:BPMNShape id="sid-52EB1772-F36E-433E-8F5B-D5DFD26E6F26_gui" bpmnElement="Scan_QR_code">\n' +
        '        <omgdc:Bounds x="468" y="120" width="100" height="80" />\n' +
        '        <bpmndi:BPMNLabel labelStyle="sid-84cb49fd-2f7c-44fb-8950-83c3fa153d3b">\n' +
        '          <omgdc:Bounds x="435.5" y="122" width="84" height="12" />\n' +
        '        </bpmndi:BPMNLabel>\n' +
        '      </bpmndi:BPMNShape>\n' +
        '      <bpmndi:BPMNShape id="sid-E49425CF-8287-4798-B622-D2A7D78EF00B_gui" bpmnElement="Open_Product">\n' +
        '        <omgdc:Bounds x="803" y="120" width="100" height="80" />\n' +
        '        <bpmndi:BPMNLabel labelStyle="sid-84cb49fd-2f7c-44fb-8950-83c3fa153d3b">\n' +
        '          <omgdc:Bounds x="770.9285736083984" y="112" width="83.14285278320312" height="36" />\n' +
        '        </bpmndi:BPMNLabel>\n' +
        '      </bpmndi:BPMNShape>\n' +
        '      <bpmndi:BPMNShape id="StartEvent_0l6sgn0_di" bpmnElement="start-event">\n' +
        '        <omgdc:Bounds x="262" y="142" width="36" height="36" />\n' +
        '        <bpmndi:BPMNLabel>\n' +
        '          <omgdc:Bounds x="259" y="179" width="43" height="27" />\n' +
        '        </bpmndi:BPMNLabel>\n' +
        '      </bpmndi:BPMNShape>\n' +
        '      <bpmndi:BPMNShape id="EndEvent_0xwuvv5_di" bpmnElement="end_event">\n' +
        '        <omgdc:Bounds x="976" y="142" width="36" height="36" />\n' +
        '        <bpmndi:BPMNLabel>\n' +
        '          <omgdc:Bounds x="968" y="181" width="54" height="14" />\n' +
        '        </bpmndi:BPMNLabel>\n' +
        '      </bpmndi:BPMNShape>\n' +
        '      <bpmndi:BPMNShape id="ExclusiveGateway_1g0eih2_di" bpmnElement="gateway_1" isMarkerVisible="true">\n' +
        '        <omgdc:Bounds x="350" y="135" width="50" height="50" />\n' +
        '        <bpmndi:BPMNLabel>\n' +
        '          <omgdc:Bounds x="210" y="160" width="90" height="12" />\n' +
        '        </bpmndi:BPMNLabel>\n' +
        '      </bpmndi:BPMNShape>\n' +
        '      <bpmndi:BPMNShape id="ExclusiveGateway_0vci1x5_di" bpmnElement="SCAN_OK" isMarkerVisible="true">\n' +
        '        <omgdc:Bounds x="660" y="135" width="50" height="50" />\n' +
        '        <bpmndi:BPMNLabel>\n' +
        '          <omgdc:Bounds x="643" y="107" width="89" height="27" />\n' +
        '        </bpmndi:BPMNLabel>\n' +
        '      </bpmndi:BPMNShape>\n' +
        '      <bpmndi:BPMNEdge id="sid-7B791A11-2F2E-4D80-AFB3-91A02CF2B4FD_gui" bpmnElement="gateway_in">\n' +
        '        <omgdi:waypoint x="298" y="160" />\n' +
        '        <omgdi:waypoint x="350" y="160" />\n' +
        '        <bpmndi:BPMNLabel>\n' +
        '          <omgdc:Bounds x="204" y="185" width="90" height="20" />\n' +
        '        </bpmndi:BPMNLabel>\n' +
        '      </bpmndi:BPMNEdge>\n' +
        '      <bpmndi:BPMNEdge id="sid-EE8A7BA0-5D66-4F8B-80E3-CC2751B3856A_gui" bpmnElement="scan_flow_out">\n' +
        '        <omgdi:waypoint x="568" y="160" />\n' +
        '        <omgdi:waypoint x="660" y="160" />\n' +
        '        <bpmndi:BPMNLabel>\n' +
        '          <omgdc:Bounds x="494" y="185" width="90" height="20" />\n' +
        '        </bpmndi:BPMNLabel>\n' +
        '      </bpmndi:BPMNEdge>\n' +
        '      <bpmndi:BPMNEdge id="sid-57EB1F24-BD94-479A-BF1F-57F1EAA19C6C_gui" bpmnElement="end_flow">\n' +
        '        <omgdi:waypoint x="903" y="160" />\n' +
        '        <omgdi:waypoint x="976" y="160" />\n' +
        '        <bpmndi:BPMNLabel>\n' +
        '          <omgdc:Bounds x="820" y="185" width="90" height="20" />\n' +
        '        </bpmndi:BPMNLabel>\n' +
        '      </bpmndi:BPMNEdge>\n' +
        '      <bpmndi:BPMNEdge id="sid-8B820AF5-DC5C-4618-B854-E08B71FB55CB_gui" bpmnElement="No">\n' +
        '        <omgdi:waypoint x="710" y="160" />\n' +
        '        <omgdi:waypoint x="803" y="160" />\n' +
        '        <bpmndi:BPMNLabel labelStyle="sid-e0502d32-f8d1-41cf-9c4a-cbb49fecf581">\n' +
        '          <omgdc:Bounds x="718" y="135" width="14" height="14" />\n' +
        '        </bpmndi:BPMNLabel>\n' +
        '      </bpmndi:BPMNEdge>\n' +
        '      <bpmndi:BPMNEdge id="sid-4DC479E5-5C20-4948-BCFC-9EC5E2F66D8D_gui" bpmnElement="scan_flow_in">\n' +
        '        <omgdi:waypoint x="400" y="160" />\n' +
        '        <omgdi:waypoint x="468" y="160" />\n' +
        '        <bpmndi:BPMNLabel>\n' +
        '          <omgdc:Bounds x="314" y="185" width="90" height="20" />\n' +
        '        </bpmndi:BPMNLabel>\n' +
        '      </bpmndi:BPMNEdge>\n' +
        '      <bpmndi:BPMNEdge id="sid-337A23B9-A923-4CCE-B613-3E247B773CCE_gui" bpmnElement="Yes">\n' +
        '        <omgdi:waypoint x="686" y="184" />\n' +
        '        <omgdi:waypoint x="685.5" y="249" />\n' +
        '        <omgdi:waypoint x="375.5" y="249" />\n' +
        '        <omgdi:waypoint x="376" y="184" />\n' +
        '        <bpmndi:BPMNLabel labelStyle="sid-e0502d32-f8d1-41cf-9c4a-cbb49fecf581">\n' +
        '          <omgdc:Bounds x="661" y="186" width="20" height="14" />\n' +
        '        </bpmndi:BPMNLabel>\n' +
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
