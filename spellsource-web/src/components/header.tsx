import React, { ReactNode, useEffect, useRef, useState } from "react";
import icon from "../../public/static/assets/icon.png";
import * as styles from "./creative-layout.module.scss";
import { AiOutlineMenu } from "@react-icons/all-files/ai/AiOutlineMenu";
import { AiOutlineClose } from "@react-icons/all-files/ai/AiOutlineClose";
import Link from "next/link";
import Image from "next/image";
import { Container } from "react-bootstrap";

const Header = ({ pages }: { pages?: any }) => {
  const headerDiv = useRef<HTMLDivElement>(null);

  const handleScroll = () => {
    sessionStorage.setItem("scrollPosition", headerDiv.current!.scrollLeft + "");
  };

  const keepHorizontalScroll = () => {
    if (sessionStorage.getItem("scrollPosition") !== null) {
      headerDiv.current!.scrollLeft = parseFloat(sessionStorage.getItem("scrollPosition")!);
    }
  };

  useEffect(() => {
    keepHorizontalScroll();
  }, []);

  return (
    <header className={styles.navbarHeader}>
      <Container className={styles.navbarContainer} ref={headerDiv} onScroll={handleScroll}>
        <Link key={"headerImage"} href="/" style={{ display: "flex", alignItems: "center" }}>
          <Image src={icon} alt={"Icon"} style={{ width: 36, height: 36 }} />
          <strong style={{ color: "#000" }}>Spellsource</strong>
        </Link>
        <DesktopNavbar pages={pages} />
        <MobileNavbar pages={pages} />
      </Container>
    </header>
  );
};

const DesktopNavbar = ({ pages }: { pages?: ReactNode }) => {
  return (
    <ul className={styles.desktopNavbar}>
      {/*<li key={'search'}><Search placeholder={'Search'}/></li>*/}
      {/*<li key={"javadocs"}>
        <a href="/javadoc">Docs</a>
      </li>*/}
      {pages}
      {/* <li key={'download'}><Link href="/download">Play Now</Link></li> */}
    </ul>
  );
};

const MobileNavbar = ({ pages }: { pages?: ReactNode }) => {
  const [open, setOpen] = useState(false);

  return (
    <div className={styles.mobileNavbar}>
      {!open ? (
        <AiOutlineMenu
          color="#000"
          size={32}
          onClick={() => {
            setOpen(!open);
          }}
        />
      ) : (
        <div>
          <AiOutlineMenu
            color="#000"
            size={32}
            onClick={() => {
              setOpen(!open);
            }}
          />
          <ul className={styles.mobileUl}>
            {/*<li key={'search'}><Search placeholder={'Search'}/></li>*/}
            <li key={"javadocs"}>
              <a href="/javadoc">Docs</a>
            </li>
            {pages}
            {/* <li key={'download'}><Link href="/download">Play Now</Link></li> */}
          </ul>
        </div>
      )}
    </div>
  );
};

export default Header;
