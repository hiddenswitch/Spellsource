import { ResponseContext, RequestContext, HttpFile } from '../http/http';
import { Configuration} from '../configuration'
import { Observable, of, from } from '../rxjsStub';
import {mergeMap, map} from  '../rxjsStub';
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

import { DefaultApiRequestFactory, DefaultApiResponseProcessor} from "../apis/DefaultApi";
export class ObservableDefaultApi {
    private requestFactory: DefaultApiRequestFactory;
    private responseProcessor: DefaultApiResponseProcessor;
    private configuration: Configuration;

    public constructor(
        configuration: Configuration,
        requestFactory?: DefaultApiRequestFactory,
        responseProcessor?: DefaultApiResponseProcessor
    ) {
        this.configuration = configuration;
        this.requestFactory = requestFactory || new DefaultApiRequestFactory(configuration);
        this.responseProcessor = responseProcessor || new DefaultApiResponseProcessor();
    }

    /**
     * Returns an image given a content hash. 
     * (API) Get image
     * @param digest A digest of the request used to generate the imaeg
     */
    public apiV1ImagesDigestGet(digest: string, _options?: Configuration): Observable<HttpFile> {
        const requestContextPromise = this.requestFactory.apiV1ImagesDigestGet(digest, _options);

        // build promise chain
        let middlewarePreObservable = from<RequestContext>(requestContextPromise);
        for (let middleware of this.configuration.middleware) {
            middlewarePreObservable = middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => middleware.pre(ctx)));
        }

        return middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx))).
            pipe(mergeMap((response: ResponseContext) => {
                let middlewarePostObservable = of(response);
                for (let middleware of this.configuration.middleware) {
                    middlewarePostObservable = middlewarePostObservable.pipe(mergeMap((rsp: ResponseContext) => middleware.post(rsp)));
                }
                return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.apiV1ImagesDigestGet(rsp)));
            }));
    }

    /**
     * Return the last prompt run anywhere that was used to produce an image  The prompt object can be POSTed to run the image again with your own parameters.  The last prompt, whether it was in the UI or via the API, will be returned here. 
     * (API) Get prompt
     */
    public apiV1PromptsGet(_options?: Configuration): Observable<{ [key: string]: PromptNode; }> {
        const requestContextPromise = this.requestFactory.apiV1PromptsGet(_options);

        // build promise chain
        let middlewarePreObservable = from<RequestContext>(requestContextPromise);
        for (let middleware of this.configuration.middleware) {
            middlewarePreObservable = middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => middleware.pre(ctx)));
        }

        return middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx))).
            pipe(mergeMap((response: ResponseContext) => {
                let middlewarePostObservable = of(response);
                for (let middleware of this.configuration.middleware) {
                    middlewarePostObservable = middlewarePostObservable.pipe(mergeMap((rsp: ResponseContext) => middleware.post(rsp)));
                }
                return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.apiV1PromptsGet(rsp)));
            }));
    }

    /**
     * Run a prompt to generate an image.  Blocks until the image is produced. This may take an arbitrarily long amount of time due to model loading.  Prompts that produce multiple images will return the last SaveImage output node in the Prompt by default. To return a specific image, remove other SaveImage nodes.  When images are included in your request body, these are saved and their filenames will be used in your Prompt. 
     * (API) Generate image
     * @param requestBody 
     */
    public apiV1PromptsPost(requestBody?: { [key: string]: PromptNode; }, _options?: Configuration): Observable<ApiV1PromptsPost200Response | void> {
        const requestContextPromise = this.requestFactory.apiV1PromptsPost(requestBody, _options);

        // build promise chain
        let middlewarePreObservable = from<RequestContext>(requestContextPromise);
        for (let middleware of this.configuration.middleware) {
            middlewarePreObservable = middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => middleware.pre(ctx)));
        }

        return middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx))).
            pipe(mergeMap((response: ResponseContext) => {
                let middlewarePostObservable = of(response);
                for (let middleware of this.configuration.middleware) {
                    middlewarePostObservable = middlewarePostObservable.pipe(mergeMap((rsp: ResponseContext) => middleware.post(rsp)));
                }
                return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.apiV1PromptsPost(rsp)));
            }));
    }

    /**
     * (UI) Get embeddings
     */
    public getEmbeddings(_options?: Configuration): Observable<Array<string>> {
        const requestContextPromise = this.requestFactory.getEmbeddings(_options);

        // build promise chain
        let middlewarePreObservable = from<RequestContext>(requestContextPromise);
        for (let middleware of this.configuration.middleware) {
            middlewarePreObservable = middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => middleware.pre(ctx)));
        }

        return middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx))).
            pipe(mergeMap((response: ResponseContext) => {
                let middlewarePostObservable = of(response);
                for (let middleware of this.configuration.middleware) {
                    middlewarePostObservable = middlewarePostObservable.pipe(mergeMap((rsp: ResponseContext) => middleware.post(rsp)));
                }
                return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.getEmbeddings(rsp)));
            }));
    }

    /**
     * (UI) Get extensions
     */
    public getExtensions(_options?: Configuration): Observable<Array<string>> {
        const requestContextPromise = this.requestFactory.getExtensions(_options);

        // build promise chain
        let middlewarePreObservable = from<RequestContext>(requestContextPromise);
        for (let middleware of this.configuration.middleware) {
            middlewarePreObservable = middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => middleware.pre(ctx)));
        }

        return middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx))).
            pipe(mergeMap((response: ResponseContext) => {
                let middlewarePostObservable = of(response);
                for (let middleware of this.configuration.middleware) {
                    middlewarePostObservable = middlewarePostObservable.pipe(mergeMap((rsp: ResponseContext) => middleware.post(rsp)));
                }
                return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.getExtensions(rsp)));
            }));
    }

    /**
     * (UI) Get history
     */
    public getHistory(_options?: Configuration): Observable<{ [key: string]: GetHistory200ResponseValue; }> {
        const requestContextPromise = this.requestFactory.getHistory(_options);

        // build promise chain
        let middlewarePreObservable = from<RequestContext>(requestContextPromise);
        for (let middleware of this.configuration.middleware) {
            middlewarePreObservable = middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => middleware.pre(ctx)));
        }

        return middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx))).
            pipe(mergeMap((response: ResponseContext) => {
                let middlewarePostObservable = of(response);
                for (let middleware of this.configuration.middleware) {
                    middlewarePostObservable = middlewarePostObservable.pipe(mergeMap((rsp: ResponseContext) => middleware.post(rsp)));
                }
                return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.getHistory(rsp)));
            }));
    }

    /**
     * (UI) Get object info
     */
    public getObjectInfo(_options?: Configuration): Observable<{ [key: string]: Array<Node>; }> {
        const requestContextPromise = this.requestFactory.getObjectInfo(_options);

        // build promise chain
        let middlewarePreObservable = from<RequestContext>(requestContextPromise);
        for (let middleware of this.configuration.middleware) {
            middlewarePreObservable = middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => middleware.pre(ctx)));
        }

        return middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx))).
            pipe(mergeMap((response: ResponseContext) => {
                let middlewarePostObservable = of(response);
                for (let middleware of this.configuration.middleware) {
                    middlewarePostObservable = middlewarePostObservable.pipe(mergeMap((rsp: ResponseContext) => middleware.post(rsp)));
                }
                return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.getObjectInfo(rsp)));
            }));
    }

    /**
     * (UI) Get queue info
     */
    public getPrompt(_options?: Configuration): Observable<GetPrompt200Response> {
        const requestContextPromise = this.requestFactory.getPrompt(_options);

        // build promise chain
        let middlewarePreObservable = from<RequestContext>(requestContextPromise);
        for (let middleware of this.configuration.middleware) {
            middlewarePreObservable = middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => middleware.pre(ctx)));
        }

        return middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx))).
            pipe(mergeMap((response: ResponseContext) => {
                let middlewarePostObservable = of(response);
                for (let middleware of this.configuration.middleware) {
                    middlewarePostObservable = middlewarePostObservable.pipe(mergeMap((rsp: ResponseContext) => middleware.post(rsp)));
                }
                return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.getPrompt(rsp)));
            }));
    }

    /**
     * (UI) Get queue
     */
    public getQueue(_options?: Configuration): Observable<GetQueue200Response> {
        const requestContextPromise = this.requestFactory.getQueue(_options);

        // build promise chain
        let middlewarePreObservable = from<RequestContext>(requestContextPromise);
        for (let middleware of this.configuration.middleware) {
            middlewarePreObservable = middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => middleware.pre(ctx)));
        }

        return middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx))).
            pipe(mergeMap((response: ResponseContext) => {
                let middlewarePostObservable = of(response);
                for (let middleware of this.configuration.middleware) {
                    middlewarePostObservable = middlewarePostObservable.pipe(mergeMap((rsp: ResponseContext) => middleware.post(rsp)));
                }
                return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.getQueue(rsp)));
            }));
    }

    /**
     * (UI) index.html
     */
    public getRoot(_options?: Configuration): Observable<void> {
        const requestContextPromise = this.requestFactory.getRoot(_options);

        // build promise chain
        let middlewarePreObservable = from<RequestContext>(requestContextPromise);
        for (let middleware of this.configuration.middleware) {
            middlewarePreObservable = middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => middleware.pre(ctx)));
        }

        return middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx))).
            pipe(mergeMap((response: ResponseContext) => {
                let middlewarePostObservable = of(response);
                for (let middleware of this.configuration.middleware) {
                    middlewarePostObservable = middlewarePostObservable.pipe(mergeMap((rsp: ResponseContext) => middleware.post(rsp)));
                }
                return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.getRoot(rsp)));
            }));
    }

    /**
     * (UI) Post history
     * @param postHistoryRequest 
     */
    public postHistory(postHistoryRequest?: PostHistoryRequest, _options?: Configuration): Observable<void> {
        const requestContextPromise = this.requestFactory.postHistory(postHistoryRequest, _options);

        // build promise chain
        let middlewarePreObservable = from<RequestContext>(requestContextPromise);
        for (let middleware of this.configuration.middleware) {
            middlewarePreObservable = middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => middleware.pre(ctx)));
        }

        return middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx))).
            pipe(mergeMap((response: ResponseContext) => {
                let middlewarePostObservable = of(response);
                for (let middleware of this.configuration.middleware) {
                    middlewarePostObservable = middlewarePostObservable.pipe(mergeMap((rsp: ResponseContext) => middleware.post(rsp)));
                }
                return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.postHistory(rsp)));
            }));
    }

    /**
     * (UI) Post interrupt
     */
    public postInterrupt(_options?: Configuration): Observable<void> {
        const requestContextPromise = this.requestFactory.postInterrupt(_options);

        // build promise chain
        let middlewarePreObservable = from<RequestContext>(requestContextPromise);
        for (let middleware of this.configuration.middleware) {
            middlewarePreObservable = middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => middleware.pre(ctx)));
        }

        return middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx))).
            pipe(mergeMap((response: ResponseContext) => {
                let middlewarePostObservable = of(response);
                for (let middleware of this.configuration.middleware) {
                    middlewarePostObservable = middlewarePostObservable.pipe(mergeMap((rsp: ResponseContext) => middleware.post(rsp)));
                }
                return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.postInterrupt(rsp)));
            }));
    }

    /**
     * (UI) Post prompt
     * @param promptRequest 
     */
    public postPrompt(promptRequest?: PromptRequest, _options?: Configuration): Observable<string> {
        const requestContextPromise = this.requestFactory.postPrompt(promptRequest, _options);

        // build promise chain
        let middlewarePreObservable = from<RequestContext>(requestContextPromise);
        for (let middleware of this.configuration.middleware) {
            middlewarePreObservable = middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => middleware.pre(ctx)));
        }

        return middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx))).
            pipe(mergeMap((response: ResponseContext) => {
                let middlewarePostObservable = of(response);
                for (let middleware of this.configuration.middleware) {
                    middlewarePostObservable = middlewarePostObservable.pipe(mergeMap((rsp: ResponseContext) => middleware.post(rsp)));
                }
                return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.postPrompt(rsp)));
            }));
    }

    /**
     * (UI) Post queue
     * @param postHistoryRequest 
     */
    public postQueue(postHistoryRequest?: PostHistoryRequest, _options?: Configuration): Observable<void> {
        const requestContextPromise = this.requestFactory.postQueue(postHistoryRequest, _options);

        // build promise chain
        let middlewarePreObservable = from<RequestContext>(requestContextPromise);
        for (let middleware of this.configuration.middleware) {
            middlewarePreObservable = middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => middleware.pre(ctx)));
        }

        return middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx))).
            pipe(mergeMap((response: ResponseContext) => {
                let middlewarePostObservable = of(response);
                for (let middleware of this.configuration.middleware) {
                    middlewarePostObservable = middlewarePostObservable.pipe(mergeMap((rsp: ResponseContext) => middleware.post(rsp)));
                }
                return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.postQueue(rsp)));
            }));
    }

    /**
     * Uploads an image to the input/ directory.  Never replaces files. The method will return a renamed file name if it would have overwritten an existing file. 
     * (UI) Upload an image.
     * @param image The image binary data
     */
    public uploadImage(image?: HttpFile, _options?: Configuration): Observable<UploadImage200Response> {
        const requestContextPromise = this.requestFactory.uploadImage(image, _options);

        // build promise chain
        let middlewarePreObservable = from<RequestContext>(requestContextPromise);
        for (let middleware of this.configuration.middleware) {
            middlewarePreObservable = middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => middleware.pre(ctx)));
        }

        return middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx))).
            pipe(mergeMap((response: ResponseContext) => {
                let middlewarePostObservable = of(response);
                for (let middleware of this.configuration.middleware) {
                    middlewarePostObservable = middlewarePostObservable.pipe(mergeMap((rsp: ResponseContext) => middleware.post(rsp)));
                }
                return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.uploadImage(rsp)));
            }));
    }

    /**
     * (UI) View image
     * @param filename 
     * @param type 
     * @param subfolder 
     */
    public viewImage(filename: string, type?: 'output' | 'input' | 'temp', subfolder?: string, _options?: Configuration): Observable<HttpFile> {
        const requestContextPromise = this.requestFactory.viewImage(filename, type, subfolder, _options);

        // build promise chain
        let middlewarePreObservable = from<RequestContext>(requestContextPromise);
        for (let middleware of this.configuration.middleware) {
            middlewarePreObservable = middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => middleware.pre(ctx)));
        }

        return middlewarePreObservable.pipe(mergeMap((ctx: RequestContext) => this.configuration.httpApi.send(ctx))).
            pipe(mergeMap((response: ResponseContext) => {
                let middlewarePostObservable = of(response);
                for (let middleware of this.configuration.middleware) {
                    middlewarePostObservable = middlewarePostObservable.pipe(mergeMap((rsp: ResponseContext) => middleware.post(rsp)));
                }
                return middlewarePostObservable.pipe(map((rsp: ResponseContext) => this.responseProcessor.viewImage(rsp)));
            }));
    }

}
