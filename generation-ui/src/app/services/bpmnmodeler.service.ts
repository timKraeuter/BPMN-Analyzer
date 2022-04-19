import {Injectable} from '@angular/core';

/**
 * You may include a different variant of BpmnJS:
 *
 * bpmn-viewer  - displays BPMN diagrams without the ability
 *                to navigate them
 * bpmn-modeler - bootstraps a full-fledged BPMN editor
 */
// @ts-ignore
import * as BpmnJS from 'bpmn-js/dist/bpmn-modeler.production.min.js';

@Injectable({
  providedIn: 'root'
})
export class BPMNModelerService {
  private bpmnJS: BpmnJS;

  constructor() {
  }

  getBPMNJs(): BpmnJS {
    if (!this.bpmnJS) {
      this.bpmnJS = new BpmnJS({keyboard: {bindTo: document}});
    }
    return this.bpmnJS;
  }
}
