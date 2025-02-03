import { NextApiRequest, NextApiResponse } from "next";
import { isArray, keyBy } from "lodash";
import { ImageDef } from "../../../lib/art-generation";
import { getAllArt } from "../../../lib/fs-utils";

let artById: Promise<Record<string, ImageDef>>;

export const getArtById = async () => {
  if (!artById) {
    console.log("Reading art from disk");
    artById = getAllArt().then((value) => keyBy(value, (value) => value.name));
  }

  return artById;
};

export default async (req: NextApiRequest, res: NextApiResponse) => {
  const nameParam = req.query["name"] ?? "";
  const name = isArray(nameParam) ? nameParam.join("/") : nameParam;

  const artById = await getArtById();

  if (name in artById) {
    res.redirect("/" + artById[name].src);
  } else {
    res.statusCode = 404;
    res.end();
  }
};
