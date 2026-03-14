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

    describe('getModeler', () => {
        it('should return a modeler instance', () => {
            const modeler = service.getModeler();
            expect(modeler).toBeTruthy();
        });

        it('should return the same modeler on multiple calls', () => {
            expect(service.getModeler()).toBe(service.getModeler());
        });
    });

    describe('getViewer', () => {
        it('should return a viewer instance', () => {
            const viewer = service.getViewer();
            expect(viewer).toBeTruthy();
        });

        it('should return the same viewer on multiple calls', () => {
            expect(service.getViewer()).toBe(service.getViewer());
        });
    });

    describe('getTokenModeler', () => {
        it('should return a token modeler instance', () => {
            const tokenModeler = service.getTokenModeler();
            expect(tokenModeler).toBeTruthy();
        });

        it('should return the same token modeler on multiple calls', () => {
            expect(service.getTokenModeler()).toBe(service.getTokenModeler());
        });
    });

    describe('getBPMNModelXMLBlob', () => {
        it('should return a Blob with XML content', async () => {
            const xml = '<definitions/>';
            spyOn(service.getModeler(), 'saveXML').and.returnValue(
                Promise.resolve({ xml }),
            );

            const blob = await service.getBPMNModelXMLBlob();

            expect(blob.type).toBe('text/xml;charset=utf-8');
            const text = await blob.text();
            expect(text).toBe(xml);
        });

        it('should return empty Blob when saveXML returns no xml', async () => {
            spyOn(service.getModeler(), 'saveXML').and.returnValue(
                Promise.resolve({ xml: undefined as unknown as string }),
            );

            const blob = await service.getBPMNModelXMLBlob();

            expect(blob.size).toBe(0);
        });
    });

    describe('getTokenModelXMLBlob', () => {
        it('should return a Blob with XML content from token modeler', async () => {
            const xml = '<token-definitions/>';
            spyOn(service.getTokenModeler(), 'saveXML').and.returnValue(
                Promise.resolve({ xml }),
            );

            const blob = await service.getTokenModelXMLBlob();

            expect(blob.type).toBe('text/xml;charset=utf-8');
            const text = await blob.text();
            expect(text).toBe(xml);
        });

        it('should return empty Blob when token saveXML returns no xml', async () => {
            spyOn(service.getTokenModeler(), 'saveXML').and.returnValue(
                Promise.resolve({ xml: undefined as unknown as string }),
            );

            const blob = await service.getTokenModelXMLBlob();

            expect(blob.size).toBe(0);
        });
    });

    describe('updateViewerBPMNModel', () => {
        it('should import modeler XML into viewer', async () => {
            const xml = '<definitions/>';
            spyOn(service.getModeler(), 'saveXML').and.returnValue(
                Promise.resolve({ xml }),
            );
            const importSpy = spyOn(
                service.getViewer(),
                'importXML',
            ).and.returnValue(Promise.resolve({ warnings: [] }));

            await service.updateViewerBPMNModel();

            expect(importSpy).toHaveBeenCalledWith(xml);
        });

        it('should not import when modeler saveXML returns no xml', async () => {
            spyOn(service.getModeler(), 'saveXML').and.returnValue(
                Promise.resolve({ xml: undefined as unknown as string }),
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
        it('should import XML into token modeler on first call', async () => {
            const xml = '<definitions/>';
            spyOn(service.getModeler(), 'saveXML').and.returnValue(
                Promise.resolve({ xml }),
            );
            const importSpy = spyOn(
                service.getTokenModeler(),
                'importXML',
            ).and.returnValue(Promise.resolve({ warnings: [] }));

            await service.updateTokenBPMNModelIfNeeded();

            expect(importSpy).toHaveBeenCalledWith(xml);
        });

        it('should skip import when XML has not changed', async () => {
            const xml = '<definitions/>';
            spyOn(service.getModeler(), 'saveXML').and.returnValue(
                Promise.resolve({ xml }),
            );
            const importSpy = spyOn(
                service.getTokenModeler(),
                'importXML',
            ).and.returnValue(Promise.resolve({ warnings: [] }));

            await service.updateTokenBPMNModelIfNeeded();
            expect(importSpy).toHaveBeenCalledTimes(1);

            await service.updateTokenBPMNModelIfNeeded();
            expect(importSpy).toHaveBeenCalledTimes(1);
        });

        it('should re-import when XML has changed', async () => {
            const modelerSaveXML = spyOn(
                service.getModeler(),
                'saveXML',
            ).and.returnValue(
                Promise.resolve({ xml: '<definitions>v1</definitions>' }),
            );
            const importSpy = spyOn(
                service.getTokenModeler(),
                'importXML',
            ).and.returnValue(Promise.resolve({ warnings: [] }));

            await service.updateTokenBPMNModelIfNeeded();
            expect(importSpy).toHaveBeenCalledTimes(1);

            modelerSaveXML.and.returnValue(
                Promise.resolve({ xml: '<definitions>v2</definitions>' }),
            );

            await service.updateTokenBPMNModelIfNeeded();
            expect(importSpy).toHaveBeenCalledTimes(2);
            expect(importSpy).toHaveBeenCalledWith(
                '<definitions>v2</definitions>',
            );
        });

        it('should not import when saveXML returns no xml', async () => {
            spyOn(service.getModeler(), 'saveXML').and.returnValue(
                Promise.resolve({ xml: undefined as unknown as string }),
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
            const xml = '<definitions/>';
            spyOn(service.getModeler(), 'saveXML').and.returnValue(
                Promise.resolve({ xml }),
            );

            const result = await service.getBpmnXML();

            expect(result).toBe(xml);
        });

        it('should return empty string when no XML', async () => {
            spyOn(service.getModeler(), 'saveXML').and.returnValue(
                Promise.resolve({ xml: undefined as unknown as string }),
            );

            const result = await service.getBpmnXML();

            expect(result).toBe('');
        });
    });

    describe('getTokenXML', () => {
        it('should return XML string from token modeler', async () => {
            const xml = '<token-definitions/>';
            spyOn(service.getTokenModeler(), 'saveXML').and.returnValue(
                Promise.resolve({ xml }),
            );

            const result = await service.getTokenXML();

            expect(result).toBe(xml);
        });

        it('should return empty string when no XML', async () => {
            spyOn(service.getTokenModeler(), 'saveXML').and.returnValue(
                Promise.resolve({ xml: undefined as unknown as string }),
            );

            const result = await service.getTokenXML();

            expect(result).toBe('');
        });
    });
});
