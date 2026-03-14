import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import {
    BPMNSpecificPropertiesResponse,
    ModelCheckingResponse,
} from '../models/model-checking-response';
import { Proposition } from '../models/proposition';

const baseURL = environment.production
    ? globalThis.location.href
    : environment.apiURL;
const generateGGAndZipURL = baseURL + 'generateGGAndZip';
const checkBPMNSpecificPropertiesURL = baseURL + 'checkBPMNSpecificProperties';
const checkTemporalLogicPropertyURL = baseURL + 'checkTemporalLogic';

@Injectable({
    providedIn: 'root',
})
export class ModelCheckingService {
    constructor(private readonly httpClient: HttpClient) {}

    downloadGG(
        xmlModel: Blob,
        propositions: Proposition[],
    ): Observable<ArrayBuffer> {
        const formData = new FormData();
        formData.append('file', xmlModel);
        formData.append('propositions', JSON.stringify(propositions));

        return this.httpClient.post(generateGGAndZipURL, formData, {
            responseType: 'arraybuffer',
        });
    }

    checkBPMNSpecificProperties(
        bpmnSpecificPropertiesToBeChecked: string[],
        xmlModel: Blob,
    ): Observable<BPMNSpecificPropertiesResponse> {
        const formData = new FormData();
        bpmnSpecificPropertiesToBeChecked.forEach((property) =>
            formData.append('propertiesToBeChecked', property),
        );
        formData.append('file', xmlModel);

        return this.httpClient.post<BPMNSpecificPropertiesResponse>(
            checkBPMNSpecificPropertiesURL,
            formData,
        );
    }

    checkTemporalLogic(
        logic: string,
        property: string,
        xmlModel: Blob,
        propositions: Proposition[] = [],
    ): Observable<ModelCheckingResponse> {
        const formData = new FormData();
        formData.append('logic', logic);
        formData.append('property', property);
        formData.append('file', xmlModel);
        formData.append('propositions', JSON.stringify(propositions));
        return this.httpClient.post<ModelCheckingResponse>(
            checkTemporalLogicPropertyURL,
            formData,
        );
    }
}
