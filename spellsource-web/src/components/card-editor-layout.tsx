import React from 'react'
import Header from './header'
import * as styles from './creative-layout.module.scss'
import {pages} from './creative-layout';


export default ({children}) => {
  return <div className={styles.container} style={{height: "100vh"}}>
    <Header pages={pages}/>
    {children}
  </div>
}
