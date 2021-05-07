import React from 'react'
import { Link } from 'gatsby'
import containerStyles from './404.module.css'

export default () => (<div className={containerStyles.container}>
    <h1>404</h1>

    <p>The requested page could not be found.</p>
    <p>If you reached this page by clicking on a link in the wiki, would you like to <Link to="/contribute">help us fill in</Link> the missing pages?</p>
    <p>
      <Link to={'/'}>Return to home.</Link>
    </p>
  </div>
)