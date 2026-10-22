import { Outlet } from 'react-router-dom'
import Navbar from './Navbar'
import Footer from './Footer'

export default function Layout() {
  return (
    <div className="relative flex min-h-screen flex-col">
      <Navbar />
      <main className="mx-auto w-full max-w-6xl flex-1 px-4 py-6 sm:px-6 sm:py-10">
        <Outlet />
      </main>
      <Footer />
    </div>
  )
}
