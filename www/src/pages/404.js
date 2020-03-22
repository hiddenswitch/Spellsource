import React from 'react'
import { Link } from 'gatsby'
import containerStyles from './404.module.css'

export default () => (<div className={containerStyles.container}>
    <h1>404</h1>

    <p><strong>Page not found</strong></p>
    <p>The requested page could not be found.</p>
    <p>
      <Link to={'/'}>Return to home.</Link>
    </p>
  </div>
)