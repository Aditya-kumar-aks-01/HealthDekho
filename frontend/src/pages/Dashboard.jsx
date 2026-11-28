
import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import {
  ShieldCheck,
  LogOut,
  UserRound,
  FileText,
  Compass,
  ArrowRight,
  CalendarDays,
  IndianRupee,
  RefreshCw,
  Sparkles,
} from 'lucide-react'

import { useAuth } from '../context/AuthContext'
import {
  getMyPolicies,
  getAvailableProducts,
} from '../api/dashboardApi'

const formatCurrency = (amount) => {
  if (amount === null || amount === undefined) return '—'

  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 0,
  }).format(Number(amount))
}

const formatDate = (date) => {
  if (!date) return '—'

  return new Date(`${date}T00:00:00`).toLocaleDateString('en-IN', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  })
}

const formatTerm = (months) => {
  if (!months) return '—'

  const years = Math.floor(months / 12)
  const remainingMonths = months % 12

  if (years && remainingMonths) {
    return `${years} yr ${remainingMonths} mo`
  }

  if (years) return `${years} ${years === 1 ? 'year' : 'years'}`

  return `${months} months`
}

function StatCard({ icon: Icon, label, value, description }) {
  return (
    <div className="rounded-xl border border-slate-200 bg-white p-5">
      <div className="flex items-center justify-between">
        <p className="text-sm font-medium text-slate-500">{label}</p>
        <div className="rounded-lg bg-blue-50 p-2 text-blue-700">
          <Icon size={19} />
        </div>
      </div>

      <p className="mt-4 text-3xl font-semibold text-slate-900">
        {value}
      </p>

      <p className="mt-1 text-xs text-slate-500">{description}</p>
    </div>
  )
}

function EmptyState({ title, description }) {
  return (
    <div className="rounded-xl border border-dashed border-slate-300 bg-slate-50 px-6 py-10 text-center">
      <FileText className="mx-auto text-slate-400" size={30} />

      <h3 className="mt-3 font-semibold text-slate-800">{title}</h3>

      <p className="mx-auto mt-1 max-w-sm text-sm text-slate-500">
        {description}
      </p>
    </div>
  )
}

function Dashboard() {
  const { customer, logout } = useAuth()
  const navigate = useNavigate()

  const [policies, setPolicies] = useState([])
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const loadDashboard = useCallback(async () => {
    setLoading(true)
    setError('')

    try {
      const [policyResult, productResult] = await Promise.all([
        getMyPolicies(),
        getAvailableProducts(),
      ])

      setPolicies(Array.isArray(policyResult) ? policyResult : [])
      setProducts(Array.isArray(productResult) ? productResult : [])
    } catch (err) {
      setError(
        err.response?.data?.message ||
        'Unable to load your dashboard. Please try again.'
      )
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadDashboard()
  }, [loadDashboard])

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  const firstName = customer?.fullName?.trim()?.split(/\s+/)[0] || 'Customer'

  return (
    <div className="min-h-screen bg-slate-50">
      {/* Navigation */}
      <header className="sticky top-0 z-20 border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-5 py-4 sm:px-8">
          <Link to="/dashboard" className="flex items-center gap-2">
            <div className="rounded-lg bg-blue-700 p-2 text-white">
              <ShieldCheck size={22} />
            </div>

            <span className="text-xl font-bold tracking-tight text-slate-900">
              HealthDekho
            </span>
          </Link>

          <div className="flex items-center gap-3">
            <div className="hidden text-right sm:block">
              <p className="text-sm font-semibold text-slate-800">
                {customer?.fullName}
              </p>
              <p className="text-xs text-slate-500">
                Customer account
              </p>
            </div>

            <button
              onClick={handleLogout}
              className="flex items-center gap-2 rounded-lg border border-slate-200 px-3 py-2 text-sm font-medium text-slate-600 transition hover:bg-slate-100"
            >
              <LogOut size={16} />
              <span className="hidden sm:inline">Sign out</span>
            </button>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-7xl px-5 py-8 sm:px-8 sm:py-10">
        {/* Welcome */}
        <section className="flex flex-col justify-between gap-5 sm:flex-row sm:items-end">
          <div>
            <p className="text-sm font-semibold uppercase tracking-wider text-blue-700">
              Customer dashboard
            </p>

            <h1 className="mt-2 text-3xl font-bold tracking-tight text-slate-900">
              Welcome back, {firstName}!
            </h1>

            <p className="mt-2 text-slate-500">
              Here's an overview of your insurance with HealthDekho.
            </p>
          </div>

          <button
            onClick={loadDashboard}
            disabled={loading}
            className="inline-flex w-fit items-center gap-2 rounded-lg border border-slate-200 bg-white px-4 py-2.5 text-sm font-medium text-slate-700 transition hover:bg-slate-100 disabled:opacity-50"
          >
            <RefreshCw size={16} className={loading ? 'animate-spin' : ''} />
            Refresh
          </button>
        </section>

        {/* Statistics */}
        <section className="mt-8 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <StatCard
            icon={ShieldCheck}
            label="My policies"
            value={loading ? '—' : policies.length}
            description="Insurance policies registered"
          />

          <StatCard
            icon={Compass}
            label="Available products"
            value={loading ? '—' : products.length}
            description="Products you can explore"
          />

          <StatCard
            icon={UserRound}
            label="Account"
            value="Active"
            description="Your HealthDekho account"
          />
        </section>

        {/* Error */}
        {error && (
          <div
            role="alert"
            className="mt-6 flex flex-col justify-between gap-3 rounded-xl border border-red-200 bg-red-50 p-4 sm:flex-row sm:items-center"
          >
            <p className="text-sm text-red-700">{error}</p>

            <button
              onClick={loadDashboard}
              className="text-sm font-semibold text-red-700 hover:underline"
            >
              Try again
            </button>
          </div>
        )}

        {/* Customer information */}
        <section className="mt-8 rounded-xl border border-slate-200 bg-white p-6">
          <div className="flex items-center gap-2">
            <UserRound className="text-blue-700" size={20} />
            <h2 className="text-lg font-semibold text-slate-900">
              Personal information
            </h2>
          </div>

          <div className="mt-5 grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
            <div>
              <p className="text-xs font-medium uppercase tracking-wide text-slate-400">
                Full name
              </p>
              <p className="mt-1 font-medium text-slate-800">
                {customer?.fullName || '—'}
              </p>
            </div>

            <div>
              <p className="text-xs font-medium uppercase tracking-wide text-slate-400">
                Email address
              </p>
              <p className="mt-1 break-all font-medium text-slate-800">
                {customer?.email || '—'}
              </p>
            </div>

            <div>
              <p className="text-xs font-medium uppercase tracking-wide text-slate-400">
                Contact number
              </p>
              <p className="mt-1 font-medium text-slate-800">
                {customer?.phone || '—'}
              </p>
            </div>

            <div>
              <p className="text-xs font-medium uppercase tracking-wide text-slate-400">
                Date of birth
              </p>
              <p className="mt-1 font-medium text-slate-800">
                {formatDate(customer?.dateOfBirth)}
              </p>
            </div>
          </div>
        </section>

        {/* Registered policies */}
        <section className="mt-10">
          <div className="mb-5 flex items-center justify-between gap-3">
            <div>
              <h2 className="text-xl font-bold text-slate-900">
                My insurance policies
              </h2>
              <p className="mt-1 text-sm text-slate-500">
                Your currently registered insurance policies.
              </p>
            </div>
          </div>

          {loading ? (
            <div className="rounded-xl border border-slate-200 bg-white p-8 text-center text-sm text-slate-500">
              Loading your policies...
            </div>
          ) : policies.length === 0 ? (
            <EmptyState
              title="No policies registered yet"
              description="Explore the available insurance products below to find coverage that suits your needs."
            />
          ) : (
            <div className="grid gap-4 lg:grid-cols-2">
              {policies.map((policy) => (
                <article
                  key={policy.policyNumber}
                  className="rounded-xl border border-slate-200 bg-white p-5 transition hover:border-blue-200"
                >
                  <div className="flex flex-wrap items-start justify-between gap-3">
                    <div>
                      <p className="text-xs font-semibold uppercase tracking-wide text-blue-700">
                        {policy.policyType || 'Insurance'}
                      </p>

                      <h3 className="mt-1 text-lg font-semibold text-slate-900">
                        {policy.policyName}
                      </h3>

                      <p className="mt-1 text-xs text-slate-500">
                        {policy.policyNumber}
                      </p>
                    </div>

                    <span className="rounded-full bg-green-50 px-3 py-1 text-xs font-semibold text-green-700">
                      {policy.status || 'Registered'}
                    </span>
                  </div>

                  <div className="mt-5 grid grid-cols-2 gap-4 border-t border-slate-100 pt-4">
                    <div>
                      <p className="text-xs text-slate-500">Sum insured</p>
                      <p className="mt-1 font-semibold text-slate-900">
                        {formatCurrency(policy.sumInsured)}
                      </p>
                    </div>

                    <div>
                      <p className="text-xs text-slate-500">
                        Premium ({policy.premiumFrequency || '—'})
                      </p>
                      <p className="mt-1 font-semibold text-slate-900">
                        {formatCurrency(policy.premiumAmount)}
                      </p>
                    </div>

                    <div>
                      <p className="text-xs text-slate-500">Policy term</p>
                      <p className="mt-1 font-medium text-slate-800">
                        {formatTerm(policy.termMonths)}
                      </p>
                    </div>

                    <div>
                      <p className="text-xs text-slate-500">Maturity date</p>
                      <p className="mt-1 font-medium text-slate-800">
                        {formatDate(policy.maturityDate)}
                      </p>
                    </div>
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>

        {/* Available products */}
        <section className="mt-10">
          <div className="mb-5">
            <h2 className="text-xl font-bold text-slate-900">
              Explore insurance products
            </h2>
            <p className="mt-1 text-sm text-slate-500">
              Browse active products that you haven't registered yet.
            </p>
          </div>

          {loading ? (
            <div className="rounded-xl border border-slate-200 bg-white p-8 text-center text-sm text-slate-500">
              Loading available products...
            </div>
          ) : products.length === 0 ? (
            <EmptyState
              title="No additional products available"
              description="There are currently no other eligible products available for your account."
            />
          ) : (
            <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
              {products.map((product) => (
                <article
                  key={product.productId}
                  className="flex flex-col rounded-xl border border-slate-200 bg-white p-5 transition hover:-translate-y-0.5 hover:border-blue-200 hover:shadow-sm"
                >
                  <div className="flex items-start justify-between gap-3">
                    <div className="rounded-lg bg-blue-50 p-2.5 text-blue-700">
                      <ShieldCheck size={22} />
                    </div>

                    <span className="rounded-full bg-slate-100 px-2.5 py-1 text-xs font-medium text-slate-600">
                      {product.policyType}
                    </span>
                  </div>

                  <p className="mt-4 text-xs font-medium text-slate-400">
                    {product.productCode}
                  </p>

                  <h3 className="mt-1 text-lg font-semibold text-slate-900">
                    {product.policyName}
                  </h3>

                  <div className="mt-5 space-y-3">
                    <div className="flex items-center justify-between text-sm">
                      <span className="text-slate-500">Sum insured</span>
                      <span className="font-semibold text-slate-900">
                        {formatCurrency(product.sumInsured)}
                      </span>
                    </div>

                    <div className="flex items-center justify-between text-sm">
                      <span className="text-slate-500">Premium</span>
                      <span className="font-semibold text-slate-900">
                        {formatCurrency(product.premiumAmount)}
                      </span>
                    </div>

                    <div className="flex items-center justify-between text-sm">
                      <span className="text-slate-500">Frequency</span>
                      <span className="font-medium text-slate-800">
                        {product.premiumFrequency}
                      </span>
                    </div>

                    <div className="flex items-center justify-between text-sm">
                      <span className="text-slate-500">Policy term</span>
                      <span className="font-medium text-slate-800">
                        {formatTerm(product.termMonths)}
                      </span>
                    </div>

                    <div className="flex items-center justify-between text-sm">
                      <span className="text-slate-500">Entry age</span>
                      <span className="font-medium text-slate-800">
                        {product.minEntryAge}–{product.maxEntryAge} years
                      </span>
                    </div>
                  </div>

                  <Link
                    to="/policies/register"
                    state={{ product }}
                    className="mt-6 flex items-center justify-center gap-2 rounded-lg bg-blue-700 px-4 py-3 text-sm font-semibold text-white transition hover:bg-blue-800"
                  >
                    Register policy
                    <ArrowRight size={16} />
                  </Link>
                </article>
              ))}
            </div>
          )}
        </section>

        {/* AI recommendations - next implementation stage */}
        <section className="mt-10 overflow-hidden rounded-xl border border-blue-100 bg-blue-50 p-6 sm:p-8">
          <div className="flex flex-col justify-between gap-5 sm:flex-row sm:items-center">
            <div className="flex gap-4">
              <div className="h-fit rounded-xl bg-white p-3 text-blue-700">
                <Sparkles size={24} />
              </div>

              <div>
                <h2 className="text-lg font-bold text-slate-900">
                  Looking for the right insurance?
                </h2>

                <p className="mt-1 max-w-xl text-sm leading-6 text-slate-600">
                  HealthDekho's AI recommendation feature will help identify
                  products based on your goals and preferences.
                </p>
              </div>
            </div>

          <Link
  to="/recommendations"
  className="rounded-full bg-white px-5 py-3 text-sm font-semibold text-blue-700 shadow-sm transition hover:bg-blue-50"
>
  Get recommendations
</Link>
          </div>
        </section>

        <footer className="mt-12 border-t border-slate-200 py-6 text-center text-xs text-slate-400">
          HealthDekho · Your insurance, simplified.
        </footer>
      </main>
    </div>
  )
}

export default Dashboard
