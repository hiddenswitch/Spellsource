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

import { HttpFile } from '../http/http';

export class WorkflowNodesInnerOutputsInner {
    'name'?: string;
    'type'?: string;
    'links'?: Array<number>;
    'slot_index'?: number;

    static readonly discriminator: string | undefined = undefined;

    static readonly attributeTypeMap: Array<{name: string, baseName: string, type: string, format: string}> = [
        {
            "name": "name",
            "baseName": "name",
            "type": "string",
            "format": ""
        },
        {
            "name": "type",
            "baseName": "type",
            "type": "string",
            "format": ""
        },
        {
            "name": "links",
            "baseName": "links",
            "type": "Array<number>",
            "format": ""
        },
        {
            "name": "slot_index",
            "baseName": "slot_index",
            "type": "number",
            "format": ""
        }    ];

    static getAttributeTypeMap() {
        return WorkflowNodesInnerOutputsInner.attributeTypeMap;
    }

    public constructor() {
    }
}
