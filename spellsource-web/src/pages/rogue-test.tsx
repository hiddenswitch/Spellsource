import { getServerSession } from "next-auth";
import { NextOptions } from "./api/auth/[...nextauth]";
import { useMutation, useQuery } from "@apollo/client/react";
import { GetServerSideProps } from "next";

import Layout from "../components/creative-layout";
import { GetUserIdTestDocument, StartRogueRunDocument } from "../__generated__/client";
import { Container } from "react-bootstrap";
import { Button } from "../components/typed-bootstrap";

export const getServerSideProps: GetServerSideProps = async (context) => ({
  props: { session: await getServerSession(context.req, context.res, NextOptions) },
});

export default () => {
  useQuery(GetUserIdTestDocument);

  const [startRogueRun] = useMutation(StartRogueRunDocument);

  return (
    <Layout className={"overflow-hidden"}>
      <Container className={"d-flex flex-row justify-content-center p-3"}>
        <Button
          onClick={async () => {
            const rogueRun = await startRogueRun({
              variables: {
                seed: 0,
                heroClass: "TEST",
              },
            }).then((value) => value.data?.startRogueRun);

            console.log(rogueRun);
          }}
        >
          Start
        </Button>
      </Container>
    </Layout>
  );
};
