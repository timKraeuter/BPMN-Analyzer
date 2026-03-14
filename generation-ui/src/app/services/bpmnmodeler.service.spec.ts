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

    [
        {
            method: 'getBPMNModelXMLBlob' as const,
            modelerGetter: 'getModeler' as const,
        },
        {
            method: 'getTokenModelXMLBlob' as const,
            modelerGetter: 'getTokenModeler' as const,
        },
    ].forEach(({ method, modelerGetter }) => {
        describe(method, () => {
            it('should return a blob with XML content', async () => {
                spyOn(service[modelerGetter](), 'saveXML').and.returnValue(
                    Promise.resolve({ xml: '<definitions/>' }),
                );

                const blob = await service[method]();

                expect(blob).toBeTruthy();
                expect(blob.type).toBe('text/xml;charset=utf-8');
                expect(blob.size).toBeGreaterThan(0);
            });

            it('should return empty blob when saveXML returns no xml', async () => {
                spyOn(service[modelerGetter](), 'saveXML').and.returnValue(
                    Promise.resolve({} as any),
                );

                const blob = await service[method]();

                expect(blob).toBeTruthy();
                expect(blob.size).toBe(0);
            });
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

    [
        {
            method: 'getBpmnXML' as const,
            modelerGetter: 'getModeler' as const,
        },
        {
            method: 'getTokenXML' as const,
            modelerGetter: 'getTokenModeler' as const,
        },
    ].forEach(({ method, modelerGetter }) => {
        describe(method, () => {
            it('should return XML string', async () => {
                spyOn(service[modelerGetter](), 'saveXML').and.returnValue(
                    Promise.resolve({ xml: '<xml-content/>' }),
                );

                const xml = await service[method]();

                expect(xml).toBe('<xml-content/>');
            });

            it('should return empty string when no XML available', async () => {
                spyOn(service[modelerGetter](), 'saveXML').and.returnValue(
                    Promise.resolve({} as any),
                );

                const xml = await service[method]();

                expect(xml).toBe('');
            });
        });
    });
});
