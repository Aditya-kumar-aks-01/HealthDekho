
import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import {
  ArrowLeft,
  ShieldCheck,
  CalendarDays,
  IndianRupee,
  CheckCircle2,
  AlertCircle,
  LoaderCircle,
} from 'lucide-react'

import { registerPolicy } from '../api/policyApi'
import { getAvailableProducts } from '../api/dashboardApi'

const formatCurrency = (amount) => {
  if (amount === null || amount === undefined) return '—'

  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 0,
  }).format(Number(amount))
}

const formatTerm = (months) => {
  if (!months) return '—'

  const years = Math.floor(months / 12)
  const remaining = months % 12

  if (years && remaining) return `${years} years ${remaining} months`
  if (years) return `${years} ${years === 1 ? 'year' : 'years'}`

  return `${months} months`
}

function PolicyRegistration() {
  const location = useLocation()
  const navigate = useNavigate()

  const selectedProduct = location.state?.product || null

  const [products, setProducts] = useState([])
  const [product, setProduct] = useState(selectedProduct)
  const [startDate, setStartDate] = useState(
    new Date().toLocaleDateString('en-CA')
  )

  const [loadingProducts, setLoadingProducts] = useState(!selectedProduct)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [confirmation, setConfirmation] = useState(null)

  useEffect(() => {
    if (selectedProduct) return

    const loadProducts = async () => {
      try {
        const result = await getAvailableProducts()
        const available = Array.isArray(result) ? result : []

        setProducts(available)

        if (available.length === 1) {
          setProduct(available[0])
        }
      } catch (err) {
        setError(
          err.response?.data?.message ||
          'Unable to load available insurance products.'
        )
      } finally {
        setLoadingProducts(false)
      }
    }

    loadProducts()
  }, [selectedProduct])

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError('')

    if (!product) {
      setError('Please select an insurance product.')
      return
    }

    if (!startDate) {
      setError('Please select a policy start date.')
      return
    }

    setSubmitting(true)

    try {
      const result = await registerPolicy({
        productId: product.productId,
        startDate,
      })

      setConfirmation(result)
    } catch (err) {
      setError(
        err.response?.data?.message ||
        'Policy registration failed. Please try again.'
      )
    } finally {
      setSubmitting(false)
    }
  }

  if (confirmation) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-slate-50 px-4 py-10">
        <section className="w-full max-w-xl rounded-2xl border border-slate-200 bg-white p-7 text-center shadow-sm sm:p-10">
          <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-green-50 text-green-600">
            <CheckCircle2 size={36} />
          </div>

          <h1 className="mt-5 text-2xl font-bold text-slate-900">
            Policy registered successfully!
          </h1>

          <p className="mt-2 text-sm leading-6 text-slate-500">
            Your insurance policy has been registered with HealthDekho.
          </p>

          <div className="mt-7 rounded-xl bg-slate-50 p-5 text-left">
            <p className="text-xs font-semibold uppercase tracking-wide text-slate-400">
              Policy number
            </p>

            <p className="mt-1 text-xl font-bold text-blue-700">
              {confirmation.policyNumber}
            </p>

            <div className="mt-5 grid grid-cols-2 gap-4">
              <div>
                <p className="text-xs text-slate-500">Policy name</p>
                <p className="mt-1 font-medium text-slate-800">
                  {confirmation.policyName}
                </p>
              </div>

              <div>
                <p className="text-xs text-slate-500">Status</p>
                <p className="mt-1 font-medium text-green-700">
                  {confirmation.status}
                </p>
              </div>

              <div>
                <p className="text-xs text-slate-500">Total premium</p>
                <p className="mt-1 font-semibold text-slate-900">
                  {formatCurrency(confirmation.totalPremiumPayable)}
                </p>
              </div>

              <div>
                <p className="text-xs text-slate-500">Installments</p>
                <p className="mt-1 font-semibold text-slate-900">
                  {confirmation.totalInstallments}
                </p>
              </div>

              <div>
                <p className="text-xs text-slate-500">Start date</p>
                <p className="mt-1 font-medium text-slate-800">
                  {confirmation.startDate}
                </p>
              </div>

              <div>
                <p className="text-xs text-slate-500">Maturity date</p>
                <p className="mt-1 font-medium text-slate-800">
                  {confirmation.maturityDate}
                </p>
              </div>
            </div>
          </div>

          <button
            onClick={() => navigate('/dashboard', { replace: true })}
            className="mt-7 w-full rounded-lg bg-blue-700 px-5 py-3 text-sm font-semibold text-white transition hover:bg-blue-800"
          >
            Return to dashboard
          </button>
        </section>
      </main>
    )
  }

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-5xl items-center gap-3 px-5 py-4 sm:px-8">
          <ShieldCheck className="text-blue-700" size={25} />
          <span className="text-xl font-bold text-slate-900">
            HealthDekho
          </span>
        </div>
      </header>

      <main className="mx-auto max-w-3xl px-5 py-8 sm:px-8 sm:py-10">
        <Link
          to="/dashboard"
          className="inline-flex items-center gap-2 text-sm font-medium text-slate-500 transition hover:text-blue-700"
        >
          <ArrowLeft size={16} />
          Back to dashboard
        </Link>

        <div className="mt-7">
          <p className="text-sm font-semibold uppercase tracking-wider text-blue-700">
            Insurance application
          </p>

          <h1 className="mt-2 text-3xl font-bold text-slate-900">
            Register a policy
          </h1>

          <p className="mt-2 text-sm leading-6 text-slate-500">
            Review your selected insurance product and provide the policy
            start date to complete registration.
          </p>
        </div>

        <form onSubmit={handleSubmit} className="mt-8 space-y-6">
          <section className="rounded-xl border border-slate-200 bg-white p-6">
            <h2 className="text-lg font-semibold text-slate-900">
              Selected insurance product
            </h2>

            {loadingProducts ? (
              <div className="flex items-center gap-2 py-8 text-sm text-slate-500">
                <LoaderCircle size={18} className="animate-spin" />
                Loading available products...
              </div>
            ) : selectedProduct ? (
              <div className="mt-5 rounded-lg border border-blue-100 bg-blue-50/50 p-5">
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <p className="text-xs font-semibold uppercase tracking-wide text-blue-700">
                      {product?.productCode}
                    </p>

                    <h3 className="mt-1 text-xl font-bold text-slate-900">
                      {product?.policyName}
                    </h3>

                    <p className="mt-1 text-sm text-slate-500">
                      {product?.policyType}
                    </p>
                  </div>

                  <ShieldCheck className="shrink-0 text-blue-700" size={25} />
                </div>

                <div className="mt-5 grid grid-cols-2 gap-5">
                  <div>
                    <p className="text-xs text-slate-500">Sum insured</p>
                    <p className="mt-1 font-semibold text-slate-900">
                      {formatCurrency(product?.sumInsured)}
                    </p>
                  </div>

                  <div>
                    <p className="text-xs text-slate-500">Premium</p>
                    <p className="mt-1 font-semibold text-slate-900">
                      {formatCurrency(product?.premiumAmount)}
                    </p>
                  </div>

                  <div>
                    <p className="text-xs text-slate-500">Premium frequency</p>
                    <p className="mt-1 font-medium text-slate-800">
                      {product?.premiumFrequency}
                    </p>
                  </div>

                  <div>
                    <p className="text-xs text-slate-500">Policy term</p>
                    <p className="mt-1 font-medium text-slate-800">
                      {formatTerm(product?.termMonths)}
                    </p>
                  </div>

                  <div>
                    <p className="text-xs text-slate-500">Eligible entry age</p>
                    <p className="mt-1 font-medium text-slate-800">
                      {product?.minEntryAge}–{product?.maxEntryAge} years
                    </p>
                  </div>
                </div>
              </div>
            ) : (
              <div className="mt-5">
                <label
                  htmlFor="product"
                  className="mb-2 block text-sm font-medium text-slate-700"
                >
                  Choose an insurance product
                </label>

                <select
                  id="product"
                  value={product?.productId || ''}
                  onChange={(event) => {
                    const selected = products.find(
                      (item) => String(item.productId) === event.target.value
                    )
                    setProduct(selected || null)
                  }}
                  required
                  className="w-full rounded-lg border border-slate-300 bg-white px-4 py-3 text-sm outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-100"
                >
                  <option value="">Select a product</option>

                  {products.map((item) => (
                    <option key={item.productId} value={item.productId}>
                      {item.policyName} — {item.policyType}
                    </option>
                  ))}
                </select>

                {product && (
                  <div className="mt-4 rounded-lg bg-slate-50 p-4">
                    <p className="font-semibold text-slate-900">
                      {product.policyName}
                    </p>
                    <p className="mt-1 text-sm text-slate-500">
                      Coverage: {formatCurrency(product.sumInsured)}
                    </p>
                    <p className="mt-1 text-sm text-slate-500">
                      Premium: {formatCurrency(product.premiumAmount)} / {product.premiumFrequency}
                    </p>
                  </div>
                )}

                {!products.length && (
                  <p className="mt-3 text-sm text-slate-500">
                    No eligible products are currently available.
                  </p>
                )}
              </div>
            )}
          </section>

          <section className="rounded-xl border border-slate-200 bg-white p-6">
            <div className="flex items-center gap-2">
              <CalendarDays className="text-blue-700" size={20} />
              <h2 className="text-lg font-semibold text-slate-900">
                Policy start date
              </h2>
            </div>

            <p className="mt-2 text-sm text-slate-500">
              Select the date from which you want your policy to begin.
            </p>

            <label
              htmlFor="startDate"
              className="mb-2 mt-5 block text-sm font-medium text-slate-700"
            >
              Start date
            </label>

            <input
              id="startDate"
              type="date"
              value={startDate}
              min={new Date().toLocaleDateString('en-CA')}
              onChange={(event) => setStartDate(event.target.value)}
              required
              className="w-full rounded-lg border border-slate-300 px-4 py-3 text-sm outline-none focus:border-blue-600 focus:ring-2 focus:ring-blue-100 sm:max-w-sm"
            />
          </section>

          {error && (
            <div
              role="alert"
              className="flex items-start gap-3 rounded-lg border border-red-200 bg-red-50 p-4 text-sm text-red-700"
            >
              <AlertCircle size={19} className="mt-0.5 shrink-0" />
              {error}
            </div>
          )}

          <section className="rounded-xl border border-amber-200 bg-amber-50 p-4">
            <p className="text-sm leading-6 text-amber-900">
              Please review the product details and start date before
              submitting. Registration will create your policy and its
              installment schedule.
            </p>
          </section>

          <button
            type="submit"
            disabled={submitting || loadingProducts || !product}
            className="flex w-full items-center justify-center gap-2 rounded-lg bg-blue-700 px-5 py-3.5 text-sm font-semibold text-white transition hover:bg-blue-800 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {submitting ? (
              <>
                <LoaderCircle size={18} className="animate-spin" />
                Registering policy...
              </>
            ) : (
              <>
                <ShieldCheck size={18} />
                Confirm policy registration
              </>
            )}
          </button>
        </form>
      </main>
    </div>
  )
}

export default PolicyRegistration
