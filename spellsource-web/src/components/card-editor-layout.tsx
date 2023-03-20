import React, {ReactNode} from 'react'
import Header from './header'
import * as styles from './creative-layout.module.scss'

export default ({ children }: {children?: ReactNode}) => {
  return <div className={styles.container}>
    <Header />
    <main style={{width: 900}}>
      {children}
    </main>
    <br/>
    {/*<footer><p>Copyright © 2020 Hidden Switch</p></footer>*/}
  </div>
}