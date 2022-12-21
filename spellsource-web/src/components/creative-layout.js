import React from 'react'
import Header from './header'
import Footer from './footer'

import * as styles from './creative-layout.module.scss'

export default ({ children }) => {
  return <div className={styles.container}>
    <Header />
    <main>
      {children}
    </main>
    <br/>
    <Footer/>
  </div>
}