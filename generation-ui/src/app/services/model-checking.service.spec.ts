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
import { ModelCheckingResponse } from '../models/model-checking-response';

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
        it('should POST to generateGGAndZip endpoint', () => {
            const xmlModel = new Blob(['<xml/>'], { type: 'text/xml' });
            const propositions = [{ name: 'p1', xml: '<token/>' }];

            service
                .downloadGG(xmlModel, propositions)
                .subscribe((data: ArrayBuffer) => {
                    expect(data).toBeTruthy();
                });

            const req = httpMock.expectOne(
                'http://localhost:8080/generateGGAndZip',
            );
            expect(req.request.method).toBe('POST');
            expect(req.request.body instanceof FormData).toBeTrue();
            expect(req.request.responseType).toBe('arraybuffer');
            req.flush(new ArrayBuffer(8));
        });
    });

    describe('checkBPMNSpecificProperties', () => {
        it('should POST to checkBPMNSpecificProperties endpoint', () => {
            const properties = ['Safeness', 'OptionToComplete'];
            const xmlModel = new Blob(['<xml/>'], { type: 'text/xml' });

            service
                .checkBPMNSpecificProperties(properties, xmlModel)
                .subscribe((data) => {
                    expect(data).toBeTruthy();
                });

            const req = httpMock.expectOne(
                'http://localhost:8080/checkBPMNSpecificProperties',
            );
            expect(req.request.method).toBe('POST');
            expect(req.request.body instanceof FormData).toBeTrue();
            req.flush({
                propertyCheckingResults: [{ name: 'Safeness', valid: true }],
            });
        });
    });

    describe('checkTemporalLogic', () => {
        it('should POST to checkTemporalLogic endpoint', () => {
            const xmlModel = new Blob(['<xml/>'], { type: 'text/xml' });
            const propositions = [{ name: 'p1', xml: '<token/>' }];

            service
                .checkTemporalLogic(
                    'CTL',
                    'AG(!Unsafe)',
                    xmlModel,
                    propositions,
                )
                .subscribe((response: ModelCheckingResponse) => {
                    expect(response.property).toBe('AG(!Unsafe)');
                    expect(response.valid).toBeTrue();
                    expect(response.error).toBe('');
                });

            const req = httpMock.expectOne(
                'http://localhost:8080/checkTemporalLogic',
            );
            expect(req.request.method).toBe('POST');
            expect(req.request.body instanceof FormData).toBeTrue();
            req.flush({
                property: 'AG(!Unsafe)',
                valid: true,
                error: '',
            });
        });

        it('should use empty propositions by default', () => {
            const xmlModel = new Blob(['<xml/>'], { type: 'text/xml' });

            service.checkTemporalLogic('CTL', 'EF(p)', xmlModel).subscribe();

            const req = httpMock.expectOne(
                'http://localhost:8080/checkTemporalLogic',
            );
            expect(req.request.method).toBe('POST');
            req.flush({ property: 'EF(p)', valid: true, error: '' });
        });
    });

    describe('ModelCheckingResponse', () => {
        it('should construct with all fields', () => {
            const response: ModelCheckingResponse = {
                property: 'AG(!Unsafe)',
                valid: true,
                error: '',
            };
            expect(response.property).toBe('AG(!Unsafe)');
            expect(response.valid).toBeTrue();
            expect(response.error).toBe('');
        });

        it('should construct with error', () => {
            const response: ModelCheckingResponse = {
                property: 'G(!Unsafe)',
                valid: false,
                error: 'Invalid syntax',
            };
            expect(response.property).toBe('G(!Unsafe)');
            expect(response.valid).toBeFalse();
            expect(response.error).toBe('Invalid syntax');
        });
    });
});
