'use client'

import { useEffect, useState } from 'react'
import Layout from '@/components/Layout'
import { useAuth } from '@/contexts/AuthContext'
import { useRouter } from 'next/navigation'
import { userApi, planApi, appointmentApi, User, Plan, Appointment } from '@/lib/api'
import { Users, CreditCard, Calendar, Activity, UserCheck, UserX, CheckCircle, XCircle } from 'lucide-react'
import Link from 'next/link'
import toast from 'react-hot-toast'

export default function AdminPage() {
  const { user } = useAuth()
  const router = useRouter()
  const [stats, setStats] = useState({
    totalUsers: 0,
    activeUsers: 0,
    totalPlans: 0,
    activePlans: 0,
    totalAppointments: 0,
    todayAppointments: 0,
  })
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (user?.role !== 'ADMIN') {
      toast.error('Acesso negado. Apenas administradores podem acessar esta página.')
      router.push('/dashboard')
      return
    }
    loadStats()
  }, [user])

  const loadStats = async () => {
    try {
      const [users, plans, appointments] = await Promise.all([
        userApi.getAll().catch(() => []),
        planApi.getAll().catch(() => []),
        appointmentApi.getAll().catch(() => []),
      ])

      const today = new Date()
      today.setHours(0, 0, 0, 0)

      setStats({
        totalUsers: users.length,
        activeUsers: users.filter((u: User) => u.active).length,
        totalPlans: plans.length,
        activePlans: plans.filter((p: Plan) => p.active).length,
        totalAppointments: appointments.length,
        todayAppointments: appointments.filter((a: Appointment) => {
          const appointmentDate = new Date(a.scheduledAt)
          appointmentDate.setHours(0, 0, 0, 0)
          return appointmentDate.getTime() === today.getTime()
        }).length,
      })
    } catch (error: any) {
      toast.error('Erro ao carregar estatísticas')
      console.error(error)
    } finally {
      setLoading(false)
    }
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
        <div>
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Painel Administrativo</h1>
          <p className="text-gray-600">Gerencie usuários, planos e consultas do sistema</p>
        </div>

        {/* Stats Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Total de Usuários</p>
                <p className="text-3xl font-bold text-gray-900 mt-1">{stats.totalUsers}</p>
                <p className="text-xs text-green-600 mt-1">
                  {stats.activeUsers} ativos
                </p>
              </div>
              <Users className="h-12 w-12 text-primary-600" />
            </div>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Total de Planos</p>
                <p className="text-3xl font-bold text-gray-900 mt-1">{stats.totalPlans}</p>
                <p className="text-xs text-green-600 mt-1">
                  {stats.activePlans} ativos
                </p>
              </div>
              <CreditCard className="h-12 w-12 text-green-600" />
            </div>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Total de Consultas</p>
                <p className="text-3xl font-bold text-gray-900 mt-1">{stats.totalAppointments}</p>
                <p className="text-xs text-blue-600 mt-1">
                  {stats.todayAppointments} hoje
                </p>
              </div>
              <Calendar className="h-12 w-12 text-blue-600" />
            </div>
          </div>
        </div>

        {/* Quick Actions */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <Link
            href="/admin/users"
            className="bg-white rounded-lg shadow p-6 hover:shadow-lg transition-shadow"
          >
            <div className="flex items-center space-x-4">
              <div className="bg-primary-100 p-3 rounded-lg">
                <Users className="h-8 w-8 text-primary-600" />
              </div>
              <div>
                <h3 className="font-semibold text-gray-900">Gerenciar Usuários</h3>
                <p className="text-sm text-gray-600">Visualizar, ativar e desativar usuários</p>
              </div>
            </div>
          </Link>

          <Link
            href="/admin/plans"
            className="bg-white rounded-lg shadow p-6 hover:shadow-lg transition-shadow"
          >
            <div className="flex items-center space-x-4">
              <div className="bg-green-100 p-3 rounded-lg">
                <CreditCard className="h-8 w-8 text-green-600" />
              </div>
              <div>
                <h3 className="font-semibold text-gray-900">Gerenciar Planos</h3>
                <p className="text-sm text-gray-600">Criar, editar e gerenciar planos</p>
              </div>
            </div>
          </Link>

          <Link
            href="/admin/appointments"
            className="bg-white rounded-lg shadow p-6 hover:shadow-lg transition-shadow"
          >
            <div className="flex items-center space-x-4">
              <div className="bg-blue-100 p-3 rounded-lg">
                <Calendar className="h-8 w-8 text-blue-600" />
              </div>
              <div>
                <h3 className="font-semibold text-gray-900">Todas as Consultas</h3>
                <p className="text-sm text-gray-600">Visualizar todas as consultas do sistema</p>
              </div>
            </div>
          </Link>
        </div>
      </div>
    </Layout>
  )
}

