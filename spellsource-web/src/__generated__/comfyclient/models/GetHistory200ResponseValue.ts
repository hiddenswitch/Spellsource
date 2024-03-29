/**
 * comfyui
 * No description provided (generated by Openapi Generator https://github.com/openapitools/openapi-generator)
 *
 * OpenAPI spec version: 0.0.1
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

import { QueueTupleInner } from '../models/QueueTupleInner';
import { HttpFile } from '../http/http';

export class GetHistory200ResponseValue {
    'timestamp'?: number;
    /**
    * The first item is the queue priority The second item is the hash id of the prompt object The third item is a Prompt The fourth item is an ExtraData 
    */
    'prompt'?: Array<QueueTupleInner>;
    'outputs'?: any;

    static readonly discriminator: string | undefined = undefined;

    static readonly attributeTypeMap: Array<{name: string, baseName: string, type: string, format: string}> = [
        {
            "name": "timestamp",
            "baseName": "timestamp",
            "type": "number",
            "format": ""
        },
        {
            "name": "prompt",
            "baseName": "prompt",
            "type": "Array<QueueTupleInner>",
            "format": ""
        },
        {
            "name": "outputs",
            "baseName": "outputs",
            "type": "any",
            "format": ""
        }    ];

    static getAttributeTypeMap() {
        return GetHistory200ResponseValue.attributeTypeMap;
    }

    public constructor() {
    }
}

