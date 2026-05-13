import { GetServerSideProps } from "next";
import { getSession } from "next-auth/react";
import Layout from "../components/creative-layout";
import { useGetClassesQuery, useGetUserIdTestQuery, useStartRogueRunMutation } from "../__generated__/client";
import { Container } from "react-bootstrap";
import { Button } from "../components/typed-bootstrap";

export const getServerSideProps: GetServerSideProps = async (context) => ({
  props: { session: await getSession(context) },
});

export default () => {
  const getHeroClass = useGetClassesQuery({
    variables: {
      filter: {
        collectible: { equalTo: true },
      },
    },
  });

  const classes = getHeroClass?.data?.allClasses?.nodes;

  useGetUserIdTestQuery({ onCompleted: (data) => console.log(data) });

  const [startRogueRun] = useStartRogueRunMutation();

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
