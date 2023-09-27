import { Injectable } from '@angular/core';

/**
 * You may include a different variant of BpmnJS:
 *
 * bpmn-viewer  - displays BPMN diagrams without the ability
 *                to navigate them
 * bpmn-modeler - bootstraps a full-fledged BPMN editor
 */
// @ts-ignore
import Modeler from 'bpmn-js/lib/Modeler';
import Viewer from 'bpmn-js/lib/Viewer';

@Injectable({
    providedIn: 'root',
})
export class BPMNModelerService {
    private modeler: Modeler = new Modeler({
        keyboard: { bindTo: document },
    });
    private viewer: Viewer = new Viewer({
        keyboard: { bindTo: document },
    });

    public getModeler(): Modeler {
        return this.modeler;
    }

    public getViewer(): Viewer {
        return this.viewer;
    }

    public async getBPMNModelXML(): Promise<Blob> {
        const xmlResult = await this.getModeler().saveXML({ format: true });
        if (xmlResult.xml) {
            return new Blob([xmlResult.xml], {
                type: 'text/xml;charset=utf-8',
            });
        }
        return new Blob([]);
    }

    /**
     * Updates the Viewer with the newest bpmn model from the modeler.
     */
    async updateViewerBPMNModel() {
        const saveXMLResult = await this.modeler.saveXML();
        if (saveXMLResult.xml) {
            this.viewer.importXML(saveXMLResult.xml);
        }
    }
}
