'use client'

import { useEffect, useState } from 'react'
import Layout from '@/components/Layout'
import { planApi, Plan } from '@/lib/api'
import { Check, CreditCard, Calendar, Video, MessageSquare, FileText, Award } from 'lucide-react'
import toast from 'react-hot-toast'
import { useAuth } from '@/contexts/AuthContext'

export default function PlansPage() {
  const { user } = useAuth()
  const [plans, setPlans] = useState<Plan[]>([])
  const [loading, setLoading] = useState(true)
  const [subscribing, setSubscribing] = useState<number | null>(null)

  useEffect(() => {
    loadPlans()
  }, [])

  const loadPlans = async () => {
    try {
      const data = await planApi.getActivePlans()
      setPlans(data)
    } catch (error: any) {
      toast.error('Erro ao carregar planos')
      console.error(error)
    } finally {
      setLoading(false)
    }
  }

  const handleSubscribe = async (planId: number) => {
    if (!confirm('Deseja assinar este plano?')) {
      return
    }

    setSubscribing(planId)
    try {
      await planApi.subscribe(planId)
      toast.success('Plano assinado com sucesso!')
      // Recarregar dados do usuário
      window.location.reload()
    } catch (error: any) {
      const message = error.response?.data?.message || 'Erro ao assinar plano'
      toast.error(message)
    } finally {
      setSubscribing(null)
    }
  }

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    }).format(price)
  }

  if (loading) {
    return (
      <Layout>
        <div className="flex items-center justify-center h-64">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
        </div>
      </Layout>
    )
  }

  return (
    <Layout>
      <div className="space-y-6">
        {/* Header */}
        <div>
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Planos Disponíveis</h1>
          <p className="text-gray-600">
            Escolha o plano que melhor se adapta às suas necessidades
          </p>
        </div>

        {/* Current Plan Info */}
        {user?.planName && (
          <div className="bg-green-50 border border-green-200 rounded-lg p-4">
            <div className="flex items-center space-x-2">
              <Check className="h-5 w-5 text-green-600" />
              <p className="text-green-800">
                <strong>Plano Atual:</strong> {user.planName}
              </p>
            </div>
          </div>
        )}

        {/* Plans Grid */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {plans.map((plan) => {
            const isCurrentPlan = user?.planId === plan.id
            const isPopular = plan.name.toLowerCase().includes('padrão') || plan.name.toLowerCase().includes('standard')

            return (
              <div
                key={plan.id}
                className={`bg-white rounded-lg shadow-lg overflow-hidden ${
                  isPopular ? 'ring-2 ring-primary-500 scale-105' : ''
                } ${isCurrentPlan ? 'border-2 border-green-500' : ''}`}
              >
                {isPopular && (
                  <div className="bg-primary-600 text-white text-center py-2 text-sm font-medium">
                    Mais Popular
                  </div>
                )}

                <div className="p-6">
                  <div className="mb-4">
                    <h3 className="text-2xl font-bold text-gray-900 mb-2">{plan.name}</h3>
                    <div className="flex items-baseline">
                      <span className="text-4xl font-bold text-gray-900">
                        {formatPrice(plan.price)}
                      </span>
                      <span className="text-gray-600 ml-2">/mês</span>
                    </div>
                    {plan.description && (
                      <p className="text-gray-600 mt-2 text-sm">{plan.description}</p>
                    )}
                  </div>

                  {/* Features */}
                  <div className="space-y-3 mb-6">
                    {plan.maxAppointmentsMonth ? (
                      <div className="flex items-center space-x-2">
                        <Calendar className="h-5 w-5 text-primary-600" />
                        <span className="text-sm text-gray-700">
                          {plan.maxAppointmentsMonth} consultas por mês
                        </span>
                      </div>
                    ) : (
                      <div className="flex items-center space-x-2">
                        <Calendar className="h-5 w-5 text-primary-600" />
                        <span className="text-sm text-gray-700 font-medium">
                          Consultas ilimitadas
                        </span>
                      </div>
                    )}

                    {plan.hasVideoCall && (
                      <div className="flex items-center space-x-2">
                        <Video className="h-5 w-5 text-primary-600" />
                        <span className="text-sm text-gray-700">Videochamada</span>
                      </div>
                    )}

                    {plan.hasChat && (
                      <div className="flex items-center space-x-2">
                        <MessageSquare className="h-5 w-5 text-primary-600" />
                        <span className="text-sm text-gray-700">Chat com médico</span>
                      </div>
                    )}

                    {plan.hasPrescription && (
                      <div className="flex items-center space-x-2">
                        <FileText className="h-5 w-5 text-primary-600" />
                        <span className="text-sm text-gray-700">Prescrição digital</span>
                      </div>
                    )}

                    {plan.hasMedicalCertificate && (
                      <div className="flex items-center space-x-2">
                        <Award className="h-5 w-5 text-primary-600" />
                        <span className="text-sm text-gray-700">Atestado médico</span>
                      </div>
                    )}

                    {plan.features && plan.features.length > 0 && (
                      <div className="pt-2 border-t border-gray-200">
                        {plan.features.map((feature, index) => (
                          <div key={index} className="flex items-center space-x-2 mt-2">
                            <Check className="h-4 w-4 text-green-600" />
                            <span className="text-sm text-gray-700">{feature}</span>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>

                  {/* Subscribe Button */}
                  {isCurrentPlan ? (
                    <button
                      disabled
                      className="w-full py-3 px-4 bg-green-600 text-white rounded-lg font-medium cursor-not-allowed"
                    >
                      <Check className="h-5 w-5 inline mr-2" />
                      Plano Atual
                    </button>
                  ) : (
                    <button
                      onClick={() => handleSubscribe(plan.id)}
                      disabled={subscribing === plan.id}
                      className="w-full py-3 px-4 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors font-medium disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      {subscribing === plan.id ? (
                        <>
                          <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white inline-block mr-2"></div>
                          Assinando...
                        </>
                      ) : (
                        <>
                          <CreditCard className="h-5 w-5 inline mr-2" />
                          Assinar Plano
                        </>
                      )}
                    </button>
                  )}
                </div>
              </div>
            )
          })}
        </div>

        {/* Info */}
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-6">
          <h3 className="font-semibold text-gray-900 mb-2">Como funciona?</h3>
          <ul className="space-y-2 text-sm text-gray-700">
            <li>• Escolha o plano que melhor atende suas necessidades</li>
            <li>• Assine e tenha acesso imediato aos benefícios</li>
            <li>• Agende suas consultas conforme o limite do seu plano</li>
            <li>• Cancele quando quiser, sem fidelidade</li>
          </ul>
        </div>
      </div>
    </Layout>
  )
}

