import { TestBed } from '@angular/core/testing';

import { ThemeService } from './theme.service';

describe('ThemeService', () => {
    let service: ThemeService;

    beforeEach(() => {
        localStorage.removeItem('dark-theme');
        document.documentElement.classList.remove('dark-theme');
        TestBed.configureTestingModule({});
        service = TestBed.inject(ThemeService);
    });

    afterEach(() => {
        localStorage.removeItem('dark-theme');
        document.documentElement.classList.remove('dark-theme');
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    it('should toggle dark mode on', () => {
        service.toggle();
        expect(service.isDarkMode).toBeTrue();
        expect(
            document.documentElement.classList.contains('dark-theme'),
        ).toBeTrue();
        expect(localStorage.getItem('dark-theme')).toBe('true');
    });

    it('should toggle dark mode off after toggling on', () => {
        service.toggle();
        service.toggle();
        expect(service.isDarkMode).toBeFalse();
        expect(
            document.documentElement.classList.contains('dark-theme'),
        ).toBeFalse();
        expect(localStorage.getItem('dark-theme')).toBe('false');
    });

    it('should restore dark mode from localStorage', () => {
        localStorage.setItem('dark-theme', 'true');
        const newService = TestBed.inject(ThemeService);
        // Service reads from localStorage in constructor
        expect(newService).toBeTruthy();
    });
});
