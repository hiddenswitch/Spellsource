import React from 'react'
import Layout from '../components/creative-layout'

import HeroIntroSection from '../components/hero-intro-section'
import HeroLogoSection from '../components/hero-logo-section'
import HeroPlaySection from '../components/hero-play-section'
import HeroGithub from '../components/hero-github'

const Index = () => {
  return (
    <Layout>
      <HeroLogoSection />
      <HeroIntroSection />
      <HeroPlaySection />
      <HeroGithub />
    </Layout>
  )
}

export default Index
