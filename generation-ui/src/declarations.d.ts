declare module 'file-saver-es' {
    export function saveAs(data: Blob, filename: string): void;
}

declare module 'bpmn-js-properties-panel' {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    export const BpmnPropertiesPanelModule: any;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    export const BpmnPropertiesProviderModule: any;
}
