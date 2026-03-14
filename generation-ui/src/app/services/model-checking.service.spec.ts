import { TestBed } from '@angular/core/testing';
import {
    provideHttpClient,
    withInterceptorsFromDi,
} from '@angular/common/http';
import {
    HttpTestingController,
    provideHttpClientTesting,
} from '@angular/common/http/testing';

import { ModelCheckingService } from './model-checking.service';
import { Proposition } from './shared-state.service';

describe('ModelCheckingService', () => {
    let service: ModelCheckingService;
    let httpMock: HttpTestingController;

    beforeEach(() => {
        TestBed.configureTestingModule({
            providers: [
                provideHttpClient(withInterceptorsFromDi()),
                provideHttpClientTesting(),
            ],
        });
        service = TestBed.inject(ModelCheckingService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    describe('downloadGG', () => {
        it('should POST to generateGGAndZip with file and propositions', () => {
            const blob = new Blob(['<xml/>'], { type: 'text/xml' });
            const propositions: Proposition[] = [
                { name: 'p1', xml: '<prop/>' },
            ];

            service
                .downloadGG(blob, propositions)
                .subscribe((result) =>
                    expect(result).toEqual(jasmine.any(ArrayBuffer)),
                );

            const req = httpMock.expectOne((r) =>
                r.url.endsWith('generateGGAndZip'),
            );
            expect(req.request.method).toBe('POST');
            expect(req.request.responseType).toBe('arraybuffer');

            const body = req.request.body as FormData;
            expect(body.get('file')).toBeTruthy();
            expect(body.get('propositions')).toBe(JSON.stringify(propositions));

            req.flush(new ArrayBuffer(8));
        });

        it('should send empty propositions array', () => {
            const blob = new Blob(['<xml/>']);

            service.downloadGG(blob, []).subscribe();

            const req = httpMock.expectOne((r) =>
                r.url.endsWith('generateGGAndZip'),
            );
            const body = req.request.body as FormData;
            expect(body.get('propositions')).toBe('[]');

            req.flush(new ArrayBuffer(0));
        });
    });

    describe('checkBPMNSpecificProperties', () => {
        it('should POST properties and file to checkBPMNSpecificProperties', () => {
            const properties = ['Safeness', 'OptionToComplete'];
            const blob = new Blob(['<xml/>'], { type: 'text/xml' });

            service.checkBPMNSpecificProperties(properties, blob).subscribe();

            const req = httpMock.expectOne((r) =>
                r.url.endsWith('checkBPMNSpecificProperties'),
            );
            expect(req.request.method).toBe('POST');

            const body = req.request.body as FormData;
            const sentProperties = body.getAll('propertiesToBeChecked');
            expect(sentProperties).toEqual(['Safeness', 'OptionToComplete']);
            expect(body.get('file')).toBeTruthy();

            req.flush({ results: [] });
        });

        it('should handle single property', () => {
            const blob = new Blob(['<xml/>']);

            service.checkBPMNSpecificProperties(['Safeness'], blob).subscribe();

            const req = httpMock.expectOne((r) =>
                r.url.endsWith('checkBPMNSpecificProperties'),
            );
            const body = req.request.body as FormData;
            expect(body.getAll('propertiesToBeChecked')).toEqual(['Safeness']);

            req.flush({});
        });
    });

    describe('checkTemporalLogic', () => {
        it('should POST logic, property, file, and propositions', () => {
            const blob = new Blob(['<xml/>']);
            const propositions: Proposition[] = [
                { name: 'p1', xml: '<prop/>' },
            ];

            service
                .checkTemporalLogic('CTL', 'AG(true)', blob, propositions)
                .subscribe((response) => {
                    expect(response.property).toBe('AG(true)');
                    expect(response.valid).toBeTrue();
                });

            const req = httpMock.expectOne((r) =>
                r.url.endsWith('checkTemporalLogic'),
            );
            expect(req.request.method).toBe('POST');

            const body = req.request.body as FormData;
            expect(body.get('logic')).toBe('CTL');
            expect(body.get('property')).toBe('AG(true)');
            expect(body.get('file')).toBeTruthy();
            expect(body.get('propositions')).toBe(JSON.stringify(propositions));

            req.flush({ property: 'AG(true)', valid: true, error: '' });
        });

        it('should default propositions to empty array when not provided', () => {
            const blob = new Blob(['<xml/>']);

            service.checkTemporalLogic('CTL', 'EF(true)', blob).subscribe();

            const req = httpMock.expectOne((r) =>
                r.url.endsWith('checkTemporalLogic'),
            );
            const body = req.request.body as FormData;
            expect(body.get('propositions')).toBe('[]');

            req.flush({ property: 'EF(true)', valid: true, error: '' });
        });

        it('should handle error response', () => {
            const blob = new Blob(['<xml/>']);

            service
                .checkTemporalLogic('CTL', 'invalid', blob)
                .subscribe((response) => {
                    expect(response.valid).toBeFalse();
                    expect(response.error).toBe('Parse error');
                });

            const req = httpMock.expectOne((r) =>
                r.url.endsWith('checkTemporalLogic'),
            );
            req.flush({
                property: 'invalid',
                valid: false,
                error: 'Parse error',
            });
        });
    });
});
