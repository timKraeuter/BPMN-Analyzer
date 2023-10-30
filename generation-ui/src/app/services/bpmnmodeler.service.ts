import { Injectable } from '@angular/core';

/**
 * You may include a different variant of BpmnJS:
 *
 * bpmn-viewer  - displays BPMN diagrams without the ability
 *                to navigate them
 * bpmn-modeler - bootstraps a full-fledged BPMN editor
 */
import Modeler from 'bpmn-js/lib/Modeler';
import TokenModeler from 'bpmn-token/lib/Modeler';
import Viewer from 'bpmn-js/lib/Viewer';
import { SaveXMLResult } from 'bpmn-js/lib/BaseViewer';

import TokenContextPadProvider from 'bpmn-token/lib/features/token-context-pad/TokenContextPadProvider';
import TokenPaletteProvider from 'bpmn-token/lib/features/token-palette/TokenPaletteProvider';
import TokenKeyboardBindings from 'bpmn-token/lib/features/token-keyboard/TokenKeyboardBindings';
import TokenRules from 'bpmn-token/lib/features/token-rules/TokenRules';

import KeyboardMoveModule from 'diagram-js/lib/navigation/keyboard-move';
import MoveCanvasModule from 'diagram-js/lib/navigation/movecanvas';
import TouchModule from 'diagram-js/lib/navigation/touch';
import ZoomScrollModule from 'diagram-js/lib/navigation/zoomscroll';

import {
    BpmnPropertiesPanelModule,
    BpmnPropertiesProviderModule,
    // @ts-ignore
} from 'bpmn-js-properties-panel';

const tokenOverrideModule = {
    contextPadProvider: ['type', TokenContextPadProvider],
    paletteProvider: ['type', TokenPaletteProvider],
    keyboardBindings: ['type', TokenKeyboardBindings],
    bpmnRules: ['type', TokenRules],
};

@Injectable({
    providedIn: 'root',
})
export class BPMNModelerService {
    private modeler: Modeler = new Modeler({
        keyboard: { bindTo: document },
        additionalModules: [
            BpmnPropertiesPanelModule,
            BpmnPropertiesProviderModule,
        ],
    });
    private tokenModeler: TokenModeler = new TokenModeler({
        additionalModules: [tokenOverrideModule],
    });
    private viewer: Viewer = new Viewer({
        additionalModules: [
            KeyboardMoveModule,
            MoveCanvasModule,
            TouchModule,
            ZoomScrollModule,
        ],
    });

    private lastXMLLoadedByTokenModeler: string = '';

    public getModeler(): Modeler {
        return this.modeler;
    }

    public getViewer(): Viewer {
        return this.viewer;
    }

    getTokenModeler(): TokenModeler {
        return this.tokenModeler;
    }

    public async getBPMNModelXMLBlob(): Promise<Blob> {
        const xmlResult = await this.getModeler().saveXML({ format: true });
        return this.returnAsBlob(xmlResult);
    }

    public async getTokenModelXMLBlob(): Promise<Blob> {
        const xmlResult = await this.getTokenModeler().saveXML({
            format: true,
        });
        return this.returnAsBlob(xmlResult);
    }

    private returnAsBlob(xmlResult: SaveXMLResult) {
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
            await this.viewer.importXML(saveXMLResult.xml);
        }
    }

    async updateTokenBPMNModelIfNeeded() {
        const saveXMLResult = await this.modeler.saveXML();
        if (
            saveXMLResult.xml &&
            this.lastXMLLoadedByTokenModeler !== saveXMLResult.xml
        ) {
            await this.tokenModeler.importXML(saveXMLResult.xml);
            this.lastXMLLoadedByTokenModeler = saveXMLResult.xml;
        }
    }

    async getBpmnXML() {
        return this.getXML(this.modeler);
    }

    async getTokenXML() {
        return this.getXML(this.tokenModeler);
    }

    private async getXML(modeler: Modeler | TokenModeler) {
        const saveXMLResult = await modeler.saveXML();
        if (saveXMLResult.xml) {
            return saveXMLResult.xml;
        }
        return '';
    }

    public unbindKeyboards() {
        // @ts-ignore
        this.modeler.get('keyboard').unbind();
        // @ts-ignore
        this.tokenModeler.get('keyboard').unbind();
    }

    public bindModelerKeyboard() {
        this.unbindKeyboards();
        // @ts-ignore
        this.modeler.get('keyboard').bind(document);
    }

    public bindTokenModelerKeyboard() {
        this.unbindKeyboards();
        // @ts-ignore
        this.tokenModeler.get('keyboard').bind(document);
    }
}
