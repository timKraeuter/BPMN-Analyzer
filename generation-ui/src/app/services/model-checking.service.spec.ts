import { TestBed } from '@angular/core/testing';

import { ModelCheckingService } from './model-checking.service';

describe('GrooveService', () => {
    let service: ModelCheckingService;

    beforeEach(() => {
        TestBed.configureTestingModule({});
        service = TestBed.inject(ModelCheckingService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });
});
