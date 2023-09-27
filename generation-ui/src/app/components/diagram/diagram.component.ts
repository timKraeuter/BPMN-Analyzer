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
export class DiagramComponent implements AfterContentInit, OnDestroy, OnInit {
    private modeler: Modeler | Viewer;

    @ViewChild('ref', { static: true }) private el!: ElementRef;
    @Input() public viewer: boolean = false;

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
    }

    ngOnDestroy(): void {
        this.modeler.destroy();
    }

    public initialDiagram: string =
        '<?xml version="1.0" encoding="UTF-8"?>\n' +
        '<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC" xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" targetNamespace="" xsi:schemaLocation="http://www.omg.org/spec/BPMN/20100524/MODEL http://www.omg.org/spec/BPMN/2.0/20100501/BPMN20.xsd">\n' +
        '  <collaboration id="sid-c0e745ff-361e-4afb-8c8d-2a1fc32b1424">\n' +
        '    <participant id="sid-87F4C1D6-25E1-4A45-9DA7-AD945993D06F" name="Customer" processRef="sid-C3803939-0872-457F-8336-EAE484DC4A04" />\n' +
        '  </collaboration>\n' +
        '  <process id="sid-C3803939-0872-457F-8336-EAE484DC4A04" name="Customer" processType="None" isClosed="false" isExecutable="false">\n' +
        '    <extensionElements />\n' +
        '    <laneSet id="sid-b167d0d7-e761-4636-9200-76b7f0e8e83a">\n' +
        '      <lane id="sid-57E4FE0D-18E4-478D-BC5D-B15164E93254">\n' +
        '        <flowNodeRef>sid-52EB1772-F36E-433E-8F5B-D5DFD26E6F26</flowNodeRef>\n' +
        '        <flowNodeRef>sid-E49425CF-8287-4798-B622-D2A7D78EF00B</flowNodeRef>\n' +
        '        <flowNodeRef>sid-D7F237E8-56D0-4283-A3CE-4F0EFE446138</flowNodeRef>\n' +
        '        <flowNodeRef>sid-E433566C-2289-4BEB-A19C-1697048900D2</flowNodeRef>\n' +
        '        <flowNodeRef>sid-5134932A-1863-4FFA-BB3C-A4B4078B11A9</flowNodeRef>\n' +
        '        <flowNodeRef>SCAN_OK</flowNodeRef>\n' +
        '      </lane>\n' +
        '    </laneSet>\n' +
        '    <task id="sid-52EB1772-F36E-433E-8F5B-D5DFD26E6F26" name="Scan QR code">\n' +
        '      <incoming>sid-4DC479E5-5C20-4948-BCFC-9EC5E2F66D8D</incoming>\n' +
        '      <outgoing>sid-EE8A7BA0-5D66-4F8B-80E3-CC2751B3856A</outgoing>\n' +
        '    </task>\n' +
        '    <task id="sid-E49425CF-8287-4798-B622-D2A7D78EF00B" name="Open product information in mobile  app">\n' +
        '      <incoming>sid-8B820AF5-DC5C-4618-B854-E08B71FB55CB</incoming>\n' +
        '      <outgoing>sid-57EB1F24-BD94-479A-BF1F-57F1EAA19C6C</outgoing>\n' +
        '    </task>\n' +
        '    <startEvent id="sid-D7F237E8-56D0-4283-A3CE-4F0EFE446138" name="Notices&#10;QR code">\n' +
        '      <outgoing>sid-7B791A11-2F2E-4D80-AFB3-91A02CF2B4FD</outgoing>\n' +
        '    </startEvent>\n' +
        '    <endEvent id="sid-E433566C-2289-4BEB-A19C-1697048900D2" name="Is informed">\n' +
        '      <incoming>sid-57EB1F24-BD94-479A-BF1F-57F1EAA19C6C</incoming>\n' +
        '    </endEvent>\n' +
        '    <exclusiveGateway id="sid-5134932A-1863-4FFA-BB3C-A4B4078B11A9">\n' +
        '      <incoming>sid-7B791A11-2F2E-4D80-AFB3-91A02CF2B4FD</incoming>\n' +
        '      <incoming>sid-337A23B9-A923-4CCE-B613-3E247B773CCE</incoming>\n' +
        '      <outgoing>sid-4DC479E5-5C20-4948-BCFC-9EC5E2F66D8D</outgoing>\n' +
        '    </exclusiveGateway>\n' +
        '    <exclusiveGateway id="SCAN_OK" name="Scan successful?&#10;">\n' +
        '      <incoming>sid-EE8A7BA0-5D66-4F8B-80E3-CC2751B3856A</incoming>\n' +
        '      <outgoing>sid-8B820AF5-DC5C-4618-B854-E08B71FB55CB</outgoing>\n' +
        '      <outgoing>sid-337A23B9-A923-4CCE-B613-3E247B773CCE</outgoing>\n' +
        '    </exclusiveGateway>\n' +
        '    <sequenceFlow id="sid-7B791A11-2F2E-4D80-AFB3-91A02CF2B4FD" sourceRef="sid-D7F237E8-56D0-4283-A3CE-4F0EFE446138" targetRef="sid-5134932A-1863-4FFA-BB3C-A4B4078B11A9" />\n' +
        '    <sequenceFlow id="sid-EE8A7BA0-5D66-4F8B-80E3-CC2751B3856A" sourceRef="sid-52EB1772-F36E-433E-8F5B-D5DFD26E6F26" targetRef="SCAN_OK" />\n' +
        '    <sequenceFlow id="sid-57EB1F24-BD94-479A-BF1F-57F1EAA19C6C" sourceRef="sid-E49425CF-8287-4798-B622-D2A7D78EF00B" targetRef="sid-E433566C-2289-4BEB-A19C-1697048900D2" />\n' +
        '    <sequenceFlow id="sid-8B820AF5-DC5C-4618-B854-E08B71FB55CB" name="No" sourceRef="SCAN_OK" targetRef="sid-E49425CF-8287-4798-B622-D2A7D78EF00B" />\n' +
        '    <sequenceFlow id="sid-4DC479E5-5C20-4948-BCFC-9EC5E2F66D8D" sourceRef="sid-5134932A-1863-4FFA-BB3C-A4B4078B11A9" targetRef="sid-52EB1772-F36E-433E-8F5B-D5DFD26E6F26" />\n' +
        '    <sequenceFlow id="sid-337A23B9-A923-4CCE-B613-3E247B773CCE" name="Yes" sourceRef="SCAN_OK" targetRef="sid-5134932A-1863-4FFA-BB3C-A4B4078B11A9" />\n' +
        '  </process>\n' +
        '  <bpmndi:BPMNDiagram id="sid-74620812-92c4-44e5-949c-aa47393d3830">\n' +
        '    <bpmndi:BPMNPlane id="sid-cdcae759-2af7-4a6d-bd02-53f3352a731d" bpmnElement="sid-c0e745ff-361e-4afb-8c8d-2a1fc32b1424">\n' +
        '      <bpmndi:BPMNShape id="sid-87F4C1D6-25E1-4A45-9DA7-AD945993D06F_gui" bpmnElement="sid-87F4C1D6-25E1-4A45-9DA7-AD945993D06F" isHorizontal="true">\n' +
        '        <omgdc:Bounds x="158" y="55" width="933" height="250" />\n' +
        '        <bpmndi:BPMNLabel labelStyle="sid-84cb49fd-2f7c-44fb-8950-83c3fa153d3b">\n' +
        '          <omgdc:Bounds x="47.49999999999999" y="170.42857360839844" width="12.000000000000014" height="59.142852783203125" />\n' +
        '        </bpmndi:BPMNLabel>\n' +
        '      </bpmndi:BPMNShape>\n' +
        '      <bpmndi:BPMNShape id="sid-57E4FE0D-18E4-478D-BC5D-B15164E93254_gui" bpmnElement="sid-57E4FE0D-18E4-478D-BC5D-B15164E93254" isHorizontal="true">\n' +
        '        <omgdc:Bounds x="188" y="55" width="903" height="250" />\n' +
        '      </bpmndi:BPMNShape>\n' +
        '      <bpmndi:BPMNShape id="sid-52EB1772-F36E-433E-8F5B-D5DFD26E6F26_gui" bpmnElement="sid-52EB1772-F36E-433E-8F5B-D5DFD26E6F26">\n' +
        '        <omgdc:Bounds x="468" y="120" width="100" height="80" />\n' +
        '        <bpmndi:BPMNLabel labelStyle="sid-84cb49fd-2f7c-44fb-8950-83c3fa153d3b">\n' +
        '          <omgdc:Bounds x="435.5" y="122" width="84" height="12" />\n' +
        '        </bpmndi:BPMNLabel>\n' +
        '      </bpmndi:BPMNShape>\n' +
        '      <bpmndi:BPMNShape id="sid-E49425CF-8287-4798-B622-D2A7D78EF00B_gui" bpmnElement="sid-E49425CF-8287-4798-B622-D2A7D78EF00B">\n' +
        '        <omgdc:Bounds x="803" y="120" width="100" height="80" />\n' +
        '        <bpmndi:BPMNLabel labelStyle="sid-84cb49fd-2f7c-44fb-8950-83c3fa153d3b">\n' +
        '          <omgdc:Bounds x="770.9285736083984" y="112" width="83.14285278320312" height="36" />\n' +
        '        </bpmndi:BPMNLabel>\n' +
        '      </bpmndi:BPMNShape>\n' +
        '      <bpmndi:BPMNShape id="StartEvent_0l6sgn0_di" bpmnElement="sid-D7F237E8-56D0-4283-A3CE-4F0EFE446138">\n' +
        '        <omgdc:Bounds x="262" y="142" width="36" height="36" />\n' +
        '        <bpmndi:BPMNLabel>\n' +
        '          <omgdc:Bounds x="259" y="179" width="43" height="27" />\n' +
        '        </bpmndi:BPMNLabel>\n' +
        '      </bpmndi:BPMNShape>\n' +
        '      <bpmndi:BPMNShape id="EndEvent_0xwuvv5_di" bpmnElement="sid-E433566C-2289-4BEB-A19C-1697048900D2">\n' +
        '        <omgdc:Bounds x="976" y="142" width="36" height="36" />\n' +
        '        <bpmndi:BPMNLabel>\n' +
        '          <omgdc:Bounds x="968" y="181" width="54" height="14" />\n' +
        '        </bpmndi:BPMNLabel>\n' +
        '      </bpmndi:BPMNShape>\n' +
        '      <bpmndi:BPMNShape id="ExclusiveGateway_1g0eih2_di" bpmnElement="sid-5134932A-1863-4FFA-BB3C-A4B4078B11A9" isMarkerVisible="true">\n' +
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
        '      <bpmndi:BPMNEdge id="sid-7B791A11-2F2E-4D80-AFB3-91A02CF2B4FD_gui" bpmnElement="sid-7B791A11-2F2E-4D80-AFB3-91A02CF2B4FD">\n' +
        '        <omgdi:waypoint x="298" y="160" />\n' +
        '        <omgdi:waypoint x="350" y="160" />\n' +
        '        <bpmndi:BPMNLabel>\n' +
        '          <omgdc:Bounds x="204" y="185" width="90" height="20" />\n' +
        '        </bpmndi:BPMNLabel>\n' +
        '      </bpmndi:BPMNEdge>\n' +
        '      <bpmndi:BPMNEdge id="sid-EE8A7BA0-5D66-4F8B-80E3-CC2751B3856A_gui" bpmnElement="sid-EE8A7BA0-5D66-4F8B-80E3-CC2751B3856A">\n' +
        '        <omgdi:waypoint x="568" y="160" />\n' +
        '        <omgdi:waypoint x="660" y="160" />\n' +
        '        <bpmndi:BPMNLabel>\n' +
        '          <omgdc:Bounds x="494" y="185" width="90" height="20" />\n' +
        '        </bpmndi:BPMNLabel>\n' +
        '      </bpmndi:BPMNEdge>\n' +
        '      <bpmndi:BPMNEdge id="sid-57EB1F24-BD94-479A-BF1F-57F1EAA19C6C_gui" bpmnElement="sid-57EB1F24-BD94-479A-BF1F-57F1EAA19C6C">\n' +
        '        <omgdi:waypoint x="903" y="160" />\n' +
        '        <omgdi:waypoint x="976" y="160" />\n' +
        '        <bpmndi:BPMNLabel>\n' +
        '          <omgdc:Bounds x="820" y="185" width="90" height="20" />\n' +
        '        </bpmndi:BPMNLabel>\n' +
        '      </bpmndi:BPMNEdge>\n' +
        '      <bpmndi:BPMNEdge id="sid-8B820AF5-DC5C-4618-B854-E08B71FB55CB_gui" bpmnElement="sid-8B820AF5-DC5C-4618-B854-E08B71FB55CB">\n' +
        '        <omgdi:waypoint x="710" y="160" />\n' +
        '        <omgdi:waypoint x="803" y="160" />\n' +
        '        <bpmndi:BPMNLabel labelStyle="sid-e0502d32-f8d1-41cf-9c4a-cbb49fecf581">\n' +
        '          <omgdc:Bounds x="718" y="135" width="14" height="14" />\n' +
        '        </bpmndi:BPMNLabel>\n' +
        '      </bpmndi:BPMNEdge>\n' +
        '      <bpmndi:BPMNEdge id="sid-4DC479E5-5C20-4948-BCFC-9EC5E2F66D8D_gui" bpmnElement="sid-4DC479E5-5C20-4948-BCFC-9EC5E2F66D8D">\n' +
        '        <omgdi:waypoint x="400" y="160" />\n' +
        '        <omgdi:waypoint x="468" y="160" />\n' +
        '        <bpmndi:BPMNLabel>\n' +
        '          <omgdc:Bounds x="314" y="185" width="90" height="20" />\n' +
        '        </bpmndi:BPMNLabel>\n' +
        '      </bpmndi:BPMNEdge>\n' +
        '      <bpmndi:BPMNEdge id="sid-337A23B9-A923-4CCE-B613-3E247B773CCE_gui" bpmnElement="sid-337A23B9-A923-4CCE-B613-3E247B773CCE">\n' +
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
