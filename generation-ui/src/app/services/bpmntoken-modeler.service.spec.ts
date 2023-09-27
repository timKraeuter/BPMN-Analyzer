import { TestBed } from '@angular/core/testing';

import { BPMNTokenModelerService } from './bpmntoken-modeler.service';

describe('BPMNTokenModelerService', () => {
    let service: BPMNTokenModelerService;

    beforeEach(() => {
        TestBed.configureTestingModule({});
        service = TestBed.inject(BPMNTokenModelerService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });
});
