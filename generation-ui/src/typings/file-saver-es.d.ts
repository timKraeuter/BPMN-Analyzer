declare module 'file-saver-es' {
    export function saveAs(
        data: Blob | string,
        filename?: string,
        options?: { autoBom?: boolean },
    ): void;
}
