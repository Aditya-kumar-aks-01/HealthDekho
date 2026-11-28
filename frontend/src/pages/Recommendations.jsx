
import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import {
  ArrowLeft,
  Sparkles,
  ShieldCheck,
  Loader2,
  RefreshCw,
  FileText,
} from 'lucide-react'
import { generateRecommendations } from '../api/recommendationApi'

const formatCurrency = (value) => {
  if (value === null || value === undefined) return '—'

  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 2,
  }).format(Number(value))
}

const formatTerm = (months) => {
  if (!months) return '—'

  const years = Math.floor(months / 12)
  const remainingMonths = months % 12

  if (years && remainingMonths) {
    return `${years} years ${remainingMonths} months`
  }

  if (years) return `${years} years`
  return `${months} months`
}

const suitabilityStyles = {
  HIGH: 'bg-green-100 text-green-700',
  MEDIUM: 'bg-yellow-100 text-yellow-700',
  LOW: 'bg-red-100 text-red-700',
}

function Recommendations() {
  const navigate = useNavigate()

  const [insuranceGoals, setInsuranceGoals] = useState('')
  const [preferredTerm, setPreferredTerm] = useState('No preference')
  const [preferredPolicyType, setPreferredPolicyType] = useState('')

  const [recommendations, setRecommendations] = useState(null)
  const [calculatedAge, setCalculatedAge] = useState(null)
  const [requestedAt, setRequestedAt] = useState(null)

  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = async (event) => {
    event.preventDefault()

    if (!insuranceGoals.trim()) {
      setError('Please describe your insurance goals.')
      return
    }

    setLoading(true)
    setError('')
    setRecommendations(null)

    try {
      const response = await generateRecommendations({
        insuranceGoals: insuranceGoals.trim(),
        preferredTerm: preferredTerm.trim() || 'No preference',
        preferredPolicyType: preferredPolicyType.trim() || null,
      })

      setRecommendations(response.recommendations ?? [])
      setCalculatedAge(response.calculatedAge)
      setRequestedAt(response.requestedAt)
    } catch (err) {
      setError(
        err.response?.data?.message ||
        err.response?.data?.error ||
        'Unable to generate recommendations. Please try again.'
      )
    } finally {
      setLoading(false)
    }
  }

  const handleRegister = (product) => {
    navigate('/policies/register', {
      state: {
        product: {
          productId: product.productId,
          productCode: product.productCode,
          policyName: product.policyName,
          policyType: product.policyType,
          termMonths: product.termMonths,
          sumInsured: product.sumInsured,
          premiumFrequency: product.premiumFrequency,
          premiumAmount: product.premiumAmount,
        },
      },
    })
  }

  return (
    <div className="min-h-screen bg-slate-50 px-4 py-8 sm:px-6 lg:px-10">
      <div className="mx-auto max-w-7xl">

        <Link
          to="/dashboard"
          className="mb-6 inline-flex items-center gap-2 text-sm font-medium text-slate-600 hover:text-blue-700"
        >
          <ArrowLeft size={18} />
          Back to dashboard
        </Link>

        <div className="mb-8 rounded-2xl bg-gradient-to-r from-blue-700 to-indigo-700 p-7 text-white shadow-sm">
          <div className="flex items-start gap-4">
            <div className="rounded-xl bg-white/15 p-3">
              <Sparkles size={28} />
            </div>

            <div>
              <p className="mb-2 text-sm font-medium text-blue-100">
                HEALTHDEKHO AI
              </p>

              <h1 className="text-2xl font-bold sm:text-3xl">
                Find insurance that fits your life
              </h1>

              <p className="mt-2 max-w-2xl text-sm leading-6 text-blue-100">
                Tell us what you are looking for. Our AI will analyse
                your preferences and recommend suitable products from
                our available insurance catalogue.
              </p>
            </div>
          </div>
        </div>

        <div className="grid gap-8 lg:grid-cols-[380px_1fr]">

          {/* Recommendation form */}
          <section className="h-fit rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <div className="mb-6">
              <h2 className="text-lg font-bold text-slate-900">
                Your preferences
              </h2>

              <p className="mt-1 text-sm text-slate-500">
                Help us understand your insurance needs.
              </p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-5">

              <div>
                <label className="mb-2 block text-sm font-semibold text-slate-700">
                  What are your insurance goals?
                  <span className="text-red-500"> *</span>
                </label>

                <textarea
                  value={insuranceGoals}
                  onChange={(e) => setInsuranceGoals(e.target.value)}
                  rows={5}
                  maxLength={1000}
                  required
                  placeholder="Example: I want financial protection for my family, coverage for medical emergencies, and long-term financial security."
                  className="w-full rounded-xl border border-slate-300 px-4 py-3 text-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
                />

                <p className="mt-1 text-right text-xs text-slate-400">
                  {insuranceGoals.length}/1000
                </p>
              </div>

              <div>
                <label className="mb-2 block text-sm font-semibold text-slate-700">
                  Preferred policy term
                </label>

                <input
                  type="text"
                  value={preferredTerm}
                  onChange={(e) => setPreferredTerm(e.target.value)}
                  placeholder="Example: 10 years"
                  maxLength={100}
                  className="w-full rounded-xl border border-slate-300 px-4 py-3 text-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
                />

                <p className="mt-1 text-xs text-slate-400">
                  You can enter a duration or leave it as No preference.
                </p>
              </div>

              <div>
                <label className="mb-2 block text-sm font-semibold text-slate-700">
                  Preferred insurance type
                </label>

                <input
                  type="text"
                  value={preferredPolicyType}
                  onChange={(e) => setPreferredPolicyType(e.target.value)}
                  placeholder="Example: Health, Life"
                  maxLength={100}
                  className="w-full rounded-xl border border-slate-300 px-4 py-3 text-sm outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
                />

                <p className="mt-1 text-xs text-slate-400">
                  Optional. Leave blank if you have no preference.
                </p>
              </div>

              {error && (
                <div className="rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700">
                  {error}
                </div>
              )}

              <button
                type="submit"
                disabled={loading}
                className="flex w-full items-center justify-center gap-2 rounded-xl bg-blue-700 px-5 py-3 font-semibold text-white transition hover:bg-blue-800 disabled:cursor-not-allowed disabled:opacity-60"
              >
                {loading ? (
                  <>
                    <Loader2 size={18} className="animate-spin" />
                    Analysing your preferences...
                  </>
                ) : (
                  <>
                    <Sparkles size={18} />
                    Generate recommendations
                  </>
                )}
              </button>

              <div className="flex items-start gap-2 rounded-xl bg-slate-50 p-3">
                <ShieldCheck
                  size={18}
                  className="mt-0.5 shrink-0 text-green-600"
                />

                <p className="text-xs leading-5 text-slate-500">
                  Recommendations are generated using your profile
                  and eligible products available in HealthDekho.
                </p>
              </div>
            </form>
          </section>

          {/* Recommendation results */}
          <section className="min-w-0">

            {recommendations === null && (
              <div className="flex min-h-[420px] flex-col items-center justify-center rounded-2xl border border-dashed border-slate-300 bg-white p-8 text-center">
                <div className="mb-5 rounded-2xl bg-blue-50 p-5 text-blue-700">
                  <Sparkles size={36} />
                </div>

                <h2 className="text-xl font-bold text-slate-900">
                  Your recommendations will appear here
                </h2>

                <p className="mt-2 max-w-md text-sm leading-6 text-slate-500">
                  Submit your preferences to receive personalised
                  insurance recommendations with explanations and
                  suitability ratings.
                </p>
              </div>
            )}

            {recommendations !== null && (
              <div>
                <div className="mb-5 flex flex-wrap items-center justify-between gap-3">
                  <div>
                    <h2 className="text-xl font-bold text-slate-900">
                      Recommended for you
                    </h2>

                    <p className="mt-1 text-sm text-slate-500">
                      {recommendations.length} suitable product
                      {recommendations.length === 1 ? '' : 's'} found
                      {calculatedAge !== null &&
                        ` · Profile age: ${calculatedAge}`}
                    </p>
                  </div>

                  <button
                    type="button"
                    onClick={() => {
                      setRecommendations(null)
                      setError('')
                    }}
                    className="inline-flex items-center gap-2 rounded-lg border border-slate-300 px-3 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50"
                  >
                    <RefreshCw size={15} />
                    New search
                  </button>
                </div>

                {requestedAt && (
                  <p className="mb-4 text-xs text-slate-400">
                    Generated: {new Date(requestedAt).toLocaleString('en-IN')}
                  </p>
                )}

                {recommendations.length === 0 ? (
                  <div className="rounded-2xl border border-slate-200 bg-white p-10 text-center">
                    <FileText
                      size={36}
                      className="mx-auto mb-4 text-slate-400"
                    />

                    <h3 className="font-semibold text-slate-900">
                      No suitable products found
                    </h3>

                    <p className="mt-2 text-sm text-slate-500">
                      Try adjusting your insurance goals or preferences
                      and generate recommendations again.
                    </p>
                  </div>
                ) : (
                  <div className="space-y-5">
                    {recommendations.map((product) => (
                      <article
                        key={product.recommendationItemId}
                        className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm"
                      >
                        <div className="flex flex-wrap items-start justify-between gap-3 border-b border-slate-100 p-5">
                          <div>
                            <p className="text-xs font-semibold uppercase tracking-wide text-blue-700">
                              Recommendation #{product.rankOrder}
                            </p>

                            <h3 className="mt-1 text-lg font-bold text-slate-900">
                              {product.policyName}
                            </h3>

                            <p className="mt-1 text-sm text-slate-500">
                              {product.productCode} · {product.policyType}
                            </p>
                          </div>

                          <span
                            className={`rounded-full px-3 py-1 text-xs font-bold ${
                              suitabilityStyles[product.suitability] ||
                              'bg-slate-100 text-slate-700'
                            }`}
                          >
                            {product.suitability} SUITABILITY
                          </span>
                        </div>

                        <div className="grid grid-cols-2 gap-4 p-5 sm:grid-cols-4">
                          <div>
                            <p className="text-xs text-slate-500">
                              Policy term
                            </p>
                            <p className="mt-1 font-semibold text-slate-900">
                              {formatTerm(product.termMonths)}
                            </p>
                          </div>

                          <div>
                            <p className="text-xs text-slate-500">
                              Sum insured
                            </p>
                            <p className="mt-1 font-semibold text-slate-900">
                              {formatCurrency(product.sumInsured)}
                            </p>
                          </div>

                          <div>
                            <p className="text-xs text-slate-500">
                              Premium frequency
                            </p>
                            <p className="mt-1 font-semibold text-slate-900">
                              {product.premiumFrequency}
                            </p>
                          </div>

                          <div>
                            <p className="text-xs text-slate-500">
                              Premium amount
                            </p>
                            <p className="mt-1 font-semibold text-slate-900">
                              {formatCurrency(product.premiumAmount)}
                            </p>
                          </div>
                        </div>

                        <div className="border-t border-slate-100 bg-blue-50/60 p-5">
                          <p className="text-xs font-bold uppercase tracking-wide text-blue-800">
                            Why we recommend this
                          </p>

                          <p className="mt-2 text-sm leading-6 text-slate-700">
                            {product.recommendationReason}
                          </p>

                          <button
                            type="button"
                            onClick={() => handleRegister(product)}
                            className="mt-4 rounded-lg bg-blue-700 px-4 py-2.5 text-sm font-semibold text-white hover:bg-blue-800"
                          >
                            Register policy
                          </button>
                        </div>
                      </article>
                    ))}
                  </div>
                )}
              </div>
            )}
          </section>
        </div>
      </div>
    </div>
  )
}

export default Recommendations
