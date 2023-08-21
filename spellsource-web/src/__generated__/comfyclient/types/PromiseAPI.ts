import { ResponseContext, RequestContext, HttpFile } from '../http/http';
import { Configuration} from '../configuration'

import { ApiV1PromptsPost200Response } from '../models/ApiV1PromptsPost200Response';
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
import { ObservableDefaultApi } from './ObservableAPI';

import { DefaultApiRequestFactory, DefaultApiResponseProcessor} from "../apis/DefaultApi";
export class PromiseDefaultApi {
    private api: ObservableDefaultApi

    public constructor(
        configuration: Configuration,
        requestFactory?: DefaultApiRequestFactory,
        responseProcessor?: DefaultApiResponseProcessor
    ) {
        this.api = new ObservableDefaultApi(configuration, requestFactory, responseProcessor);
    }

    /**
     * Returns an image given a content hash. 
     * (API) Get image
     * @param digest A digest of the request used to generate the imaeg
     */
    public apiV1ImagesDigestGet(digest: string, _options?: Configuration): Promise<HttpFile> {
        const result = this.api.apiV1ImagesDigestGet(digest, _options);
        return result.toPromise();
    }

    /**
     * Return the last prompt run anywhere that was used to produce an image  The prompt object can be POSTed to run the image again with your own parameters.  The last prompt, whether it was in the UI or via the API, will be returned here. 
     * (API) Get prompt
     */
    public apiV1PromptsGet(_options?: Configuration): Promise<{ [key: string]: PromptNode; }> {
        const result = this.api.apiV1PromptsGet(_options);
        return result.toPromise();
    }

    /**
     * Run a prompt to generate an image.  Blocks until the image is produced. This may take an arbitrarily long amount of time due to model loading.  Prompts that produce multiple images will return the last SaveImage output node in the Prompt by default. To return a specific image, remove other SaveImage nodes.  When images are included in your request body, these are saved and their filenames will be used in your Prompt. 
     * (API) Generate image
     * @param requestBody 
     */
    public apiV1PromptsPost(requestBody?: { [key: string]: PromptNode; }, _options?: Configuration): Promise<ApiV1PromptsPost200Response | void> {
        const result = this.api.apiV1PromptsPost(requestBody, _options);
        return result.toPromise();
    }

    /**
     * (UI) Get embeddings
     */
    public getEmbeddings(_options?: Configuration): Promise<Array<string>> {
        const result = this.api.getEmbeddings(_options);
        return result.toPromise();
    }

    /**
     * (UI) Get extensions
     */
    public getExtensions(_options?: Configuration): Promise<Array<string>> {
        const result = this.api.getExtensions(_options);
        return result.toPromise();
    }

    /**
     * (UI) Get history
     */
    public getHistory(_options?: Configuration): Promise<{ [key: string]: GetHistory200ResponseValue; }> {
        const result = this.api.getHistory(_options);
        return result.toPromise();
    }

    /**
     * (UI) Get object info
     */
    public getObjectInfo(_options?: Configuration): Promise<{ [key: string]: Array<Node>; }> {
        const result = this.api.getObjectInfo(_options);
        return result.toPromise();
    }

    /**
     * (UI) Get queue info
     */
    public getPrompt(_options?: Configuration): Promise<GetPrompt200Response> {
        const result = this.api.getPrompt(_options);
        return result.toPromise();
    }

    /**
     * (UI) Get queue
     */
    public getQueue(_options?: Configuration): Promise<GetQueue200Response> {
        const result = this.api.getQueue(_options);
        return result.toPromise();
    }

    /**
     * (UI) index.html
     */
    public getRoot(_options?: Configuration): Promise<void> {
        const result = this.api.getRoot(_options);
        return result.toPromise();
    }

    /**
     * (UI) Post history
     * @param postHistoryRequest 
     */
    public postHistory(postHistoryRequest?: PostHistoryRequest, _options?: Configuration): Promise<void> {
        const result = this.api.postHistory(postHistoryRequest, _options);
        return result.toPromise();
    }

    /**
     * (UI) Post interrupt
     */
    public postInterrupt(_options?: Configuration): Promise<void> {
        const result = this.api.postInterrupt(_options);
        return result.toPromise();
    }

    /**
     * (UI) Post prompt
     * @param promptRequest 
     */
    public postPrompt(promptRequest?: PromptRequest, _options?: Configuration): Promise<string> {
        const result = this.api.postPrompt(promptRequest, _options);
        return result.toPromise();
    }

    /**
     * (UI) Post queue
     * @param postHistoryRequest 
     */
    public postQueue(postHistoryRequest?: PostHistoryRequest, _options?: Configuration): Promise<void> {
        const result = this.api.postQueue(postHistoryRequest, _options);
        return result.toPromise();
    }

    /**
     * Uploads an image to the input/ directory.  Never replaces files. The method will return a renamed file name if it would have overwritten an existing file. 
     * (UI) Upload an image.
     * @param image The image binary data
     */
    public uploadImage(image?: HttpFile, _options?: Configuration): Promise<UploadImage200Response> {
        const result = this.api.uploadImage(image, _options);
        return result.toPromise();
    }

    /**
     * (UI) View image
     * @param filename 
     * @param type 
     * @param subfolder 
     */
    public viewImage(filename: string, type?: 'output' | 'input' | 'temp', subfolder?: string, _options?: Configuration): Promise<HttpFile> {
        const result = this.api.viewImage(filename, type, subfolder, _options);
        return result.toPromise();
    }


}



