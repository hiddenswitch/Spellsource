import React from 'react'
import Header from './header'
import '../styles/base.scss'
import '../styles/_dark.scss'

export default ({ children }) => {
  return <div className="container">
    <Header/>
    <main>
      {children}
    </main>
    <footer><p>Copyright © 2020 Hidden Switch</p></footer>
  </div>
}