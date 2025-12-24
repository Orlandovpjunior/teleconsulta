'use client'

import { useEffect, useState } from 'react'
import Layout from '@/components/Layout'
import { useAuth } from '@/contexts/AuthContext'
import { useRouter } from 'next/navigation'
import { planApi, Plan, CreatePlanRequest } from '@/lib/api'
import { CreditCard, Plus, Edit, CheckCircle, XCircle, Trash2 } from 'lucide-react'
import toast from 'react-hot-toast'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'

const planSchema = z.object({
  name: z.string().min(1, 'Nome é obrigatório'),
  description: z.string().optional(),
  price: z.number().min(0.01, 'Preço deve ser maior que zero'),
  durationMonths: z.number().min(1).optional(),
  maxAppointmentsMonth: z.number().min(1).optional(),
  hasVideoCall: z.boolean().optional(),
  hasChat: z.boolean().optional(),
  hasPrescription: z.boolean().optional(),
  hasMedicalCertificate: z.boolean().optional(),
  features: z.array(z.string()).optional(),
})

type PlanFormData = z.infer<typeof planSchema>

export default function AdminPlansPage() {
  const { user } = useAuth()
  const router = useRouter()
  const [plans, setPlans] = useState<Plan[]>([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [editingPlan, setEditingPlan] = useState<Plan | null>(null)

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    formState: { errors },
  } = useForm<PlanFormData>({
    resolver: zodResolver(planSchema),
    defaultValues: {
      hasVideoCall: true,
      hasChat: true,
      hasPrescription: true,
      hasMedicalCertificate: true,
      features: [],
    },
  })

  useEffect(() => {
    if (user?.role !== 'ADMIN') {
      toast.error('Acesso negado')
      router.push('/dashboard')
      return
    }
    loadPlans()
  }, [user])

  const loadPlans = async () => {
    try {
      const data = await planApi.getAll()
      setPlans(data)
    } catch (error: any) {
      toast.error('Erro ao carregar planos')
      console.error(error)
    } finally {
      setLoading(false)
    }
  }

  const handleEdit = (plan: Plan) => {
    setEditingPlan(plan)
    setValue('name', plan.name)
    setValue('description', plan.description || '')
    setValue('price', plan.price)
    setValue('durationMonths', plan.durationMonths || undefined)
    setValue('maxAppointmentsMonth', plan.maxAppointmentsMonth || undefined)
    setValue('hasVideoCall', plan.hasVideoCall)
    setValue('hasChat', plan.hasChat)
    setValue('hasPrescription', plan.hasPrescription)
    setValue('hasMedicalCertificate', plan.hasMedicalCertificate)
    setValue('features', plan.features || [])
    setShowForm(true)
  }

  const handleNew = () => {
    setEditingPlan(null)
    reset()
    setShowForm(true)
  }

  const onSubmit = async (data: PlanFormData) => {
    try {
      if (editingPlan) {
        await planApi.update(editingPlan.id, data)
        toast.success('Plano atualizado com sucesso!')
      } else {
        await planApi.create(data)
        toast.success('Plano criado com sucesso!')
      }
      setShowForm(false)
      reset()
      loadPlans()
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Erro ao salvar plano')
    }
  }

  const handleToggleStatus = async (planId: number, currentStatus: boolean) => {
    try {
      if (currentStatus) {
        await planApi.deactivate(planId)
        toast.success('Plano desativado com sucesso!')
      } else {
        await planApi.activate(planId)
        toast.success('Plano ativado com sucesso!')
      }
      loadPlans()
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Erro ao alterar status do plano')
    }
  }

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'BRL',
    }).format(price)
  }

  if (user?.role !== 'ADMIN') {
    return null
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
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-gray-900 mb-2">Gerenciar Planos</h1>
            <p className="text-gray-600">Crie, edite e gerencie os planos disponíveis</p>
          </div>
          <div className="flex space-x-3">
            <button
              onClick={() => router.back()}
              className="px-4 py-2 text-gray-700 hover:bg-gray-100 rounded-lg"
            >
              ← Voltar
            </button>
            <button
              onClick={handleNew}
              className="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 flex items-center"
            >
              <Plus className="h-5 w-5 mr-2" />
              Novo Plano
            </button>
          </div>
        </div>

        {/* Form Modal */}
        {showForm && (
          <div className="bg-white rounded-lg shadow-lg p-6">
            <h2 className="text-xl font-bold mb-4">
              {editingPlan ? 'Editar Plano' : 'Novo Plano'}
            </h2>
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Nome do Plano *
                  </label>
                  <input
                    {...register('name')}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500"
                  />
                  {errors.name && (
                    <p className="text-sm text-red-600 mt-1">{errors.name.message}</p>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Preço (R$) *
                  </label>
                  <input
                    {...register('price', { valueAsNumber: true })}
                    type="number"
                    step="0.01"
                    min="0.01"
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500"
                  />
                  {errors.price && (
                    <p className="text-sm text-red-600 mt-1">{errors.price.message}</p>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Duração (meses)
                  </label>
                  <input
                    {...register('durationMonths', { valueAsNumber: true })}
                    type="number"
                    min="1"
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500"
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Consultas por mês (deixe vazio para ilimitado)
                  </label>
                  <input
                    {...register('maxAppointmentsMonth', { valueAsNumber: true })}
                    type="number"
                    min="1"
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Descrição
                </label>
                <textarea
                  {...register('description')}
                  rows={3}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500"
                />
              </div>

              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                <label className="flex items-center space-x-2">
                  <input
                    type="checkbox"
                    {...register('hasVideoCall')}
                    className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                  />
                  <span className="text-sm text-gray-700">Videochamada</span>
                </label>
                <label className="flex items-center space-x-2">
                  <input
                    type="checkbox"
                    {...register('hasChat')}
                    className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                  />
                  <span className="text-sm text-gray-700">Chat</span>
                </label>
                <label className="flex items-center space-x-2">
                  <input
                    type="checkbox"
                    {...register('hasPrescription')}
                    className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                  />
                  <span className="text-sm text-gray-700">Prescrição</span>
                </label>
                <label className="flex items-center space-x-2">
                  <input
                    type="checkbox"
                    {...register('hasMedicalCertificate')}
                    className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                  />
                  <span className="text-sm text-gray-700">Atestado</span>
                </label>
              </div>

              <div className="flex space-x-3">
                <button
                  type="submit"
                  className="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700"
                >
                  {editingPlan ? 'Atualizar' : 'Criar'} Plano
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setShowForm(false)
                    reset()
                  }}
                  className="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50"
                >
                  Cancelar
                </button>
              </div>
            </form>
          </div>
        )}

        {/* Plans List */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {plans.map((plan) => (
            <div
              key={plan.id}
              className={`bg-white rounded-lg shadow p-6 ${
                !plan.active ? 'opacity-60' : ''
              }`}
            >
              <div className="flex items-start justify-between mb-4">
                <div className="flex-1">
                  <h3 className="text-xl font-bold text-gray-900">{plan.name}</h3>
                  <p className="text-2xl font-bold text-primary-600 mt-2">
                    {formatPrice(plan.price)}
                    <span className="text-sm text-gray-500 font-normal">/mês</span>
                  </p>
                </div>
                {plan.active ? (
                  <span className="px-2 py-1 bg-green-100 text-green-800 text-xs font-medium rounded-full">
                    Ativo
                  </span>
                ) : (
                  <span className="px-2 py-1 bg-red-100 text-red-800 text-xs font-medium rounded-full">
                    Inativo
                  </span>
                )}
              </div>

              {plan.description && (
                <p className="text-sm text-gray-600 mb-4">{plan.description}</p>
              )}

              <div className="space-y-2 mb-4 text-sm">
                {plan.maxAppointmentsMonth ? (
                  <p>• {plan.maxAppointmentsMonth} consultas/mês</p>
                ) : (
                  <p>• Consultas ilimitadas</p>
                )}
                {plan.hasVideoCall && <p>• Videochamada</p>}
                {plan.hasChat && <p>• Chat com médico</p>}
                {plan.hasPrescription && <p>• Prescrição digital</p>}
                {plan.hasMedicalCertificate && <p>• Atestado médico</p>}
              </div>

              <div className="flex space-x-2">
                <button
                  onClick={() => handleEdit(plan)}
                  className="flex-1 px-3 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 text-sm flex items-center justify-center"
                >
                  <Edit className="h-4 w-4 mr-1" />
                  Editar
                </button>
                <button
                  onClick={() => handleToggleStatus(plan.id, plan.active)}
                  className={`px-3 py-2 rounded-lg text-sm flex items-center ${
                    plan.active
                      ? 'bg-red-600 text-white hover:bg-red-700'
                      : 'bg-green-600 text-white hover:bg-green-700'
                  }`}
                >
                  {plan.active ? (
                    <>
                      <XCircle className="h-4 w-4 mr-1" />
                      Desativar
                    </>
                  ) : (
                    <>
                      <CheckCircle className="h-4 w-4 mr-1" />
                      Ativar
                    </>
                  )}
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>
    </Layout>
  )
}

