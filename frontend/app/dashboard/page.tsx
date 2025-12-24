'use client'

import { useEffect, useState } from 'react'
import Layout from '@/components/Layout'
import { useAuth } from '@/contexts/AuthContext'
import { appointmentApi, Appointment } from '@/lib/api'
import { Calendar, Clock, User, Stethoscope, AlertCircle } from 'lucide-react'
import { format } from 'date-fns'
import Link from 'next/link'
import toast from 'react-hot-toast'

export default function DashboardPage() {
  const { user } = useAuth()
  const [appointments, setAppointments] = useState<Appointment[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadAppointments()
  }, [])

  const loadAppointments = async () => {
    try {
      const data = await appointmentApi.getAll()
      // Ordenar por data mais próxima
      const sorted = data.sort((a, b) => 
        new Date(a.scheduledAt).getTime() - new Date(b.scheduledAt).getTime()
      )
      setAppointments(sorted.slice(0, 5)) // Mostrar apenas as 5 próximas
    } catch (error: any) {
      toast.error('Erro ao carregar consultas')
      console.error(error)
    } finally {
      setLoading(false)
    }
  }

  const getStatusColor = (status: Appointment['status']) => {
    const colors: Record<string, string> = {
      SCHEDULED: 'bg-yellow-100 text-yellow-800',
      CONFIRMED: 'bg-blue-100 text-blue-800',
      IN_PROGRESS: 'bg-purple-100 text-purple-800',
      COMPLETED: 'bg-green-100 text-green-800',
      CANCELLED: 'bg-red-100 text-red-800',
      NO_SHOW: 'bg-gray-100 text-gray-800',
    }
    return colors[status] || 'bg-gray-100 text-gray-800'
  }

  const getStatusLabel = (status: Appointment['status']) => {
    const labels: Record<string, string> = {
      SCHEDULED: 'Agendada',
      CONFIRMED: 'Confirmada',
      IN_PROGRESS: 'Em Andamento',
      COMPLETED: 'Concluída',
      CANCELLED: 'Cancelada',
      NO_SHOW: 'Não Compareceu',
    }
    return labels[status] || status
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
        {/* Welcome Section */}
        <div className="bg-gradient-to-r from-primary-600 to-primary-800 rounded-xl p-6 text-white">
          <h1 className="text-3xl font-bold mb-2">
            Olá, {user?.name?.split(' ')[0]}! 👋
          </h1>
          <p className="text-primary-100">
            {user?.role === 'PATIENT' 
              ? 'Gerencie suas consultas e encontre os melhores médicos'
              : user?.role === 'DOCTOR'
              ? 'Gerencie suas consultas e pacientes'
              : 'Painel administrativo'}
          </p>
        </div>

        {/* Quick Stats */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Consultas Agendadas</p>
                <p className="text-2xl font-bold text-gray-900 mt-1">
                  {appointments.filter(a => a.status === 'SCHEDULED' || a.status === 'CONFIRMED').length}
                </p>
              </div>
              <Calendar className="h-12 w-12 text-primary-600" />
            </div>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Consultas Hoje</p>
                <p className="text-2xl font-bold text-gray-900 mt-1">
                  {appointments.filter(a => {
                    const date = new Date(a.scheduledAt)
                    const today = new Date()
                    return date.toDateString() === today.toDateString()
                  }).length}
                </p>
              </div>
              <Clock className="h-12 w-12 text-green-600" />
            </div>
          </div>

          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-600">Total de Consultas</p>
                <p className="text-2xl font-bold text-gray-900 mt-1">
                  {appointments.length}
                </p>
              </div>
              <Stethoscope className="h-12 w-12 text-blue-600" />
            </div>
          </div>
        </div>

        {/* Recent Appointments */}
        <div className="bg-white rounded-lg shadow">
          <div className="p-6 border-b border-gray-200">
            <div className="flex items-center justify-between">
              <h2 className="text-xl font-bold text-gray-900">Próximas Consultas</h2>
              <Link
                href="/appointments"
                className="text-primary-600 hover:text-primary-700 text-sm font-medium"
              >
                Ver todas
              </Link>
            </div>
          </div>

          <div className="p-6">
            {appointments.length === 0 ? (
              <div className="text-center py-12">
                <AlertCircle className="h-12 w-12 text-gray-400 mx-auto mb-4" />
                <p className="text-gray-500">Nenhuma consulta agendada</p>
                {user?.role === 'PATIENT' && (
                  <Link
                    href="/doctors"
                    className="mt-4 inline-block text-primary-600 hover:text-primary-700 font-medium"
                  >
                    Agendar uma consulta
                  </Link>
                )}
              </div>
            ) : (
              <div className="space-y-4">
                {appointments.map((appointment) => (
                  <div
                    key={appointment.id}
                    className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow"
                  >
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <div className="flex items-center space-x-3 mb-2">
                          <Calendar className="h-5 w-5 text-gray-400" />
                          <span className="font-medium text-gray-900">
                            {format(new Date(appointment.scheduledAt), "dd/MM/yyyy 'às' HH:mm")}
                          </span>
                          <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(appointment.status)}`}>
                            {getStatusLabel(appointment.status)}
                          </span>
                        </div>
                        <div className="flex items-center space-x-4 text-sm text-gray-600">
                          {user?.role === 'PATIENT' ? (
                            <>
                              <div className="flex items-center space-x-1">
                                <Stethoscope className="h-4 w-4" />
                                <span>{appointment.doctorName}</span>
                                {appointment.doctorSpecialty && (
                                  <span className="text-gray-400">- {appointment.doctorSpecialty}</span>
                                )}
                              </div>
                            </>
                          ) : (
                            <div className="flex items-center space-x-1">
                              <User className="h-4 w-4" />
                              <span>{appointment.patientName}</span>
                            </div>
                          )}
                        </div>
                        {appointment.patientComplaint && (
                          <p className="mt-2 text-sm text-gray-600">
                            <strong>Queixa:</strong> {appointment.patientComplaint}
                          </p>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        {/* Quick Actions */}
        {user?.role === 'PATIENT' && (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <Link
              href="/doctors"
              className="bg-white rounded-lg shadow p-6 hover:shadow-lg transition-shadow"
            >
              <div className="flex items-center space-x-4">
                <div className="bg-primary-100 p-3 rounded-lg">
                  <Stethoscope className="h-6 w-6 text-primary-600" />
                </div>
                <div>
                  <h3 className="font-semibold text-gray-900">Encontrar Médicos</h3>
                  <p className="text-sm text-gray-600">Busque por especialidade</p>
                </div>
              </div>
            </Link>

            <Link
              href="/plans"
              className="bg-white rounded-lg shadow p-6 hover:shadow-lg transition-shadow"
            >
              <div className="flex items-center space-x-4">
                <div className="bg-green-100 p-3 rounded-lg">
                  <Calendar className="h-6 w-6 text-green-600" />
                </div>
                <div>
                  <h3 className="font-semibold text-gray-900">Ver Planos</h3>
                  <p className="text-sm text-gray-600">Escolha o melhor plano para você</p>
                </div>
              </div>
            </Link>
          </div>
        )}
      </div>
    </Layout>
  )
}

