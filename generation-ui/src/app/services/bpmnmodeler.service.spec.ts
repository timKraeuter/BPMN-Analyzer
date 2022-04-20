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
});
