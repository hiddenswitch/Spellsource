import { useSession } from "next-auth/react"
import React, {useEffect, useMemo, useState} from "react"
import { CardType } from "../__generated__/spellsource-game"
import { useGetCardsQuery, useGetClassesQuery } from "../__generated__/client"
import { CardDef } from "./card-display"
import { Button, Dropdown, Form, Table } from "react-bootstrap"
import { formatCurated } from "../lib/blockly-misc-utils"
import { chain, clamp } from "lodash"
import { useSet } from "react-use"
import DropdownToggle from "react-bootstrap/DropdownToggle"
import DropdownItem from "react-bootstrap/DropdownItem"
import DropdownMenu from "react-bootstrap/DropdownMenu"

const ShowCardTypes: CardType[] = ["MINION", "SPELL", "WEAPON", "HERO", "HERO_POWER", "CLASS"]
const DefaultShowCardTypes: CardType[] = ["MINION", "SPELL", "WEAPON"]
const limit = 20

export default () => {
  const { data: session, status } = useSession()

  const user = session?.token?.sub

  const [offset, setOffset] = useState(0)
  const [search, setSearch] = useState("")
  const [heroClass, setHeroClass] = useState("")
  const [cardTypes, { has, toggle }] = useSet(new Set(DefaultShowCardTypes))

  const getCards = useGetCardsQuery({
    variables: {
      limit,
      offset,
      filter: {
        type: { in: [...cardTypes] },
        class: heroClass ? { equalTo: heroClass } : undefined,
        searchMessage: search ? { includesInsensitive: search.trim() } : undefined,
        collectible: { equalTo: true },
      },
    },
  })
  const cards = getCards?.data?.allCollectionCards?.nodes ?? getCards?.previousData?.allCollectionCards?.nodes ?? []
  const total =
    getCards?.data?.allCollectionCards?.totalCount ?? getCards?.previousData?.allCollectionCards?.totalCount ?? 0

  const getClasses = useGetClassesQuery({ variables: { filter: { collectible: { equalTo: true } } } })
  const classes = useMemo(
    () =>
      chain(getClasses.data?.allClasses?.nodes ?? [])
        .sortBy((card) => card.class !== "ANY")
        .keyBy((card) => card.class)
        .mapValues((card) => card.cardScript.name)
        .value(),
    [getClasses.data]
  )

  const showing = Math.min(limit, cards.length)

  const changeOffset = (delta: number = 0) => {
    const newOffset = clamp(offset + delta, 0, total - limit)
    setOffset(newOffset)
  }

  useEffect(changeOffset, [getCards.data])

  console.log(classes)

  return (
    <div>
      <div id={"Top Bar"} className={"d-flex flex-row flex-wrap gap-2 pt-2 ps-2 align-items-center"}>
        <Button disabled={!getCards.data || offset === 0} variant={"secondary"} onClick={() => changeOffset(-limit)}>
          Prev
        </Button>
        <div className={"mb-1 text-center"}>
          {cards?.length ? offset + 1 : "?"}-{cards?.length ? offset + showing : "?"} of {cards?.length ? total : "??"}
        </div>
        <Button
          disabled={!getCards.data || offset === total - limit}
          variant={"secondary"}
          onClick={() => changeOffset(limit)}
        >
          Next
        </Button>
        <Form.Control
          placeholder={"Search"}
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          style={{ maxWidth: "25rem" }}
        />
        '
        <Dropdown>
          <DropdownToggle>{heroClass ? classes[heroClass] : "Hero Class"}</DropdownToggle>
          <DropdownMenu className={"overflow-scroll d-flex flex-column gap-1"} style={{ maxHeight: "25rem" }}>
            <DropdownItem as={Button} type={"toggle"} active={!heroClass} onSelect={() => setHeroClass("")}>
              All
            </DropdownItem>
            {Object.entries(classes).map(([classId, className]) => (
              <DropdownItem
                key={classId}
                as={Button}
                type={"toggle"}
                active={heroClass === classId}
                onSelect={() => setHeroClass(classId)}
              >
                {className}
              </DropdownItem>
            ))}
          </DropdownMenu>
        </Dropdown>
        <Dropdown>
          <DropdownToggle>Card Types</DropdownToggle>
          <DropdownMenu className={"d-flex flex-column gap-1"}>
            {ShowCardTypes.map((cardType) => (
              <DropdownItem
                key={cardType}
                as={Button}
                type={"toggle"}
                active={has(cardType)}
                onSelect={() => toggle(cardType)}
              >
                {cardType}
              </DropdownItem>
            ))}
          </DropdownMenu>
        </Dropdown>
      </div>
      {/*<div id={"Cards"} className={styles.collectionCards}>
        {cards.map((card, i) => (
          <div key={i} className={styles.collectionCard}>
            <CardDisplay {...card} />
          </div>
        ))}
      </div>*/}
      <Table striped={true} className={"border-top mt-2 ms-2"}>
        <thead>
          <tr>
            <th>Cost</th>
            <th>Name</th>
            <th>Class</th>
            <th>Type</th>
            <th>Stats</th>
            <th>Description</th>
          </tr>
        </thead>
        <tbody>
          {cards.map((card, i) => {
            const cardScript = card.cardScript as CardDef
            return (
              <tr key={i}>
                <td>{cardScript.baseManaCost ?? 0}</td>
                <td>{cardScript.name}</td>
                <td>{classes[cardScript.heroClass] ?? "Any"}</td>
                <td>{formatCurated(cardScript.type)}</td>
                <td>
                  {!cardScript.baseAttack && !cardScript.baseHp
                    ? "n/a"
                    : `${cardScript.baseAttack ?? 0}/${cardScript.baseHp ?? 0}`}
                </td>
                <td>{cardScript.description?.replaceAll(new RegExp("[$#\\[\\]]", "g"), "") ?? ""}</td>
              </tr>
            )
          })}
        </tbody>
      </Table>
    </div>
  )
}
