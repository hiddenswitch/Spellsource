import { ResponseContext, RequestContext, HttpFile } from '../http/http';
import { Configuration} from '../configuration'

import { ApiV1PromptsPostRequest } from '../models/ApiV1PromptsPostRequest';
import { ExtraData } from '../models/ExtraData';
import { ExtraDataExtraPnginfo } from '../models/ExtraDataExtraPnginfo';
import { GetHistory200ResponseValue } from '../models/GetHistory200ResponseValue';
import { GetPrompt200Response } from '../models/GetPrompt200Response';
import { GetPrompt200ResponseExecInfo } from '../models/GetPrompt200ResponseExecInfo';
import { GetQueue200Response } from '../models/GetQueue200Response';
import { Node } from '../models/Node';
import { NodeInput } from '../models/NodeInput';
import { NodeInputRequiredValueInner } from '../models/NodeInputRequiredValueInner';
import { NodeInputRequiredValueInnerOneOf } from '../models/NodeInputRequiredValueInnerOneOf';
import { PostHistoryRequest } from '../models/PostHistoryRequest';
import { PromptNode } from '../models/PromptNode';
import { PromptNodeInputsValue } from '../models/PromptNodeInputsValue';
import { PromptNodeInputsValueOneOfInner } from '../models/PromptNodeInputsValueOneOfInner';
import { PromptRequest } from '../models/PromptRequest';
import { QueueTupleInner } from '../models/QueueTupleInner';
import { UploadImage200Response } from '../models/UploadImage200Response';
import { Workflow } from '../models/Workflow';
import { WorkflowLinksInnerInner } from '../models/WorkflowLinksInnerInner';
import { WorkflowNodesInner } from '../models/WorkflowNodesInner';
import { WorkflowNodesInnerInputsInner } from '../models/WorkflowNodesInnerInputsInner';
import { WorkflowNodesInnerOutputsInner } from '../models/WorkflowNodesInnerOutputsInner';
import { WorkflowNodesInnerSize } from '../models/WorkflowNodesInnerSize';

import { ObservableDefaultApi } from "./ObservableAPI";
import { DefaultApiRequestFactory, DefaultApiResponseProcessor} from "../apis/DefaultApi";

export interface DefaultApiApiV1ImagesDigestGetRequest {
    /**
     * A digest of the request used to generate the imaeg
     * @type string
     * @memberof DefaultApiapiV1ImagesDigestGet
     */
    digest: string
}

export interface DefaultApiApiV1PromptsGetRequest {
}

export interface DefaultApiApiV1PromptsPostRequest {
    /**
     * 
     * @type { [key: string]: PromptNode; }
     * @memberof DefaultApiapiV1PromptsPost
     */
    requestBody?: { [key: string]: PromptNode; }
}

export interface DefaultApiGetEmbeddingsRequest {
}

export interface DefaultApiGetExtensionsRequest {
}

export interface DefaultApiGetHistoryRequest {
}

export interface DefaultApiGetObjectInfoRequest {
}

export interface DefaultApiGetPromptRequest {
}

export interface DefaultApiGetQueueRequest {
}

export interface DefaultApiGetRootRequest {
}

export interface DefaultApiPostHistoryRequest {
    /**
     * 
     * @type PostHistoryRequest
     * @memberof DefaultApipostHistory
     */
    postHistoryRequest?: PostHistoryRequest
}

export interface DefaultApiPostInterruptRequest {
}

export interface DefaultApiPostPromptRequest {
    /**
     * 
     * @type PromptRequest
     * @memberof DefaultApipostPrompt
     */
    promptRequest?: PromptRequest
}

export interface DefaultApiPostQueueRequest {
    /**
     * 
     * @type PostHistoryRequest
     * @memberof DefaultApipostQueue
     */
    postHistoryRequest?: PostHistoryRequest
}

export interface DefaultApiUploadImageRequest {
    /**
     * The image binary data
     * @type HttpFile
     * @memberof DefaultApiuploadImage
     */
    image?: HttpFile
}

export interface DefaultApiViewImageRequest {
    /**
     * 
     * @type string
     * @memberof DefaultApiviewImage
     */
    filename: string
    /**
     * 
     * @type &#39;output&#39; | &#39;input&#39; | &#39;temp&#39;
     * @memberof DefaultApiviewImage
     */
    type?: 'output' | 'input' | 'temp'
    /**
     * 
     * @type string
     * @memberof DefaultApiviewImage
     */
    subfolder?: string
}

export class ObjectDefaultApi {
    private api: ObservableDefaultApi

    public constructor(configuration: Configuration, requestFactory?: DefaultApiRequestFactory, responseProcessor?: DefaultApiResponseProcessor) {
        this.api = new ObservableDefaultApi(configuration, requestFactory, responseProcessor);
    }

    /**
     * Returns an image given a content hash. 
     * (API) Get image
     * @param param the request object
     */
    public apiV1ImagesDigestGet(param: DefaultApiApiV1ImagesDigestGetRequest, options?: Configuration): Promise<HttpFile> {
        return this.api.apiV1ImagesDigestGet(param.digest,  options).toPromise();
    }

    /**
     * Return the last prompt run anywhere that was used to produce an image  The prompt object can be POSTed to run the image again with your own parameters.  The last prompt, whether it was in the UI or via the API, will be returned here. 
     * (API) Get prompt
     * @param param the request object
     */
    public apiV1PromptsGet(param: DefaultApiApiV1PromptsGetRequest = {}, options?: Configuration): Promise<{ [key: string]: PromptNode; }> {
        return this.api.apiV1PromptsGet( options).toPromise();
    }

    /**
     * Run a prompt to generate an image.  Blocks until the image is produced. This may take an arbitrarily long amount of time due to model loading.  Prompts that produce multiple images will return the last SaveImage output node in the Prompt by default. To return a specific image, remove other SaveImage nodes.  When images are included in your request body, these are saved and their filenames will be used in your Prompt. 
     * (API) Generate image
     * @param param the request object
     */
    public apiV1PromptsPost(param: DefaultApiApiV1PromptsPostRequest = {}, options?: Configuration): Promise<void | string> {
        return this.api.apiV1PromptsPost(param.requestBody,  options).toPromise();
    }

    /**
     * (UI) Get embeddings
     * @param param the request object
     */
    public getEmbeddings(param: DefaultApiGetEmbeddingsRequest = {}, options?: Configuration): Promise<Array<string>> {
        return this.api.getEmbeddings( options).toPromise();
    }

    /**
     * (UI) Get extensions
     * @param param the request object
     */
    public getExtensions(param: DefaultApiGetExtensionsRequest = {}, options?: Configuration): Promise<Array<string>> {
        return this.api.getExtensions( options).toPromise();
    }

    /**
     * (UI) Get history
     * @param param the request object
     */
    public getHistory(param: DefaultApiGetHistoryRequest = {}, options?: Configuration): Promise<{ [key: string]: GetHistory200ResponseValue; }> {
        return this.api.getHistory( options).toPromise();
    }

    /**
     * (UI) Get object info
     * @param param the request object
     */
    public getObjectInfo(param: DefaultApiGetObjectInfoRequest = {}, options?: Configuration): Promise<{ [key: string]: Array<Node>; }> {
        return this.api.getObjectInfo( options).toPromise();
    }

    /**
     * (UI) Get queue info
     * @param param the request object
     */
    public getPrompt(param: DefaultApiGetPromptRequest = {}, options?: Configuration): Promise<GetPrompt200Response> {
        return this.api.getPrompt( options).toPromise();
    }

    /**
     * (UI) Get queue
     * @param param the request object
     */
    public getQueue(param: DefaultApiGetQueueRequest = {}, options?: Configuration): Promise<GetQueue200Response> {
        return this.api.getQueue( options).toPromise();
    }

    /**
     * (UI) index.html
     * @param param the request object
     */
    public getRoot(param: DefaultApiGetRootRequest = {}, options?: Configuration): Promise<void> {
        return this.api.getRoot( options).toPromise();
    }

    /**
     * (UI) Post history
     * @param param the request object
     */
    public postHistory(param: DefaultApiPostHistoryRequest = {}, options?: Configuration): Promise<void> {
        return this.api.postHistory(param.postHistoryRequest,  options).toPromise();
    }

    /**
     * (UI) Post interrupt
     * @param param the request object
     */
    public postInterrupt(param: DefaultApiPostInterruptRequest = {}, options?: Configuration): Promise<void> {
        return this.api.postInterrupt( options).toPromise();
    }

    /**
     * (UI) Post prompt
     * @param param the request object
     */
    public postPrompt(param: DefaultApiPostPromptRequest = {}, options?: Configuration): Promise<string> {
        return this.api.postPrompt(param.promptRequest,  options).toPromise();
    }

    /**
     * (UI) Post queue
     * @param param the request object
     */
    public postQueue(param: DefaultApiPostQueueRequest = {}, options?: Configuration): Promise<void> {
        return this.api.postQueue(param.postHistoryRequest,  options).toPromise();
    }

    /**
     * Uploads an image to the input/ directory.  Never replaces files. The method will return a renamed file name if it would have overwritten an existing file. 
     * (UI) Upload an image.
     * @param param the request object
     */
    public uploadImage(param: DefaultApiUploadImageRequest = {}, options?: Configuration): Promise<UploadImage200Response> {
        return this.api.uploadImage(param.image,  options).toPromise();
    }

    /**
     * (UI) View image
     * @param param the request object
     */
    public viewImage(param: DefaultApiViewImageRequest, options?: Configuration): Promise<HttpFile> {
        return this.api.viewImage(param.filename, param.type, param.subfolder,  options).toPromise();
    }

}
