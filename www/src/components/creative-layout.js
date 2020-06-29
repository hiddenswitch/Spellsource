import React from 'react'
import Header from './header'
import styles from './creative-layout.module.scss'

export default ({ children }) => {
  return <div className={styles.container}>
    <Header/>
    <main>
      <section>
        {children}
      </section>
    </main>
    <footer><p>Copyright © 2020 Hidden Switch</p></footer>
  </div>
}