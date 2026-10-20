export const WEBGL_ROOT = "https://s3-builds.appmana.com/spellsource-build-artifacts/spellsource-client/webgl/";

export type UnityBuild = {
  schemaVersion?: number;
  buildId?: string;
  sourceRevision?: string;
  unityRevision?: string;
  unityVersion?: string;
  baseUrl: string;
  loaderUrl: string;
  dataUrl: string;
  frameworkUrl: string;
  codeUrl: string;
  symbolsUrl?: string;
  productVersion?: string;
};

export function validBuildId(value: string): boolean {
  return /^[A-Za-z0-9][A-Za-z0-9_-]{0,159}$/.test(value);
}

export function validateBuild(value: UnityBuild): UnityBuild {
  if (value.schemaVersion !== 1 || !value.buildId || !validBuildId(value.buildId)) {
    throw new Error("Invalid browser build descriptor");
  }
  const prefix = `${WEBGL_ROOT}${value.buildId}/`;
  if (value.baseUrl !== prefix ||
      [value.loaderUrl, value.dataUrl, value.frameworkUrl, value.codeUrl, value.symbolsUrl]
        .filter(Boolean).some(url => !new URL(url!).href.startsWith(prefix))) {
    throw new Error("Browser build assets do not belong to the selected build");
  }
  return value;
}

/** Compatibility while the first descriptor-based release is being staged. */
export async function resolveLegacyBuild(index: string): Promise<UnityBuild> {
  const read = async (url: string) => {
    const response = await fetch(url, { cache: "no-store" });
    if (!response.ok) throw new Error(`Browser build returned HTTP ${response.status}`);
    return response.text();
  };
  let html = await read(index);
  const target = html.match(/url=([^"\s]+)/)?.[1];
  if (target) {
    index = new URL(target, index).href;
    html = await read(index);
  }
  const baseUrl = new URL(".", index).href;
  const attr = (name: string) => html.match(new RegExp(`${name}:\\s*"([^"]+)"`))?.[1];
  const asset = (value?: string) => value ? new URL(value, baseUrl).href : undefined;
  const loaderUrl = asset(html.match(/<script src="([^"]+\.loader\.js)"/)?.[1]);
  const dataUrl = asset(attr("dataUrl"));
  const frameworkUrl = asset(attr("frameworkUrl"));
  const codeUrl = asset(attr("codeUrl"));
  if (!loaderUrl || !dataUrl || !frameworkUrl || !codeUrl) throw new Error("Browser build is missing assets");
  return { baseUrl, loaderUrl, dataUrl, frameworkUrl, codeUrl,
    symbolsUrl: asset(attr("symbolsUrl")), productVersion: attr("productVersion") };
}
