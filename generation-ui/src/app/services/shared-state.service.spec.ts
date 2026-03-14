import { TestBed } from '@angular/core/testing';

import { SharedStateService } from './shared-state.service';

describe('SharedStateService', () => {
    let service: SharedStateService;

    beforeEach(() => {
        TestBed.configureTestingModule({});
        service = TestBed.inject(SharedStateService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    it('should have default model file name', () => {
        expect(service.modelFileName).toBe('model');
    });

    it('should have empty propositions by default', () => {
        expect(service.propositions).toEqual([]);
    });

    it('should return empty array for getPropositionNames when no propositions', () => {
        expect(service.getPropositionNames()).toEqual([]);
    });

    it('should return proposition names', () => {
        service.propositions = [
            { name: 'p1', xml: '<xml>1</xml>' },
            { name: 'p2', xml: '<xml>2</xml>' },
        ];
        expect(service.getPropositionNames()).toEqual(['p1', 'p2']);
    });
});
