import { Injectable } from '@angular/core';

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
    private readonly modeler: Modeler = new Modeler({
        additionalModules: [
            BpmnPropertiesPanelModule,
            BpmnPropertiesProviderModule,
        ],
    });
    private readonly tokenModeler: TokenModeler = new TokenModeler({
        additionalModules: [tokenOverrideModule],
    });
    private readonly viewer: Viewer = new Viewer({
        additionalModules: [
            KeyboardMoveModule,
            MoveCanvasModule,
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
}
