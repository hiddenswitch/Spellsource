import Link from 'next/link'
import React from 'react'

export default () => (<div>
    <h1>404</h1>

    <p>The requested page could not be found.</p>
    <p>If you reached this page by clicking on a link in the wiki, would you like to <Link href="/contribute">help us fill in</Link> the missing pages?</p>
    <p>
      <Link href={'/'}>Return to home.</Link>
    </p>
  </div>
)