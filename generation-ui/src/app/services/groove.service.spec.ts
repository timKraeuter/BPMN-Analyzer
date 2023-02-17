import { TestBed } from '@angular/core/testing';

import { GrooveService } from './groove.service';

describe('GrooveService', () => {
    let service: GrooveService;

    beforeEach(() => {
        TestBed.configureTestingModule({});
        service = TestBed.inject(GrooveService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });
});
