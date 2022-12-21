import React, { useEffect, useRef } from 'react'
import { Link } from 'gatsby'
import { StaticImage } from 'gatsby-plugin-image'
import * as styles from './creative-layout.module.scss'
import Search from './search'

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
    <div className={styles.menuContainer} ref={headerDiv} onScroll={e => {handleScroll(e)}}>
      <a key={'headerImage'}>
        <Link to="/"><StaticImage src={'../assets/icon.png'} alt={'Icon'} style={{ width: 32, height: 32 }}/>
          <strong>Spellsource</strong>
        </Link>
        </a>
      <ul>       
        <li key={'javadocs'}><a href="/javadoc">Docs</a></li>
        {pages}
        <li key={'download'}><Link to="/download">Play Now</Link></li>
        {/* <li key={'search'}><Search placeholder={'Search'}/></li> */}
      </ul>
    </div>
  </header>
}

export default Header
