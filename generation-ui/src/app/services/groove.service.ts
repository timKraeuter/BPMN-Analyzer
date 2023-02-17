import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';

const baseURL = environment.production
    ? window.location.href
    : environment.apiURL;
const generateGGAndZipURL = baseURL + 'generateGGAndZip';
const checkBPMNSpecificPropertiesURL = baseURL + 'checkBPMNSpecificProperties';
const checkTemporalLogicPropertyURL = baseURL + 'checkTemporalLogic';

@Injectable({
    providedIn: 'root',
})
export class GrooveService {
    constructor(private httpClient: HttpClient) {}

    downloadGG(xmlModel: Blob): Observable<ArrayBuffer> {
        const options = {
            responseType: 'arraybuffer',
        } as any; // Expect a zip/file response type.
        const formData = new FormData();
        formData.append('file', xmlModel);

        return this.httpClient.post(generateGGAndZipURL, formData, options);
    }

    checkBPMNSpecificProperties(
        bpmnSpecificPropertiesToBeChecked: string[],
        xmlModel: Blob
    ) {
        const formData = new FormData();
        bpmnSpecificPropertiesToBeChecked.forEach((property) =>
            formData.append('propertiesToBeChecked[]', property)
        );
        formData.append('file', xmlModel);

        return this.httpClient.post(checkBPMNSpecificPropertiesURL, formData);
    }

    checkTemporalLogic(
        logic: string,
        property: string,
        xmlModel: Blob
    ): Observable<ModelCheckingResponse> {
        const formData = new FormData();
        formData.append('logic', logic);
        formData.append('property', property);
        formData.append('file', xmlModel);
        return this.httpClient.post<ModelCheckingResponse>(
            checkTemporalLogicPropertyURL,
            formData
        );
    }
}

export class ModelCheckingResponse {
    constructor(public valid: boolean, public error: string) {
        this.valid = valid;
        this.error = error;
    }
}
