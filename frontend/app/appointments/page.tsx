'use client'

import { useEffect, useState } from 'react'
import Layout from '@/components/Layout'
import { appointmentApi, Appointment } from '@/lib/api'
import { Calendar, Clock, User, Stethoscope, X, CheckCircle } from 'lucide-react'
import { format } from 'date-fns'
import Link from 'next/link'
import toast from 'react-hot-toast'
import { useAuth } from '@/contexts/AuthContext'

export default function AppointmentsPage() {
  const { user } = useAuth()
  const [appointments, setAppointments] = useState<Appointment[]>([])
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState<'all' | 'SCHEDULED' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED'>('all')

  useEffect(() => {
    loadAppointments()
  }, [])

  const loadAppointments = async () => {
    try {
      const data = await appointmentApi.getAll()
      setAppointments(data)
    } catch (error: any) {
      toast.error('Erro ao carregar consultas')
      console.error(error)
    } finally {
      setLoading(false)
    }
  }

  const handleCancel = async (id: number) => {
    if (!confirm('Tem certeza que deseja cancelar esta consulta?')) {
      return
    }

    try {
      await appointmentApi.cancel(id)
      toast.success('Consulta cancelada com sucesso!')
      loadAppointments()
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Erro ao cancelar consulta')
    }
  }

  const handleConfirm = async (id: number) => {
    try {
      await appointmentApi.confirm(id)
      toast.success('Consulta confirmada!')
      loadAppointments()
    } catch (error: any) {
      toast.error(error.response?.data?.message || 'Erro ao confirmar consulta')
    }
  }

  const getStatusColor = (status: Appointment['status']) => {
    const colors: Record<string, string> = {
      SCHEDULED: 'bg-yellow-100 text-yellow-800 border-yellow-200',
      CONFIRMED: 'bg-blue-100 text-blue-800 border-blue-200',
      IN_PROGRESS: 'bg-purple-100 text-purple-800 border-purple-200',
      COMPLETED: 'bg-green-100 text-green-800 border-green-200',
      CANCELLED: 'bg-red-100 text-red-800 border-red-200',
      NO_SHOW: 'bg-gray-100 text-gray-800 border-gray-200',
    }
    return colors[status] || 'bg-gray-100 text-gray-800 border-gray-200'
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

  const filteredAppointments = filter === 'all' 
    ? appointments 
    : appointments.filter(a => a.status === filter)

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
            <h1 className="text-3xl font-bold text-gray-900 mb-2">
              {user?.role === 'PATIENT' ? 'Minhas Consultas' : 'Consultas'}
            </h1>
            <p className="text-gray-600">Gerencie suas consultas médicas</p>
          </div>
          {user?.role === 'PATIENT' && (
            <Link
              href="/doctors"
              className="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors font-medium"
            >
              <Calendar className="h-5 w-5 inline mr-2" />
              Agendar Nova Consulta
            </Link>
          )}
        </div>

        {/* Filters */}
        <div className="bg-white rounded-lg shadow p-4">
          <div className="flex flex-wrap gap-2">
            {(['all', 'SCHEDULED', 'CONFIRMED', 'COMPLETED', 'CANCELLED'] as const).map((f) => (
              <button
                key={f}
                onClick={() => setFilter(f)}
                className={`px-4 py-2 rounded-lg font-medium transition-colors ${
                  filter === f
                    ? 'bg-primary-600 text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
              >
                {f === 'all' ? 'Todas' : getStatusLabel(f)}
              </button>
            ))}
          </div>
        </div>

        {/* Appointments List */}
        {filteredAppointments.length === 0 ? (
          <div className="bg-white rounded-lg shadow p-12 text-center">
            <Calendar className="h-16 w-16 text-gray-400 mx-auto mb-4" />
            <p className="text-gray-500 text-lg">Nenhuma consulta encontrada</p>
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
            {filteredAppointments.map((appointment) => (
              <div
                key={appointment.id}
                className="bg-white rounded-lg shadow p-6 hover:shadow-lg transition-shadow"
              >
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <div className="flex items-center space-x-3 mb-3">
                      <Calendar className="h-5 w-5 text-primary-600" />
                      <span className="text-lg font-semibold text-gray-900">
                        {format(new Date(appointment.scheduledAt), "dd/MM/yyyy 'às' HH:mm")}
                      </span>
                      <span className={`px-3 py-1 rounded-full text-xs font-medium border ${getStatusColor(appointment.status)}`}>
                        {getStatusLabel(appointment.status)}
                      </span>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
                      {user?.role === 'PATIENT' ? (
                        <>
                          <div className="flex items-center space-x-2 text-gray-700">
                            <Stethoscope className="h-5 w-5 text-gray-400" />
                            <div>
                              <p className="font-medium">{appointment.doctorName}</p>
                              {appointment.doctorSpecialty && (
                                <p className="text-sm text-gray-500">{appointment.doctorSpecialty}</p>
                              )}
                            </div>
                          </div>
                        </>
                      ) : (
                        <div className="flex items-center space-x-2 text-gray-700">
                          <User className="h-5 w-5 text-gray-400" />
                          <div>
                            <p className="font-medium">{appointment.patientName}</p>
                          </div>
                        </div>
                      )}

                      {appointment.durationMinutes && (
                        <div className="flex items-center space-x-2 text-gray-700">
                          <Clock className="h-5 w-5 text-gray-400" />
                          <span>{appointment.durationMinutes} minutos</span>
                        </div>
                      )}
                    </div>

                    {appointment.patientComplaint && (
                      <div className="mb-3">
                        <p className="text-sm text-gray-600">
                          <strong>Queixa:</strong> {appointment.patientComplaint}
                        </p>
                      </div>
                    )}

                    {appointment.diagnosis && (
                      <div className="mb-3">
                        <p className="text-sm text-gray-700">
                          <strong>Diagnóstico:</strong> {appointment.diagnosis}
                        </p>
                      </div>
                    )}

                    {appointment.prescription && (
                      <div className="mb-3">
                        <p className="text-sm text-gray-700">
                          <strong>Prescrição:</strong> {appointment.prescription}
                        </p>
                      </div>
                    )}
                  </div>

                  <div className="flex flex-col space-y-2 ml-4">
                    {appointment.status === 'SCHEDULED' && user?.role === 'DOCTOR' && (
                      <button
                        onClick={() => handleConfirm(appointment.id)}
                        className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors text-sm font-medium flex items-center"
                      >
                        <CheckCircle className="h-4 w-4 mr-1" />
                        Confirmar
                      </button>
                    )}
                    {(appointment.status === 'SCHEDULED' || appointment.status === 'CONFIRMED') && (
                      <button
                        onClick={() => handleCancel(appointment.id)}
                        className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors text-sm font-medium flex items-center"
                      >
                        <X className="h-4 w-4 mr-1" />
                        Cancelar
                      </button>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </Layout>
  )
}

