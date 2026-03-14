import { BPMNProperty } from './bpmn-property';

export interface ModelCheckingResponse {
    property: string;
    valid: boolean;
    error: string;
}

export interface BPMNSpecificPropertiesResponse {
    propertyCheckingResults: BPMNProperty[];
}
