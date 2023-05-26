import { useSession } from "next-auth/react"
import React, { FunctionComponent, useEffect, useState } from "react"
import { CardType } from "../__generated__/spellsource-game"
import { CollectionCardsOrderBy, useGetCardsQuery } from "../__generated__/client"
import { CardDef } from "./card-display"
import { Button, Dropdown, Form, Table } from "react-bootstrap"
import { formatCurated } from "../lib/blockly-misc-utils"
import { clamp } from "lodash"
import DropdownToggle from "react-bootstrap/DropdownToggle"
import DropdownItem from "react-bootstrap/DropdownItem"
import DropdownMenu from "react-bootstrap/DropdownMenu"
import { useParam, useParamArray, useParamBool, useParamInt } from "../lib/routing"
import { useRouter } from "next/router"
import { useDebounce } from "react-use"
import Link from "next/link"

const ShowCardTypes: CardType[] = ["MINION", "SPELL", "WEAPON", "HERO", "HERO_POWER", "CLASS"]
const DefaultShowCardTypes: CardType[] = ["MINION", "SPELL", "WEAPON"]
const defaultLimit = 25
const limitOptions = [25, 50, 100]

const orderings = {
  COST_ASC: "Lowest Cost",
  COST_DESC: "Highest Cost",
  NAME_ASC: "Name A-Z",
  NAME_DESC: "Name Z-A",
  LAST_MODIFIED_DESC: "Recently Changed",
  TYPE_ASC: "Type",
} as Record<CollectionCardsOrderBy, string>

interface CollectionProps {
  classes: Record<string, string>
  classColors: Record<string, string>
  heroClass: string
  setHeroClass: (heroClass: string) => void
  offset: number
  setOffset: (offset: number) => void
  mainHeroClass?: string
  addToDeck?: (card: CardDef) => void
}

export const textDecorationStyle = (heroClass: string, classColors: Record<string, string>) => ({
  textDecorationLine: "underline",
  textDecorationColor: heroClass in classColors ? classColors[heroClass] : "rgba(#888888)",
})

const Collection: FunctionComponent<CollectionProps> = ({
  classes,
  classColors,
  heroClass,
  setHeroClass,
  offset,
  setOffset,
  mainHeroClass,
  addToDeck,
}) => {
  const { data: session, status } = useSession()

  const router = useRouter()
  const user = session?.token?.sub

  const [search, setSearch] = useParam(router, "search")
  const [cardTypes, setCardTypes] = useParamArray<CardType>(router, "cardType", DefaultShowCardTypes)
  const [orderBy, setOrderBy] = useParam<CollectionCardsOrderBy>(router, "orderBy", "COST_ASC")
  const [uncollectible, setUncollectible] = useParamBool(router, "uncollectible", false)
  const [ownOnly, setOwnOnly] = useParamBool(router, "ownOnly", false)
  const [limit, setLimit] = useParamInt(router, "limit", defaultLimit)

  const [searchVisual, setSearchVisual] = useState(search)

  const [_, cancelDebounce] = useDebounce(() => setSearch(searchVisual), 500, [searchVisual])

  const getCards = useGetCardsQuery({
    variables: {
      limit,
      offset,
      orderBy: orderBy ? [orderBy as CollectionCardsOrderBy, "COST_ASC", "NAME_ASC"] : ["COST_ASC", "NAME_ASC"],
      filter: {
        type: { in: [...cardTypes] },
        class: heroClass ? (mainHeroClass ? { in: [mainHeroClass, "ANY"] } : { equalTo: heroClass }) : undefined,
        searchMessage: search ? { includesInsensitive: search.trim() } : undefined,
        collectible: { equalTo: !uncollectible },
        createdBy: ownOnly ? { equalTo: user ?? "" } : undefined,
      },
    },
  })
  const cards = getCards?.data?.allCollectionCards?.nodes ?? getCards?.previousData?.allCollectionCards?.nodes ?? []
  const total =
    getCards?.data?.allCollectionCards?.totalCount ?? getCards?.previousData?.allCollectionCards?.totalCount ?? 0

  const showing = Math.min(limit, cards.length)

  const changeOffset = (delta: number = 0) => {
    if (getCards.data) {
      const newOffset = clamp(offset + delta, 0, total - limit)
      setOffset(newOffset)
    }
  }

  useEffect(changeOffset, [getCards.data])

  return (
    <div>
      <div id={"Top Bar"} className={"d-flex flex-row flex-wrap gap-2 pt-2 ps-2 align-items-center"}>
        <Form
          onSubmit={(event) => {
            event.preventDefault()
            cancelDebounce()
            setSearch(searchVisual)
          }}
          className={"me-lg-auto"}
        >
          <Form.Control
            placeholder={"Search"}
            value={searchVisual}
            onChange={(event) => setSearchVisual(event.target.value)}
          />
        </Form>
        <Dropdown>
          <DropdownToggle variant={"light"} disabled={!getCards.data}>
            Per Page
          </DropdownToggle>
          <DropdownMenu className={"d-flex flex-column gap-1"}>
            {limitOptions.map((value, i) => (
              <DropdownItem key={i} as={Button} active={limit === value} onSelect={() => setLimit(value)}>
                {value}
              </DropdownItem>
            ))}
          </DropdownMenu>
        </Dropdown>
        <Button disabled={!getCards.data || offset <= 0} variant={"secondary"} onClick={() => changeOffset(-limit)}>
          Prev
        </Button>
        <div className={"mb-1 text-center user-select-none"}>
          {cards?.length ? offset + 1 : "0"}-{cards?.length ? offset + showing : "0"} of {cards?.length ? total : "0"}
        </div>
        <Button
          disabled={!getCards.data || offset >= total - limit}
          variant={"secondary"}
          onClick={() => changeOffset(limit)}
        >
          Next
        </Button>
        <div className={"w-100 d-none d-lg-block"} />
        <Dropdown>
          <DropdownToggle style={{ minWidth: 100 }} disabled={!getCards.data}>
            {heroClass ? classes[heroClass] || formatCurated(heroClass) : "Hero Class"}
          </DropdownToggle>
          <DropdownMenu className={"overflow-scroll d-flex flex-column gap-1"} style={{ maxHeight: "25rem" }}>
            {mainHeroClass && (
              <DropdownItem
                key={"Allowed"}
                as={Button}
                active={heroClass === "ALLOWED"}
                onSelect={() => setHeroClass("ALLOWED")}
              >
                Allowed
              </DropdownItem>
            )}
            <DropdownItem as={Button} type={"toggle"} active={!heroClass} onSelect={() => setHeroClass("")}>
              All
            </DropdownItem>
            <DropdownItem as={Button} type={"toggle"} active={heroClass === "ANY"} onSelect={() => setHeroClass("ANY")}>
              Any
            </DropdownItem>
            {Object.entries(classes).map(([classId, className]) => (
              <DropdownItem
                key={classId}
                as={Button}
                active={heroClass === classId}
                onSelect={() => setHeroClass(classId)}
              >
                {className}
              </DropdownItem>
            ))}
          </DropdownMenu>
        </Dropdown>
        <Dropdown>
          <DropdownToggle disabled={!getCards.data}>Card Types</DropdownToggle>
          <DropdownMenu className={"d-flex flex-column gap-1"}>
            <DropdownItem
              key={"All"}
              as={Button}
              active={!ShowCardTypes.find((type) => !cardTypes.includes(type))}
              onSelect={() =>
                setCardTypes(
                  ShowCardTypes.find((type) => !cardTypes.includes(type)) ? ShowCardTypes : DefaultShowCardTypes
                )
              }
            >
              All
            </DropdownItem>
            {ShowCardTypes.map((cardType) => (
              <DropdownItem
                key={cardType}
                as={Button}
                active={cardTypes.includes(cardType)}
                onSelect={() =>
                  setCardTypes(
                    cardTypes.includes(cardType)
                      ? cardTypes.filter((type) => type !== cardType)
                      : [...cardTypes, cardType]
                  )
                }
              >
                {formatCurated(cardType)}
              </DropdownItem>
            ))}
          </DropdownMenu>
        </Dropdown>
        <Dropdown>
          <DropdownToggle disabled={!getCards.data}>Order By</DropdownToggle>
          <DropdownMenu className={"d-flex flex-column gap-1"}>
            {Object.entries(orderings).map(([order, name]) => (
              <DropdownItem
                key={order}
                as={Button}
                active={orderBy === order}
                onSelect={() => setOrderBy(order as CollectionCardsOrderBy)}
              >
                {name}
              </DropdownItem>
            ))}
          </DropdownMenu>
        </Dropdown>
        <Button
          disabled={!getCards.data}
          variant={"light"}
          active={!!uncollectible}
          onClick={() => setUncollectible(!uncollectible)}
        >
          {uncollectible ? "Uncollectible" : "Collectible"}
        </Button>
        <Button disabled={!getCards.data} variant={"light"} active={!!ownOnly} onClick={() => setOwnOnly(!ownOnly)}>
          {ownOnly ? "Your Cards" : "Public Cards"}
        </Button>
      </div>
      <div className={"w-100 overflow-scroll mt-2"}>
        <Table striped={true} className={`border-top table-responsive ${addToDeck && "table-hover"}`}>
          <thead>
            <tr>
              <th>Cost</th>
              <th>Name</th>
              <th>Class</th>
              <th>Type</th>
              <th>Stats</th>
              <th>Description</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {cards.map((card) => {
              const cardScript = card.cardScript as CardDef
              return (
                <tr
                  key={card.id}
                  onClick={() => addToDeck?.(cardScript)}
                  style={{ cursor: addToDeck ? "pointer" : "initial" }}
                >
                  <td>{cardScript.baseManaCost ?? 0}</td>
                  <td>{cardScript.name}</td>
                  <td
                    onClick={() => setHeroClass(cardScript.heroClass)}
                    style={{
                      cursor: "pointer",
                      ...textDecorationStyle(cardScript.heroClass, classColors)
                    }}
                  >
                    {classes[cardScript.heroClass] ?? "Any"}
                  </td>
                  <td>{formatCurated(cardScript.type)}</td>
                  <td>
                    {!cardScript.baseAttack && !cardScript.baseHp
                      ? "n/a"
                      : `${cardScript.baseAttack ?? 0}/${cardScript.baseHp ?? 0}`}
                  </td>
                  <td>{cardScript.description?.replaceAll(new RegExp("[$#\\[\\]]", "g"), "") ?? ""}</td>
                  <td>
                    {card.blocklyWorkspace ? (
                      <Link href={`/card-editor?card=${encodeURIComponent(card.id)}`} target={"_blank"}>
                        Edit
                      </Link>
                    ) : (
                      <></>
                    )}
                  </td>
                </tr>
              )
            })}
          </tbody>
        </Table>
      </div>
    </div>
  )
}

export default Collection
