import React from 'react'
import Header from './header'
import styles from './creative-layout.module.scss'

export default ({ children }) => {
  return <div className={styles.container}>
    <Header />
    <main style={{width: children?._source?.fileName?.includes('card-editor') ? 900 : 700}}>
      {children}
    </main>
    <br/>
    {/*<footer><p>Copyright © 2020 Hidden Switch</p></footer>*/}
  </div>
}