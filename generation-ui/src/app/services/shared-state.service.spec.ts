import { TestBed } from '@angular/core/testing';

import { SharedStateService } from './shared-state.service';

describe('PropositionService', () => {
    let service: SharedStateService;

    beforeEach(() => {
        TestBed.configureTestingModule({});
        service = TestBed.inject(SharedStateService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });
});
