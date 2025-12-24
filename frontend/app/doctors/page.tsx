'use client'

import { useEffect, useState } from 'react'
import Layout from '@/components/Layout'
import { doctorApi, User } from '@/lib/api'
import { Search, Stethoscope, Calendar, MapPin, Star } from 'lucide-react'
import Link from 'next/link'
import toast from 'react-hot-toast'

export default function DoctorsPage() {
  const [doctors, setDoctors] = useState<User[]>([])
  const [filteredDoctors, setFilteredDoctors] = useState<User[]>([])
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedSpecialty, setSelectedSpecialty] = useState<string>('')

  useEffect(() => {
    loadDoctors()
  }, [])

  useEffect(() => {
    filterDoctors()
  }, [searchTerm, selectedSpecialty, doctors])

  const loadDoctors = async () => {
    try {
      const data = await doctorApi.getAll()
      setDoctors(data)
      setFilteredDoctors(data)
    } catch (error: any) {
      toast.error('Erro ao carregar médicos')
      console.error(error)
    } finally {
      setLoading(false)
    }
  }

  const filterDoctors = () => {
    let filtered = [...doctors]

    if (searchTerm) {
      filtered = filtered.filter(
        (doctor) =>
          doctor.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
          doctor.specialty?.toLowerCase().includes(searchTerm.toLowerCase())
      )
    }

    if (selectedSpecialty) {
      filtered = filtered.filter(
        (doctor) => doctor.specialty?.toLowerCase() === selectedSpecialty.toLowerCase()
      )
    }

    setFilteredDoctors(filtered)
  }

  const specialties = Array.from(
    new Set(doctors.map((doctor) => doctor.specialty).filter(Boolean))
  ) as string[]

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
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Encontre seu Médico</h1>
          <p className="text-gray-600">
            Escolha um profissional qualificado para sua consulta online
          </p>
        </div>

        {/* Search and Filters */}
        <div className="bg-white rounded-lg shadow p-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Search */}
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
              <input
                type="text"
                placeholder="Buscar por nome ou especialidade..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10 w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              />
            </div>

            {/* Specialty Filter */}
            <select
              value={selectedSpecialty}
              onChange={(e) => setSelectedSpecialty(e.target.value)}
              className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
            >
              <option value="">Todas as especialidades</option>
              {specialties.map((specialty) => (
                <option key={specialty} value={specialty}>
                  {specialty}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Results Count */}
        <div className="text-sm text-gray-600">
          {filteredDoctors.length === 0 ? (
            <p>Nenhum médico encontrado</p>
          ) : (
            <p>
              {filteredDoctors.length} médico{filteredDoctors.length !== 1 ? 's' : ''} encontrado
              {filteredDoctors.length !== 1 ? 's' : ''}
            </p>
          )}
        </div>

        {/* Doctors Grid */}
        {filteredDoctors.length === 0 ? (
          <div className="bg-white rounded-lg shadow p-12 text-center">
            <Stethoscope className="h-16 w-16 text-gray-400 mx-auto mb-4" />
            <p className="text-gray-500 text-lg">Nenhum médico encontrado</p>
            <p className="text-gray-400 text-sm mt-2">
              Tente ajustar os filtros de busca
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredDoctors.map((doctor) => (
              <div
                key={doctor.id}
                className="bg-white rounded-lg shadow hover:shadow-lg transition-shadow p-6"
              >
                <div className="flex items-start space-x-4 mb-4">
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

                <div className="space-y-2 mb-4">
                  {doctor.phoneNumber && (
                    <div className="flex items-center text-sm text-gray-600">
                      <MapPin className="h-4 w-4 mr-2" />
                      {doctor.phoneNumber}
                    </div>
                  )}
                  <div className="flex items-center text-sm text-gray-600">
                    <Star className="h-4 w-4 mr-2 text-yellow-400 fill-yellow-400" />
                    <span className="font-medium">Disponível para consultas online</span>
                  </div>
                </div>

                <Link
                  href={`/appointments/new?doctorId=${doctor.id}`}
                  className="block w-full text-center py-3 px-4 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors font-medium"
                >
                  <Calendar className="h-5 w-5 inline mr-2" />
                  Agendar Consulta
                </Link>
              </div>
            ))}
          </div>
        )}
      </div>
    </Layout>
  )
}

