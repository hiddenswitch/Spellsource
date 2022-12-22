import React from 'react'
import Layout from '../components/creative-layout'
import HeroIntroSection from '../components/hero-intro-section'
import HeroLogoSection from '../components/hero-logo-section'
import HeroPlaySection from '../components/hero-play-section'

const Index = () => {
  return (
    <Layout>
      <HeroLogoSection />
      <HeroIntroSection />
      <HeroPlaySection/>
    </Layout>
  )
}

export default Index
