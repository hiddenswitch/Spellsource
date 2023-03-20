import React, {ReactNode, useEffect, useRef, useState} from 'react'
import { Link } from 'gatsby'
import { StaticImage } from 'gatsby-plugin-image'
import icon from '../assets/icon.png'
import styles from './creative-layout.module.scss'
import Search from './search'
import  {AiOutlineMenu} from "@react-icons/all-files/ai/AiOutlineMenu"
import {AiOutlineClose} from "@react-icons/all-files/ai/AiOutlineClose"



const Header = ({pages}: {pages?: any}) => {
  const headerDiv = useRef<HTMLDivElement>(null)

  const handleScroll = () => {
    sessionStorage.setItem('scrollPosition', headerDiv.current!.scrollLeft + "")
  }

  const keepHorizontalScroll = () => {
    if (sessionStorage.getItem('scrollPosition') !== null) {
      headerDiv.current!.scrollLeft = parseFloat(sessionStorage.getItem('scrollPosition')!)
    }
  }

  useEffect(() => {
    keepHorizontalScroll()
  }, [])

  return <header>
      <div className={styles.navbarContainer} ref={headerDiv} onScroll={handleScroll}>
        <a key={'headerImage'}>
          <Link to="/" style={{display: 'flex'}}><img src={icon} alt={'Icon'} style={{ width: 36, height: 36 }}/>
            <strong style={{color: '#000', paddingTop: '4px'}}>Spellsource</strong>
          </Link>
        </a>
        <DesktopNavbar pages={pages}/>
        <MobileNavbar pages={pages}/>
      </div>
  </header>
}

const DesktopNavbar = ({pages}: {pages?: ReactNode}) => {
  return (
    <ul className={styles.desktopNavbar}>
      <li key={'javadocs'}><a href="/javadoc">Docs</a></li>
        {pages}
      {/* <li key={'download'}><Link to="/download">Play Now</Link></li> */}
      <li key={'search'}><Search placeholder={'Search'}/></li>
    </ul>
  )
}

const MobileNavbar = ({pages}: {pages?: ReactNode}) => {
  const [open, setOpen] = useState(false);

  return(
    <div className={styles.mobileNavbar}>
      {!open ?
        <AiOutlineMenu color="#000" size={32} onClick={()=>{setOpen(!open);}} /> :
        <div>
          <AiOutlineClose color="#000" size={32} onClick={()=>{setOpen(!open);}}/>
          <ul className={styles.mobileUl}>
            <li key={'javadocs'}><a href="/javadoc">Docs</a></li>
              {pages}
            {/* <li key={'download'}><Link to="/download">Play Now</Link></li> */}
            <li key={'search'}><Search placeholder={'Search'}/></li>
          </ul>
        </div>}
  </div>
  )
}

export default Header
