import { TestBed } from '@angular/core/testing';

import { SharedStateService } from './shared-state.service';
import { Proposition } from './shared-state.service';

describe('SharedStateService', () => {
    let service: SharedStateService;

    beforeEach(() => {
        TestBed.configureTestingModule({});
        service = TestBed.inject(SharedStateService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    it('should have default modelFileName of "model"', () => {
        expect(service.modelFileName).toBe('model');
    });

    it('should have empty propositions by default', () => {
        expect(service.propositions).toEqual([]);
    });

    it('should return empty array from getPropositionNames when no propositions', () => {
        expect(service.getPropositionNames()).toEqual([]);
    });

    it('should return proposition names in order', () => {
        const propositions: Proposition[] = [
            { name: 'prop1', xml: '<xml>1</xml>' },
            { name: 'prop2', xml: '<xml>2</xml>' },
            { name: 'prop3', xml: '<xml>3</xml>' },
        ];
        service.propositions = propositions;

        expect(service.getPropositionNames()).toEqual([
            'prop1',
            'prop2',
            'prop3',
        ]);
    });

    it('should return single proposition name', () => {
        service.propositions = [{ name: 'only', xml: '<xml/>' }];

        expect(service.getPropositionNames()).toEqual(['only']);
    });

    it('should reflect changes to propositions in getPropositionNames', () => {
        service.propositions.push({ name: 'added', xml: '<xml/>' });

        expect(service.getPropositionNames()).toEqual(['added']);
    });
});
