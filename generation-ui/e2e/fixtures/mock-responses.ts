/**
 * Canned API responses for mocking the backend in E2E tests.
 *
 * Endpoints mocked:
 *  - POST /checkBPMNSpecificProperties
 *  - POST /checkTemporalLogic
 *  - POST /generateGGAndZip
 */

export const bpmnPropertiesAllValid = {
    propertyCheckingResults: [
        { name: 'Safeness', valid: true, additionalInfo: '' },
        {
            name: 'Option to complete',
            valid: true,
            additionalInfo: '',
        },
        { name: 'Proper completion', valid: true, additionalInfo: '' },
        {
            name: 'No dead activities',
            valid: true,
            additionalInfo: '',
        },
    ],
};

export const bpmnPropertiesMixed = {
    propertyCheckingResults: [
        { name: 'Safeness', valid: true, additionalInfo: '' },
        {
            name: 'Option to complete',
            valid: false,
            additionalInfo: '',
        },
    ],
};

export const ctlPropertyValid = {
    property: 'AG(!Unsafe)',
    valid: true,
    error: '',
};

export const ctlPropertyInvalid = {
    property: 'AF(AllTerminated)',
    valid: false,
    error: '',
};

export const ctlPropertyError = {
    property: 'INVALID_FORMULA',
    valid: false,
    error: 'Parse error in CTL formula',
};

/**
 * A minimal ZIP-like buffer for mocking the /generateGGAndZip response.
 * This is the local file header signature (PK\x03\x04) followed by zeros.
 */
export function fakeZipBuffer(): ArrayBuffer {
    const bytes = new Uint8Array([
        0x50, 0x4b, 0x03, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
    ]);
    return bytes.buffer;
}
