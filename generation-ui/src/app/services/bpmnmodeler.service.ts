import { Injectable } from '@angular/core';

/**
 * You may include a different variant of BpmnJS:
 *
 * bpmn-viewer  - displays BPMN diagrams without the ability
 *                to navigate them
 * bpmn-modeler - bootstraps a full-fledged BPMN editor
 */
// @ts-ignore
import BpmnModeler from 'bpmn-js/lib/Modeler';

@Injectable({
    providedIn: 'root',
})
export class BPMNModelerService {
    private bpmnJS: BpmnModeler = new BpmnModeler({
        keyboard: { bindTo: document },
    });

    public getBPMNJs(): BpmnModeler {
        return this.bpmnJS;
    }

    public async getBPMNModelXML(): Promise<Blob> {
        const xmlResult = await this.getBPMNJs().saveXML({ format: true });
        if (xmlResult.xml) {
            return new Blob([xmlResult.xml], {
                type: 'text/xml;charset=utf-8',
            });
        }
        return new Blob([]);
    }
}
