import React, { useEffect, useRef, useState } from 'react'
import { Link } from 'gatsby'
import { StaticImage } from 'gatsby-plugin-image'
import * as styles from './creative-layout.module.scss'
import Search from './search'
import  {AiOutlineMenu} from "@react-icons/all-files/ai/AiOutlineMenu"
import {AiOutlineClose} from "@react-icons/all-files/ai/AiOutlineClose"



const Header = ({pages}) => {
  const headerDiv = useRef(null)

  const handleScroll = event => {
    sessionStorage.setItem('scrollPosition', headerDiv.current.scrollLeft)
  }

  const keepHorizontalScroll = () => {
    if (sessionStorage.getItem('scrollPosition') !== null) {
      headerDiv.current.scrollLeft = sessionStorage.getItem('scrollPosition')
    }
  }

  useEffect(() => {
    keepHorizontalScroll()
  }, [])

  return <header>
      <div className={styles.navbarContainer} ref={headerDiv} onScroll={e => {handleScroll(e)}}>
        <a key={'headerImage'}>
          <Link to="/"><StaticImage src={'../assets/icon.png'} alt={'Icon'} style={{ width: 36, height: 36 }}/>
            <strong>Spellsource</strong>
          </Link>
        </a>
        <DesktopNavbar pages={pages}/>
        <MobileNavbar pages={pages}/>
      </div>
  </header>
}

const DesktopNavbar = ({pages}) => {
  return (
    <ul className={styles.desktopNavbar}>       
      <li key={'javadocs'}><a href="/javadoc">Docs</a></li>
        {pages}
      {/* <li key={'download'}><Link to="/download">Play Now</Link></li> */}
      <li key={'search'}><Search placeholder={'Search'}/></li>
    </ul>
  )
}

const MobileNavbar = ({pages}) => {
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
