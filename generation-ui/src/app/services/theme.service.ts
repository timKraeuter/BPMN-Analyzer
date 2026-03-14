import { Injectable } from '@angular/core';

@Injectable({
    providedIn: 'root',
})
export class ThemeService {
    private readonly storageKey = 'dark-theme';
    private _isDarkMode = false;

    get isDarkMode(): boolean {
        return this._isDarkMode;
    }

    constructor() {
        const stored = localStorage.getItem(this.storageKey);
        if (stored !== null) {
            this._isDarkMode = stored === 'true';
        } else {
            this._isDarkMode =
                window.matchMedia &&
                window.matchMedia('(prefers-color-scheme: dark)').matches;
        }
        this.applyTheme();
    }

    toggle(): void {
        this._isDarkMode = !this._isDarkMode;
        localStorage.setItem(this.storageKey, String(this._isDarkMode));
        this.applyTheme();
    }

    private applyTheme(): void {
        if (this._isDarkMode) {
            document.documentElement.classList.add('dark-theme');
        } else {
            document.documentElement.classList.remove('dark-theme');
        }
    }
}
