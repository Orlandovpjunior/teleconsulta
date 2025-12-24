'use client'

import { useEffect, useState } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import Layout from '@/components/Layout'
import { doctorApi, appointmentApi, User } from '@/lib/api'
import { Calendar, Clock, Stethoscope, AlertCircle } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import toast from 'react-hot-toast'

const appointmentSchema = z.object({
  doctorId: z.number(),
  scheduledAt: z.string().min(1, 'Data e hora são obrigatórias'),
  patientComplaint: z.string().max(1000, 'Máximo 1000 caracteres').optional(),
})

type AppointmentFormData = z.infer<typeof appointmentSchema>

export default function NewAppointmentPage() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const doctorIdParam = searchParams.get('doctorId')
  
  const [doctor, setDoctor] = useState<User | null>(null)
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)

  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors },
  } = useForm<AppointmentFormData>({
    resolver: zodResolver(appointmentSchema),
  })

  useEffect(() => {
    if (doctorIdParam) {
      loadDoctor(parseInt(doctorIdParam))
      setValue('doctorId', parseInt(doctorIdParam))
    } else {
      router.push('/doctors')
    }
  }, [doctorIdParam])

  const loadDoctor = async (id: number) => {
    try {
      const doctors = await doctorApi.getAll()
      const found = doctors.find(d => d.id === id)
      if (found) {
        setDoctor(found)
      } else {
        toast.error('Médico não encontrado')
        router.push('/doctors')
      }
    } catch (error: any) {
      toast.error('Erro ao carregar dados do médico')
      console.error(error)
    } finally {
      setLoading(false)
    }
  }

  const onSubmit = async (data: AppointmentFormData) => {
    setSubmitting(true)
    try {
      // Validar se a data é futura
      const selectedDate = new Date(data.scheduledAt)
      const now = new Date()
      
      if (selectedDate <= now) {
        toast.error('A consulta deve ser agendada para uma data futura')
        setSubmitting(false)
        return
      }

      await appointmentApi.create({
        doctorId: data.doctorId,
        scheduledAt: data.scheduledAt,
        patientComplaint: data.patientComplaint,
      })

      toast.success('Consulta agendada com sucesso!')
      router.push('/appointments')
    } catch (error: any) {
      const message = error.response?.data?.message || 'Erro ao agendar consulta'
      toast.error(message)
    } finally {
      setSubmitting(false)
    }
  }

  // Obter data mínima (hoje)
  const getMinDateTime = () => {
    const now = new Date()
    now.setMinutes(now.getMinutes() + 30) // Mínimo 30 minutos a partir de agora
    return now.toISOString().slice(0, 16)
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

  if (!doctor) {
    return null
  }

  return (
    <Layout>
      <div className="max-w-2xl mx-auto space-y-6">
        {/* Header */}
        <div>
          <button
            onClick={() => router.back()}
            className="text-primary-600 hover:text-primary-700 mb-4 text-sm font-medium"
          >
            ← Voltar
          </button>
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Agendar Consulta</h1>
          <p className="text-gray-600">Preencha os dados para agendar sua consulta</p>
        </div>

        {/* Doctor Info */}
        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex items-start space-x-4">
            <div className="bg-primary-100 p-3 rounded-full">
              <Stethoscope className="h-8 w-8 text-primary-600" />
            </div>
            <div className="flex-1">
              <h3 className="text-xl font-bold text-gray-900">{doctor.name}</h3>
              {doctor.specialty && (
                <p className="text-primary-600 font-medium">{doctor.specialty}</p>
              )}
              {doctor.crm && (
                <p className="text-sm text-gray-500 mt-1">{doctor.crm}</p>
              )}
            </div>
          </div>
        </div>

        {/* Appointment Form */}
        <form onSubmit={handleSubmit(onSubmit)} className="bg-white rounded-lg shadow p-6 space-y-6">
          <div>
            <label htmlFor="scheduledAt" className="block text-sm font-medium text-gray-700 mb-2">
              <Calendar className="h-5 w-5 inline mr-2" />
              Data e Hora da Consulta
            </label>
            <input
              {...register('scheduledAt')}
              type="datetime-local"
              id="scheduledAt"
              min={getMinDateTime()}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
            />
            {errors.scheduledAt && (
              <p className="mt-1 text-sm text-red-600">{errors.scheduledAt.message}</p>
            )}
            <p className="mt-2 text-xs text-gray-500">
              A consulta deve ser agendada com pelo menos 30 minutos de antecedência
            </p>
          </div>

          <div>
            <label htmlFor="patientComplaint" className="block text-sm font-medium text-gray-700 mb-2">
              Queixa Principal (Opcional)
            </label>
            <textarea
              {...register('patientComplaint')}
              id="patientComplaint"
              rows={4}
              maxLength={1000}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              placeholder="Descreva brevemente o motivo da consulta..."
            />
            {errors.patientComplaint && (
              <p className="mt-1 text-sm text-red-600">{errors.patientComplaint.message}</p>
            )}
            <p className="mt-2 text-xs text-gray-500">
              Esta informação ajudará o médico a se preparar para sua consulta
            </p>
          </div>

          <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
            <div className="flex items-start space-x-3">
              <AlertCircle className="h-5 w-5 text-blue-600 mt-0.5" />
              <div className="text-sm text-blue-800">
                <p className="font-medium mb-1">Informações importantes:</p>
                <ul className="list-disc list-inside space-y-1 text-blue-700">
                  <li>A consulta será realizada por videochamada</li>
                  <li>Você receberá um link de acesso antes do horário agendado</li>
                  <li>Certifique-se de ter uma conexão estável com a internet</li>
                  <li>Esteja em um local privado e silencioso</li>
                </ul>
              </div>
            </div>
          </div>

          <div className="flex space-x-4">
            <button
              type="button"
              onClick={() => router.back()}
              className="flex-1 px-4 py-3 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors font-medium"
            >
              Cancelar
            </button>
            <button
              type="submit"
              disabled={submitting}
              className="flex-1 px-4 py-3 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors font-medium disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center"
            >
              {submitting ? (
                <>
                  <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white mr-2"></div>
                  Agendando...
                </>
              ) : (
                <>
                  <Calendar className="h-5 w-5 mr-2" />
                  Confirmar Agendamento
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </Layout>
  )
}

