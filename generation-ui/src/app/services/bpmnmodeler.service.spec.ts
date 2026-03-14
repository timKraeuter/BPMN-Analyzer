import { TestBed } from '@angular/core/testing';

import { BPMNModelerService } from './bpmnmodeler.service';

describe('BPMNModelerService', () => {
    let service: BPMNModelerService;

    beforeEach(() => {
        TestBed.configureTestingModule({});
        service = TestBed.inject(BPMNModelerService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    it('should return a modeler instance', () => {
        expect(service.getModeler()).toBeTruthy();
    });

    it('should return a viewer instance', () => {
        expect(service.getViewer()).toBeTruthy();
    });

    it('should return a token modeler instance', () => {
        expect(service.getTokenModeler()).toBeTruthy();
    });

    describe('getBPMNModelXMLBlob', () => {
        it('should return a blob with XML content', async () => {
            spyOn(service.getModeler(), 'saveXML').and.returnValue(
                Promise.resolve({ xml: '<definitions/>' }),
            );

            const blob = await service.getBPMNModelXMLBlob();

            expect(blob).toBeTruthy();
            expect(blob.type).toBe('text/xml;charset=utf-8');
            expect(blob.size).toBeGreaterThan(0);
        });

        it('should return empty blob when saveXML returns no xml', async () => {
            spyOn(service.getModeler(), 'saveXML').and.returnValue(
                Promise.resolve({} as any),
            );

            const blob = await service.getBPMNModelXMLBlob();

            expect(blob).toBeTruthy();
            expect(blob.size).toBe(0);
        });
    });

    describe('getTokenModelXMLBlob', () => {
        it('should return a blob with XML content', async () => {
            spyOn(service.getTokenModeler(), 'saveXML').and.returnValue(
                Promise.resolve({ xml: '<token-definitions/>' }),
            );

            const blob = await service.getTokenModelXMLBlob();

            expect(blob).toBeTruthy();
            expect(blob.type).toBe('text/xml;charset=utf-8');
            expect(blob.size).toBeGreaterThan(0);
        });

        it('should return empty blob when saveXML returns no xml', async () => {
            spyOn(service.getTokenModeler(), 'saveXML').and.returnValue(
                Promise.resolve({} as any),
            );

            const blob = await service.getTokenModelXMLBlob();

            expect(blob).toBeTruthy();
            expect(blob.size).toBe(0);
        });
    });

    describe('updateViewerBPMNModel', () => {
        it('should import XML into viewer when modeler has XML', async () => {
            spyOn(service.getModeler(), 'saveXML').and.returnValue(
                Promise.resolve({ xml: '<definitions/>' }),
            );
            const importSpy = spyOn(
                service.getViewer(),
                'importXML',
            ).and.returnValue(Promise.resolve({ warnings: [] }));

            await service.updateViewerBPMNModel();

            expect(importSpy).toHaveBeenCalledWith('<definitions/>');
        });

        it('should not import when modeler saveXML returns no xml', async () => {
            spyOn(service.getModeler(), 'saveXML').and.returnValue(
                Promise.resolve({} as any),
            );
            const importSpy = spyOn(
                service.getViewer(),
                'importXML',
            ).and.returnValue(Promise.resolve({ warnings: [] }));

            await service.updateViewerBPMNModel();

            expect(importSpy).not.toHaveBeenCalled();
        });
    });

    describe('updateTokenBPMNModelIfNeeded', () => {
        it('should import XML into token modeler when XML changes', async () => {
            spyOn(service.getModeler(), 'saveXML').and.returnValue(
                Promise.resolve({ xml: '<definitions-v1/>' }),
            );
            const importSpy = spyOn(
                service.getTokenModeler(),
                'importXML',
            ).and.returnValue(Promise.resolve({ warnings: [] }));

            await service.updateTokenBPMNModelIfNeeded();

            expect(importSpy).toHaveBeenCalledWith('<definitions-v1/>');
        });

        it('should not import when XML is unchanged', async () => {
            const saveXMLSpy = spyOn(
                service.getModeler(),
                'saveXML',
            ).and.returnValue(Promise.resolve({ xml: '<definitions-same/>' }));
            const importSpy = spyOn(
                service.getTokenModeler(),
                'importXML',
            ).and.returnValue(Promise.resolve({ warnings: [] }));

            // First call -- should import
            await service.updateTokenBPMNModelIfNeeded();
            expect(importSpy).toHaveBeenCalledTimes(1);

            // Second call with same XML -- should skip
            saveXMLSpy.and.returnValue(
                Promise.resolve({ xml: '<definitions-same/>' }),
            );
            await service.updateTokenBPMNModelIfNeeded();
            expect(importSpy).toHaveBeenCalledTimes(1);
        });

        it('should import again when XML changes between calls', async () => {
            const saveXMLSpy = spyOn(
                service.getModeler(),
                'saveXML',
            ).and.returnValue(Promise.resolve({ xml: '<definitions-v1/>' }));
            const importSpy = spyOn(
                service.getTokenModeler(),
                'importXML',
            ).and.returnValue(Promise.resolve({ warnings: [] }));

            await service.updateTokenBPMNModelIfNeeded();
            expect(importSpy).toHaveBeenCalledTimes(1);

            // XML changes
            saveXMLSpy.and.returnValue(
                Promise.resolve({ xml: '<definitions-v2/>' }),
            );
            await service.updateTokenBPMNModelIfNeeded();
            expect(importSpy).toHaveBeenCalledTimes(2);
        });

        it('should not import when saveXML returns no xml', async () => {
            spyOn(service.getModeler(), 'saveXML').and.returnValue(
                Promise.resolve({} as any),
            );
            const importSpy = spyOn(
                service.getTokenModeler(),
                'importXML',
            ).and.returnValue(Promise.resolve({ warnings: [] }));

            await service.updateTokenBPMNModelIfNeeded();

            expect(importSpy).not.toHaveBeenCalled();
        });
    });

    describe('getBpmnXML', () => {
        it('should return XML string from modeler', async () => {
            spyOn(service.getModeler(), 'saveXML').and.returnValue(
                Promise.resolve({ xml: '<bpmn-xml/>' }),
            );

            const xml = await service.getBpmnXML();

            expect(xml).toBe('<bpmn-xml/>');
        });

        it('should return empty string when no XML available', async () => {
            spyOn(service.getModeler(), 'saveXML').and.returnValue(
                Promise.resolve({} as any),
            );

            const xml = await service.getBpmnXML();

            expect(xml).toBe('');
        });
    });

    describe('getTokenXML', () => {
        it('should return XML string from token modeler', async () => {
            spyOn(service.getTokenModeler(), 'saveXML').and.returnValue(
                Promise.resolve({ xml: '<token-xml/>' }),
            );

            const xml = await service.getTokenXML();

            expect(xml).toBe('<token-xml/>');
        });

        it('should return empty string when no XML available', async () => {
            spyOn(service.getTokenModeler(), 'saveXML').and.returnValue(
                Promise.resolve({} as any),
            );

            const xml = await service.getTokenXML();

            expect(xml).toBe('');
        });
    });
});
