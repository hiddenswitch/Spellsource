import {NextApiRequest, NextApiResponse} from "next";
import {isArray} from "lodash";
import {getAllArt} from "../../../server/art";

export default async (req: NextApiRequest, res: NextApiResponse) => {
  const nameParam = req.query["name"];
  const name = isArray(nameParam) ? nameParam.join("/") : nameParam;

  const artById = await getAllArt();

  if (name in artById) {
    res.redirect("/" + artById[name].src)
  } else {
    res.statusCode = 404;
    res.end();
  }
}
