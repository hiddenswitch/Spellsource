import React from 'react'
import Header from './header'
import * as styles from './creative-layout.module.scss'

export default ({ children }) => {
  return <div className={styles.container}>
    <Header />
    <main>
      {children}
    </main>
    <br/>
    {/*<footer><p>Copyright © 2020 Hidden Switch</p></footer>*/}
  </div>
}